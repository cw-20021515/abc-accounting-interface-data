package com.abc.us.accounting.documents.model

import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.generated.models.AccountingAttributeType
import com.abc.us.generated.models.AccountingLedgerState
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


/**
 * 수정 필요
 */
data class SearchLedgerFilters(
    val pageable: SearchPageRequest = SearchPageRequest(0, 10),
    val current: Int = 1,
    val size: Int = 30,
    val dateType: DocumentDateType = DocumentDateType.POSTING_DATE,
    val fromDate: LocalDate = LocalDate.of(2000, 1, 1),
    val toDate: LocalDate = LocalDate.now(),
    val accountGroup: String? = null,
    val accountCodeFrom: String? = null,
    val accountCodeTo: String? = null,
    val companyCode: CompanyCode? = null,
    val costCenter: String? = null,
    val orderId: String? = null,
    val customerId: String? = null,
    val materialId: String? = null,
    val serialNumber: String? = null,
    val vendorId: String? = null,
    val payoutId: String? = null,
    val purchaseOrderId: String? = null,    // PO(발주번호)
    val accountingAttributeType: AccountingAttributeType = AccountingAttributeType.ALL,
    val attributeType: DocumentAttributeType? = null,
    val attributeTypeValue: String? = null,
    val accountingLedgerState: AccountingLedgerState = AccountingLedgerState.ALL,

    val direction: Sort.Direction = Sort.Direction.DESC,
)

data class LedgerResult(
    // DocumentItem 테이블 컬럼 관련
    val docItemId: String,
    val docId: String,
    val lineNumber: Int,
    val docItemStatus: DocumentItemStatus,
    val status:OpenItemStatus? = null,
    val companyCode: CompanyCode = CompanyCode.N200,
    val accountCode: String,
    val accountSide: AccountSide,
    val txCurrency: String? = null,
    val txAmount: BigDecimal? = null,
    val currency: String? = null,
    val amount: BigDecimal? = null,
    val exchangeRateId: String? = null,
    val text: String,
    val docTemplateCode: DocumentTemplateCode? = null,

    val costCenter: String,
    val profitCenter: String? = null,
    val segment:String? = null,
    val project: String? = null,

    val docType: DocumentType? = null,
    val docHash: String? = null,
    val documentDate: LocalDate? = null,
    val postingDate: LocalDate? = null,
    val entryDate: LocalDate? = null,
    val docStatus: DocumentStatus? = null,
    val workflowStatus: WorkflowStatus? = null,
    val workflowId: String? = null,

    // documentItemAttribute 관련
    val customerId: String? = null,     // 고객ID
    val orderId: String? = null,        // 주문ID
    val orderItemId: String? = null,    // 주문아이템ID
    val contractId: String? = null,     // 계약ID
    val serialNumber: String? = null,   // 시리얼번호

    val salesType: String? = null,      // 판매유형 (일시불,운용리스,금융리스,멤버쉽,고객서비스
    val salesItem: String? = null,      // 판매항목
    val rentalCode: String? = null,     // 렌탈코드
    val channelId: String? = null,      // 채널ID
    val referralCode: String? = null,   // 레퍼럴코드

    val vendorId: String? = null,       // 거래처ID
    val payoutId: String? = null,       // 지급ID
    val invoiceId: String? = null,      // 인보이스
    val purchaseOrderId: String? = null,    //Perchase Order Id
    val materialId: String? = null,     // 자재ID

    val materialType: String? = null,   // 자재유형
    val materialCategory: String? = null,   // 자재분류 코드 (정수기 제품, 정수기 필터, 정수기 부품, ...)
    val installType: String? = null,    // 자재 설치유형 속성코드 (카운터탑(데스크탑) 유형, 빌트인(언더싱크 유형), 스탠드 유형)
    val filterType: String? = null,     // 자재 필터 유형 속성 코드 (역삼투압(RO) 유형, 나노트랩(NT) 유형)
    val featureType: String? = null,    // 자재 주요 기능 속성 코드 (냉정, 냉온정, 냉정+얼음, 온정, 정수)

    val createTime: OffsetDateTime,
    val createdBy: String,
    val updateTime: OffsetDateTime,
    val updatedBy: String,

    val document: DocumentResult? = null,
    val itemAttributes: MutableList<DocumentItemAttributeResult> = mutableListOf()
) {
    fun toAccountKey(): AccountKey {
        return AccountKey.of(companyCode, accountCode)
    }

    fun copy():LedgerResult {
        return LedgerResult(
            docItemId = docItemId,
            docId = docId,
            lineNumber = lineNumber,
            docItemStatus = docItemStatus,
            status = status,
            accountCode = accountCode,
            accountSide = accountSide,
            txCurrency = txCurrency,
            txAmount = txAmount,
            currency = currency,
            amount = amount,
            exchangeRateId = exchangeRateId,
            text = text,
            docTemplateCode = docTemplateCode,

            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,

            docType = docType,
            docHash = docHash,

            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,

            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = companyCode,

            // documentItemAttribute 관련
            customerId = customerId,
            orderId = orderId,
            orderItemId = orderItemId,
            contractId = contractId,
            serialNumber = serialNumber,

            salesType = salesType,
            salesItem = salesItem,
            rentalCode = rentalCode,
            channelId = channelId,
            referralCode = referralCode,

            vendorId = vendorId,
            payoutId = payoutId,
            invoiceId = invoiceId,
            purchaseOrderId = purchaseOrderId,
            materialId = materialId,

            materialType = materialType,
            materialCategory = materialCategory,
            installType = installType,
            filterType = filterType,
            featureType = featureType,

            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
            document = document,
            itemAttributes = itemAttributes.map { it.copy() }.toMutableList()
        )
    }

    companion object {
        fun toResult(
            param: DocumentItem
        ): LedgerResult {
            return LedgerResult(
                docItemId = param.id,
                docId = param.docId,
                lineNumber = param.lineNumber,
                docItemStatus = param.docItemStatus,
                //status = param.status,
                accountCode = param.accountCode,
                accountSide = param.accountSide,
                txCurrency = param.txMoney.currency.toString(),
                txAmount = param.txMoney.amount,
                currency = param.money.currency.toString(),
                amount = param.money.amount,
                exchangeRateId = param.exchangeRateId,
                text = param.text,
                docTemplateCode = param.docTemplateCode,

                costCenter = param.costCenter,
                profitCenter = param.profitCenter,
                segment = param.segment,
                project = param.project,

                customerId = param.customerId,
                vendorId = param.vendorId,

                createTime = param.createTime,
                createdBy = param.createdBy,
                updateTime = param.updateTime,
                updatedBy = param.updatedBy
            )
        }
    }
}

