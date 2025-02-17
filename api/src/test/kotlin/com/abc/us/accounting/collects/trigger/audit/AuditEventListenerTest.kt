package com.abc.us.accounting.collects.trigger.audit
//
//import com.abc.us.accounting.collects.domain.entity.audit.AuditEntityLog
//import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
//import com.abc.us.accounting.supports.properties.SchedulingProperties
//import io.kotest.core.spec.style.AnnotationSpec
//import io.mockk.*
//import jakarta.persistence.EntityManager
//import jakarta.persistence.TypedQuery
//import org.springframework.context.ApplicationEventPublisher
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//import org.junit.jupiter.api.DisplayName
//
//class AuditEventListenerTest : AnnotationSpec() {
//
//    private val entityManager = mockk<EntityManager>()
//    private val eventPublisher = mockk<ApplicationEventPublisher>()
//    private val schedulingProperties = mockk<SchedulingProperties>()
//    private lateinit var listener: AuditEventListener
//
//    @BeforeEach
//    fun setup() {
//        clearAllMocks()
//        listener = AuditEventListener(schedulingProperties, entityManager, eventPublisher)
//    }
//
//    @Test
//    @DisplayName("미처리된 감사 로그를 정상적으로 조회해야 한다")
//    fun testGetUnprocessedAuditLogs() {
//        // given
//        val mockQuery = mockk<TypedQuery<AuditEntityLog>>()
//        val expectedLogs = listOf(
//            AuditEntityLog().apply {
//                eventTableName = "test_table"
//                entityId = "1"
//                actionType = "INSERT"
//                processed = false
//            }
//        )
//
//        every {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        } returns mockQuery
//        every { mockQuery.resultList } returns expectedLogs
//
//        // when
//        val result = listener.getUnprocessedAuditLogs()
//
//        // then
//        assertEquals(expectedLogs, result)
//        verify { entityManager.createQuery(any<String>(), AuditEntityLog::class.java) }
//    }
//
//    @Test
//    @DisplayName("이벤트 테이블이 올바르게 매핑되어야 한다")
//    fun testMapEventTables() {
//        // given
//        val auditLogs = listOf(
//            AuditEntityLog().apply {
//                eventTableName = "test_table"
//                entityId = "1"
//                actionType = "INSERT"
//            },
//            AuditEntityLog().apply {
//                eventTableName = "test_table"
//                entityId = "2"
//                actionType = "UPDATE"
//            }
//        )
//
//        // when
//        val result = listener.mapEventTables(auditLogs)
//
//        // then
//        assertEquals(1, result.size)
//        assertEquals(2, result["test_table"]?.size)
//        assertTrue(result["test_table"]?.containsKey("1") == true)
//        assertTrue(result["test_table"]?.containsKey("2") == true)
//    }
//
//    @Test
//    @DisplayName("이벤트가 올바르게 발행되어야 한다")
//    fun testPublishTrigger() {
//        // given
//        val eventTables = mutableMapOf<String, MutableMap<String, AuditEntityLog>>()
//        val innerMap = mutableMapOf<String, AuditEntityLog>()
//        innerMap["1"] = AuditEntityLog().apply {
//            eventTableName = "test_table"
//            entityId = "1"
//        }
//        eventTables["test_table"] = innerMap
//
//        every { eventPublisher.publishEvent(any<AsyncEventTrailer>()) } just Runs
//
//        // when
//        listener.publishTrigger(eventTables)
//
//        // then
//        verify { eventPublisher.publishEvent(any<AsyncEventTrailer>()) }
//    }
//
//    @Test
//    @DisplayName("전체 감사 로그 처리 프로세스가 정상 동작해야 한다")
//    fun testProcessAuditLogs() {
//        // given
//        val mockLogs = listOf(
//            AuditEntityLog().apply {
//                eventTableName = "test_table"
//                entityId = "1"
//                actionType = "INSERT"
//                processed = false
//            }
//        )
//        val mockQuery = mockk<TypedQuery<AuditEntityLog>>()
//
//        every {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        } returns mockQuery
//        every { mockQuery.resultList } returns mockLogs
//        every { eventPublisher.publishEvent(any<AsyncEventTrailer>()) } just Runs
//
//        // when
//        listener.processAuditLogs()
//
//        // then
//        verifySequence {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//            mockQuery.resultList
//            eventPublisher.publishEvent(any<AsyncEventTrailer>())
//        }
//    }
//
//    @Test
//    @DisplayName("빈 로그 리스트에 대해 정상 처리되어야 한다")
//    fun testProcessEmptyAuditLogs() {
//        // given
//        val mockQuery = mockk<TypedQuery<AuditEntityLog>>()
//
//        every {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        } returns mockQuery
//        every { mockQuery.resultList } returns emptyList()
//
//        // when
//        listener.processAuditLogs()
//
//        // then
//        verify {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        }
//        verify(exactly = 0) { eventPublisher.publishEvent(any<AsyncEventTrailer>()) }
//    }
//
//    @Test
//    @DisplayName("pollAuditLogs에서 예외 발생시 로그가 기록되어야 한다")
//    fun testPollAuditLogsExceptionHandling() {
//        // given
//        val mockQuery = mockk<TypedQuery<AuditEntityLog>>()
//
//        every {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        } throws RuntimeException("Test Exception")
//
//        // when
//        listener.pollAuditLogs()
//
//        // then
//        verify {
//            entityManager.createQuery(any<String>(), AuditEntityLog::class.java)
//        }
//    }
//}