package com.abc.us.accounting.documents.model

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.abc.us.generated.models.*
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


enum class RequestType{
    DRAFT, SUBMIT, POSTING, REVERSING, CLEARING
    ;

    /**
     * 전표 상태 결정
     * TODO: 추가 구현 필요 (아직은 임시)
     * @param requestType 요청 타입
     */
    fun decisionDocStatus(): DocumentStatus {
        return when (this) {
            DRAFT -> DocumentStatus.verify(DocumentStatus.DRAFT)
            SUBMIT -> DocumentStatus.verify(DocumentStatus.REVIEW)
            POSTING -> DocumentStatus.verify(DocumentStatus.NORMAL)
            REVERSING -> DocumentStatus.verify(DocumentStatus.REVERSAL)
            CLEARING -> DocumentStatus.verify(DocumentStatus.NORMAL)
        }
    }

    fun decisionWorkflowStatus(): WorkflowStatus {
        return when (this) {
            DRAFT -> WorkflowStatus.INITIAL
            SUBMIT -> WorkflowStatus.SUBMITTED
            POSTING -> WorkflowStatus.INITIAL
            REVERSING -> WorkflowStatus.INITIAL
            CLEARING -> WorkflowStatus.INITIAL
        }
    }
}


data class FilteringRule(
    val companyCodes: List<CompanyCode> = listOf(),
    val docTemplateCodes: List<DocumentTemplateCode> = listOf(),
    val orderIds:List<String> = listOf(),
    val orderItemIds:List<String> = listOf(),
    val customerIds:List<String> = listOf(),
    val materialIds:List<String> = listOf(),
    val serviceFlowIds:List<String> = listOf(),
    val contractIds:List<String> = listOf(),
    val bisSystems:List<BizSystemType> = listOf(),
    val bizTxIds:List<String> = listOf(),
    val bizProcesses: List<BizProcessType> = listOf(),
    val bizEvents: List<BizEventType> = listOf(),
    val accountingEvents: List<String> = listOf(),
)

data class DocumentServiceContext (
    val containDocItems: Boolean = false,
    val containRelations: Boolean = false,

    val enableBatchLimit:Boolean = false,
    val maxResult:Int = Int.MAX_VALUE,
    val isSave:Boolean = false,
    val debug: Boolean = false,
    val filteringRule:FilteringRule? = null,
) {
    override fun toString() = toStringByReflection()

    fun withEnableBatchLimit():DocumentServiceContext {
        return DocumentServiceContext(
            containDocItems = containDocItems,
            containRelations = containRelations,
            isSave = isSave,
            debug = debug,
            enableBatchLimit = true,
        )
    }

    companion object {
        val DEFAULT = DocumentServiceContext(
            containDocItems = false,
            containRelations = false,
            isSave = true,
            enableBatchLimit = true,
            debug = false
        )

        val DEFAULT_DETAIL = DocumentServiceContext(
            containDocItems = true,
            containRelations = true,
            isSave = true,
            enableBatchLimit = true,
            debug = false
        )


        val ONLY_DEBUG = DocumentServiceContext(
            containDocItems = true,
            containRelations = true,
            isSave = false,
            enableBatchLimit = false,
            debug = true
        )

        val SAVE_DEBUG = DocumentServiceContext(
            containDocItems = true,
            containRelations = true,
            isSave = true,
            enableBatchLimit = false,
            debug = true
        )

        fun withSaveDebug(maxResult:Int = Int.MAX_VALUE) = DocumentServiceContext(
            containDocItems = true,
            containRelations = true,
            isSave = true,
            maxResult = maxResult,
            enableBatchLimit = false,
            debug = true
        )

        fun withOrderIds(orderIds:List<String>): DocumentServiceContext {
            val filteringRule = FilteringRule(
                orderIds = orderIds
            )
            return DocumentServiceContext(
                containDocItems = true,
                containRelations = true,
                isSave = true,
                enableBatchLimit = false,
                debug = true,
                filteringRule = filteringRule
            )
        }

        fun withOrderItemIds(orderItemIds:List<String>): DocumentServiceContext {
            val filteringRule = FilteringRule(
                orderItemIds = orderItemIds
            )
            return DocumentServiceContext(
                containDocItems = true,
                containRelations = true,
                isSave = true,
                enableBatchLimit = false,
                debug = true,
                filteringRule = filteringRule
            )
        }

        fun withCustomerIds(customerIds:List<String>): DocumentServiceContext {
            val filteringRule = FilteringRule(
                customerIds = customerIds
            )
            return DocumentServiceContext(
                containDocItems = true,
                containRelations = true,
                isSave = true,
                enableBatchLimit = false,
                debug = true,
                filteringRule = filteringRule
            )
        }


        fun withMaterialIds(materialIds: List<String>): DocumentServiceContext {
            val filteringRule = FilteringRule(
                materialIds = materialIds
            )
            return DocumentServiceContext(
                containDocItems = true,
                containRelations = true,
                isSave = true,
                enableBatchLimit = false,
                debug = true,
                filteringRule = filteringRule
            )
        }

        fun withFilteringRule(filteringRule: FilteringRule?): DocumentServiceContext {
            return DocumentServiceContext(
                containDocItems = true,
                containRelations = true,
                isSave = true,
                enableBatchLimit = false,
                debug = true,
                filteringRule = filteringRule
            )
        }

    }
}

