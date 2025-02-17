package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.fail


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentIdGeneratorTests  (
    private val documentIdGenerator: DocumentIdGenerator,
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun `document id generator test`() {
        timeLogger.measureAndLog {
            val docType = DocumentType.ACCOUNTING_DOCUMENT

            val date = LocalDate.of(2021, 1, 1)
            var documentId = documentIdGenerator.generateDocumentId(docType, date)
            val adjustedYear = (date.year - 2000).toString().padStart(2, '0')
            val dayOfYear = date.dayOfYear.toString().padStart(3, '0')

            documentId shouldBe "${docType.code}${adjustedYear}${dayOfYear}00001"

            logger.info("Generated document ID: $documentId")

            documentId = documentIdGenerator.generateDocumentId(docType, date)
            documentId shouldBe "${docType.code}${adjustedYear}${dayOfYear}00002"
            logger.info("Next Generated document ID: $documentId")
        }
    }

    @Test
    fun `multiple docId generator test`() {
        timeLogger.measureAndLog {
            val count = 100
            val date = LocalDate.of(2021, 1, 2)
            for (i in 1..count) {
                val docType = DocumentType.entries.random()

                val documentId = documentIdGenerator.generateDocumentId(docType, date)
                val adjustedYear = (date.year - 2000).toString().padStart(2, '0')
                val dayOfYear = date.dayOfYear.toString().padStart(3, '0')

                val expected = "${docType.code}${adjustedYear}${dayOfYear}${i.toString().padStart(5, '0')}"
                logger.debug("Generated, $i, document ID: $documentId, expected: $expected")

                documentId shouldBe expected
            }
        }
    }

    /**
     * 비동기로 여러개의 시퀀스 값을 생성하는 테스트
     * 좀더 확인 필요
     */
    @Test
    fun `stress test with many concurrent docId generation requests`() = runBlocking {
        val concurrentRequests = 500

        val date = LocalDate.of(2021, 1, 3)
        val results = mutableSetOf<String>()
        timeLogger.measureAndLog {
            withContext(Dispatchers.IO.limitedParallelism(10)) {
                val values = (1..concurrentRequests).map {
                    async {
                        try {
                            val docType = DocumentType.entries.random()
                            val documentId = documentIdGenerator.generateDocumentId(docType, date)
                            val adjustedYear = (date.year - 2000).toString().padStart(2, '0')
                            val dayOfYear = date.dayOfYear.toString().padStart(3, '0')
                            val expected = "${docType.code}${adjustedYear}${dayOfYear}"
                            logger.debug("End async sequence generator, $it, documentId: $documentId")
                            documentId shouldContain expected
                            documentId
                        } catch (e: Exception) {
                            fail("Error while getting sequence value", e)
                        }
                    }
                }.awaitAll().sorted()

                results.addAll(values)
            }

            logger.info("size:${results.size}, values: $results")
            results shouldHaveSize concurrentRequests
            results.size shouldBe concurrentRequests
        }
    }
}
