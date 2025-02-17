package com.abc.us.accounting.commons.service

import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.fail

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class SequenceServiceTests(
    private val sequenceService: SequenceService,
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun `multiple sync sequence generator test`() {
        val sequenceName = "TEST_SEQ"
        val concurrentRequests = 20

        timeLogger.measureAndLog {
            val values = (1..concurrentRequests).map {
                try {
                    val value = sequenceService.getNextValueWithRetry(sequenceName)
//                    logger.info("End sync sequence generator, $it, value: $value")
                    value
                } catch (e: Exception) {
                    fail("Error while getting sequence value", e)
                }
            }.sorted()
            logger.info("size:${values.size}, values: $values")

            // 검증
            values.size shouldBe concurrentRequests
            values.distinct().size shouldBe concurrentRequests
            (values.last() - values.first()).shouldBe(concurrentRequests - 1L)
        }
    }


    /**
     * 비동기로 여러개의 시퀀스 값을 생성하는 테스트
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stress test with many concurrent requests`() = runBlocking {
        val sequenceName = "TEST_SEQ"
        val concurrentRequests = 100

        val results = mutableSetOf<Long>()
        timeLogger.measureAndLog {
            withContext(Dispatchers.IO.limitedParallelism(10)) {
                val values = (1..concurrentRequests).map {
                    async {
                        try {
                            val value = sequenceService.getNextValueWithRetry(sequenceName)
//                            logger.info("End async sequence generator, $it, value: $value")
                            value
                        } catch (e: Exception) {
                            fail("Error while getting sequence value", e)
                        }
                    }
                }.awaitAll().sorted()

                results.addAll(values)
            }

            results shouldHaveSize concurrentRequests
            results.size shouldBe concurrentRequests
        }
    }
}