/**
 * filtering 조건으로 금액 계산
 */
fun calculateItemTxAmount(accountSide: AccountSide,
                          accountCode: String?=null,
                          items:MutableList<DocumentItemRequest>): BigDecimal {
    return items.filter { it.accountSide == accountSide }
        .filter { if ( accountCode != null ) it.accountCode == accountCode else true }
        .sumOf { it.txAmount }
}

abstract class DocumentRequest (
    open val documentDate: LocalDate,
    open val postingDate: LocalDate,
    open val companyCode: CompanyCode = CompanyCode.N200,
    open val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    open val createTime: OffsetDateTime = OffsetDateTime.now(),
    open val createdBy: String = Constants.APP_NAME,
)

abstract class HashableDocumentRequest (
    override val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    open val docHash: String? = null,
    open val txCurrency: String = CurrencyCode.USD.code,
    open val reference:String? = null,
    open val text:String? = null,
    open val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
    open val docItems:MutableList<DocumentItemRequest> = mutableListOf(),
): DocumentRequest(
    documentDate = LocalDate.now(),
    postingDate = LocalDate.now(),
    companyCode = CompanyCode.N200,
    docType = DocumentType.JOURNAL_ENTRY,
    createTime = OffsetDateTime.now(),
    createdBy = Constants.APP_NAME
)

/**
 * 전표 생성 요청 (일반 전표)
 */
data class CreateDocumentRequest(
    override val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    override val docHash: String? = null,

    override val documentDate: LocalDate,
    override val postingDate: LocalDate = documentDate,
    override val companyCode: CompanyCode = CompanyCode.N200,
    override val txCurrency: String = CurrencyCode.USD.code,
    override val reference:String? = null,
    override val text:String? = null,

    override val createTime: OffsetDateTime = OffsetDateTime.now(),
    override val createdBy: String = Constants.APP_NAME,

    override val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
    override val docItems:MutableList<DocumentItemRequest> = mutableListOf(),
    val notes:MutableList<DocumentNoteRequest> = mutableListOf(),
    val attachments:MutableList<DocumentAttachmentRequest> = mutableListOf(),
) : HashableDocumentRequest(
    docType = docType,
    docHash = docHash,
    txCurrency = txCurrency,
    reference = reference,
    text = text,
    docOrigin = docOrigin,
    docItems = docItems,
) {
}

// 기존에 draft 전표 상태인 경우
data class UpdateDraftDocumentRequest(
    val docId: String,
    override val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    override val docHash: String? = null,

    override val documentDate: LocalDate,
    override val postingDate: LocalDate = documentDate,
    override val companyCode: CompanyCode = CompanyCode.N200,
    override val txCurrency: String = CurrencyCode.USD.code,
    override val reference:String? = null,
    override val text:String? = null,

    override val createTime: OffsetDateTime = OffsetDateTime.now(),
    override val createdBy: String = Constants.APP_NAME,

    override val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
    override val docItems:MutableList<DocumentItemRequest> = mutableListOf(),
    val notes:MutableList<DocumentNoteRequest> = mutableListOf(),
    val attachments:MutableList<DocumentAttachmentRequest> = mutableListOf(),
) : HashableDocumentRequest(
    docType = docType,
    docHash = docHash,
    docOrigin = docOrigin
)
/**
 * 전표 반제(Clearing) 요청
 * 수동 반제 요청
 */