data class LedgerOutputResult(
    val documentStatus: AccountingLedgerState,
    val documentId: String,
    val documentTypeCode: String,
    val documentType: DocumentType,

    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val entryDate: LocalDate,

    val documentItemId: String,     // 전표항목 ID
    val companyCode: CompanyCode,   // 회사코드

    val accountCode: String,            // 계정코드
    val accountName: String? = null,    // 계정명
    val remark: String? = null,     // 적요

    val currency: String,           // 통화
    val debitAmount: BigDecimal?,   // 차변금액
    val creditAmount: BigDecimal?,  // 대변금액
    val balance: BigDecimal?,       // 잔액

    val documentTemplateCode: String? = null,       //템플릿코드

    val costCenter: String,             // 코스트센터
    val profitCenter: String? = null,   // 손익센터
    val segment: String? = null,        // 세그먼트
    val project: String? = null,        // 프로젝트

    // documentItemAttribute 관련
    val customerId: String? = null,     // 고객ID
    val orderId: String? = null,        // 주문ID
    val orderItemId: String? = null,    // 주문아이템ID
    val contractId: String? = null,     // 계약ID
    val serialNumber: String? = null,   // 시리얼번호

    val salesType: String? = null,      // 판매유형 (일시불,운용리스,금융리스,멤버쉽,고객서비스
    val salesItem: String? = null,      // 판매항목
    val rentalCode: String? = null,     // 렌탈코드
    val commitmentDuration: String? = null, //약정기간 - 추가 25.01.15
    val channelId: String? = null,      // 채널ID
    val channelName: String? = null,    // 채널명 - 추가 25.01.15
    val channelType: String? = null,    // 채널타입 - 추가 25.01.15
    val channelDetail: String? = null,  // 채널디테일 - 추가 25.01.15
    val referralCode: String? = null,   // 레퍼럴코드

    val branchId: String? = null,       // 브랜치ID - 추가 25.01.15
    val vendorId: String? = null,       // 거래처ID
    val payoutId: String? = null,       // 지급ID
    val invoiceId: String? = null,      // 인보이스
    val purchaseOrderId: String? = null,    //Perchase Order Id
    val materialId: String? = null,     // 자재ID

    val materialType: String? = null,   // 자재유형
    val materialCategory: String? = null,   // 자재분류 코드 (정수기 제품, 정수기 필터, 정수기 부품, ...)
    val installType: String? = null,    // 자재 설치유형 속성코드 (카운터탑(데스크탑) 유형, 빌트인(언더싱크 유형), 스탠드 유형)
    val filterType: String? = null,     // 자재 필터 유형 속성 코드 (역삼투압(RO) 유형, 나노트랩(NT) 유형)
    val featureType: String? = null,    // 자재 주요 기능 속성 코드 (냉정, 냉온정, 냉정+얼음, 온정, 정수)

    val searchTime: String? = null,
    val syncTime: String? = null
)

data class LedgerDefaultResult(
    // 원장 기본 조회시 결과 값으로 사용하기 위한 data class, 우선 정렬을 전기일 desc로 사용, 추후 정렬기준 추가가 필요하면 속성 추가 필요
    val docItemId: String? = null,          // 전표아이템ID
    val docId: String? = null,              // 전표ID
    val postingDate: LocalDate? = null,     // 전기일
)