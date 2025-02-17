package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.repository.*
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.IdGenerator
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class DocumentSupportService (
    private val companyService: CompanyService,
    private val accountService: AccountServiceable,
    private val documentIdGenerator: DocumentIdGenerator,
    private val exchangeRateService: ExchangeRateService,
    private val persistenceService: DocumentPersistenceService,
){
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }


    fun createDocuments(context: DocumentServiceContext, requestType: RequestType, requests: List<CreateDocumentRequest>): List<Document> {
        return requests.map { request ->
            createDocument(context, requestType, request)
        }
    }

    /**
     * 전표 생성
     * @param context DocumentServiceContext
     * @param request CreateDocumentRequest
     * @param requestType RequestType
     * @return Document
     */
    fun createDocument(context: DocumentServiceContext, requestType: RequestType, request: CreateDocumentRequest,
                       exist:Document? = null): Document {

        // documentId가 없는 경우 생성
        val docId = exist?.id ?: documentIdGenerator.generateDocumentId(request.docType, LocalDate.now())
        logger.debug("generate new docId: $docId by docType: ${request.docType}")
        val docType = request.docType

        // default docHash is generated from the request itself
        val docHash = calculateDocHash(context, request)

        val documentDate = request.documentDate
        val postingDate = request.postingDate
        val entryDate = LocalDate.now()

        // 초기 docStatus 결정
        val docStatus = requestType.decisionDocStatus()
        val workflowStatus = requestType.decisionWorkflowStatus()
        val workflowId: String? = null

        val companyCode = request.companyCode
        val txCurrency = request.txCurrency

        val company = companyService.getCompany(companyCode)
        val currency = company.currency
        val fiscalYearMonth = company.fiscalRule.from(postingDate)

        val totalTxAmount = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
        val txMoney = Money.of(totalTxAmount, txCurrency)

        // cache로 환율 계산
        val money = exchangeRateService.convertCurrency(txMoney, currency.name).toMoney

        val reference = request.reference
        val text = request.text

        val createTime = exist?.createTime ?: request.createTime
        val createdBy = exist?.createdBy ?: request.createdBy
        val updateTime = OffsetDateTime.now()
        val updatedBy = request.createdBy

        // version 설정
        val version = exist?.version ?: Constants.DEFAULT_VERSION

        val document = Document(
            _id = docId,
            version = version,
            docType = docType,
            docHash = docHash,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,

            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYearMonth = fiscalYearMonth,

            companyCode = companyCode,
            txMoney = txMoney,
            money = money,

            reference = reference,
            text = text,
            isDeleted = false,

            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        ).also {
            if ( exist != null ){
                it.markNotNew()
            }
        }
        return document
    }

    fun updateDraftDocument(context: DocumentServiceContext, requestType: RequestType, request: UpdateDraftDocumentRequest,
                            exist:Document): Document {

        require(request.docId == exist.id) { "docId must be same, but request:${request.docId}, exist:${exist.id}" }
        require(exist.docStatus == DocumentStatus.DRAFT) { "DocumentStatus must be DRAFT, but ${exist.docStatus}" }

        // documentId는 기존 것 사용
        val docId = exist.id
        val docType = request.docType

        // default docHash is generated from the request itself
        val docHash = calculateDocHash(context, request)

        val documentDate = request.documentDate
        val postingDate = request.postingDate
        val entryDate = LocalDate.now()

        // 초기 docStatus 결정
        val docStatus = when (requestType) {
            RequestType.POSTING -> {
                exist.docStatus.transit(DocumentStatus.NORMAL)
            }
            RequestType.DRAFT -> {
                exist.docStatus.transit(DocumentStatus.DRAFT)
            }
            RequestType.SUBMIT -> {
                exist.docStatus.transit(DocumentStatus.REVIEW)
            }
            else -> {
                throw IllegalStateException("Invalid RequestType: $requestType")
            }
        }

        val workflowStatus = requestType.decisionWorkflowStatus()
        val workflowId: String? = null


        val companyCode = request.companyCode
        val txCurrency = request.txCurrency

        val company = companyService.getCompany(companyCode)
        val currency = company.currency
        val fiscalYearMonth = company.fiscalRule.from(postingDate)

        val totalTxAmount = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
        val txMoney = Money.of(totalTxAmount, txCurrency)

        // cache로 환율 계산
        val money = exchangeRateService.convertCurrency(txMoney, currency.name).toMoney

        val reference = request.reference
        val text = request.text

        val createTime = exist.createTime
        val createdBy = exist.createdBy
        val updateTime = OffsetDateTime.now()
        val updatedBy = request.createdBy

        val document = Document(
            _id = docId,
            version = exist.version,
            docType = docType,
            docHash = docHash,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,

            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYearMonth = fiscalYearMonth,

            companyCode = companyCode,
            txMoney = txMoney,
            money = money,

            reference = reference,
            text = text,
            isDeleted = false,

            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        ).also {
            it.markNotNew()
        }
        return document
    }


    fun createDocumentItems(
        context: DocumentServiceContext,
        requestType: RequestType = RequestType.POSTING,
        docTemplateCode:DocumentTemplateCode? = null,
        document: Document,
        inputs: List<DocumentItemRequest>,
        exists: List<DocumentItem> = listOf(),
        isUpdateDraft:Boolean =  false
    ): List<DocumentItem> {
        val docId = document.id
        val companyCode = document.companyCode
        val totalAmount = document.money.amount
        val company = companyService.getCompany(companyCode)
        val currency = company.currency
        val fiscalYearMonth = company.fiscalRule.from(document.postingDate)


        val exchangeRate = exchangeRateService.getExchangeRate(currency.name, currency.name, LocalDate.now())

        // 미결항목 확인
        val docItemStatus = DocumentItemStatus.verify(DocumentItemStatus.NORMAL)
        var sumOfDebitAmount = BigDecimal.ZERO.setScale(Constants.ACCOUNTING_SCALE)
        var sumOfCreditAmount = BigDecimal.ZERO.setScale(Constants.ACCOUNTING_SCALE)

        val debitCount = inputs.count { it.accountSide == AccountSide.DEBIT }
        val creditCount = inputs.count { it.accountSide == AccountSide.CREDIT }

        var curDebitCount:Int = 0
        var curCreditCount:Int = 0

        return inputs.mapIndexed { index, input ->
            val lineNumber = index + 1
            val txMoney = Money.of(input.txAmount, input.txCurrency)

            var money:Money? = null
            if (input.accountSide == AccountSide.DEBIT) {
                curDebitCount++
                if ( curDebitCount < debitCount )   {
                    sumOfDebitAmount = sumOfDebitAmount.add(txMoney.amount)
                    money = exchangeRate.toConversionResult(money = txMoney).toMoney
                } else if (curDebitCount == debitCount) {
                    val remain = totalAmount.subtract(sumOfDebitAmount).toScale(Constants.ACCOUNTING_SCALE)
                    money = Money.of(remain, currency.name)
                }
            } else {
                curCreditCount++
                if ( curCreditCount < creditCount )   {
                    sumOfCreditAmount = sumOfCreditAmount.add(txMoney.amount)
                    money = exchangeRate.toConversionResult(money = txMoney).toMoney
                } else if (curCreditCount == creditCount) {
                    val remain = totalAmount.subtract(sumOfCreditAmount).toScale(Constants.ACCOUNTING_SCALE)
                    money = Money.of(remain, currency.name)
                }
            }
            val docItemId = DocumentItem.getDocumentItemId(docId, lineNumber)
            val exchangeRateId = exchangeRate.id

            require(money != null) { "money must not be null, docItemId:$docItemId, lineNumber:$lineNumber, curDebitCount:$curDebitCount, curCreditCount:$curCreditCount" }

            val exist = exists.firstOrNull { it.lineNumber == lineNumber }
            // version 설정
            val version = if( isUpdateDraft ) {
                // updateDraft상태에서는 삭제후 insert이기 때문에 version을 올려서 넣어야 함
                document.version + 1
            } else {
                exist?.version ?: Constants.DEFAULT_VERSION
            }
            val docItem = DocumentItem(
                _id = docItemId,
                version = version,
                docId = docId,
                lineNumber = lineNumber,
                docItemStatus = docItemStatus,
                accountCode = input.accountCode,
                accountSide = input.accountSide,
                companyCode = companyCode,
                txMoney = txMoney,
                money = money,
                exchangeRateId = exchangeRateId,
                text = input.text,
                docTemplateCode = exist?.docTemplateCode ?: docTemplateCode,
                costCenter= input.costCenter,
                profitCenter= input.profitCenter,
                segment= input.segment,
                project= input.project,
                customerId= input.customerId,
                vendorId= input.vendorId,
                createTime = exist?.createTime ?: OffsetDateTime.now(),
                createdBy = exist?.createdBy ?: document.createdBy,
                updateTime = OffsetDateTime.now(),
                updatedBy = document.updatedBy,
                _isNew = exist == null
            )
            docItem
        }
    }


    fun createDocumentItemAttributes(
        context: DocumentServiceContext,
        docId: String,
        docItems: List<DocumentItem>,
        requests: List<DocumentItemRequest>
    ): List<DocumentItemAttribute> {
        return requests.mapIndexed() {index, request ->
            val docItem = docItems.firstOrNull{ it.lineNumber == index+1 }
            if (docItem == null) {
                logger.error("DocumentItem not found: $docId, ${index+1}")
                throw DocumentException.DocumentItemNotFoundException(docId, "lineNumber: ${index+1} not found")
            }
            createDocumentItemAttributes(context, docId, docItem.id, request.attributes)
        }.flatten()
    }

    fun createDocumentItemAttributes(
        context: DocumentServiceContext,
        docId: String,
        docItemId: String,
        list: List<DocumentItemAttributeRequest>
    ): List<DocumentItemAttribute> {
        return list.map { it ->
            val documentItemAttributeId = DocumentItemAttributeId(docItemId, it.attributeType)
            DocumentItemAttribute(
                attributeId = documentItemAttributeId,
                value = it.attributeValue,
            )
        }
    }

    fun createDocumentResults(context: DocumentServiceContext,
                              documents: List<Document>,
                              docOrigins: List<DocumentOrigin>,
                              docItems: List<DocumentItem>,
                              docItemAttributes: List<DocumentItemAttribute>): List<DocumentResult> {
        return documents.map { document ->
            val docOrigin = docOrigins.firstOrNull{ it.docId == document.id }
            val items = docItems.filter { it.docId == document.id }
            val itemAttributes = docItemAttributes.filter { it.attributeId.docItemId == document.id }

            val fiscalYearMonth = companyService.getCompany(document.companyCode).fiscalRule.from(document.postingDate)
            val result = createDocumentResult(context, document, docOrigin, items, itemAttributes, fiscalYearMonth)
            result
        }
    }

    fun createDocumentResult(context: DocumentServiceContext,
                             document: Document,
                             documentOrigin: DocumentOrigin? = null,
                             docItems:List<DocumentItem> = listOf(),
                             docItemAttributes: List<DocumentItemAttribute> = listOf(),
                             fiscalYearMonth: FiscalYearMonth): DocumentResult {

        val docItemResults = createDocumentItemResult(context, docItems, docItemAttributes)

        val documentResult = DocumentResult(
            docId = document.id,
            docType = document.docType,
            docHash = document.docHash,
            documentDate = document.documentDate,
            postingDate = document.postingDate,
            entryDate = document.entryDate,

            fiscalYear = fiscalYearMonth.year,
            fiscalMonth = fiscalYearMonth.month,

            docStatus = document.docStatus,
            workflowStatus = document.workflowStatus,
            workflowId = document.workflowId,
            companyCode = document.companyCode,

            txCurrency = document.txMoney.currencyCode(),
            txAmount = document.txMoney.amount,
            currency = document.money.currencyCode(),
            amount = document.money.amount,

            text = document.text,
            docOrigin = documentOrigin?.toResult(),
            createTime = document.createTime,
            createdBy = document.createdBy,
            updateTime = document.updateTime,
            updatedBy = document.updatedBy,
            docItems = docItemResults.toMutableList()
        )

        return documentResult
    }


    /**
     * DocumentItem 생성 (DocumentItemAttributes 포함)
     */
    fun createDocumentItemResult(context: DocumentServiceContext,
                                 docItems:List<DocumentItem>,
                                 docItemAttributes: List<DocumentItemAttribute> = listOf()): List<DocumentItemResult> {

        val docItemResults = docItems.map { docItem ->
            val isOpenItemMgmt = accountService.getAccount(docItem.toAccountKey()).isOpenItemMgmt
            val result = docItem.toResult(isOpenItemMgmt)

            if ( docItemAttributes.isNotEmpty() ) {
                val attributeResults = createDocumentItemAttributeResults(context, docItemAttributes.filter { it.attributeId.docItemId == docItem.id } )
                result.attributes.addAll(attributeResults)
            }
            result
        }
        return docItemResults
    }

    fun createDocumentItemAttributeResults(context: DocumentServiceContext,
                                           docItemAttributes: List<DocumentItemAttribute>): List<DocumentItemAttributeResult> {
        return docItemAttributes.map { it -> it.toResult() }
    }


    fun createReversingDocument(context: DocumentServiceContext, request: ReversingDocumentRequest): Document {
        val refDocument = persistenceService.findDocument(request.refDocId)
            ?: throw DocumentException.DocumentNotFoundException(request.refDocId)

        val requestType = RequestType.REVERSING
        val docType = DocumentType.JOURNAL_ENTRY
        val docId = documentIdGenerator.generateDocumentId(docType, LocalDate.now())
        val docHash = Hashs.hash(request)

        val documentDate = request.documentDate
        val postingDate = request.postingDate

        val docStatus = requestType.decisionDocStatus()
        val workflowStatus = requestType.decisionWorkflowStatus()
        val workflowId: String? = null
        val createTime = request.createTime
        val createdBy = request.createdBy

        val fiscalYearMonth = companyService.getCompany(request.companyCode).fiscalRule.from(postingDate)

        val document = refDocument.reverse(
            docId = docId,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            createTime = createTime,
            createdBy = createdBy,
            fiscalYearMonth = fiscalYearMonth
        )

        return document
    }

    fun createReversingDocItems (context: DocumentServiceContext, request: ReversingDocumentRequest, document: Document): List<DocumentItem> {

        val refDocId = request.refDocId
        val docId = document.id

        val refDocument = persistenceService.findDocument(refDocId)
            ?: throw DocumentException.DocumentNotFoundException(refDocId)
        val refDocItems = persistenceService.findDocumentItemsByDocIdIn(listOf(refDocId))

        val docItems = refDocItems.map { refDocItem -> refDocItem.reversal(docId, request.createTime, request.createdBy) }
        return docItems
    }

    fun createDocumentRelation(context: DocumentServiceContext,
                               document:Document, refDocument:Document,
                               relationType: RelationType, reason:String?=null): DocumentRelation {
        val refDocId = refDocument.id
        val docId = document.id

        return DocumentRelation(
            id = IdGenerator.generateId(),
            docId = docId,
            refDocId = refDocId,
            relationType = relationType,
            reason = reason,
        )
    }

    fun createDocumentItemRelations(context: DocumentServiceContext,
                                    docItems:List<DocumentItem>, refDocItems: List<DocumentItem>,
                                    relationType: RelationType, reason:String?=null): List<DocumentItemRelation> {
        val relations = docItems.map { docItem ->
            val refDocItem = refDocItems.first { it.lineNumber == docItem.lineNumber }
            createDocumentItemRelation(context, docItem, refDocItem, relationType, reason)
        }
        return relations
    }

    fun createDocumentItemRelation(context: DocumentServiceContext,
                                   docItem: DocumentItem, refDocItem:DocumentItem,
                                   relationType: RelationType, reason: String? = null): DocumentItemRelation {
        val relation = DocumentItemRelation(
                id = IdGenerator.generateId(),
                docItemId = docItem.id,
                refDocItemId = refDocItem.id,
                relationType = relationType,
                reason = reason,
                amount = docItem.money.amount,
                refAmount = refDocItem.money.amount,
            )
        return relation
    }


    /**
     * 전표 해시값 계산
     * docHash가 이미 존재하는 경우에는 해당 값을 그대로 반환
     * docHash가 없는 경우 기본 규칙으로 생성
     *
     * @param requestType 요청 타입
     * @param request 요청
     */
    fun calculateDocHash(
        context: DocumentServiceContext,
        request: HashableDocumentRequest
    ): String {
        if (request.docHash != null) return request.docHash!!
        return Hashs.hash(
            request.docType,
            request.documentDate.toString(),
            request.postingDate.toString(),
            request.companyCode,
            request.txCurrency,
            request.text,
            request.docOrigin?.docTemplateCode,
            request.docOrigin?.bizSystem?.name,
            request.docOrigin?.bizTxId,
            request.docOrigin?.bizEvent?.name,
            request.docOrigin?.accountingEvent,
        )
    }

    fun clearingDocument(context: DocumentServiceContext, requestType: RequestType, request: ClearingDocumentRequest, exist:Document? = null): Document {
        require(requestType== RequestType.CLEARING) { "RequestType must be CLEARING" }


        val docType = request.docType
        val companyCode = request.companyCode
        val docId = exist?.id ?: documentIdGenerator.generateDocumentId(docType, LocalDate.now())

        //  추후 검토 필요
//        val docHash = calculateDocHash(context, request)
        val docHash = request.docHash ?: Hashs.hash(request)

        val documentDate = request.documentDate
        val postingDate = request.postingDate
        val entryDate   = LocalDate.now()

        val docStatus = requestType.decisionDocStatus()
        val workflowStatus = requestType.decisionWorkflowStatus()
        val workflowId: String? = null

        val totalTxAmount = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
        val txMoney = Money.of(totalTxAmount, request.txCurrency)

        val company = companyService.getCompany(companyCode)
        val currency = company.currency
        val fiscalYearMonth = company.fiscalRule.from(postingDate)

        val money = exchangeRateService.convertCurrency(txMoney, currency.name).toMoney

        val createTime = exist?.createTime ?: request.createTime
        val createdBy = exist?.createdBy ?: request.createdBy
        val updateTime = OffsetDateTime.now()
        val updatedBy = request.createdBy
        val version = exist?.version ?: Constants.DEFAULT_VERSION

        val document = Document(
            _id = docId,
            version = version,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYearMonth = fiscalYearMonth,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = request.companyCode,
            txMoney = txMoney,
            money = money,
            reference = request.reference,
            text = request.text,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        ).also {
            if ( exist != null ) {
                it.markNotNew()
            }
        }

        return document
    }


    fun fillDetails(context: DocumentServiceContext, docResults: List<DocumentResult>):List<DocumentResult> {
        if ( !context.containDocItems  && !context.containRelations ) {
            return docResults
        }

        val docIds = docResults.map { it.docId }
        val docItems:MutableList<DocumentItem> = mutableListOf()
        val docItemAttributeMap:MutableMap<String, List<DocumentItemAttribute>> = mutableMapOf()

        if ( context.containDocItems ) {
            val curDocItems = persistenceService.findDocumentItemsByDocIdIn(docIds)
            val curDocItemAttributes = persistenceService.findDocumentItemAttributesByDocItemIdIn(curDocItems.map { it.id })

            docItems.addAll(curDocItems)
            curDocItems.forEach { docItem ->
                val docItemAttributes = curDocItemAttributes.filter { it.attributeId.docItemId == docItem.id }
                docItemAttributeMap[docItem.id] = docItemAttributes
            }
        }

        val docRelations = if (context.containRelations) {
            persistenceService.findDocumentRelations(docIds)
        } else {
            listOf()
        }
        val docItemRelations = if (context.containRelations) {
            persistenceService.findDocumentItemRelations(docItems.map { it.id }, docItems.map { it.id }, RelationType.entries)
        } else {
            listOf()
        }

        return fillDetails(context, docResults, docItems, docItemAttributeMap, docRelations, docItemRelations)
    }


    fun fillDetails(context:DocumentServiceContext, documentResults: List<DocumentResult>,
                    docItems: List<DocumentItem>, docItemAttributeMap:Map<String, List<DocumentItemAttribute>>,
                    docRelations:List<DocumentRelation>, docItemRelations:List<DocumentItemRelation>):List<DocumentResult> {
        if ( !context.containDocItems  && !context.containRelations ) {
            return documentResults
        }

        val newDocumentResults:MutableList<DocumentResult> = mutableListOf()

        for ( originDocumentResult in documentResults ) {
            val newDocumentResult = originDocumentResult.copyWithoutFill()

            val docId = originDocumentResult.docId
            val docItemResults = if (context.containDocItems) {
                val items = docItems.filter { it.docId == docId }
                val docItemIds = items.map { it.id }
                // 성능문제는 수정 필요
                val attributes = docItemIds.map { docItemId ->  docItemAttributeMap[docItemId] ?: listOf() }.flatten()
                createDocumentItemResult(context, items, attributes)
            } else {
                listOf()
            }
            newDocumentResult.docItems.addAll(docItemResults)

            if (context.containRelations) {
                val items = docItems.filter { it.docId == docId }
                val docItemIds = items.map { it.id }

                val docRelationResults = docRelations.filter { it.docId == docId }.map { it.toResult() }
                val docItemRelationResults = docItemRelations.filter { it.docItemId in docItemIds }.map { it.toResult() }
                newDocumentResult.docRelations.addAll(docRelationResults)
                newDocumentResult.docItemRelations.addAll(docItemRelationResults)
            }

            newDocumentResults.add(newDocumentResult)
        }

        return newDocumentResults
    }
}