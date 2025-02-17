package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.repository.*
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.LookupRefDocItemRequest
import com.abc.us.accounting.documents.model.RefDocItemResult
import com.abc.us.accounting.documents.model.SearchDocumentFilters
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class DocumentPersistenceService (
    private val documentRepository: DocumentRepository,
    private val documentOriginRepository: DocumentOriginRepository,
    private val documentItemRepository: DocumentItemRepository,
    private val customDocumentItemRepository: CustomDocumentItemRepository,
    private val documentItemAttributeRepository: DocumentItemAttributeRepository,

    private val documentHistoryRepository: DocumentHistoryRepository,
    private val documentItemHistoryRepository: DocumentItemHistoryRepository,

    private val documentSearchRepository: DocumentSearchRepository,
    private val documentNoteRepository: DocumentNoteRepository,
    private val documentAttachmentRepository: DocumentAttachmentRepository,

    private val documentRelationRepository: DocumentRelationRepository,
    private val customDocumentRelationRepository: CustomDocumentRelationRepository,
    private val documentItemRelationRepository: DocumentItemRelationRepository,
    private val customDocumentItemRelationRepository: CustomDocumentItemRelationRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // document
    fun findDocument(id: String): Document? {
        return documentRepository.findByIdOrNull(id)
    }

    fun findDocuments(ids: List<String>): List<Document> {
        return documentRepository.readOnlyFindAllByIds(Document::class.java, ids)
    }

    fun findDocumentsByDocHashIn(docHashes: List<String>): List<Document> {
        return documentRepository.findAllByDocHashIn(docHashes)
    }

    // document items
    fun findDocumentItems(ids: List<String>): List<DocumentItem> {
        val results = documentItemRepository.readOnlyFindAllByIds(DocumentItem::class.java, ids)
        return results
    }

    fun findDocumentItemsByDocIdIn(docIds: List<String>): List<DocumentItem> {
        return documentItemRepository.findAllByDocIdIn(docIds)
    }

    fun lookupForClearing(companyCode: CompanyCode, startTime: OffsetDateTime, endTime: OffsetDateTime): List<Document>{
        require(startTime.isBefore(endTime)) { "startTime must be before endTime, startTime:${startTime}, endTime:${endTime}" }

        val results = documentRepository.lookupForClearing(companyCode, startTime, endTime)

        return results
    }
    /**
     * 참조전표항목 조회: 조회조건에 orderItemId 추가
     */
    fun lookupRefDocItems(requests: List<LookupRefDocItemRequest>): List<RefDocItemResult> {

        val docTemplateCodes = requests.map { it.docTemplateCode.symbol }.toTypedArray()
        val accountCodes = requests.map { it.accountCode }.toTypedArray()
        val accountSides = requests.map { it.accountSide.code }.toTypedArray()
        val customerIds = requests.map { it.customerId ?: "" }.toTypedArray()
        val vendorIds = requests.map { it.vendorId ?: "" }.toTypedArray()
        val orderItemIds = requests.map { it.orderItemId ?: "" }.toTypedArray()
//
//        return documentItemRepository.findByMatchedParams(docTemplateCodes, accountCodes, accountSides, customerIds, vendorIds)
//        return documentItemRepository.findByMatchedParamsByCriteria(docTemplateCodes, accountCodes, accountSides, customerIds, vendorIds, orderItemIds)
        return customDocumentItemRepository.findByMatchedParamsByCriteria(docTemplateCodes, accountCodes, accountSides, customerIds, vendorIds, orderItemIds)
    }

    // document origins
    fun findDocumentOriginsByDocIdIn(docIds: List<String>): List<DocumentOrigin> {
        return documentOriginRepository.findAllByDocIdIn(docIds)
    }

    fun findDocumentItemAttributesByDocItemIdIn(docItemIds: List<String>): List<DocumentItemAttribute> {
        return documentItemAttributeRepository.findAllByDocItemIdIn(docItemIds)
    }


    // document relations
    fun findDocumentRelations(documentIds: List<String>): List<DocumentRelation> {
        return documentRelationRepository.findAllByDocIdIn(documentIds)
    }

    fun findDocumentRelations(docIds: List<String>, refDocIds:List<String>, relationTypes: List<RelationType> = RelationType.entries ): List<DocumentRelation> {
//        return documentRelationRepository.findRelations(docIds, refDocIds, relationTypes)
        return customDocumentRelationRepository.findRelations(docIds, refDocIds, relationTypes)
    }

    // document item relations
    fun findDocumentItemRelations(docItemIds: List<String>, refDocItemIds:List<String>, relationTypes: List<RelationType> = RelationType.entries): List<DocumentItemRelation> {
        return customDocumentItemRelationRepository.findRelations(docItemIds, refDocItemIds, relationTypes)
    }

    fun searchDocuments(request: SearchDocumentFilters): Page<Document> {
        return documentSearchRepository.searchDocuments(request)
    }




    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun saveDocumentsWithRetry(context: DocumentServiceContext,
                               documents: List<Document>):List<Document>{

        logger.debug { "saveDocumentsWithRetry, start, documents=${documents.map { it.id }}" }

        var lastException: Exception? = null
        var documentsToSave = documents
        var attempt = 1
        if ( documentsToSave.isEmpty() ) {
            logger.error { "documentsToSave was empty" }
            return listOf()
        }

        for ( i in 1..Constants.MAX_ATTEMPT) {
            attempt = i
            try {
                logger.debug { "saveDocumentsWithRetry, attempt:${attempt}, documentsToSave=${documentsToSave.map { it.id }}" }
                documentsToSave =  bulkSaveDocuments(documentsToSave)
                logger.debug { "saveDocumentsWithRetry, succeeded by attempt:${attempt}, documentsToSave=${documentsToSave.map { it.id }}" }
                return documentsToSave
            } catch (e: Exception) {
                lastException = e
                logger.warn("saveDocumentsWithRetry, failed by attempt $attempt, exception: ${e.message}, documentsToSave=${documentsToSave.size}, exception: ${lastException.message}", lastException)

                val byDocHashError = when (e) {
                    is DataIntegrityViolationException -> true
                    else -> false
                }

                if ( byDocHashError) {
                    logger.warn("saveDocumentsWithRetry, this error is not retryable exception!!", lastException)
                    throw lastException
                }

                // 버전 업데이트를 새로운 트랜잭션에서 실행
                documentsToSave = try {
                    updateDocumentVersions(context, documentsToSave)
                } catch (updateEx: Exception) {
                    logger.error("version update failed", updateEx)
                    documentsToSave
                }
            }
            if (documentsToSave.isEmpty()) break
        }

        logger.error { "saveAllDocumentsWithRetry failed by attempt:$attempt, documents=${documents.map { it.id }}, documentsToSave=${documentsToSave.size}" }
        if (documentsToSave.isNotEmpty()) {
            throw DocumentException.DocumentSaveException(
                "Failed after $attempt attempts",
                cause = lastException
            )
        }
        return documentsToSave
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun saveDocItemsWithRetry(context: DocumentServiceContext,
                              docItems: List<DocumentItem>,
                              isUpdateDraft:Boolean = false):List<DocumentItem>{
        logger.debug { "saveDocItemsWithRetry, start, docItems=${docItems.map { it.id }}" }

        var lastException: Exception? = null
        var docItemsToSave = docItems
        var attempt = 1

        for ( i in 1..Constants.MAX_ATTEMPT) {
            attempt = i
            try {
                docItemsToSave =  bulkSaveDocumentItems(docItemsToSave, isUpdateDraft)
                logger.debug { "saveDocItemsWithRetry, succeeded by attempt:${attempt}, docItemsToSave=${docItemsToSave.map { it.id }}" }
                return docItemsToSave
            } catch (e: Exception) {
                lastException = e
                logger.warn("saveDocItemsWithRetry attempt $attempt failed: ${e.message}, docItemsToSave=${docItemsToSave.size}, exception: ${lastException.message}", lastException)

                val reloadIds = docItemsToSave.map { it.id }.toSet()
                if (reloadIds.isEmpty()) break

                // 버전 업데이트를 새로운 트랜잭션에서 실행
                docItemsToSave = try {
                    updateDocItemVersions(context, docItemsToSave)
                } catch (updateEx: Exception) {
                    logger.error("saveDocItemsWithRetry, version update failed", updateEx)
                    docItemsToSave
                }
            }
            if (docItemsToSave.isEmpty()) break
        }

        logger.error { "saveDocItemsWithRetry, failed by attempt:$attempt, docItems=${docItems.map { it.id }}, docItemsToSave=${docItemsToSave.size}" }
        if (docItemsToSave.isNotEmpty()) {
            throw DocumentException.DocumentSaveException(
                "Failed after $attempt attempts",
                cause = lastException
            )
        }
        return docItemsToSave
    }

    private fun updateDocumentVersions(context: DocumentServiceContext, documents: List<Document>): List<Document> {
        val foundDocuments = documentRepository.readOnlyFindAllByIds(Document::class.java, documents.map { it.id })
        logger.debug { "updateDocumentVersions - foundDocuments:${foundDocuments.size}, inputs: docIds:${documents.map { it.id }}}" }
        return documents
            .map { doc ->
                var newDoc = doc
                val current = foundDocuments.find { it.id == doc.id }
                if (current != null) {
                    newDoc = newDoc.copy(current.version)
                }
                newDoc
            }
    }


    private fun updateDocItemVersions(context: DocumentServiceContext, docItems: List<DocumentItem>): List<DocumentItem> {
        logger.debug { "updateDocItemVersions, docItems=${docItems.map { it.id }}" }
        val foundDocItems = documentItemRepository.readOnlyFindAllByIds(DocumentItem::class.java, docItems.map { it.id })

        return docItems
            .map { docItem ->
                var newDocItem = docItem
                val current = foundDocItems.find { it.id == docItem.id }
                if (current != null) {
                    newDocItem = newDocItem.copy(current.version)
                }
                newDocItem
            }
    }

    fun bulkSaveDocuments(entities: List<Document>): List<Document> {
        val (toInsert, toUpdate) = entities.partition { it.isNew }
        logger.info{"bulkSaveDocuments, entities: ${entities.size}, toInsert: ${toInsert.map { it.id }}, toUpdate: ${toUpdate.map{it.id}}"}
        val inserted = documentRepository.bulkInsert(toInsert)
        val updated = documentRepository.bulkUpdate(toUpdate)
        return inserted + updated
    }

    fun bulkSaveDocumentItems(entities: List<DocumentItem>, isUpdateDraft:Boolean = false): List<DocumentItem> {
        val (toInsert, toUpdate) = entities.partition { it.isNew }
        logger.info{"bulkSaveDocItems, isUpdateDrafe:$isUpdateDraft, entities: ${entities.size}, toInsert: ${toInsert.map { it.id }}, toUpdate: ${toUpdate.map{it.id}}"}

        if ( isUpdateDraft ) {
            documentItemRepository.bulkDeleteByIds(DocumentItem::class.java, entities.map { it.id })
            val inserted = documentItemRepository.bulkInsert(entities)
            return inserted
        } else {
            val inserted = documentItemRepository.bulkInsert(toInsert)
            val updated = documentItemRepository.bulkUpdate(toUpdate)
            return inserted + updated
        }
    }

    fun bulkSaveDocumentOrigins(documents:List<Document>, entities: List<DocumentOrigin>): List<DocumentOrigin> {
        val (toInsert, toUpdate) = entities.partition {  origin ->
            val document = documents.find { document -> document.id == origin.docId }
            val update = (document != null && origin.docId == document.id && !document.isNew)
            !update
        }
        logger.info{"bulkSaveDocumentOrigins, entities: ${entities.size}, toInsert: ${toInsert.map { it.docId }}, toUpdate: ${toUpdate.map{it.docId}}"}
        val inserted = documentOriginRepository.bulkInsert(toInsert)
        val updated = documentOriginRepository.bulkUpdate(toUpdate)
        return inserted + updated
    }

    fun bulkSaveDocumentItemAttributes(docItems:List<DocumentItem>, entities: List<DocumentItemAttribute>): List<DocumentItemAttribute> {
        logger.info{"bulkSaveDocumentItemAttributes, entities: ${entities.size}"}

        // 전체 삭제 및 추가로 변경
        val attributeIds = entities.map { it.attributeId }.toSet()
        documentItemAttributeRepository.bulkDeleteByIds(DocumentItemAttribute::class.java, attributeIds)
        val saved = documentItemAttributeRepository.bulkInsert(entities)
        return saved
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun saveAllWithRetry(context: DocumentServiceContext,
                documents: List<Document>,
                documentItems:List<DocumentItem>,
                documentOrigins:List<DocumentOrigin>,
                documentItemAttributes:List<DocumentItemAttribute>,
                documentRelations:List<DocumentRelation> = listOf(),
                documentItemRelations:List<DocumentItemRelation> = listOf(),
                isUpdateDraft:Boolean = false) {
        logger.info{"saveAllWithRetry, documents: ${documents.size}, documentItems: ${documentItems.size}, documentOrigins: ${documentOrigins.size}, documentItemAttributes: ${documentItemAttributes.size}, documentRelations: ${documentRelations.size}, documentItemRelations: ${documentItemRelations.size}"}

        val savedDocuments = saveDocumentsWithRetry(context, documents)
        val savedDocumentItems = saveDocItemsWithRetry(context, documentItems, isUpdateDraft)
        bulkSaveDocumentOrigins(documents, documentOrigins)
        bulkSaveDocumentItemAttributes(documentItems, documentItemAttributes)

        documentRelationRepository.bulkInsert(documentRelations)
        documentItemRelationRepository.bulkInsert(documentItemRelations)
        documentHistoryRepository.bulkInsert(savedDocuments.map { it.toHistory() })
        documentItemHistoryRepository.bulkInsert(savedDocumentItems.map { it.toHistory() })
    }

    /**
     * WARNING: for test
     * 전표 삭제 (테스트 목적을 위해 만든 메소드, dev, stg, prd 환경 에서는 사용 하면 안됨)
     */
    @Transactional
    fun cleanup(context: DocumentServiceContext, docIds: List<String>) {
        logger.warn { "cleanup, must be use by test only!!! count:${docIds.size}, docIds: $docIds, context:$context" }

        val docItems = documentItemRepository.findAllByDocIdIn(docIds)
        val docItemAttributes = documentItemAttributeRepository.findAllByDocItemIdIn(docItems.map { it.id })

        val docRelations = findDocumentRelations(docIds)
        val docItemRelations = findDocumentItemRelations(docItems.map { it.id }, docItems.map { it.id }, RelationType.entries)

        val docNotes = documentNoteRepository.findAllByDocIdIn(docIds)
        val docAttachments = documentAttachmentRepository.findAllByDocIdIn(docIds)
        val docHistories = documentHistoryRepository.findAllByDocIdIn(docIds)
        val docItemHistories = documentItemHistoryRepository.findAllByDocItemIdIn(docItems.map { it.id })

        documentHistoryRepository.bulkDeleteByIds(DocumentHistory::class.java, docHistories.map { it.id })
        documentItemHistoryRepository.bulkDeleteByIds(DocumentItemHistory::class.java, docItemHistories.map { it.id })

        documentRelationRepository.bulkDeleteByIds(DocumentRelation::class.java, docRelations.map { it.id })
        documentItemRelationRepository.bulkDeleteByIds(DocumentItemRelation::class.java, docItemRelations.map { it.id })

        documentNoteRepository.bulkDeleteByIds(DocumentNote::class.java, docNotes.map { it.id })
        documentAttachmentRepository.bulkDeleteByIds(DocumentAttachment::class.java, docAttachments.map { it.id })
        documentItemAttributeRepository.bulkDeleteByIds(DocumentItemAttribute::class.java, docItemAttributes.map { it.attributeId })
        documentItemRepository.bulkDeleteByIds(DocumentItem::class.java, docItems.map { it.id })
        documentOriginRepository.bulkDeleteByIds(DocumentOrigin::class.java, docIds)
        documentRepository.bulkDeleteByIds(Document::class.java, docIds)
    }
}