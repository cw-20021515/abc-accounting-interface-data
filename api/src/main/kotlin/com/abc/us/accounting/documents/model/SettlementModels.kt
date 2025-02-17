package com.abc.us.accounting.documents.model

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.generated.models.AccountingClearingReasonType
import java.math.BigDecimal
import java.time.LocalDate


/**
 * 수정 필요
 */
data class SearchSettlementFilters(
    val pageable: SearchPageRequest = SearchPageRequest(0, 100),
    val current: Int = 1,
    val size: Int = 100,

    val postingDateFrom: LocalDate,
    val postingDateTo: LocalDate,
    val companyCode: CompanyCode = CompanyCode.N200,

    val accountCode: String? = null,    // 계정코드 7자리 체크 필요
    val costCenter: String? = null,     // 코스트센터
    val vendorId: String? = null,       // 거래처
    val customerId: String? = null,     // 고객

    val sortBy: Sort.By = Sort.By.POSTING_DATE,
    val direction: Sort.Direction = Sort.Direction.DESC,
)

data class SettlementDefaultResult(
    // 기본 조회시 결과 값으로 사용하기 위한 data class, 우선 정렬을 증빙일 desc로 사용, 추후 정렬기준 추가가 필요하면 속성 추가 필요
    val docItemId: String? = null,          // 전표아이템ID
    val docId: String? = null,              // 전표ID
    val documentDate: LocalDate? = null,     // 증빙일
    val postingDate: LocalDate? = null,     // 전기일
)

data class SettlementOutputResult(
    val documentItemId: String,
    val documentTypeCode: String,
    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val accountCode: String,
    val accountName: String,
    val remark: String,
    val customerId: String? = null,             // 고객ID
    val vendorId: String? = null,               // 거래처ID
    val currency: String? = null,               // 통화
    val documentAmount: BigDecimal? = null,    // 전표금액

    val searchTime: String? = null,
    val syncTime: String? = null
)

// 화면에서 받는 반제 request
data class ClearingDocumentInputRequest(
    val postingDate: LocalDate,
    val offsetAccountCode: String,
    val discountAccountCode: String? = null,
    val settlementItems: MutableList<ClearingDocumentItemInputRequest> = mutableListOf(),   // 반제 대상 목록
)

// 화면에서 받는 반제 대상 목록
data class ClearingDocumentItemInputRequest(
    val documentItemId: String,                 // 전표항목ID
    val accountCode: String,
    val openAmount: BigDecimal,                 // 반제대상금액
    val allocatedAmount: BigDecimal,            // 반제금액
    val discountAmount: BigDecimal? = null,     // 할인금액
    val remainingAmount: BigDecimal,            // 거래잔액
    val clearingReason: AccountingClearingReasonType    // 반제사유
)

// 계정코드 조회
data class SearchAccountFilters(
    val pageable: SearchPageRequest = SearchPageRequest(0, 50),
    val current: Int = 1,
    val size: Int = 50,

    val companyCode: CompanyCode = CompanyCode.N200,
    val accountCode: String? = null,    // 계정코드 7자리 체크 필요
    val isOpenItemMgmt: OpenItemStatus = OpenItemStatus.NONE,

    val sortBy: Sort.By = Sort.By.ACCOUNT_CODE,
    val sortDirection: Sort.Direction = Sort.Direction.ASC,
)

// companyCode를 통한 Account 테이블 결과 FE용
data class AccountCodeOutputResult(
    val companyCode: CompanyCode,
    val accountCode: String,    // 계정코드 7자리 체크 필요
    val accountName: String,
    val description: String,
//    val accountType: String,
//    val accountClass: String,
//    val isActive: String,
    val isOpenItem: Boolean,    // 미결관리여부, is_open_item_mgmt
)