data class ClearingDocumentRequest(
    override val docType: DocumentType = DocumentType.JOURNAL_ENTRY, // 반제 전표 유형
    override val docHash: String? = null,

    override val postingDate: LocalDate,
    override val documentDate: LocalDate = postingDate,
    override val companyCode: CompanyCode = CompanyCode.N200,
    override val txCurrency: String,
    override val reference:String? = null,
    override val text:String? = null,
    override val createTime: OffsetDateTime = OffsetDateTime.now(),
    override val createdBy: String = Constants.APP_NAME,
    override val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
    override val docItems:MutableList<DocumentItemRequest> = mutableListOf(),

    val refDocItemIds: List<String>, // 반제할 전표 ID
    val reason: String? = null, // 반제 사유 -> 코드화는 추후 고려

) : HashableDocumentRequest(
    docType = docType,
    docHash = docHash,
    docOrigin = docOrigin
) {
    fun copy(newRefDocItemIds:List<String>):ClearingDocumentRequest {
        return ClearingDocumentRequest(
            docType = docType,
            docHash = docHash,
            postingDate = postingDate,
            documentDate = documentDate,
            companyCode = companyCode,
            txCurrency = txCurrency,
            reference = reference,
            text = text,
            createTime = createTime,
            createdBy = createdBy,
            docOrigin = docOrigin,
            docItems = docItems.map { it.copy() }.toMutableList(),
            refDocItemIds = newRefDocItemIds,
            reason = reason
        )
    }
}

/**
 * 전표 역분개(Reversing) 요청
 * 사전 조건
 * 1. 전표는 반드시 정상 상태여야 한다.
 * 2. 역분개된 전표는 역분개 할 수 없다.
 * 3. 반제전표를 역분개하면 open 상태로 바뀐다.
 */
data class ReversingDocumentRequest(
    val refDocId: String,
    val reason: String? = null,

    override val documentDate: LocalDate = LocalDate.now(),
    override val postingDate: LocalDate = documentDate,
    override val companyCode: CompanyCode = CompanyCode.N200,
    override val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    override val createTime: OffsetDateTime = OffsetDateTime.now(),
    override val createdBy: String = Constants.APP_NAME,
    val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
): DocumentRequest(
    documentDate = documentDate,
    postingDate = postingDate,
    companyCode = companyCode,
    docType = docType,
    createTime = createTime,
    createdBy = createdBy
){
    override fun toString() = toStringByReflection()
}



// TODO: 추후 nullable -> not null로 변경예정
data class DocumentOriginRequest (
    val docTemplateCode: DocumentTemplateCode,
    val bizSystem:BizSystemType = BizSystemType.ABC_ACCOUNTING,
    val bizTxId:String?= null,
    val bizProcess: BizProcessType?= null,
    val bizEvent: BizEventType?= null,
    val accountingEvent: String? = null
) {
    fun toEntity(docId: String): DocumentOrigin {
        return DocumentOrigin(
            docId = docId,
            docTemplateCode = docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizProcess = bizProcess,
            bizEvent = bizEvent,
            accountingEvent = accountingEvent
        )
    }

    override fun toString() = toStringByReflection()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentOriginRequest) return false

        return this.docTemplateCode == other.docTemplateCode &&
                this.bizSystem == other.bizSystem &&
                this.bizTxId == other.bizTxId &&
                this.bizProcess == other.bizProcess &&
                this.bizEvent == other.bizEvent &&
                this.accountingEvent == other.accountingEvent
    }

    // enum은 hashCode 값이 계속 변함
    override fun hashCode(): Int {
        var result = docTemplateCode.name.hashCode()
        result = 31 * result + bizSystem.name.hashCode()
        result = 31 * result + (bizTxId?.hashCode() ?: 0)
        result = 31 * result + (bizProcess?.name.hashCode() ?: 0)
        result = 31 * result + (bizEvent?.name.hashCode() ?: 0)
        result = 31 * result + (accountingEvent?.hashCode() ?: 0)
        return result
    }
}

data class DocumentItemRequest(
    val companyCode: CompanyCode = CompanyCode.N200,
    val accountCode:String,
    val accountSide: AccountSide,
    val txCurrency: String = CurrencyCode.USD.code,
    val txAmount:BigDecimal,
    val text:String,
    val costCenter: String,
    val profitCenter: String? = null,
    val segment:String? = null,
    val project: String? = null,
    val customerId: String? = null,
    val vendorId: String? = null,
    val attributes: MutableList<DocumentItemAttributeRequest> = mutableListOf(),
){
    fun toAccountKey():AccountKey {
        return AccountKey.of(companyCode, accountCode)
    }
}


data class DocumentItemAttributeRequest(
    val attributeType: DocumentAttributeType,
    var attributeValue: String = ""
)

data class DocumentNoteRequest(
    val contents: String,
    val createdTime: OffsetDateTime = OffsetDateTime.now(),
    val createdBy: String?= null,
)

data class DocumentAttachmentRequest(
    val fileName: String,
    val internalPath: String,
    val createdTime: OffsetDateTime = OffsetDateTime.now(),
    val createdBy: String?= null
)


