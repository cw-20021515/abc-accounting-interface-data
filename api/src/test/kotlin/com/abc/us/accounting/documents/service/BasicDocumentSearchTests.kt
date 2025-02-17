package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.SearchDocumentFilters
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentSearchTests  (
    private val documentSearchServiceable: DocumentSearchServiceable,
    private val documentService: DocumentService,
    private val persistenceService: DocumentPersistenceService,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `basic search documents test` () {
        timeLogger.measureAndLog {
            val template = TestDocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED
            val requests:MutableList<CreateDocumentRequest> = mutableListOf()
            requests.addAll(CreateDocumentRequestFixture.generateByTemplateList(1, DocumentType.CUSTOMER_DOCUMENT, template = template))
            requests.addAll(CreateDocumentRequestFixture.generateByTemplateList(3, DocumentType.JOURNAL_ENTRY))

            for ( request in requests ) {
                logger.info("request: templateId:${request.docOrigin?.docTemplateCode}, contents:$request")
                request.docOrigin!!.docTemplateCode shouldNotBe null

                val template = TestDocumentTemplateMapping.findByTemplateCode(request.docOrigin!!.docTemplateCode!!, request.companyCode)
                val accounts = template.getAccountInfos(request.companyCode)
                request.docItems.size shouldBe accounts.size
                for ( item in request.docItems) {
                    logger.info("item:$item")
                    item.accountCode shouldBeIn accounts.map { it.code }
                }
            }

            val context = DocumentServiceContext.SAVE_DEBUG
            val results = documentService.posting(context, requests)
            results.size shouldBe requests.size
            val minDate = results.map { it.postingDate }.minOrNull()
            val maxDate = results.map { it.postingDate }.maxOrNull()
            logger.info("minDate: $minDate, maxDate: $maxDate")

            val searchRequest = SearchDocumentFilters(docType= DocumentType.JOURNAL_ENTRY)
            var searchResults = documentSearchServiceable.searchDocuments(context, searchRequest)
            logger.info("searchResults: ${searchResults.content.map { it.documentId }}")
            logger.info("searchResults with create_time: ${searchResults.content.map { it.createTime }}")
            searchResults.content.size shouldBe 3

            searchResults = documentSearchServiceable.searchDocuments(context, SearchDocumentFilters(fromDate = LocalDate.of(2024,11,1), toDate = LocalDate.of(2024,11,1)))
            searchResults.content.size shouldBe 0
            persistenceService.cleanup (context, results.map { it.docId })
        }
    }
}