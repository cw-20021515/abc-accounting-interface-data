package com.abc.us.accounting.documents.service

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.repository.*
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.ClearingReason
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.OpenItemStatus
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.DocumentSaveService.Companion
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.abc.us.accounting.supports.mapper.MapperUtil
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SettlementService(
    private val persistenceService: DocumentPersistenceService,

    private val settlementRepository: SettlementRepository,
    private val documentServiceable: DocumentServiceable,
    private val accountService: AccountServiceable,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Transactional(readOnly = true)
    fun searchSettlement(context: DocumentServiceContext, request: SearchSettlementFilters): Page<SettlementOutputResult>? {
        logger.info { "searchSettlement, context:$context, request: ${request.toStringByReflection()}" }
        val current = if (request.current == 1) 0 else request.current
        val filters = SearchSettlementFilters(
            pageable = SearchPageRequest(current, request.size, sortDirection = request.direction, sortBy = Sort.By.POSTING_DATE),
            current = current,
            size = request.size,
            postingDateFrom = request.postingDateFrom,
            postingDateTo = request.postingDateTo,
            companyCode = request.companyCode,
            accountCode = request.accountCode.takeIf { !it.isNullOrEmpty() },
            costCenter = request.costCenter.takeIf { !it.isNullOrEmpty() },
            vendorId = request.vendorId.takeIf { !it.isNullOrEmpty() },
            customerId = request.vendorId.takeIf { !it.isNullOrEmpty() },
            sortBy = request.sortBy,
            direction = request.direction
        )
        val defaultResult = settlementRepository.searchSettlement(filters)
        val docItemIds = defaultResult.map { it.docItemId }.toList()
        val docIds = defaultResult.map { it.docId }.toList()
        val outputResult = searchAllDocItemsByDocItemIds(context, docItemIds as List<String>, docIds as List<String>)
        logger.info {"searchSettlement OutputResult : ${MapperUtil.logMapCheck(outputResult)} "}
        logger.info {"searchSettlement OutputResult.size : ${outputResult.size} "}

        return PageImpl(outputResult, defaultResult.pageable, defaultResult.totalElements)
    }

    fun searchAllDocItemsByDocItemIds(context: DocumentServiceContext, docItemIds: List<String>, docIds: List<String>): List<SettlementOutputResult> {
        logger.info { "searchAllDocItemsByDocItemIds, context:$context, documentItemIds: $docItemIds, documentIds: $docIds" }
        val docItems = persistenceService.findDocumentItems(docItemIds)
        val documents = persistenceService.findDocuments(docIds)

        return createSettlementResults(context, docItems, documents)
    }

    fun createSettlementResults(context: DocumentServiceContext,
                            docItems: List<DocumentItem>,
                            documents: List<Document>): List<SettlementOutputResult> {
        return docItems.map { docItem ->
            val document = documents.firstOrNull{ it.id == docItem.docId }
            val result = createSettlementResult(context, docItem, document)
            result
        }
    }

    fun createSettlementResult(context: DocumentServiceContext,
                           docItem: DocumentItem,
                           document: Document? = null): SettlementOutputResult {
        val settlementResult = SettlementOutputResult(
            documentItemId = docItem.id,
            documentTypeCode = document?.docType!!.code,
            accountCode = docItem.accountCode,
            documentDate = document.documentDate,
            postingDate = document.postingDate,
            accountName = accountService.getAccount(docItem.toAccountKey()).name,
            remark = docItem.text,
            customerId = docItem.customerId,
            vendorId = docItem.vendorId,
            currency = docItem.money.currency.toString(),
            documentAmount = docItem.money.amount,
            searchTime = null,
            syncTime = null,
        )
        return settlementResult
    }

    // 전기 - 반제 수동 등록
    fun createSettlementDocuments(context: DocumentServiceContext, request: ClearingDocumentInputRequest): List<DocumentResult> {
        logger.info { "searchSettlement, context:$context, filters: ${request.toStringByReflection()}" }

        val convertedRequest = convertToClearingDocumentRequest(request)
        val result = documentServiceable.clearing(DocumentServiceContext.SAVE_DEBUG, convertedRequest)

        return result
    }

    // 화면에서 받은 inputRequest 를 List<ClearingDocumentRequest> 로 변경
    fun convertToClearingDocumentRequest(
        inputRequest: ClearingDocumentInputRequest
    ): List<ClearingDocumentRequest> {
        val companyCode = CompanyCode.N200
        val postingDate = inputRequest.postingDate
        val offsetAccountCode1 = inputRequest.offsetAccountCode
        val offsetAccountCode2 = inputRequest.discountAccountCode.takeIf { !it.isNullOrEmpty() }
        val docItemIds = inputRequest.settlementItems.map { it.documentItemId }
        val docItemAttributes = persistenceService.findDocumentItemAttributesByDocItemIdIn(docItemIds)
        val docItems = persistenceService.findDocumentItems(docItemIds)
        val documentOrigins = persistenceService.findDocumentOriginsByDocIdIn(docItems.map { it.docId })
        return  inputRequest.settlementItems.map { settlementItem ->
            val docItem = docItems.filter { it.id == settlementItem.documentItemId }
            val docItemAttribute = docItemAttributes.filter { it.attributeId.docItemId == settlementItem.documentItemId }
            val documentOrigin = documentOrigins.first { it.docId == docItem.first().docId}
            val result =
                creatClearingDocumentRequestResult(companyCode, postingDate, offsetAccountCode1,
                    offsetAccountCode2, settlementItem, docItem, docItemAttribute, documentOrigin)

            result!!
        }
    }


    fun creatClearingDocumentRequestResult(
        companyCode : CompanyCode,
        postingDate : LocalDate,
        accountCode1 : String,
        accountCode2 : String? = null,
        settlementItem : ClearingDocumentItemInputRequest,
        docItem : List<DocumentItem>,
        docItemAttribute : List<DocumentItemAttribute>,
        documentOrigin : DocumentOrigin?
    ): ClearingDocumentRequest {
        val docItemResult = docItem.first()
        val docItemAttr = docItemAttribute.map { it->
            DocumentItemAttributeRequest(
                attributeType = it.attributeId.attributeType,
                attributeValue = it.value
            )
        }
        val docOriginRequest: DocumentOriginRequest? = documentOrigin?.let {
            DocumentOriginRequest(
                docTemplateCode = it.docTemplateCode,
                bizSystem = it.bizSystem,
                bizTxId = it.bizTxId,
                bizProcess = it.bizProcess,
                bizEvent = it.bizEvent,
                accountingEvent = it.accountingEvent
            )
        }
        val inputReason = settlementItem.clearingReason.toString()
        val reason = ClearingReason.getDescriptionEnByCode(inputReason)
        logger.info { "ClearingReason reason = ${reason}, inputReason = ${inputReason}" }
        // 1. 대상이 되는 차변 데이터
//        val docItemReq1 = DocumentItemRequest(
////            companyCode = companyCode,
//            accountCode = settlementItem.accountCode,
//            accountSide = AccountSide.CREDIT,
//            txCurrency = CurrencyCode.USD.code,
//            txAmount = settlementItem.openAmount,
//            text = settlementItem.clearingReason,
//            costCenter = docItemResult.costCenter,
//            customerId = docItemResult.customerId,
//            vendorId = docItemResult.vendorId,
//            attributes = docItemAttr.toMutableList()
//        )

        // 2.위 차변 데이터를 새로운 전표에 대변 데이터로 생성
        val docItemReq2 = DocumentItemRequest(
//            companyCode = companyCode,
            accountCode = settlementItem.accountCode,
            accountSide = AccountSide.DEBIT,
            txCurrency = CurrencyCode.USD.code,
            txAmount = settlementItem.openAmount,
            text = reason.toString(),
            costCenter = docItemResult.costCenter,
            customerId = docItemResult.customerId,
            vendorId = docItemResult.vendorId,
            attributes = docItemAttr.toMutableList()
        )

        // 3-1.위 대변 데이터에 대한 차변 데이터 생성
        val docItemReq3 = DocumentItemRequest(
//            companyCode = companyCode,
            accountCode = accountCode1,
            accountSide = AccountSide.CREDIT,
            txCurrency = CurrencyCode.USD.code,
            txAmount = settlementItem.allocatedAmount,
            text = reason.toString(),
            costCenter = docItemResult.costCenter,
            customerId = docItemResult.customerId,
            vendorId = docItemResult.vendorId,
            attributes = docItemAttr.toMutableList()
        )

        // 3-2.위 대변 데이터에 대한 차변 데이터 생성(accountCode2 값이 있을 경우)
        var docItemReq4:DocumentItemRequest? = null
        if(!accountCode2.isNullOrEmpty()){
            docItemReq4 = DocumentItemRequest(
//                companyCode = companyCode,
                accountCode = accountCode2,
                accountSide = AccountSide.CREDIT,
                txCurrency = CurrencyCode.USD.code,
                txAmount = settlementItem.allocatedAmount,
                text = reason.toString(),
                costCenter = docItemResult.costCenter,
                customerId = docItemResult.customerId,
                vendorId = docItemResult.vendorId,
                attributes = docItemAttr.toMutableList()
            )

        }

        val docItems = mutableListOf<DocumentItemRequest>().apply {
//            add(docItemReq1)
            add(docItemReq2)
            add(docItemReq3)
            docItemReq4?.let { add(it) } // null이 아닐 때만 추가
        }

        return ClearingDocumentRequest(
            companyCode = companyCode,
            postingDate = postingDate,
            documentDate = postingDate,
            txCurrency = CurrencyCode.USD.code,
            reference = reason,
            text = "",
            createdBy = Constants.APP_NAME,
            docOrigin = docOriginRequest,
            docItems = docItems,
            refDocItemIds = listOf(settlementItem.documentItemId),
            reason = reason.toString(),
        )
    }

    // 계정코드 조회
    fun searchAccountCode(request: SearchAccountFilters): Page<AccountCodeOutputResult> {        //
        logger.info { "searchOpenItemAccountCode, filters: ${request.toStringByReflection()}" }
        val accounts = settlementRepository.searchAccountCode(request)
        logger.info { "searchOpenItemAccountCode accounts : ${MapperUtil.logMapCheck(accounts)} "}
        val results = createAccountResults(accounts.toList())
        return PageImpl(results, accounts.pageable, accounts.totalElements)
    }

    // 미결항목 계정코드 조회
    fun searchOpenItemAccountCode(request: SearchAccountFilters): Page<AccountCodeOutputResult> {        //
        logger.info { "searchOpenItemAccountCode, filters: ${request.toStringByReflection()}" }
        val filters = request.copy(isOpenItemMgmt = OpenItemStatus.OPEN)        // 미결항목 관리 적용
        val accounts = settlementRepository.searchAccountCode(filters)
        logger.info { "searchOpenItemAccountCode accounts : ${MapperUtil.logMapCheck(accounts)} "}
        val results = createAccountResults(accounts.toList())
        return PageImpl(results, accounts.pageable, accounts.totalElements)
    }

    // account 테이블 정보를 AccountCodeResult로 변경
    fun createAccountResults(accounts: List<Account>): List<AccountCodeOutputResult> {
        return accounts.map { account ->
            AccountCodeOutputResult(
                companyCode = account.accountKey.companyCode,
                accountCode = account.accountKey.accountCode,
                accountName = account.name,
                description = account.description,
                //accountType = account.accountType.toString(),
                //accountClass = account.accountClass.toString(),
                //isActive = account.isActive.toString(),
                isOpenItem = account.isOpenItemMgmt
            )
        }
    }


}