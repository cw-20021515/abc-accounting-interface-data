package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.repository.DocumentRepository
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.OffsetDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentReversingTests  (
    private val documentService: DocumentService,
    private val persistenceService: DocumentPersistenceService,
    private val documentRepository: DocumentRepository,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec() {


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun generate(context: DocumentServiceContext = DocumentServiceContext.ONLY_DEBUG,
                 docType:DocumentType, docTemplateId:TestDocumentTemplateMapping): List<DocumentResult> {
        val request = CreateDocumentRequestFixture.generateByTemplate(docType, docTemplateId)
        val result = documentService.posting(context, listOf(request))
        return result
    }

    @Test
    fun `기본 역분개 항목 생성`() {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG

            val results = generate(context, DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED)
            logger.info("Posting results: $results")

            results.size shouldBe 1

            val cleanups:MutableList<DocumentResult> = mutableListOf()
            cleanups.addAll(results)

            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId   shouldContain DocumentType.ACCOUNTING_DOCUMENT.code

                val request = ReversingDocumentRequest(
                    refDocId = result.docId,
                    reason = "Reversing Test",
                    postingDate = LocalDate.now(),
                    documentDate = LocalDate.now(),
                    createTime = OffsetDateTime.now(),
                    createdBy = Constants.APP_NAME,
                )
                val reversing = documentService.reversing(context, listOf(request))
                reversing.size shouldBe 1

                cleanups.addAll(reversing)
            }


            persistenceService.cleanup(context, cleanups.map { it.docId })
        }
    }

    @Test
    fun `refDocId가 없는 경우 확인`() {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.ONLY_DEBUG
            val invalidRefDocId = "1234"
            val request = ReversingDocumentRequest(
                refDocId = invalidRefDocId,
                reason = "Reversing Test",
                postingDate = LocalDate.now(),
                documentDate = LocalDate.now(),
                createTime = OffsetDateTime.now(),
                createdBy = Constants.APP_NAME,
            )
            val exception = shouldThrow<DocumentException.DocumentValidationException> {
                documentService.reversing(context, listOf(request))
            }
            exception.message shouldContain "Invalid document ids"
        }
    }

    @Test
    fun `이미 역분개 된 경우에 대한 확인`() {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG

            // 최초 역분개
            val results = generate(context, DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED)
            logger.info("Posting results: $results")

            results.size shouldBe 1

            val cleanups:MutableList<DocumentResult> = mutableListOf()

            cleanups.addAll(results)

            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId   shouldContain DocumentType.ACCOUNTING_DOCUMENT.code

                val request = ReversingDocumentRequest(
                    refDocId = result.docId,
                    reason = "Reversing Test",
                    postingDate = LocalDate.now(),
                    documentDate = LocalDate.now(),
                    createTime = OffsetDateTime.now(),
                    createdBy = Constants.APP_NAME,
                )
                val reversing = documentService.reversing(context, listOf(request))
                reversing.size shouldBe 1


                // 역분개 참조전표ID로 역분개할때 오류 테스트
                val request1 = ReversingDocumentRequest(
                    refDocId = result.docId,
                    reason = "Reversing Test already #1",
                    postingDate = LocalDate.now(),
                    documentDate = LocalDate.now(),
                    createTime = OffsetDateTime.now(),
                    createdBy = Constants.APP_NAME,
                )

                val exception1 = shouldThrow<DocumentException.DocumentValidationException> {
                    documentService.reversing(context, listOf(request1))
                }
                exception1.message shouldContain "Already reversed document ids"


                // 역분개 전표ID로 역분개할때 오류 테스트
                val request2 = ReversingDocumentRequest(
                    refDocId = reversing[0].docId,
                    reason = "Reversing Test already #2",
                    postingDate = LocalDate.now(),
                    documentDate = LocalDate.now(),
                    createTime = OffsetDateTime.now(),
                    createdBy = Constants.APP_NAME,
                )

                val exception2 = shouldThrow<DocumentException.DocumentValidationException> {
                    documentService.reversing(context, listOf(request1))
                }
                exception2.message shouldContain "Already reversed document ids"
                cleanups.addAll(reversing)
            }

            persistenceService.cleanup(context, cleanups.map { it.docId })
        }
    }

}