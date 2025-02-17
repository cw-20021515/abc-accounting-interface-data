package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.fixtures.UpdateDraftDocumentRequestFixture
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.model.RequestType.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import okhttp3.internal.immutableListOf
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicUpdateDraftDocumentTests  (
    private val documentService: DocumentService,
    private val persistenceService: DocumentPersistenceService,
): AnnotationSpec(){

    val companyCode = CompanyCode.T200

    @Test
    fun `basic update draft documents success`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val documentCount = 3

        // 1) 임시전표 생성
        logger.info("before document generate: ${documentCount}")
        val posted = eachTest(context, 1, count = documentCount, requestType = DRAFT, companyCode = companyCode)
        logger.info("after document posted : ${posted.size}")

        // 2) 정식전표 생성 (전표 항목 숫자가 다른 것으로)
        val requests = posted.map { result -> toAdjustedUpdateRequest(result) }
        val results = documentService.posting(context, requests)

        requests.size shouldBe documentCount
        results.size shouldBe documentCount

        requests.zip(results).forEach { (request, result) ->
            request.docId shouldBe result.docId
            request.docType shouldBe result.docType
            logger.info("before docItems: ${result.docItems.size}, after docItems : ${result.docItems.size}")
        }

        persistenceService.cleanup (context, results.map { it.docId })
    }


    fun toAdjustedUpdateRequest(docResult:DocumentResult, docId:String?= null, docHash:String?=null, companyCode: CompanyCode? = null):UpdateDraftDocumentRequest {
        val modifiedDocId = docId ?: docResult.docId
        val modifiedDocHash = docHash ?: docResult.docHash
        val docOrigin = docResult.docOrigin
        require(docOrigin != null) { "Document Origin is null" }

        val templateMapping = TestDocumentTemplateMapping.entries.random()

        val customerId = docResult.docItems.firstOrNull { it.customerId != null }?.customerId
        val vendorId = docResult.docItems.firstOrNull { it.vendorId != null }?.vendorId
        val newCompanyCode = companyCode ?: docResult.companyCode

        val adjustedRequest = UpdateDraftDocumentRequestFixture.generateByTemplate(
            documentId = modifiedDocId,
            docResult.docType,
            templateMapping,
            docOrigin.toRequest(),
            totalAmount = docResult.txAmount + BigDecimal(100).toScale(),
            docHash = modifiedDocHash,
            customerId = customerId,
            vendorId = vendorId,
            companyCode = newCompanyCode
        )

        return adjustedRequest
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun eachTest(context:DocumentServiceContext, iteration:Int, count:Int=1, requestType: RequestType, docType:DocumentType?=null, companyCode: CompanyCode? = null):List<DocumentResult> {
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

        val results = when(requestType) {
            POSTING -> documentService.posting(context, requests)
            DRAFT -> documentService.draft(context, requests)
            SUBMIT -> TODO()
            REVERSING -> TODO()
            CLEARING -> TODO()
        }

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