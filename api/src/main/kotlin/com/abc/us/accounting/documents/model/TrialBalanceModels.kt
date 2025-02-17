package com.abc.us.accounting.documents.model

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import java.math.BigDecimal
import java.time.LocalDate


/**
 * 수정 필요
 */
data class SearchTrialBalanceFilters(
    val pageable: SearchPageRequest = SearchPageRequest(0, 200),
    val current: Int = 1,
    val size: Int = 200,
    val fromMonth: String,
    val toMonth:  String,
    val fromDate: LocalDate = LocalDate.of(2025, 1, 1),
    val toDate: LocalDate = LocalDate.now(),
    // 기초잔액 계산에 필요한 일자
    val beginningFromDate: LocalDate = LocalDate.of(2024, 7, 1),
    val beginningToDate: LocalDate = LocalDate.now(),
    val companyCode: CompanyCode = CompanyCode.N200,
    val accountGroupFrom: String? = null,   // 계정코드 그룹(미정)
    val accountGroupTo: String? = null,     // 계정코드 그룹(미정)
    val accountCodeFrom: String? = null,    // 계정코드 7자리 체크 필요
    val accountCodeTo: String? = null,      // 계정코드 7자리 체크 필요
    val displayLevel: String? = null,
    //val displayLevel: AccountingDisplayLevel = AccountingDisplayLevel.LEVEL_5,
    val sortBy: Sort.By = Sort.By.ACCOUNT_CODE,
    val sortDirection: Sort.Direction = Sort.Direction.ASC,
)

data class TrialBalanceResult(
    val companyCode: CompanyCode,
    val accountCode: String,
//    val level: Int? = null,
    val accountName: String,

    val beginningDebitBalance: BigDecimal? = null,      //차변기초잔액
    val totalDebitAmount: BigDecimal? = null,           //차변합계
    val endingDebitBalance: BigDecimal? = null,         //차변기말잔액

    val beginningCreditBalance: BigDecimal? = null,     //대변기초잔액
    val totalCreditAmount: BigDecimal? = null,          //대변합계
    val endingCreditBalance: BigDecimal? = null,        //대변기말잔액

    val searchTime: String? = null,
    val syncTime: String? = null
) {
    fun copy():TrialBalanceResult {
        return TrialBalanceResult(
            companyCode = companyCode,
            accountCode = accountCode,
//            level = level,
            accountName = accountName,
            beginningDebitBalance = beginningDebitBalance,
            totalDebitAmount = totalDebitAmount,
            endingDebitBalance = endingDebitBalance,
            beginningCreditBalance = beginningCreditBalance,
            totalCreditAmount = totalCreditAmount,
            endingCreditBalance = endingCreditBalance
        )
    }
}

data class TrialBalanceDocumentItemResult(
    val docItemId: String,
    val docId: String,
    val companyCode: CompanyCode,
    val accountCode: String,
    val accountName: String? = null,
    val postingDate: LocalDate? = null,
    val accountSide: AccountSide,
    val txCurrency: String,
    val txAmount: BigDecimal,
    val currency: String,
    val amount: BigDecimal,
) {
    companion object {
        fun toResult(
            param: DocumentItem,
            setPostingDate: LocalDate
        ): TrialBalanceDocumentItemResult {
            return TrialBalanceDocumentItemResult(
                docItemId = param.id,
                docId = param.docId,
                companyCode = param.companyCode,
                accountCode = param.accountCode,
                accountSide = param.accountSide,
                postingDate = setPostingDate,
                txCurrency = param.txMoney.currency.toString(),
                txAmount = param.txMoney.amount,
                currency = param.money.currency.toString(),
                amount = param.money.amount
            )
        }
    }
}
