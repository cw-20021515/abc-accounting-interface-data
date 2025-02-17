package com.abc.us.accounting.documents.model

import com.abc.us.accounting.documents.domain.entity.AccountBalance
import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.BalanceRecordType
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.entity.FiscalRule
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


data class AccountBalanceCalculation(
    val balance: BigDecimal,
    val accumulatedDebit: BigDecimal,
    val accumulatedCredit: BigDecimal
){
    companion object {
        fun zero():AccountBalanceCalculation {
            return AccountBalanceCalculation(
                balance = BigDecimal.ZERO,
                accumulatedDebit = BigDecimal.ZERO,
                accumulatedCredit = BigDecimal.ZERO
            )
        }
    }

    fun copyWithNullable(balance: BigDecimal,
                         accumulatedDebit: BigDecimal? = null,
                         accumulatedCredit: BigDecimal? = null):AccountBalanceCalculation {

        return AccountBalanceCalculation(
            balance = balance,
            accumulatedDebit = accumulatedDebit ?: this.accumulatedDebit,
            accumulatedCredit = accumulatedCredit ?: this.accumulatedCredit
        )
    }

    fun add (accountNature: AccountSide, newDocItem:DocumentItemResult, oldDocItem:DocumentItemResult? = null): AccountBalanceCalculation{
        val newAmount = accountNature.calculateAmount(newDocItem.amount, newDocItem.accountSide)
        val oldAmount = if ( oldDocItem != null) accountNature.calculateAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO

        val changeAmount = newAmount - oldAmount

        val newDebitAmount = accountNature.debitAmount(newDocItem.amount, newDocItem.accountSide)
        val oldDebitAmount = if ( oldDocItem != null) accountNature.debitAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO
        val debitChangeAmount = newDebitAmount - oldDebitAmount

        val newCreditAmount = accountNature.creditAmount(newDocItem.amount, newDocItem.accountSide)
        val oldCreditAmount = if ( oldDocItem != null) accountNature.creditAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO
        val creditChangeAmount = newCreditAmount - oldCreditAmount

        val balanceAfterChange = balance.add(changeAmount)
        val accumulatedDebitAfterChange = accumulatedDebit.add(debitChangeAmount)
        val accumulatedCreditAfterChange = accumulatedCredit.add(creditChangeAmount)


        return AccountBalanceCalculation(
            balance = balanceAfterChange,
            accumulatedDebit = accumulatedDebitAfterChange,
            accumulatedCredit = accumulatedCreditAfterChange
        )
    }

    fun minus (accountNature: AccountSide, newDocItem:DocumentItemResult, oldDocItem:DocumentItemResult? = null): AccountBalanceCalculation{
        val newAmount = accountNature.calculateAmount(newDocItem.amount, newDocItem.accountSide)
        val oldAmount = if ( oldDocItem != null) accountNature.calculateAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO

        val changeAmount = newAmount - oldAmount

        val newDebitAmount = accountNature.debitAmount(newDocItem.amount, newDocItem.accountSide)
        val oldDebitAmount = if ( oldDocItem != null) accountNature.debitAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO
        val debitChangeAmount = newDebitAmount - oldDebitAmount

        val newCreditAmount = accountNature.creditAmount(newDocItem.amount, newDocItem.accountSide)
        val oldCreditAmount = if ( oldDocItem != null) accountNature.creditAmount(oldDocItem.amount, oldDocItem.accountSide) else BigDecimal.ZERO
        val creditChangeAmount = newCreditAmount - oldCreditAmount

        val balanceAfterChange = balance.minus(changeAmount)
        val accumulatedDebitAfterChange = accumulatedDebit.minus(debitChangeAmount)
        val accumulatedCreditAfterChange = accumulatedCredit.minus(creditChangeAmount)

        return AccountBalanceCalculation(
            balance = balanceAfterChange,
            accumulatedDebit = accumulatedDebitAfterChange,
            accumulatedCredit = accumulatedCreditAfterChange
        )
    }
}

sealed class AccountBalanceEvent {
    data class OpeningBalance(val context:DocumentServiceContext, val requests: List<OpeningBalanceRequest>) : AccountBalanceEvent()
    data class BalanceAdjustment(val context:DocumentServiceContext, val requests: List<BalanceAdjustmentRequest>) : AccountBalanceEvent()

    data class DocumentCreated(val context:DocumentServiceContext, val requests: List<DocumentBalanceRequest>) : AccountBalanceEvent()
    data class DocumentModified(val context:DocumentServiceContext, val requests: List<DocumentBalanceRequest> ) : AccountBalanceEvent()
    data class DocumentDeleted(val context:DocumentServiceContext, val requests: List<DocumentBalanceRequest> ) : AccountBalanceEvent()

    data class MonthClosed(val context:DocumentServiceContext, val fiscalYearMonth: FiscalYearMonth, val fiscalRule: FiscalRule) : AccountBalanceEvent()
}

data class OpeningBalanceRequest(
    val companyCode: CompanyCode,
    val accountCode: String,
    val amount: BigDecimal,
)

data class BalanceAdjustmentRequest(
    val companyCode: CompanyCode,
    val accountCode: String,
    val amount: BigDecimal,
)

data class DocumentBalanceRequest(
    val newDocumentResult: DocumentResult,
    val oldDocumentResult: DocumentResult? = null,
)



data class AccountBalanceRecordResult(
    val id: Long,                           // 자동으로 생성되는 값 (IdGenerator 이용)
    val companyCode: CompanyCode,           // 회사 코드
    val accountCode: String,                // 계정 코드
    val accountNature: AccountSide,
    val docItemId: String,
    val documentDate: LocalDate,
    val postingDate: LocalDate,
    val entryDate: LocalDate,
    val recordType: BalanceRecordType,      // OPENING, TRANSACTION, ADJUSTMENT 등 구분 관리

    val changeAmount: BigDecimal,
    val balanceAfterChange: BigDecimal,
    val accumulatedDebitAfterChange: BigDecimal,    // 변경 후 차변 누적액
    val accumulatedCreditAfterChange: BigDecimal,   // 변경 후 대변 누적액
    val recordTime: OffsetDateTime = OffsetDateTime.now(),
){
    fun toAccountKey(): AccountKey {
        return AccountKey.of(companyCode, accountCode)
    }
}



sealed class ClearingEvent {
    data class PostClearing(val context:DocumentServiceContext, val requests: List<DocumentResult>) : ClearingEvent()
}
