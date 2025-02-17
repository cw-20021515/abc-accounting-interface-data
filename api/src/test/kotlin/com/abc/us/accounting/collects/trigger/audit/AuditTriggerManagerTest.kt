package com.abc.us.accounting.collects.trigger.audit
//
//import com.abc.us.accounting.collects.domain.entity.audit.AuditTargetEntity
//import com.abc.us.accounting.collects.domain.repository.AuditTargetRepository
//import com.abc.us.accounting.collects.domain.type.AuditActionTypeEnum
//import com.abc.us.accounting.supports.properties.SchedulingProperties
//import io.kotest.core.spec.style.AnnotationSpec
//import io.kotest.matchers.shouldBe
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import org.junit.jupiter.api.DisplayName
//import org.springframework.context.event.ContextRefreshedEvent
//import java.sql.Connection
//import java.sql.ResultSet
//import java.sql.Statement
//import javax.sql.DataSource
//
//class AuditTriggerManagerTest : AnnotationSpec() {
//
//    private val dataSource: DataSource = mockk()
//    private val auditTargetRepository: AuditTargetRepository = mockk()
//    private val schedulingProperties: SchedulingProperties = mockk()
//    private lateinit var auditTriggerManager: AuditTriggerManager
//
//    @BeforeEach
//    fun setup() {
//        auditTriggerManager = AuditTriggerManager(
//            schedulingProperties = schedulingProperties,
//            dataSource = dataSource,
//            auditTargetRepository = auditTargetRepository
//        )
//    }
//
//    @Test
//    @DisplayName("Initialize triggers on application start")
//    fun `should initialize triggers on application start`() {
//        // Given
//        val targets = listOf(
//            AuditTargetEntity().apply {
//                entityName = "TestEntity"
//                isActive = true
//                AuditActionTypeEnum = AuditActionTypeEnum.INSERT
//            }
//        )
//        every { auditTargetRepository.findAll() } returns targets
//        every { dataSource.connection } returns mockk(relaxed = true)
//
//        // When
//        auditTriggerManager.onApplicationEvent(ContextRefreshedEvent(mockk()))
//
//        // Then
//        verify { auditTargetRepository.findAll() }
//    }
//
//    @Test
//    @DisplayName("Refresh triggers by adding and removing tables")
//    fun `should refresh triggers by adding and removing tables`() {
//        // Given
//        val connection: Connection = mockk(relaxed = true)
//        val statement: Statement = mockk(relaxed = true)
//        val resultSet: ResultSet = mockk(relaxed = true)
//
//        every { dataSource.connection } returns connection
//        every { connection.createStatement() } returns statement
//        every { statement.executeQuery(any()) } returns resultSet
//        every { resultSet.next() } returnsMany listOf(true, false) // 결과 집합에 1개의 엔트리가 있음
//        every { resultSet.getString("entity_name") } returns "NewEntity"
//
//        val auditTargetEntity = AuditTargetEntity().apply {
//            entityName = "NewEntity"
//            isActive = true
//            AuditActionTypeEnum = AuditActionTypeEnum.ALL
//        }
//
//        // Mock repository method
//        every { auditTargetRepository.findByEntityNameAndIsActiveTrue("NewEntity") } returns listOf(auditTargetEntity)
//
//        // Initial state
//        auditTriggerManager.targetTables = setOf("OldEntity")
//
//        // When
//        auditTriggerManager.refreshTriggers()
//
//        // Then
//        verify { statement.executeQuery("SELECT DISTINCT entity_name FROM audit_target_entity WHERE is_active = 'Y'") }
//        verify { resultSet.close() } // close 호출 확인
//        verify { statement.close() } // close 호출 확인
//        verify { connection.close() } // close 호출 확인
//        verify { auditTargetRepository.findByEntityNameAndIsActiveTrue("NewEntity") } // Repository 호출 확인
//    }
//
//
//    @Test
//    @DisplayName("Configure triggers for active targets")
//    fun `should configure triggers for active targets`() {
//        // Given
//        val connection: Connection = mockk(relaxed = true)
//        val target = AuditTargetEntity().apply {
//            entityName = "TestEntity"
//            isActive = true
//            AuditActionTypeEnum = AuditActionTypeEnum.INSERT
//        }
//
//        every { dataSource.connection } returns connection
//        every { connection.createStatement() } returns mockk(relaxed = true)
//        every { auditTriggerManager.existsTrigger(any(), any()) } returns false
//
//        // When
//        auditTriggerManager.configureTriggers(listOf(target))
//
//        // Then
//        verify { dataSource.connection }
//    }
//
//    @Test
//    @DisplayName("Delete trigger when a target is inactive")
//    fun `should delete trigger when target is inactive`() {
//        // Given
//        val connection: Connection = mockk(relaxed = true)
//        val target = AuditTargetEntity().apply {
//            entityName = "TestEntity"
//            isActive = false
//            AuditActionTypeEnum = AuditActionTypeEnum.INSERT
//        }
//
//        every { dataSource.connection } returns connection
//        every { connection.createStatement() } returns mockk(relaxed = true)
//
//        // When
//        auditTriggerManager.configureTriggers(listOf(target))
//
//        // Then
//        verify { dataSource.connection }
//    }
//
//    @Test
//    @DisplayName("Verify camelCase to snake_case conversion")
//    fun `should convert camelCase to snake_case`() {
//        // When
//        val result = auditTriggerManager.camelToSnake("TestEntityName")
//
//        // Then
//        result shouldBe "test_entity_name"
//    }
//
//    @Test
//    @DisplayName("Determine correct actions based on AuditActionTypeEnum")
//    fun `should determine actions from AuditActionTypeEnum`() {
//        // When
//        val actionsInsert = auditTriggerManager.determineActions(AuditActionTypeEnum.INSERT)
//        val actionsAll = auditTriggerManager.determineActions(AuditActionTypeEnum.ALL)
//
//        // Then
//        actionsInsert shouldBe "INSERT"
//        actionsAll shouldBe "INSERT|UPDATE|DELETE"
//    }
//}