data class DocumentResult(
    val docId: String,
    val docType: DocumentType,
    val docHash: String,

    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val entryDate: LocalDate,

    val fiscalYear: Int,
    val fiscalMonth: Int,

    val docStatus: DocumentStatus,
    val workflowStatus: WorkflowStatus,
    val workflowId: String?=null,

    val companyCode: CompanyCode,
    val txCurrency: String,
    val txAmount: BigDecimal,

    val currency: String,
    val amount: BigDecimal,

    val reference:String? = null,
    val text: String? = null,

    val docOrigin: DocumentOriginResult? = null,

    val createTime: OffsetDateTime,
    val createdBy: String,
    val updateTime: OffsetDateTime,
    val updatedBy: String,

    val docItems: MutableList<DocumentItemResult> = mutableListOf(),
    val docRelations: MutableList<DocumentRelationResult> = mutableListOf(),
    val docItemRelations: MutableList<DocumentItemRelationResult> = mutableListOf()
) {
    fun copy():DocumentResult {
        return DocumentResult(
            docId = docId,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYear = fiscalYear,
            fiscalMonth = fiscalMonth,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = companyCode,
            txCurrency = txCurrency,
            txAmount = txAmount,
            currency = currency,
            amount = amount,
            reference = reference,
            text = text,
            docOrigin = docOrigin,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
            docItems = docItems.map { it.copy() }.toMutableList(),
            docRelations = docRelations.map { it.copy() }.toMutableList(),
            docItemRelations = docItemRelations.map { it.copy() }.toMutableList()
        )
    }
    fun copyWithoutFill():DocumentResult {
        return DocumentResult(
            docId = docId,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYear = fiscalYear,
            fiscalMonth = fiscalMonth,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = companyCode,
            txCurrency = txCurrency,
            txAmount = txAmount,
            currency = currency,
            amount = amount,
            reference = reference,
            text = text,
            docOrigin = docOrigin,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
        )
    }
}


data class DocumentItemResult (
    val docItemId: String,
    val docId: String,
    val lineNumber: Int,
    val docItemStatus: DocumentItemStatus,
    val status:OpenItemStatus,
    val companyCode: CompanyCode,
    val accountCode: String,
    val accountName: String? = null,
    val accountSide: AccountSide,
    val txCurrency: String,
    val txAmount: BigDecimal,
    val currency: String,
    val amount: BigDecimal,
    val exchangeRateId: String? = null,
    val text: String,
    val docTemplateCode: DocumentTemplateCode? = null,

    val costCenter: String,
    val profitCenter: String? = null,
    val segment:String? = null,
    val project: String? = null,

    val customerId: String? = null,
    val vendorId: String? = null,

    val createTime: OffsetDateTime,
    val createdBy: String,
    val updateTime: OffsetDateTime,
    val updatedBy: String,
    val attributes: MutableList<DocumentItemAttributeResult> = mutableListOf()
) {
    fun toAccountKey():AccountKey {
        return AccountKey.of(companyCode, accountCode)
    }

    fun toDocTemplateKey():DocumentTemplateKey?{
        if ( docTemplateCode == null ) return null

        return DocumentTemplateKey.of(companyCode, docTemplateCode)
    }

    fun toRequest():DocumentItemRequest {
        return DocumentItemRequest(
            companyCode = this.companyCode,
            accountCode = this.accountCode,
            accountSide = this.accountSide,
            txCurrency = this.txCurrency,
            txAmount = this.txAmount,
            text = this.text,
            costCenter = this.costCenter,
            profitCenter = this.profitCenter,
            segment = this.segment,
            project = this.project,
            customerId = this.customerId,
            vendorId = this.vendorId,
            attributes = this.attributes.map { it.toRequest() }.toMutableList()
        )
    }
}

