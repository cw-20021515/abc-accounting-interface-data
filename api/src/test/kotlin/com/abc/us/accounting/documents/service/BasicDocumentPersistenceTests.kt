package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.repository.DocumentRepository
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentPersistenceTests (
    private val persistenceService: DocumentPersistenceService,
    private val documentService: DocumentService,
    private val documentRepository: DocumentRepository,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    fun generate(context: DocumentServiceContext = DocumentServiceContext.ONLY_DEBUG,
                 docType: DocumentType, docTemplateId: TestDocumentTemplateMapping
    ): List<DocumentResult> {
        val request = CreateDocumentRequestFixture.generateByTemplate(docType, docTemplateId)
        val result = documentService.posting(context, listOf(request))
        return result
    }

    @Test
    fun `basic retry test` () {

        val generateContext = DocumentServiceContext.SAVE_DEBUG
//        run {
//            val context = DocumentServiceContext.SAVE_DEBUG
//            timeLogger.measureAndLog {
//                val results:MutableList<DocumentResult> = mutableListOf()
//                results.addAll(generate(generateContext, DocumentType.ACCOUNTING_DOCUMENT, DocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED))
//
//
//                val docId = results.first().docId
//
//                val document = documentRepository.findById(docId).get()
//
//                val exception = shouldThrow<DocumentException.DocumentSaveException> {
//                    persistenceService.saveAllWithRetry(context, listOf(document.copy(0)), listOf(), listOf(), listOf())
//                }
//                exception.message shouldContain "Failed to document save batch after"
//                persistenceService.cleanup(context, results.map { it.docId })
//            }
//        }

        run {
            val context = DocumentServiceContext.SAVE_DEBUG
            timeLogger.measureAndLog {
                val results:MutableList<DocumentResult> = mutableListOf()
                results.addAll(generate(generateContext, DocumentType.ACCOUNTING_DOCUMENT, TestDocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED))

                val docId = results.first().docId
                val document = documentRepository.findById(docId).get()
                persistenceService.saveAllWithRetry(context, listOf(document), listOf(), listOf(), listOf())
                persistenceService.cleanup(context, results.map { it.docId })
            }
        }

    }

}