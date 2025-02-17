package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.repository.DocumentRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import okhttp3.internal.immutableListOf
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.concurrent.*


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicConcurentDocumentTests  (
    private val documentService: DocumentService,
    private val persistenceService: DocumentPersistenceService,
    private val documentSupportService: DocumentSupportService,
    private val documentRepository: DocumentRepository,
    private val companyServiceable: CompanyServiceable,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec(){

    val companyCode = CompanyCode.T200

    @Test
    fun `basic concurrency test`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val documentCount = 3
        val threadCount = 2
        val timeoutSeconds = 10L

        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<List<DocumentResult>>>()

        logger.info("before document generate: ${documentCount}")
        val results = eachTest(context, 1, count = documentCount, companyCode = companyCode)

        logger.info("after document generated : ${results.size}")

        try {
            // 각 스레드에 태스크 할당
            repeat(threadCount) {
                val future = executor.submit(Callable {
                    try {
                        val requests = results.map { toAdjustedRequest(it, companyCode = companyCode) }
                        logger.info("before positing started, requests:${requests.size}")
                        val value = documentService.posting(context, requests)
                        logger.info("posting finished, value:${value.size}")
                        value
                    } catch (e: Exception) {
                        logger.error("Thread failed: ${e.message}", e)
                        throw e
                    }
                })
                futures.add(future)
            }

            // 결과 수집
            val values = futures.map { future ->
                try {
                    future.get(timeoutSeconds, TimeUnit.SECONDS)
                } catch (e: TimeoutException) {
                    logger.error("Operation timed out after $timeoutSeconds seconds")
                    throw e
                } catch (e: Exception) {
                    logger.error("Error executing task", e)
                    throw e
                }
            }

            logger.info("All threads completed. Results size: ${values.size}")

        } finally {
            // Thread Pool 종료
            executor.shutdown()
            try {
                if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    executor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }

        persistenceService.cleanup (context, results.map { it.docId })
    }

    @Test
    fun `basic concurrency test2`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val documentCount = 1
        val threadCount = 2
        val timeoutSeconds = 10L

        val executor = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<List<DocumentResult>>>()

        val requests = CreateDocumentRequestFixture.generateByTemplateList(documentCount, DocumentType.ACCOUNTING_DOCUMENT, companyCode)
        logger.info("prepare requests : ${requests.size}")

        val results:MutableList<DocumentResult> = mutableListOf()
        try {
            // 각 스레드에 태스크 할당
            repeat(threadCount) {
                val future = executor.submit(Callable {
                    try {
                        logger.info("before positing started, requests:${requests.size}")
                        val value = documentService.posting(context, requests)
                        results.addAll(value)
                        logger.info("posting finished, value:${value.size}")
                        value
                    } catch (e: Exception) {
                        logger.error("Thread failed: ${e.message}", e)
                        throw e
                    }
                })
                futures.add(future)
            }

            // 결과 수집
            val values = futures.map { future ->
                try {
                    future.get(timeoutSeconds, TimeUnit.SECONDS)
                } catch (e: TimeoutException) {
                    logger.error("Operation timed out after $timeoutSeconds seconds")
                    throw e
                } catch (e: Exception) {
                    logger.error("Error executing task", e)
                    throw e
                }
            }

            logger.info("All threads completed. Results size: ${values.size}")

        } finally {
            // Thread Pool 종료
            executor.shutdown()
            try {
                if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    executor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }

        persistenceService.cleanup (context, results.map { it.docId }.distinct())
    }

    fun toAdjustedRequest(docResult:DocumentResult, docId:String?= null, docHash:String?=null, companyCode: CompanyCode? = null):CreateDocumentRequest {
        val modifiedDocId = docId ?: docResult.docId
        val modifiedDocHash = docHash ?: docResult.docHash
        val docOrigin = docResult.docOrigin
        require(docOrigin != null) { "Document Origin is null" }

        val modifiedCompanyCode = companyCode ?: docResult.companyCode

        val templateMapping = TestDocumentTemplateMapping.findByTemplateCode(docOrigin.docTemplateCode, modifiedCompanyCode)

        val customerId = docResult.docItems.firstOrNull { it.customerId != null }?.customerId
        val vendorId = docResult.docItems.firstOrNull { it.vendorId != null }?.vendorId

        val adjustedRequest = CreateDocumentRequestFixture.generateByTemplate(
            docResult.docType,
            templateMapping,
            docOrigin.toRequest(),
            totalAmount = docResult.txAmount + BigDecimal(100).toScale(),
            documentId = modifiedDocId,
            docHash = modifiedDocHash,
            customerId = customerId,
            vendorId = vendorId,
            companyCode = modifiedCompanyCode
        )

        return adjustedRequest
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun eachTest(context:DocumentServiceContext, iteration:Int, count:Int=1, docType:DocumentType?=null, companyCode: CompanyCode? = null):List<DocumentResult> {
        val requests:MutableList<CreateDocumentRequest> = mutableListOf()

        val modifiedDocType = docType ?: DocumentType.JOURNAL_ENTRY
        requests.addAll(CreateDocumentRequestFixture.generateByTemplateList(count, modifiedDocType, companyCode))
        for (request in requests) {
            val validateResults = CreateDocumentValidationRule.validateAll(context, listOf(request) )
            logger.debug("it:$iteration, Validation results: $validateResults, by request: $request")
            validateResults.size shouldBe 0
            val validateItemResults = DocumentItemValidationRule.validateAll(context, request.docItems)
            if (validateItemResults.isNotEmpty()) {
                logger.debug("it:$iteration, Item Validation results: $validateItemResults")
            }
            validateItemResults.size shouldBe 0
        }

        val results = documentService.posting(context, requests)
        logger.info("it:$iteration, Posting results: $results")
        val countOfDistinctDocId = results.map { it.docId }.distinct().size
        logger.info("it:$iteration, countOfDistinctDocId: $countOfDistinctDocId, requests.size: ${requests.size}")
        requests.size shouldBe countOfDistinctDocId
        for (result in results) {
            result.docType shouldBeIn immutableListOf(DocumentType.ACCOUNTING_DOCUMENT, DocumentType.JOURNAL_ENTRY)
            val findresult = documentService.findByDocId(context, result.docId)
            findresult.docId shouldBe result.docId
        }
        return results
    }


}