/* FE Return을 위한 결과값 복사본 */
data class DocumentItemOutputResult (
    val documentItemId: String,
    val companyCode: CompanyCode? = null,
    val accountCode: String,
    val accountName: String? = null,

    val remark: String? = null,
    val currency: String,
    val debitAmount: BigDecimal,    //차변금액
    val creditAmount: BigDecimal,   //대변금액

    val documentStatus: DocumentStatus? =null,
    val refDocumentItemId: String? = null,

    val documentTemplateCode: String? = null,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val segment:String? = null,
    val project: String? = null,

    val customerId: String? = null,         // 고객 ID
    val orderId: String? = null,            // 주문번호
    val orderItemId: String? = null,        // 주문아이템 ID
    val contractId: String? = null,         // 계약 ID
    val serialNumber: String? = null,       // 시리얼번호
    val salesType: String? = null,          // 판매유형
    val salesItem: String? = null,          // 판매항목
    val rentalCode: String? = null,         // 렌탈코드
    val commitmentDuration: String? = null, //약정기간 - 추가 25.01.15
    val channelId: String? = null,          // 채널 ID
    val channelName: String? = null,    // 채널명 - 추가 25.01.15
    val channelType: String? = null,    // 채널타입 - 추가 25.01.15
    val channelDetail: String? = null,  // 채널디테일 - 추가 25.01.15
    val referralCode: String? = null,       // 레퍼럴코드
    val branchId: String? = null,       // 브랜치ID - 추가 25.01.15
    val vendorId: String? = null,           // 거래처 ID
    val payoutId: String? = null,           // 지급 ID
    val invoiceId: String? = null,          // 인보이스
    val purchaseOrderId: String? = null,    // PO
    val materialId: String? = null,         // 자재 ID
    val materialType: String? = null,       // 자재유형
    val materialCategory: String? = null,   // 자재분류코드
    val installType: String? = null,        // 자재 설치 유형 속성 코드
    val filterType: String? = null,         // 자재 필터 유형 속성 코드
    val featureType: String? = null,        // 자재 주요 기능 속성 코드

//    val createTime: OffsetDateTime,
//    val createdBy: String,
//    val updateTime: OffsetDateTime,
//    val updatedBy: String,
//    val attributes: MutableList<DocumentItemAttributeResult> = mutableListOf(),
) {
    companion object {
        fun toResult(
            param: DocumentItemResult,
            setCompanyCode: CompanyCode,
            setAccountName: String,
            setDocumentStatus: DocumentStatus,
            docAttributeList: List<DocumentItemAttribute>
        ): DocumentItemOutputResult {
            val attrMap: MutableMap<DocumentAttributeType, String> = mutableMapOf()
            docAttributeList.forEach {
                attrMap[it.attributeId.attributeType] = it.value
            }

            return DocumentItemOutputResult(
                documentItemId = param.docItemId,
                companyCode = setCompanyCode,
                accountCode = param.accountCode,
                accountName = setAccountName,
                remark = param.text,
                currency = param.currency,
                debitAmount = if(param.accountSide == AccountSide.DEBIT){ param.amount} else { BigDecimal(0) },
                creditAmount = if(param.accountSide == AccountSide.CREDIT){ param.amount} else { BigDecimal(0) },

                documentStatus = setDocumentStatus,
                //refDocumentItemId = param.docItemId,    // 임시
                costCenter = param.costCenter,
                profitCenter = param.profitCenter,
                segment = param.segment,
                project = param.project,

                customerId = param.customerId,
                //customerId = attrMap[DocumentAttributeType.CUSTOMER_ID],
                orderId = attrMap[DocumentAttributeType.ORDER_ID],
                orderItemId = attrMap[DocumentAttributeType.ORDER_ITEM_ID],
                contractId = attrMap[DocumentAttributeType.CONTRACT_ID],
                serialNumber = attrMap[DocumentAttributeType.SERIAL_NUMBER],

                salesType = attrMap[DocumentAttributeType.SALES_TYPE],
                salesItem = attrMap[DocumentAttributeType.SALES_ITEM],
                rentalCode = attrMap[DocumentAttributeType.RENTAL_CODE],
                channelId = attrMap[DocumentAttributeType.CHANNEL_ID],
                referralCode = attrMap[DocumentAttributeType.REFERRAL_CODE],

                vendorId = param.vendorId,
                //vendorId = attrMap[DocumentAttributeType.VENDOR_ID],
                payoutId = attrMap[DocumentAttributeType.PAYOUT_ID],
                invoiceId = attrMap[DocumentAttributeType.VENDOR_INVOICE_ID],
                purchaseOrderId = attrMap[DocumentAttributeType.PURCHASE_ORDER],
                materialId = attrMap[DocumentAttributeType.MATERIAL_ID],

                commitmentDuration = attrMap[DocumentAttributeType.COMMITMENT_DURATION],
                channelName = attrMap[DocumentAttributeType.CHANNEL_NAME],
                channelType = attrMap[DocumentAttributeType.CHANNEL_TYPE],
                channelDetail = attrMap[DocumentAttributeType.CHANNEL_DETAIL],
                branchId = attrMap[DocumentAttributeType.BRANCH_ID],

            )
        }
    }
}

data class DocumentOriginResult (
    val docId: String,
    val docTemplateCode: DocumentTemplateCode,
    val bizSystem: BizSystemType,
    val bizTxId: String?=null,
    val bizProcess: BizProcessType?=null,
    val bizEvent: BizEventType?=null,
    val accountingEvent: String?=null,
){
    fun toRequest():DocumentOriginRequest {
        return DocumentOriginRequest(
            docTemplateCode = docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizProcess = bizProcess,
            bizEvent = bizEvent,
            accountingEvent = accountingEvent
        )
    }
}

