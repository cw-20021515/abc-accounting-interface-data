package com.abc.us.accounting.qbo.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.SearchDocumentFilters
import com.abc.us.accounting.documents.model.SearchPageRequest
import com.abc.us.accounting.documents.service.DocumentPersistenceService
import com.abc.us.accounting.supports.converter.JsonConverter
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class DocumentCollectService(
    private val documentPersistenceService : DocumentPersistenceService,
) {
    companion object {
        private val converter = JsonConverter()
        private val logger = KotlinLogging.logger {}
    }

    fun attachItems(docMap : MutableMap<String, Document>) : MutableMap<String, DocumentItem> {

        val docItemMap = mutableMapOf<String, DocumentItem>()
        documentPersistenceService.findDocumentItemsByDocIdIn(docMap.keys.toList()).forEach { item ->
            item.attributes = mutableListOf()
            docMap[item.docId]!!.items.add(item)
            docItemMap[item.id] = item
        }
        return docItemMap
    }

    fun attachItemAttributes(docItemMap : MutableMap<String, DocumentItem>) {
        val docItemIds = docItemMap.values.map { it.id }.toMutableSet()
        documentPersistenceService.findDocumentItemAttributesByDocItemIdIn(docItemIds.toList()).forEach { attr ->
            docItemMap[attr.attributeId.docItemId]!!.attributes.add(attr)
        }
    }

    fun attachRelation(docMap : MutableMap<String, Document>) {
        documentPersistenceService.findDocumentRelations(docMap.keys.toList()).forEach { relation ->
            when(relation.relationType) {
                RelationType.CLEARING -> TODO()
                RelationType.PARTIAL_CLEARING -> TODO()
                RelationType.REVERSING -> TODO()
                RelationType.OFFSETTING -> TODO()
            }
        }
    }


    fun canWorkflowStatus(workflowStatus: WorkflowStatus) : Boolean {
        // TODO hschoi --> 추후 유의미한 상태가 되었을 때 적용 예정
        //return workflowStatus == WorkflowStatus.APPROVED
        return true
    }
    fun canDocStatus(docStatus : DocumentStatus) : Boolean {
        return docStatus == DocumentStatus.NORMAL ||
                docStatus == DocumentStatus.REVERSED ||
                docStatus == DocumentStatus.REVERSAL
    }
    fun collect(//companyCode: String,
                fromDateTime : OffsetDateTime,
                toDateTime : OffsetDateTime,
                block : (MutableMap<String, Document>)->Unit) {
        var currentPage = 0
        do {
            var pageable = SearchPageRequest(currentPage, 120)
            var filters = SearchDocumentFilters(
                pageable = pageable,
                dateType = DocumentDateType.POSTING_DATE,
                fromDate = fromDateTime.toLocalDate(),
                toDate = toDateTime.toLocalDate(),
                //companyCode = CompanyCode.of(companyCode)
            )
            val page = documentPersistenceService.searchDocuments(filters)
            currentPage++
            if(page.totalElements > 0) {
                //logger.info { "DOCUMENT-COLLECT(${companyCode}) FROM=(${fromDateTime})~TO(${toDateTime}) TOTAL[${page.totalElements}] CURRENT[${currentPage * page.numberOfElements}]" }
                logger.info { "DOCUMENT-COLLECT " +
                        "FROM=(${fromDateTime})~TO(${toDateTime}) " +
                        "TOTAL[${page.totalElements}] " +
                        "CURRENT[${currentPage * page.numberOfElements}]" }
            }


            val docMap = mutableMapOf<String, Document>()
            page.map {document ->
                if(canWorkflowStatus(document.workflowStatus) &&
                    canDocStatus(document.docStatus)) {
                    document.items = mutableListOf()
                    docMap[document.id] = document
                }else {
                    logger.info("Exclude Submit JournalEntry : " +
                            "Document(docId=${document.id}),status=${document.docStatus.name},workflow=${document.workflowStatus.name}) ")
                }
            }
            // TODO hschoi --> 용도확인 이후 적용 예정
            //attachRelation(docMap)
            val docItemMap = attachItems(docMap)
            attachItemAttributes(docItemMap)

            block(docMap)

        }while(currentPage <= page.totalPages)
    }
}