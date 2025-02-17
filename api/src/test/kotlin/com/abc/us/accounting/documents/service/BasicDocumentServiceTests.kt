package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.repository.DocumentRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.internal.immutableListOf
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentServiceTests  (
    private val documentService: DocumentService,
    private val persistenceService: DocumentPersistenceService,
    private val documentSupportService: DocumentSupportService,
    private val documentRepository: DocumentRepository,
    private val companyServiceable: CompanyServiceable,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec(){

    val companyCode = CompanyCode.T200


    @Test
    fun `basic create document only test with two items`() {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.ONLY_DEBUG
            val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
            val documetItemRequests = mutableListOf(
                DocumentItemRequest(
                    companyCode= companyCode,
                    accountCode = "1136010",    // 카드미수금
                    accountSide = AccountSide.DEBIT,
                    txCurrency = txCurrency,
                    txAmount= BigDecimal(1000),
                    text = "test",
                    costCenter = "CC1",
                    profitCenter = "PC1",
                    segment = "SG1",
                    project = "PJ1",
                    customerId = "C1",
                    vendorId = "V1",
                ),
                DocumentItemRequest(
                    companyCode= companyCode,
                    accountCode = "2111010",    // 선수금
                    accountSide = AccountSide.CREDIT,
                    txCurrency = txCurrency,
                    txAmount= BigDecimal(1000),
                    text = "test",
                    costCenter = "CC2",
                    profitCenter = "PC2",
                    segment = "SG2",
                    project = "PJ2",
                    customerId = "C2",
                    vendorId = "V2",
                )
            )

            val request = CreateDocumentRequest(
                documentDate = LocalDate.now(),
                text = "Test Document",
                docItems = documetItemRequests
            )
            val document = documentSupportService.createDocument(context, RequestType.POSTING, request)

            document.docType.code shouldBe "AB"
            logger.info("Next Generated document ID: ${document.id}")
        }
    }

    @Test
    fun `basic validation test` () {
        val context = DocumentServiceContext.ONLY_DEBUG

        run {
            val request = CreateDocumentRequestFixture.generateByTemplate(DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED)
            val results= CreateDocumentValidationRule.validateAll(context, listOf( request) )
            logger.info("Validation results: $results")
            results.size shouldBe 0

            val itemValidationResults = DocumentItemValidationRule.validateAll(context, request.docItems)
            logger.info("Item Validation results: $itemValidationResults")
            itemValidationResults.size shouldBe 0
        }

        run {   // totalAmount가 0인 경우
            val request = CreateDocumentRequestFixture.generateByTemplate(DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.ONETIME_PAYMENT_RECEIVED)
            val modifiedRequest = request.copy(docItems = request.docItems.map { it.copy(txAmount = BigDecimal.ZERO) }.toMutableList())
            val results= CreateDocumentValidationRule.validateAll(context, listOf( modifiedRequest) )
            logger.info("Validation results: $results")
            results.size shouldBe 1

            val itemValidationResults = DocumentItemValidationRule.validateAll(context, modifiedRequest.docItems)
            logger.info("Item Validation results: $itemValidationResults")
            itemValidationResults.size shouldBe 1
        }
    }

    @Test
    fun `basic posting test without save` () {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.ONLY_DEBUG

            val request = CreateDocumentRequestFixture.generateByTemplate(DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED)
            val validateResults= CreateDocumentValidationRule.validateAll(context, listOf(request) )
            logger.info("Validation results: $validateResults")
            validateResults.size shouldBe 0

            val results = documentService.posting(context, listOf(request))
            logger.info("Posting result: $results")

            results.size shouldBe 1
            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId shouldContain DocumentType.ACCOUNTING_DOCUMENT.code
            }

        }
    }

    @Test
    fun `basic posting test with save` () {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG

            val request = CreateDocumentRequestFixture.generateByTemplate(DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED)
            val validationResults= CreateDocumentValidationRule.validateAll(context, listOf(request) )
            logger.info("Validation results: $validationResults")
            validationResults.size shouldBe 0

            val results = documentService.posting(context, listOf(request))
            logger.info("Posting result: $results")

            results.size shouldBe 1
            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId   shouldContain DocumentType.ACCOUNTING_DOCUMENT.code
            }

            val document = documentRepository.findById(results.first().docId)
            logger.info ("Document: $document")

            document.isPresent shouldBe true
            val documentByDocHash = documentRepository.findByDocHash(results.first().docHash)
            documentByDocHash shouldBe document.get()

            persistenceService.cleanup(context, listOf(document.get().id))
        }
    }

    @Test
    fun `basic posting test with save and find by docId` () {
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG

            val request = CreateDocumentRequestFixture.generateByTemplate(
                DocumentType.ACCOUNTING_DOCUMENT,
                TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED
            )
            val validationResults = CreateDocumentValidationRule.validateAll(context, listOf(request))
            logger.info("Validation results: $validationResults")
            validationResults.size shouldBe 0

            val results = documentService.posting(context, listOf(request))
            logger.info("Posting result: $results")

            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId shouldContain DocumentType.ACCOUNTING_DOCUMENT.code
                val findresult = documentService.findByDocId(context, result.docId)
                findresult.docId shouldBe result.docId
            }

            persistenceService.cleanup(context, results.map { it.docId })
        }
    }

    @Test
    fun `basic overwrite test`() {
        val context = DocumentServiceContext.SAVE_DEBUG

        run { // 충돌 안나고 overwrite 되는 케이스
            val results = eachTest(context, 1, count = 1, companyCode = companyCode)
            val findResults = documentService.findAllByDocIds(context, results.map { it.docId })
            val requests = findResults.map { toAdjustedRequest(it, companyCode = companyCode) }
            val newResults = documentService.posting(context, requests)
            logger.info("Posting results: $results")

            for (newResult in newResults) {
                val originResult = results.first { it.docId == newResult.docId }
                newResult.docType shouldBe originResult.docType
                newResult.docId shouldBe originResult.docId
                newResult.docHash shouldBe originResult.docHash
                newResult.updateTime shouldBeGreaterThan originResult.updateTime
                newResult.createTime.isEqual(originResult.createTime) shouldBe true

                logger.info ("Document: docId:${newResult.docId}, docHash:${newResult.docHash}, " +
                        "createTime new:${newResult.createTime}, old:${originResult.createTime}, " +
                        "updateTime new:${newResult.updateTime}, old:${originResult.updateTime}")
            }
            persistenceService.cleanup(context, results.map { it.docId })
        }

        run {   // 충돌나는 케이스
            val results = eachTest(context,1, companyCode = companyCode)
            val requests = results.map { toAdjustedRequest(it, docId = DocumentType.JOURNAL_ENTRY.code + "12345", docHash = null, companyCode = companyCode) }
            val newResults = documentService.posting(context, requests)
//            val exception1 = shouldThrow<DataIntegrityViolationException> {
//                documentService.posting(context, requests)
//            }
//            exception1.message shouldContain "duplicate key value violates unique constraint"
            for (newResult in newResults) {
                val originResult = results.first { it.docId == newResult.docId }

                newResult.docType shouldBe originResult.docType
                newResult.docId shouldBe originResult.docId
                newResult.docHash shouldBe originResult.docHash
                newResult.updateTime shouldBeGreaterThan originResult.updateTime
                newResult.createTime.isEqual(originResult.createTime) shouldBe true

                logger.info ("Document: docId:${newResult.docId}, docHash:${newResult.docHash}, " +
                        "createTime new:${newResult.createTime}, old:${originResult.createTime}, " +
                        "updateTime new:${newResult.updateTime}, old:${originResult.updateTime}")
            }

            persistenceService.cleanup(context, newResults.map { it.docId })
            persistenceService.cleanup(context, results.map { it.docId })
        }

        run {   // docId는 같고 docHash가 다른 경우
            val results = eachTest(context,1, companyCode = companyCode)
            val requests = results.map { toAdjustedRequest(it, docId = null, docHash = "123", companyCode=companyCode) }
            val newResults = documentService.posting(context, requests)

            persistenceService.cleanup(context, newResults.map { it.docId })
        }
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


    /**
     * Posting multiple documents with different types
     * TODO: Need to add more test cases (Transaciotn 간섭현상, 추후 수정필요, DocId 중복현상 발생)
     */
    @Test
    fun `basic multiple posting test with save` () {
        val context = DocumentServiceContext.SAVE_DEBUG
        timeLogger.measureAndLog {
            val results = eachTest(context,1)

            persistenceService.cleanup(context, results.map { it.docId })
        }
    }

    /**
     * 필요시에만 테스트
     */
    @Test
    fun `multiple posting iteration test` () {
        val context = DocumentServiceContext.SAVE_DEBUG
        timeLogger.measureAndLog {
            val results:MutableList<DocumentResult> = mutableListOf()
            for (i in 1..2) {
                logger.debug("Iteration: $i")
                results.addAll(eachTest(context, i))

            }
            persistenceService.cleanup(context, results.map { it.docId })
        }
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