data class DocumentItemAttributeResult (
    val docItemId: String,
    val type: DocumentAttributeType,
    val value: String,
){
    fun toRequest():DocumentItemAttributeRequest {
        return DocumentItemAttributeRequest(
            attributeType = type,
            attributeValue = value
        )
    }
}

data class DocumentNoteResult (
    val docNoteId: String,
    val docId: String,
    val contents: String,
    val createTime: OffsetDateTime,
    val createdBy: String,
    val updateTime: OffsetDateTime,
    val updatedBy: String,
)

data class DocumentAttachmentResult (
    val docAttachmentId: String,
    val docId: String,
    val fileName: String,
    val internalPath: String,
    val createTime: OffsetDateTime,
    val createdBy: String,
    val updateTime: OffsetDateTime,
    val updatedBy: String
)

data class DocumentRelationResult (
    val docRelationId: String,
    val docId: String,
    val refDocId: String,
    val relationType: RelationType,
    val reason:String? = null,
    val createTime: OffsetDateTime
)

data class DocumentItemRelationResult (
    val docItemRelationId: String,
    val docItemId: String,
    val refDocItemId: String,
    val relationType: RelationType,
    val reason:String? = null,
    val amount: BigDecimal,
    val refAmount: BigDecimal,
    val createTime: OffsetDateTime
)


data class RefDocItemResult (
    val docItemId: String,
    val docId:String,
    val accountCode: String,
    val accountSide: AccountSide,
    val companyCode: CompanyCode,
    val docTemplateCode: DocumentTemplateCode? = null,
    val customerId: String? = null,
    val vendorId: String? = null,
    val orderItemId: String? = null
)


/**
 * 페이지 요청
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @param sortBy 정렬 기준
 * @param sortDirection 정렬 방향
 */
data class SearchPageRequest (
    val page:Int,
    val size:Int,
    val sortBy: Sort.By = Sort.By.CREATE_TIME,
    val sortDirection: Sort.Direction = Sort.Direction.DESC
){
    fun toPageable(): Pageable {
        return PageRequest.of(page, size, sortDirection.toSortDirection(), sortBy.value)
    }
}

data class SearchDocumentFilters(
    val pageable: SearchPageRequest = SearchPageRequest(0, 10),
    val current: Int = 1,
    val size: Int = 30,
    val direction: Sort.Direction = Sort.Direction.DESC,
    val dateType: DocumentDateType = DocumentDateType.POSTING_DATE,
    val fromDate: LocalDate = LocalDate.of(2000, 1, 1),
    val toDate: LocalDate = LocalDate.now(),
    val companyCode: CompanyCode? = null,
    val fiscalYear: Int? = null,
    val fiscalMonth:Int? = null,
    val docType: DocumentType? = null,
    val createdBy: String? = null,
    val docStatus: DocumentStatus? = null,
    val documentType: String? = null,   // 화면에서 받을 때 사용
    val createUser: String? = null,     // 화면에서 받을 때 사용
    val documentStatus: String? = null, // 화면에서 받을 때 사용
){
    override fun toString(): String {
        return "SearchDocumentRequest(pageable=$pageable, dateType=$dateType, from=$fromDate, to=$toDate, companyCode=$companyCode, fiscalYear=$fiscalYear, fiscalMonth=$fiscalMonth, docType=$docType, createdBy=$createdBy, docStatus=$docStatus)"
    }
}



data class LookupRefDocItemRequest(
    val docType: DocumentType = DocumentType.JOURNAL_ENTRY,
    val companyCode: CompanyCode = CompanyCode.N200,
    val docTemplateCode:DocumentTemplateCode,
    val accountCode:String,
    val accountSide: AccountSide,
    val customerId: String? = null,
    val vendorId: String? = null,
    val orderItemId: String? = null,
){
    fun toAccountKey():AccountKey {
        return AccountKey.of(companyCode, accountCode)
    }
    fun toDocTemplateKey(): DocumentTemplateKey {
        return DocumentTemplateKey.of(companyCode, docTemplateCode)
    }

}



// 전표 목록 화면용 output result
data class DocumentOutputResult(
    val documentId: String,
    val documentTypeCode: String,
    val documentType: DocumentType,
    val documentStatus: DocumentStatus,
    val approvalStatus: WorkflowStatus,
    val workflowId: String?=null,
    val companyCode: CompanyCode,

    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val entryDate: LocalDate,

    val currency: String,
    val amount: BigDecimal,

    val remark: String? = null,
    val reference:String? = null,

    val createId:String? = null,

    val referenceDocumentType:String? = null,
    val referenceDocumentId:String? = null,
    val bizTransactionType:String? = null,
    val bizTransactionId:String? = null,
    val reversalDocumentId:String? = null,
    val reversalReasonCode:String? = null,
    val reversalReason:String? = null,
    val searchTime:String? = null,
    val syncTime:String? = null,
    @JsonIgnore
    val createTime: OffsetDateTime,
)

