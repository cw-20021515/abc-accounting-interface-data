package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.AccountBalanceConfig
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentItemStatus
import com.abc.us.accounting.documents.domain.type.DocumentStatus
import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.model.RequestType.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.retry.RecoveryCallback
import org.springframework.retry.RetryCallback
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


interface DocumentServiceable {
    // 전표 등록 (복수건, 권장)
    fun posting (context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult>

    // 전표 임시저장
    fun draft(context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult>

    // 전표 제출(승인요청)
    fun submit(context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult>

    // review에서 상태변환
    fun updateReviewStatus(context: DocumentServiceContext, docIds: List<String>, requestType:RequestType): List<DocumentResult>

    // 전표 반제
    fun clearing(context: DocumentServiceContext, requests: List<ClearingDocumentRequest>): List<DocumentResult>

    // 전표 역분개
    fun reversing(context: DocumentServiceContext, requests: List<ReversingDocumentRequest>): List<DocumentResult>

    // 전표 반제를 위한 조회
    fun lookupForClearing(context: DocumentServiceContext, companyCode: CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now()): List<DocumentResult>

    // 반제를 위한 참조전표항목ID 조회
    fun lookupRefDocItems(context: DocumentServiceContext, requests: List<LookupRefDocItemRequest>): List<RefDocItemResult>

    // 전표 상세 조회
    fun findByDocId(context: DocumentServiceContext, documentId:String):DocumentResult

    fun findAllByDocIds(context: DocumentServiceContext, documentIds:List<String>):List<DocumentResult>

    fun findByDocHash(context: DocumentServiceContext, docHash:String):DocumentResult

    fun findAllByDocHashes(context: DocumentServiceContext, docHashes:List<String>):List<DocumentResult>

}


@Service
class DocumentService(
    private val supportService: DocumentSupportService,
    private val persistence: DocumentPersistenceService,
    private val approvalRuleService: DocumentApprovalRuleService,
    private val documentRelationService: DocumentRelationService,
    private val fiscalClosingService: FiscalClosingService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val accountBalanceConfig: AccountBalanceConfig,
    @Qualifier("documentServiceRetry") private val retryTemplate: RetryTemplate
): DocumentServiceable{

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * posting documents
     * - OptimisticLock, CannotAcquireLockException 발생시 재시도 추가
     * - Transaction 격리수준: READ_COMMITTED으로 조정
     */
    override fun posting (context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult> {
        validateRequireApproval(context, requests)

        val createRequests = requests.filterIsInstance<CreateDocumentRequest>()
        val updateDraftRequests = requests.filterIsInstance<UpdateDraftDocumentRequest>()
        require(createRequests.isNotEmpty() xor updateDraftRequests.isNotEmpty()) { "requests must be create or updateDraft Request" }

        if (createRequests.isNotEmpty()) {
            return createDocuments(context, POSTING, createRequests)
        }
        if (updateDraftRequests.isNotEmpty()) {
            return updateDraftDocuments(context, POSTING, updateDraftRequests)
        }
        return listOf()
    }

    override fun draft(context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult> {
        val createRequests = requests.filterIsInstance<CreateDocumentRequest>()
        val updateDraftRequests = requests.filterIsInstance<UpdateDraftDocumentRequest>()
        require(createRequests.isNotEmpty() xor updateDraftRequests.isNotEmpty()) { "requests must be create or updateDraft Request" }

        if (createRequests.isNotEmpty()) {
            return createDocuments(context, DRAFT, createRequests)
        }
        if (updateDraftRequests.isNotEmpty()) {
            return updateDraftDocuments(context, DRAFT, updateDraftRequests)
        }
        return listOf()
    }

    override fun submit(context: DocumentServiceContext, requests: List<HashableDocumentRequest>): List<DocumentResult> {
        val createRequests = requests.filterIsInstance<CreateDocumentRequest>()
        val updateDraftRequests = requests.filterIsInstance<UpdateDraftDocumentRequest>()
        require(createRequests.isNotEmpty() xor updateDraftRequests.isNotEmpty()) { "requests must be create or updateDraft Request" }

        if (createRequests.isNotEmpty()) {
            return createDocuments(context, SUBMIT, createRequests)
        }
        if (updateDraftRequests.isNotEmpty()) {
            return updateDraftDocuments(context, SUBMIT, updateDraftRequests)
        }
        return listOf()
    }

    /**
     * 전표 신규 등록 케이스
     */
    fun createDocuments (context: DocumentServiceContext, requestType: RequestType, requests: List<CreateDocumentRequest>): List<DocumentResult> {
        return retryTemplate.execute(RetryCallback { retryContext ->
            logger.info { "create documents, requests: ${requests.size}, context:$context, requestType:$requestType" }
            val allowedRequestTypes = listOf(RequestType.DRAFT, RequestType.SUBMIT, RequestType.POSTING)
            require(allowedRequestTypes.contains(requestType) ) {
                "request type not allowed by createDocuments:requests: ${requests.size}, only allowed by ${allowedRequestTypes}"
            }

            validateFiscalClosing(context, requests)
            require(requests.isNotEmpty()) { "create documents, context:$context, requests must not be empty" }
            require(requests.all { it.docItems.isNotEmpty() }) { "docItems must not be empty" }
            if (requests.size > Constants.DOCUMENT_BATCH_SIZE) {
                logger.warn("Too many requests found by createDocuments, requests:${requests.size}, context:${context}")
                if (context.enableBatchLimit) {
                    throw IllegalArgumentException("Too many requests found by createDocuments, requests:${requests.size}, context:${context}")
                }
            }

            val documents: MutableList<Document> = mutableListOf()
            val documentOrigins: MutableList<DocumentOrigin> = mutableListOf()
            val documentItems: MutableList<DocumentItem> = mutableListOf()
            val documentItemAttributeMap: MutableMap<String, List<DocumentItemAttribute>> = mutableMapOf()

            // validation
            CreateDocumentValidationRule.validate(context, requests)
            DocumentItemValidationRule.validate(context, requests.flatMap { it.docItems })

            // 기존 전표 확인
            val docHashes = requests.mapNotNull { it.docHash }
            val exists = if (docHashes.isEmpty()) listOf() else persistence.findDocumentsByDocHashIn(docHashes)
            val existsDocItemsMap =
                if (exists.isEmpty()) mapOf() else persistence.findDocumentItemsByDocIdIn(exists.map { it.id })
                    .groupBy { it.docId }
            val existsDocMap = exists.associateBy { it.docHash }

            for (request in requests) {
                val exist = existsDocMap[request.docHash]
                val document =
                    supportService.createDocument(context, request = request, exist = exist, requestType = requestType)
                val existDocItems = existsDocItemsMap[document.id] ?: listOf()

                // 기존 전표와 신규 전표의 companyCode가 다르면 오류
                require(exist == null || exist.companyCode == document.companyCode) { "companyCode is not matched, docId:${document.id}, companyCode:${document.companyCode}, existCompanyCode:${exist?.companyCode}" }

                // 기존 전표의 docItems와 요청 docItems의 size가 다르면 오류
                require((exist == null && existDocItems.isEmpty()) || existDocItems.size == request.docItems.size) {
                    "docItems size is not matched, docId:${document.id}, docHash:${document.docHash}, docItems:${request.docItems.size}, existDocItems:${existDocItems.size}"
                }

                val documentOrigin = request.docOrigin?.toEntity(document.id)
                val items = supportService.createDocumentItems(
                    context,
                    requestType,
                    request.docOrigin?.docTemplateCode,
                    document,
                    request.docItems,
                    existDocItems
                )
                val attributes =
                    supportService.createDocumentItemAttributes(context, document.id, items, request.docItems)

                documents.add(document)
                documentItems.addAll(items)
                if (documentOrigin != null) {
                    documentOrigins.add(documentOrigin)
                }
                items.forEach { item ->
                    val itemAttributes = attributes.filter { it.attributeId.docItemId == item.id }
                    documentItemAttributeMap[item.id] = itemAttributes
                }
            }

            val documentItemAttributes = documentItemAttributeMap.map { it.value }.flatten()
            if (context.isSave) {
                logger.info { "createDocuments, save, requests: ${requests.size}, context:$context" }
//            persistence.saveAll(context, documents, documentItems, documentOrigins, documentItemAttributeMap.map { it.value }.flatten())
                persistence.saveAllWithRetry(
                    context,
                    documents,
                    documentItems,
                    documentOrigins,
                    documentItemAttributeMap.map { it.value }.flatten()
                )
            }
            val results = supportService.createDocumentResults(
                context,
                documents,
                documentOrigins,
                documentItems,
                documentItemAttributes
            )
            val existResults = supportService.createDocumentResults(
                context,
                exists,
                documentOrigins,
                existsDocItemsMap.values.flatten(),
                listOf()
            )
            val filledDocumentResults = supportService.fillDetails(
                context,
                results,
                documentItems,
                documentItemAttributeMap,
                listOf(),
                listOf()
            )

            when (requestType) {
                POSTING -> {
                    logger.info("publishEvent for request type: ${requestType}, results:${results.size}, existsResults:${existResults.size} ")
                    publishEvent(context, results, existResults)
                }
                else -> {

                }
            }


            filledDocumentResults
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    /**
     * Draft -> Normal, Draft, Submit(Review) 케이스
     * 수동전표만 가능함
     */
    fun updateDraftDocuments (context: DocumentServiceContext, requestType: RequestType, requests: List<UpdateDraftDocumentRequest>): List<DocumentResult> {
        return retryTemplate.execute(RetryCallback { retryContext ->
            logger.info { "update draft documents, requests: ${requests.size}, context:$context, requestType:$requestType"}
            val allowedRequestTypes = listOf(RequestType.DRAFT, RequestType.SUBMIT, RequestType.POSTING)
            require(allowedRequestTypes.contains(requestType) ) {
                "request type not allowed by updateDraftDocuments:requests: ${requests.size}, only allowed by ${allowedRequestTypes}"
            }
            validateFiscalClosing(context, requests)

            require(requests.isNotEmpty()) { "update draft documents, context:$context, requests must not be empty" }
            if ( requests.size > Constants.DOCUMENT_BATCH_SIZE ) {
                logger.warn("Too many requests found by updateDraftDocuments, requests:${requests.size}, context:${context} skipped!!")
                if ( context.enableBatchLimit ) {
                    throw IllegalArgumentException("Too many requests found by updateDraftDocuments, requests:${requests.size}, context:${context}, skipped!!")
                }
            }

            val documents:MutableList<Document> = mutableListOf()
            val documentOrigins:MutableList<DocumentOrigin> = mutableListOf()
            val documentItems:MutableList<DocumentItem> = mutableListOf()
            val documentItemAttributeMap:MutableMap<String, List<DocumentItemAttribute>> = mutableMapOf()

            // validation
            UpdateDraftDocumentValidationRule.validate(context, requests)
            DocumentItemValidationRule.validate(context, requests.flatMap { it.docItems })

            // 기존 전표 확인
            val exists = persistence.findDocuments(requests.map { it.docId })
            require(exists.all { it.docStatus == DocumentStatus.DRAFT }) { "all document's status must be draft status, docIds:${requests.map { it.docId }}" }

            // 전표항목은 기존 전표항목을 사용하지 않음
            val existsDocMap = exists.associateBy { it.id }

            for (request in requests) {
                val exist = existsDocMap[request.docId]
                val document = supportService.updateDraftDocument(context, requestType = requestType, request = request, exist = exist!! )

                // 기존 전표와 신규 전표의 companyCode가 다르면 오류
                require (exist.companyCode == document.companyCode) { "companyCode is not matched, docId:${document.id}, companyCode:${document.companyCode}, existCompanyCode:${exist?.companyCode}" }

                val documentOrigin = request.docOrigin?.toEntity(document.id)
                val items = supportService.createDocumentItems(context, requestType, request.docOrigin?.docTemplateCode, document, request.docItems, isUpdateDraft =  true)
                val attributes = supportService.createDocumentItemAttributes(context, document.id, items, request.docItems)

                documents.add(document)
                documentItems.addAll(items)
                if ( documentOrigin != null ) {
                    documentOrigins.add(documentOrigin)
                }
                items.forEach { item ->
                    val itemAttributes = attributes.filter { it.attributeId.docItemId == item.id }
                    documentItemAttributeMap[item.id] = itemAttributes
                }
            }

            val documentItemAttributes = documentItemAttributeMap.map { it.value }.flatten()
            if(context.isSave) {
                logger.info { "updateDraftDocuments documents, save, requests: ${requests.size}, context:$context" }
                persistence.saveAllWithRetry(context, documents, documentItems, documentOrigins, documentItemAttributes, isUpdateDraft =  true)
            }
            val results = supportService.createDocumentResults(context, documents, documentOrigins, documentItems, listOf())
            val filledDocumentResults = supportService.fillDetails(context, results, documentItems, documentItemAttributeMap, listOf(), listOf())

            when (requestType) {
                POSTING -> {
                    logger.info("publishEvent for request type: ${requestType}, size:${results.size} ")
                    publishEvent(context, results)
                }
                else -> {

                }
            }

            filledDocumentResults
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed, e:${retryContext.lastThrowable.message}", retryContext.lastThrowable)
            throw retryContext.lastThrowable
        })
    }

    /**
     * Review -> Normal, Draft
     */
    override fun updateReviewStatus(context: DocumentServiceContext, docIds: List<String>, requestType: RequestType): List<DocumentResult> {
        return retryTemplate.execute(RetryCallback { retryContext ->
            logger.info { "update review documents to requestType, docIds: ${docIds.size}, context:$context, requestType:$requestType" }

            val allowedRequestTypes = listOf(RequestType.DRAFT, RequestType.POSTING)
            require(allowedRequestTypes.contains(requestType) ) {
                "request type not allowed by requestType: ${requestType}, only allowed by ${allowedRequestTypes}"
            }

            require(docIds.isNotEmpty()) { "update draft documents, context:$context, docIds must not be empty" }
            if ( docIds.size > Constants.DOCUMENT_BATCH_SIZE ) {
                logger.warn("Too many requests found by updateDraftDocuments, docIds:${docIds.size}, context:${context} skipped!!")
                if ( context.enableBatchLimit ) {
                    throw IllegalArgumentException("Too many requests found by updateDraftDocuments, docIds:${docIds.size}, context:${context}, skipped!!")
                }
            }

            // 기존 전표 확인
            val exists = persistence.findDocuments(docIds)
            require(exists.all { it.docStatus == DocumentStatus.REVIEW }) { "all document's status must be review status, docIds:${docIds.size}}" }
            validateFiscalClosingByDocuments(context, exists)

            val claimStatus = requestType.decisionDocStatus()
            val documents = exists.map { document ->  document.copy(claimStatus = claimStatus) }

            val existsDocItems = if (exists.isEmpty()) listOf() else persistence.findDocumentItemsByDocIdIn(exists.map { it.id })


            if(context.isSave) {
                logger.info { "updateReviewStatus documents, save, docIds: ${docIds.size}, context:$context" }
                persistence.saveAllWithRetry(context, documents, listOf(), listOf(), listOf())
            }
            val results = supportService.createDocumentResults(context, documents, listOf(), existsDocItems, listOf())
            val filledDocumentResults = supportService.fillDetails(context, results, existsDocItems, emptyMap(), listOf(), listOf())

            when (requestType) {
                POSTING -> {
                    logger.info("publishEvent is allowed request type: ${requestType}, size:${results.size} ")
                    publishEvent(context, results)
                }
                else -> {

                }
            }

            filledDocumentResults
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed, e:${retryContext.lastThrowable.message}", retryContext.lastThrowable)
            throw retryContext.lastThrowable
        })
    }


    override fun clearing(context: DocumentServiceContext, requests: List<ClearingDocumentRequest>): List<DocumentResult> {
        return retryTemplate.execute(RetryCallback { retryContext ->
            logger.info { "clearing documents, requests: ${requests.size}, context:$context" }
            validateFiscalClosing(context, requests)


            require(requests.isNotEmpty()) { "clearing documents, context:$context, requests must not be empty" }
            if ( requests.size > Constants.DOCUMENT_BATCH_SIZE ) {
                logger.warn("Too many requests found by clearing, requests:${requests.size}, context:${context}")
                if ( context.enableBatchLimit ) {
                    throw IllegalArgumentException("Too many requests found by clearing, requests:${requests.size}, context:${context}")
                }
            }


            val requestType = CLEARING

            // 참조 전표 ID 리스트업
            val refDocItemIds = requests.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistence.findDocumentItems(refDocItemIds)

            val refDocIds = refDocItems.map { it.docId }.distinct()
            val refDocs = persistence.findDocuments(refDocIds)

            val allDocuments = mutableListOf<Document>()
            val allDocOrigins = mutableListOf<DocumentOrigin>()
            val allDocItems = mutableListOf<DocumentItem>()
            val allModifiedDocItems = mutableListOf<DocumentItem>()
            val allModifiedCurRefDocItems = mutableListOf<DocumentItem>()
            val allDocItemAttributeMap = mutableMapOf<String, List<DocumentItemAttribute>>()
            val allDocRelations = mutableListOf<DocumentRelation>()
            val allDocItemRelations = mutableListOf<DocumentItemRelation>()

            // TODO: validation
            ClearingDocumentValidationRule.validate(context, requests)
            DocumentItemValidationRule.validate(context, requests.flatMap { it.docItems })

            // 기존 전표 확인
            val docHashes = requests.mapNotNull { it.docHash }
            val exists = if (docHashes.isEmpty()) listOf() else persistence.findDocumentsByDocHashIn(docHashes)
            val existsDocItemsMap = if ( exists.isEmpty() ) mapOf() else persistence.findDocumentItemsByDocIdIn(exists.map { it.id }).groupBy { it.docId }
            val existsDocMap = exists.associateBy { it.docHash }


            for ( request in requests ) {

                // 참조 전표를 확인해서 반제전표 생성
                val curRefDocItems = refDocItems.filter { request.refDocItemIds.contains(it.id) }
                val curRefDocs = refDocs.filter { refDoc -> curRefDocItems.any { refDocItem -> refDocItem.docId == refDoc.id } }

                // 기존 부분반제 전표를 확인
                /**
                 * 전표 항목 ID  |   참조전표 항목 ID |  관계유형  | amount | refAmount
                 * -----------------------------------------
                 * 2           |  1             |  PARTIAL_CLEARING | 100  | 60   -> 잔액이 40 남음
                 * 3           |  1             |  CLEARING         | 100  | 40   -> 잔액에 대해서 반제해서 완전 반제가 됨
                 *
                 * => 참조전표 항목 1번으로 조회, 2번을 통해서 부분반제가 되었음을 확인
                 */
                val pcRelations = persistence.findDocumentItemRelations(listOf(),
                                        curRefDocItems.map { it.id }, listOf(RelationType.PARTIAL_CLEARING))
                val pcDocItemIds = pcRelations.map { it.docItemId }
                val pcDocItems = persistence.findDocumentItems(pcDocItemIds)

                val reason = request.reason

                val exist =  existsDocMap[request.docHash]
                val existDocItems = if (exist != null) existsDocItemsMap[exist.id] ?: listOf() else listOf()

                require (exist == null || existDocItems.size == request.docItems.size) {
                    "docItems size is not matched, docId:${exist?.id},  docHash:${request.docHash}, docItems:${request.docItems.size}, existDocItems:${existDocItems.size}"
                }

                // 반제 전표 생성
                val document = supportService.clearingDocument(context, requestType, request, exist)
                val documentOrigin = request.docOrigin?.toEntity(document.id)

                // 반제 전표 항목 생성
                val candidateDocItems = supportService.createDocumentItems(context, requestType, request.docOrigin?.docTemplateCode, document, request.docItems, existDocItems)
                val docItemAttributes = supportService.createDocumentItemAttributes(context, document.id, candidateDocItems, request.docItems)

                // 참조 전표 항목과 반제 전표 항목간의 관계 생성 (부분잔제, 완전반재 확인)
                val relationType = documentRelationService.calculationClearingRelationType(context, curRefDocItems, candidateDocItems, pcDocItems)

                val docRelations = curRefDocs.map { refDoc ->
                    supportService.createDocumentRelation(context, document, refDoc, relationType, reason)
                }
                val docItemRelations = candidateDocItems
                    .filter { docItem -> curRefDocItems.any { it.accountCode == docItem.accountCode }  }
                    .map { docItem ->
                        val  relations = curRefDocItems.filter { it.accountCode == docItem.accountCode }.map {
                            supportService.createDocumentItemRelation(context, docItem, it, relationType, reason)
                        }
                        relations
                    }.flatten()


                // clearing, partial clearing 상태 변경 (조건이 만족한 전표에 대해서만 상태 변경)
                val modifiedDocItems = candidateDocItems.map { docItem ->
                    val exist = curRefDocItems.any{it.accountCode == docItem.accountCode }
                    val newDocItem = if (exist) {
                        val newStatus = relationType.docItemStatus()
                        docItem.copy(claimStatus = newStatus)
                    } else {
                        docItem
                    }
                    newDocItem
                }
                // 참조된 전표는 완전 반제갸 된 경우에 cleared 상태로 변경
                val modifiedCurRefDocItems = curRefDocItems.mapNotNull {
                    val newStatus = relationType.refDocItemStatus()
                    val newDocItem = if (newStatus != it.docItemStatus) {
                        it.copy(newStatus)
                    } else {
                        null
                    }
                    newDocItem
                }

                // 결과 생성

                allDocuments.add(document)
                if ( allDocOrigins != null ) {
                    allDocOrigins.add(documentOrigin!!)
                }
                allModifiedDocItems.addAll(modifiedDocItems)
                allModifiedCurRefDocItems.addAll(modifiedCurRefDocItems)
                allDocItems.addAll(modifiedDocItems)
                allDocItems.addAll(modifiedCurRefDocItems)

                modifiedDocItems.forEach { item ->
                    val itemAttributes = docItemAttributes.filter { it.attributeId.docItemId == item.id }
                    allDocItemAttributeMap[item.id] = itemAttributes
                }
                allDocRelations.addAll(docRelations)
                allDocItemRelations.addAll(docItemRelations)
            }

            val allDocItemAttributes = allDocItemAttributeMap.map { it.value }.flatten()

            // TODO: 성능 최적화 필요
            if(context.isSave){
                logger.info { "clearing documents, save, requests: ${requests.size}, context:$context" }

                persistence.saveAllWithRetry(context, allDocuments, allDocItems, allDocOrigins, allDocItemAttributes, allDocRelations, allDocItemRelations)
            }

            val documentResults = supportService.createDocumentResults(context, allDocuments, allDocOrigins, allModifiedDocItems, allDocItemAttributes)
            val existResults = supportService.createDocumentResults(context, exists, allDocOrigins, existsDocItemsMap.values.flatten(), listOf())

            publishEvent(context, documentResults, existResults)

            val allDocumentResults = supportService.createDocumentResults(context, allDocuments, allDocOrigins, allModifiedDocItems + allModifiedCurRefDocItems, allDocItemAttributes)
            val filledDocumentResults = supportService.fillDetails(context, allDocumentResults,
                allDocItems, allDocItemAttributeMap, allDocRelations, allDocItemRelations)

            filledDocumentResults
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }


    /**
     * 역분개 전표 생성
     * TODO: 전표상태(DocumentStatus), 전표항목상태(DocumentItemStatus) 변경을 추가해야 함
     */
    override fun reversing(context: DocumentServiceContext, requests: List<ReversingDocumentRequest>): List<DocumentResult> {
        return retryTemplate.execute(RetryCallback { retryContext ->

            logger.info { "reversing documents, requests: ${requests.size}, context:$context" }

            validateFiscalClosing(context, requests)

            val relationType = RelationType.REVERSING
            if ( requests.size > Constants.DOCUMENT_BATCH_SIZE ) {
                logger.warn("Too many requests found by reversing, requests:${requests.size}, context:${context}")
                if ( context.enableBatchLimit ) {
                    throw IllegalArgumentException("Too many requests found by reversing, requests:${requests.size}, context:${context}")
                }
            }

            // 역분개할 참조 전표 ID 리스트업
            val refDocIds = requests.map { it.refDocId }
            val refDocs = persistence.findDocuments(refDocIds)
            val refAllDocItems = persistence.findDocumentItemsByDocIdIn(refDocIds)
            val refAllDocItemAttributes = persistence.findDocumentItemAttributesByDocItemIdIn(refAllDocItems.map { it.id })

            val allDocuments = mutableListOf<Document>()
            val allReversingDocuments = mutableListOf<Document>()
            val allReversedDocuments = mutableListOf<Document>()
            val allDocOrigins = mutableListOf<DocumentOrigin>()
            val allDocItems = mutableListOf<DocumentItem>()
            val allDocItemAttributeMap = mutableMapOf<String, List<DocumentItemAttribute>>()
            val allDocRelations = mutableListOf<DocumentRelation>()
            val allDocItemRelations = mutableListOf<DocumentItemRelation>()

            // validation
            ReversingDocumentValidationRule.validate(context, requests)

            for ( request in requests ) {

                // 참조 전표를 확인해서 이 참조전표의 역분개 전표를 생성
                val refDoc = refDocs.find { it.id == request.refDocId }
                    ?: throw DocumentException.DocumentNotFoundException(request.refDocId)
                val reason = request.reason

                val document = supportService.createReversingDocument(context, request)
                val docOrigin = request.docOrigin?.toEntity(document.id)
                val docRelation = supportService.createDocumentRelation(context, document, refDoc, relationType, reason)
                val docItems = supportService.createReversingDocItems(context, request, document)

                val refDocItems = refAllDocItems.filter { it.docId == refDoc.id }
                val documentItemRelations = docItems.mapNotNull { docItem ->
                    val refDocItem = refDocItems.find { it.lineNumber == docItem.lineNumber }?.let { it ->
                        supportService.createDocumentItemRelation(context, docItem, it, relationType, reason)
                    }
                    refDocItem
                }

                // 참조전표의 전표항목의 속성을 찾아서 신규 전표속성으로 복사
                val docItemAttributes = docItems.map { docItem ->
                    val refDocItemId = documentItemRelations.firstOrNull{ it.docItemId == docItem.id}?.refDocItemId

                    val attributes = refAllDocItemAttributes.filter{ it.attributeId.docItemId == refDocItemId }.map { it.copy() }
                    attributes
                }.flatten()

                // 원전표 상태변환 반영
                val reversedDocument = refDoc.copy(DocumentStatus.REVERSED)
                val reversedDocItems = refDocItems.map { it.copy(DocumentItemStatus.REVERSED) }


                allReversingDocuments.add(document)
                allReversedDocuments.add(reversedDocument)
                allDocuments.add(document)
                allDocuments.add(reversedDocument)

                if ( docOrigin != null ) {
                    allDocOrigins.add(docOrigin)
                }

                allDocItems.addAll(docItems)
                allDocItems.addAll(reversedDocItems)

                docItems.forEach { item ->
                    val itemAttributes = docItemAttributes.filter { it.attributeId.docItemId == item.id }
                    allDocItemAttributeMap[item.id] = itemAttributes
                }
                allDocRelations.add(docRelation)
                allDocItemRelations.addAll(documentItemRelations)
            }

            val allDocItemAttributes = allDocItemAttributeMap.map { it.value }.flatten()
            // TODO: 성능 최적화 필요
            if(context.isSave){
                logger.info { "reversing documents, save, requests: ${requests.size}, context:$context" }

                persistence.saveAllWithRetry(context, allDocuments, allDocItems, allDocOrigins, allDocItemAttributes, allDocRelations, allDocItemRelations)
            }

            val documentResults = supportService.createDocumentResults(context, allReversingDocuments, allDocOrigins, allDocItems, listOf())
            val existResults:List<DocumentResult> = listOf()
            val filledDocumentResults = supportService.fillDetails(context, documentResults, allDocItems, allDocItemAttributeMap, allDocRelations, allDocItemRelations)

            publishEvent(context, documentResults, existResults)

            filledDocumentResults
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    override fun lookupForClearing(context: DocumentServiceContext, companyCode: CompanyCode, startTime: OffsetDateTime, endTime: OffsetDateTime): List<DocumentResult>{
        require(startTime.isBefore(endTime)) { "startTime must be before endTime, startTime:${startTime}, endTime:${endTime}" }
        logger.info { "lookupForClearing, context:$context, companyCode:$companyCode, startTime:$startTime, endTime:$endTime" }

        val documents = persistence.lookupForClearing(companyCode, startTime, endTime)
        val documentIds = documents.map { it.id }
        val documentOrigins = persistence.findDocumentOriginsByDocIdIn(documentIds)
        val documentResults = supportService.createDocumentResults(context, documents, documentOrigins, listOf(), listOf())
        val filledDocumentResults = supportService.fillDetails(context, documentResults)

        return filledDocumentResults
    }

    // 반제를 위한 참조전표항목 조회
    override fun lookupRefDocItems(context: DocumentServiceContext, requests: List<LookupRefDocItemRequest>):List<RefDocItemResult> {
        logger.info { "lookupRefDocItems, context:$context, requests: $requests" }

        LookupRefDocItemValidationRule.validate(context, requests)
        val docItemDtos = persistence.lookupRefDocItems(requests)
        return docItemDtos
    }


    override fun findByDocId(context: DocumentServiceContext, documentId: String): DocumentResult {
        logger.info { "findDocumentById, context:$context, documentId: $documentId" }
        val results = findAllByDocIds(context, listOf(documentId))
        require(results.isNotEmpty()) { "documentId:$documentId is not found" }
        require(results.size == 1) { "documentId:$documentId is not unique" }

        return results.first()
    }


    override fun findAllByDocIds(context: DocumentServiceContext, documentIds: List<String>): List<DocumentResult> {
        logger.info { "findDocumentByIds, context:$context, documentIds: $documentIds" }
        val documents = persistence.findDocuments(documentIds)
        val documentOrigins = persistence.findDocumentOriginsByDocIdIn(documentIds)
        val documentResults = supportService.createDocumentResults(context, documents, documentOrigins, listOf(), listOf())

        val filledDocumentResults = supportService.fillDetails(context, documentResults)

        return filledDocumentResults
    }

    override fun findByDocHash(context: DocumentServiceContext, docHash: String): DocumentResult {
        logger.info { "findDocumentByDocHash, context:$context, docHash: $docHash" }

        val results = findAllByDocHashes(context, listOf(docHash))
        require(results.isNotEmpty()) { "documentResult by docHash:$docHash is not found" }
        require(results.size == 1) { "documentResult by docHash:$docHash is not unique" }

        return results.first()
    }

    override fun findAllByDocHashes(
        context: DocumentServiceContext,
        docHashes: List<String>
    ): List<DocumentResult> {
        logger.info { "findDocumentByDocHashes, context:$context, docHashes: $docHashes" }
        val documents = persistence.findDocumentsByDocHashIn(docHashes)
        val docOrigins = persistence.findDocumentOriginsByDocIdIn(documents.map { it.id })

        val documentResults = supportService.createDocumentResults(context, documents, docOrigins, listOf(), listOf())
        val filledDocumentResults = supportService.fillDetails(context, documentResults)
        return filledDocumentResults
    }

    /**
     * 전표 승인이 필요한지 검토
     */
    fun validateRequireApproval(context: DocumentServiceContext, requests:List<HashableDocumentRequest>) {
        val evaluateResults = approvalRuleService.evaluate(requests)
        val requireApprovals = evaluateResults.filter { it.result }
        if (requireApprovals.isNotEmpty()) {
            val json = requireApprovals.firstNotNullOfOrNull { it.approvalRule?.toJsonMessage() }
            val message = "requires to approval by rule:$json"
            throw IllegalStateException(message)
        }
    }

    /**
     * 회계 마감여부 검증
     */
    fun validateFiscalClosing(context: DocumentServiceContext, requests:List<DocumentRequest>) {
        requests.forEach { request ->
            fiscalClosingService.validateFiscalClosing(request.companyCode, request.postingDate)
        }
    }

    fun validateFiscalClosingByDocuments(context: DocumentServiceContext, documents:List<Document>) {
        documents.forEach { document ->
            fiscalClosingService.validateFiscalClosing(document.companyCode, document.postingDate)
        }
    }

    fun publishEvent (context:DocumentServiceContext, documentResults:List<DocumentResult>, existResults: List<DocumentResult> = listOf()) {
        if ( !context.isSave ) {
            logger.info("publishEvent ignored by save option is disabled, documentResults:${documentResults.size}, exists:${existResults.size}")
            return
        }

        val balanceRequests = documentResults.map { result ->
            val oldResult = existResults.find { it.docHash == result.docHash }
            DocumentBalanceRequest(result, oldResult)
        }

        logger.info { "publishEvent enable:${accountBalanceConfig.enable}, context:$context, documentResults: ${documentResults.size}, existResults: ${existResults.size}" }
        if ( accountBalanceConfig.enable ) {
            if ( existResults.isEmpty() ) {
                applicationEventPublisher.publishEvent(AccountBalanceEvent.DocumentCreated(context, balanceRequests))
            } else {
                applicationEventPublisher.publishEvent(AccountBalanceEvent.DocumentModified(context, balanceRequests))
            }
        }

    }

}