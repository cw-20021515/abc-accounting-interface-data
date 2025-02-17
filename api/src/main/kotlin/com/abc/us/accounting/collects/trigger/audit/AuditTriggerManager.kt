package com.abc.us.accounting.collects.trigger.audit

import com.abc.us.accounting.collects.domain.entity.audit.AuditTargetEntity
import com.abc.us.accounting.collects.domain.repository.AuditTargetRepository
import com.abc.us.accounting.collects.domain.type.AuditActionTypeEnum
import com.abc.us.accounting.supports.properties.SchedulingProperties

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class AuditTriggerManager(
    @Qualifier("schedulingProperties")
    private var schedulingProperties : SchedulingProperties,

    private val dataSource: DataSource,
    private val auditTargetRepository: AuditTargetRepository

) : ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Volatile
    var targetTables: Set<String> = emptySet()

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        initializeTriggers() // 애플리케이션 초기 트리거 설정
    }

    private fun initializeTriggers() {
        val targets = auditTargetRepository.findAll()
        configureTriggers(targets)
    }

    @Scheduled(
        fixedDelayString = "#{schedulingProperties.auditTriggerManager.refresh.fixedDelayMillis}",
        initialDelayString = "#{schedulingProperties.auditTriggerManager.refresh.initialDelayMillis}"
    )
    fun refreshTriggers() {
        val newTables = fetchTargetTablesFromDatabase()
        val addedTables = newTables - targetTables
        val removedTables = targetTables - newTables

        addedTables.forEach { table ->
            val targets = auditTargetRepository.findByEntityNameAndIsActiveTrue(table)
            configureTriggers(targets)
        }

        removedTables.forEach { table ->
            deleteTrigger("${camelToSnake(table)}_trigger", camelToSnake(table))
        }
        targetTables = newTables
    }
    fun configureTriggers(targets: List<AuditTargetEntity>) {
        targets.forEach { target ->
            val snakeTable = camelToSnake(target.entityName!!)
            val triggerName = "${snakeTable}_trigger"
            val actions = determineActions(target.auditActionType)

            try {
                if (target.isActive) {
                    if (!existsTrigger(triggerName, snakeTable)) {
                        createTrigger(triggerName, snakeTable, actions)
                    }
                } else {
                    deleteTrigger(triggerName, snakeTable)
                }
            } catch (e: Exception) {
                logger.error { "Failed to configure trigger $triggerName - ${e.message}" }
            }
        }
    }

    fun fetchTargetTablesFromDatabase(): Set<String> {
        dataSource.connection.use { connection ->
            val query = "SELECT DISTINCT entity_name FROM audit_target_entity WHERE is_active = 'Y'"
            connection.createStatement().use { statement ->
                statement.executeQuery(query).use { resultSet ->
                    val tables = mutableSetOf<String>()
                    while (resultSet.next()) {
                        tables.add(resultSet.getString("entity_name"))
                    }
                    return tables
                }
            }
        }
    }

    fun createTrigger(triggerName: String, tableName: String, actions: String): Boolean {
        val functionName = "${triggerName}_function"
        val actionList = actions.split("|").map { it.trim().uppercase() }.distinct()

        val createFunctionQuery = """
        CREATE OR REPLACE FUNCTION $functionName() 
        RETURNS TRIGGER AS $$
        BEGIN
            ${actionList.joinToString("\n") { action ->
            val valueSource = if (action == "INSERT") "NEW" else "OLD"
            val sqlCondition = if (action == "INSERT") "IF" else "ELSIF"
            """
                $sqlCondition (TG_OP = '$action') THEN
                    INSERT INTO audit_entity_log (
                        action_type, entity_id, timestamp, event_table_name, event_table_id, company_id, processed
                    ) VALUES (
                        '$action', $valueSource.id, CURRENT_TIMESTAMP, TG_TABLE_NAME, $valueSource.id, $valueSource.company_id, false
                    );
                    RETURN $valueSource;
                """
        }}
            END IF;
        END;
        $$ LANGUAGE plpgsql;
    """.trimIndent()

        val createTriggerQuery = """
        CREATE TRIGGER $triggerName
        AFTER ${actionList.joinToString(" OR ")} ON $tableName
        FOR EACH ROW
        EXECUTE FUNCTION $functionName();
    """.trimIndent()

        return try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { stmt ->
                    stmt.execute(createFunctionQuery) // 함수 생성
                    stmt.execute(createTriggerQuery) // 트리거 생성
                    logger.info { "Trigger '$triggerName' created successfully with actions: ${actionList.joinToString(", ")}" }
                }
            }
            true
        } catch (e: Exception) {
            logger.error { "Failed to create trigger $triggerName - ${e.message}" }
            false
        }
    }
    fun deleteTrigger(triggerName: String, tableName: String) {
        val dropTriggerSQL = "DROP TRIGGER IF EXISTS $triggerName ON $tableName;"
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(dropTriggerSQL)
            }
        }
        logger.info { "Trigger deleted: $triggerName" }
    }

    fun existsTrigger(triggerName: String, tableName: String): Boolean {
        val triggerExistsQuery = """
            SELECT COUNT(*)
            FROM information_schema.TRIGGERS
            WHERE TRIGGER_NAME = ? AND EVENT_OBJECT_TABLE = ?
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(triggerExistsQuery).use { stmt ->
                stmt.setString(1, triggerName)
                stmt.setString(2, tableName)
                stmt.executeQuery().use { rs -> rs.next() && rs.getInt(1) > 0 }
            }
        }
    }
    fun camelToSnake(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }

    fun determineActions(actionType: AuditActionTypeEnum?): String {
        return when (actionType) {
            AuditActionTypeEnum.INSERT -> "INSERT"
            AuditActionTypeEnum.UPDATE -> "UPDATE"
            AuditActionTypeEnum.DELETE -> "DELETE"
            AuditActionTypeEnum.ALL -> "INSERT|UPDATE|DELETE"
            else -> throw IllegalArgumentException("Unsupported audit action: $actionType")
        }
    }
}