// 전표상세 화면용 output result, 전표 수정 시 일단 그대로 사용
data class DocumentDetailOutputResult(
    val documentId: String,
    val documentType: DocumentType,
    val documentStatus: DocumentStatus,
    val approvalStatus: WorkflowStatus,
    val workflowId: String?=null,
    val companyCode: CompanyCode,
    val fiscalYear: Int? = null,
    val fiscalPeriod: Int? = null,

    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val entryDate: LocalDate,

    val createId:String? = null,

    val reference:String? = null,
    val description: String? = null,

    val referenceDocumentType:String? = null,
    val referenceDocumentId:String? = null,
    val bizTransactionTypeId:String? = null,
    val bizTransactionId:String? = null,
    val reversalDocumentId:String? = null,
    val reversalReasonCode:String? = null,
    val reversalReason:String? = null,

    val searchTime:String? = null,
    val syncTime:String? = null,
    @JsonIgnore
    val createTime: OffsetDateTime,

    val totalDebitAmount: BigDecimal? = null,
    val totalCreditAmount: BigDecimal? = null,

    val lineItems: List<DocumentItemOutputResult> = mutableListOf(),
    val relatedDocuments: MutableList<RelatedDocumentOutputResult> = mutableListOf(),
    val notes: MutableList<DocumentNoteOutputResult> = mutableListOf(),
    val attachments: MutableList<DocumentAttachmentOutputResult> = mutableListOf()
)

data class RelatedDocumentOutputResult (
    val documentId: String,
    val documentTypeCode: DocumentType,
    val documentType: DocumentType,
    val companyCode: DocumentItemStatus,
    val postingDate:LocalDate,
    val entryDate: LocalDate,
    val referenceDocumentType: DocumentType,
    val referenceDocumentId: String,
    val bizTransactionTypeId: String,
    val bizTransactionId: String
)

data class DocumentNoteOutputResult (
    val documentNoteId: String,
    val documentId: String,
    val notesContents: String,
    val noteRegisterTime: OffsetDateTime,
    val noteRegister: String,
) {
    companion object {
        fun toResult(
            param: DocumentNote
        ): DocumentNoteOutputResult {
            return DocumentNoteOutputResult(
                documentNoteId = param.id.toString(),
                documentId = param.docId,
                notesContents = param.contents,
                noteRegisterTime = param.createTime,
                noteRegister = param.createdBy
            )
        }
    }
}

data class DocumentAttachmentOutputResult (
    val documentAttachmentId: String,
    val documentId: String,
    val attachmentFileName: String,
    val attachmentRegisterTime: OffsetDateTime,
    val attachmentUpdateTime: OffsetDateTime,
    val attachmentRegister: String
) {
    companion object {
        fun from(
            param: DocumentAttachmentResult
        ): DocumentAttachmentOutputResult {
            return DocumentAttachmentOutputResult(
                documentAttachmentId = param.docAttachmentId,
                documentId = param.docId,
                attachmentFileName = param.fileName,
                attachmentRegisterTime = param.createTime,
                attachmentUpdateTime = param.updateTime,
                attachmentRegister = param.createdBy
            )
        }
    }
}


// 전표 등록 FE용 Document Request
// CreateDocumentInputRequest -> CreateDocumentRequest
data class CreateDocumentInputRequest(
    val documentDate: LocalDate = LocalDate.now(),
    val postingDate: LocalDate = documentDate,
    val entryDate: LocalDate = documentDate,
    val companyCode: CompanyCode = CompanyCode.N200,
    val documentTypeCode: AccountingDocumentType = AccountingDocumentType.JOURNAL_ENTRY,
    val currency: String = CurrencyCode.USD.code,
    val documentReferenceInfo: String? = null,  // 참조, reference
    val description: String? = null,            // 설명, text
    val createTime: OffsetDateTime = OffsetDateTime.now(),
    val createUser: String = Constants.APP_NAME,

    //val docOrigin: DocumentOriginRequest? = null,   // 보조 시스템에 의해서 만들어지는 전표인 경우
    val documentsCreateLineItems: MutableList<DocumentItemInputRequest> = mutableListOf(),      // docItems
    val documentsCreateNotesItems: MutableList<DocumentNoteInputRequest> = mutableListOf(),     // notes
    val documentsCreateAttachmentsItems: MutableList<DocumentAttachmentInputRequest> = mutableListOf(),     // attachments

    val saveType: String = "draft"  //  draft: 임시저장, posting: 전기(저장)
)

