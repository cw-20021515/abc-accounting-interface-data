package com.abc.us.accounting.collects.trigger.audit

import com.abc.us.accounting.collects.domain.entity.audit.AuditEntityLog
import com.abc.us.accounting.supports.properties.SchedulingProperties
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AuditEventListener(
    @Qualifier("schedulingProperties")
    private var schedulingProperties : SchedulingProperties,

    @PersistenceContext
    private val entityManager: EntityManager,
    private val eventPublisher: ApplicationEventPublisher,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Scheduled(
        fixedDelayString = "#{schedulingProperties.auditEventListener.poll.fixedDelayMillis}",
        initialDelayString = "#{schedulingProperties.auditEventListener.poll.initialDelayMillis}"
    )
    fun pollAuditLogs() {
        try {
            processAuditLogs()
        } catch (e: Exception) {
            logger.error(e) { "Error during scheduled polling" }
        }
    }

    @Transactional // 트랜잭션 추가
    public fun processAuditLogs() {
        val auditLogs = getUnprocessedAuditLogs()
        if (auditLogs.isNullOrEmpty()) {
            return
        }

        val eventTables = mapEventTables(auditLogs)
        publishTrigger(eventTables)
    }


    fun getUnprocessedAuditLogs(): List<AuditEntityLog> {
        val query = """
            SELECT a
            FROM AuditEntityLog a
            WHERE a.processed = false AND a.eventTableName != 'audit_target_entity'
        """.trimIndent()
        return entityManager.createQuery(query, AuditEntityLog::class.java).resultList
    }

    fun mapEventTables(auditLogs: List<AuditEntityLog>): MutableMap<String, MutableMap<String, AuditEntityLog>> {
        val eventTables = mutableMapOf<String, MutableMap<String, AuditEntityLog>>()
        auditLogs.forEach { log ->
            eventTables.computeIfAbsent(log.eventTableName!!) { mutableMapOf() }.put(log.entityId!!, log)
            logger.info {
                "Mapping Log - Action: ${log.actionType}, Entity ID: ${log.entityId}, Table: ${log.eventTableName}, Event ID: ${log.eventTableId} at ${log.timestamp}"
            }
        }
        return eventTables
    }

    fun eventLogs(logs : MutableMap<String, AuditEntityLog>) : String {
        val builder = StringBuilder()
        logs.forEach {(entityId,log) ->
            builder.append("entity-id=")
            builder.append(entityId)
            builder.append("action-type=")
            builder.append(log.actionType)
            builder.append("\n")
        }
        return builder.toString()
    }
    fun publishTrigger(eventTables: MutableMap<String, MutableMap<String, AuditEntityLog>>) {
        eventTables.forEach { (tableName, freights) ->
            val listenPath = "trigger/$tableName"
            val trailer = AsyncEventTrailer.Builder()
                .listener(listenPath)
                .freights(freights as MutableMap<String, Any>)
                .build(this)
            logger.info { "TRIGGER-EVENT(${listenPath}) EVENT-DETAILS[${eventLogs(freights)}]" }
            eventPublisher.publishEvent(trailer)
            logger.info { "Published event for table: $tableName" }
        }
    }
}