// 전표 등록 FE용 Document Item Request
data class DocumentItemInputRequest(
    val documentItemId: String,
    val companyCode: CompanyCode = CompanyCode.N200,
    val accountCode: String,
    //val accountSide: AccountSide,
    val currency: String = CurrencyCode.USD.code,
    val debitAmount: BigDecimal,
    val creditAmount: BigDecimal,
    val remark: String,   // 적요, text
    val costCenter: String,
    val profitCenter: String? = null,
    val segment: String? = null,
    val project: String? = null,

    val customerId: String? = null,
    val vendorId: String? = null,

//    val orderId: String? = null,
//    val orderItemId: String? = null,
//    val serialNumber: String? = null,
//    val salesType: AccountingSalesItem? = null,
//    val salesItemType: AccountingSalesItemType? = null,
//    val rentalCode: String? = null,
//    val channelId: String? = null,
//    val referralCode: String? = null,
//    val payoutId: String? = null,
//    val invoiceId: String? = null,
//    val purchaseOrderId: String? = null,
//    val materialId: String? = null,
//    val materialType: MaterialType? = null,
//    val materialCategory: MaterialCategoryCode? = null,
//    val installType: MaterialAttributeInstallationTypeCode? = null,
//    val filterType: MaterialAttributeFilterTypeCode? = null,
//    val featureType: MaterialAttributeKeyFeatureCode? = null,

    val attributes: MutableList<DocumentItemAttributeRequest> = mutableListOf()
)

// 전표 등록 FE용 Document Note Request
data class DocumentNoteInputRequest(
    val notesContents: String,       // contents
    val notesRegisterTime: OffsetDateTime = OffsetDateTime.now(),   // createdTime
    val notesRegister: String?= null        // createdBy
)

// 전표 등록 FE용 Document Attachment Request
data class DocumentAttachmentInputRequest(
    val attachmentFileName: String,           // fileName
    val attachmentRegisterTime: String,       // internalPath
    val attachmentUpdateTime: OffsetDateTime = OffsetDateTime.now(),     // createdTime
    val attachmentRegister: String?= null        // createdBy
)

// 전표 수정 FE용 Document Request
// 기존에 draft 전표 상태인 경우
// UpdateDraftDocumentInputRequest -> UpdateDraftDocumentRequest
data class UpdateDraftDocumentInputRequest(
    val documentId: String,      // docId
    val documentType: AccountingDocumentType = AccountingDocumentType.JOURNAL_ENTRY, //docType

    val documentDate: LocalDate = LocalDate.now(),
    val postingDate: LocalDate = documentDate,
    val companyCode: CompanyCode = CompanyCode.N200,
    val txCurrency: String = CurrencyCode.USD.code,
    val reference: String? = null,      // document table - reference column
    val description: String? = null,    // document table - text column

    val createId: String = Constants.APP_NAME,     // createdBy

    val lineItems:MutableList<DocumentItemOutputResult> = mutableListOf(),
    val notes:MutableList<DocumentNoteOutputResult> = mutableListOf(),
    val attachments:MutableList<DocumentAttachmentOutputResult> = mutableListOf(),
)

// 역분개 전표 FE용 Document Request
data class ReversingDocumentInputRequest(
    val postingDate: LocalDate = LocalDate.now(),
    val reason: AccountingReversalReasonType? = null,
    val creatId: String = Constants.APP_NAME,
    val documentList: MutableList<ReversingDocumentListInputRequest> = mutableListOf()
)

data class ReversingDocumentListInputRequest(
    val documentId: String,
)

data class CreateDocumentNoteRequest(
    val docId: String,
    val contents: String,
    val createdTime: OffsetDateTime = OffsetDateTime.now(),
    val createdBy: String?= null,
){
    fun toCreateEntity(
        param: CreateDocumentNoteRequest
    ): DocumentNote{
        return DocumentNote(
            id = -1,
            docId = param.docId,
            isDeleted = 'N',
            contents = param.contents,
            createTime = OffsetDateTime.now(),
            createdBy = param.createdBy!!,
            updateTime = OffsetDateTime.now(),
            updatedBy = param.createdBy!!
        )
    }
}

data class UpdateDocumentNoteRequest(
    val id: Long,
    val contents: String,
    val updateTime: OffsetDateTime = OffsetDateTime.now(),
    val updateBy: String?= null,
){
    fun toUpdateEntity(
        param: UpdateDocumentNoteRequest
    ): DocumentNote{
        return DocumentNote(
            id = param.id,
            docId = "",
            isDeleted = 'N',
            contents = param.contents,
            createTime = OffsetDateTime.now(),
            createdBy = param.updateBy!!,
            updateTime = OffsetDateTime.now(),
            updatedBy = param.updateBy!!
        )
    }
}