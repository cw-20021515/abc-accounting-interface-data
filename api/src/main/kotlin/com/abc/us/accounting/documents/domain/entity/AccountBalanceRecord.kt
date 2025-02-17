package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.AccountBalanceRecordResult
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime




@Entity
@Table(
    name = "account_balance_record",
)
@Comment("계정 잔액 변경 기록")
class AccountBalanceRecord (
    @Comment("ID")
    @Id
    val id: Long,                           // 자동으로 생성되는 값 (IdGenerator 이용)

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "company_code", column = Column(name = "company_code")),
        AttributeOverride(name = "account_code", column = Column(name = "account_code")),
    )
    var accountKey: AccountKey,



    @Comment("계정 클래스")
    @Column(name = "account_nature", nullable = false)
    @Convert(converter = AccountSideConverter::class)
    val accountNature: AccountSide,

    @Comment("전표 항목 ID")
    @Column(name = "doc_item_id")
    val docItemId: String,

    @Comment("증빙일")
    @Column(name = "document_date")
    val documentDate: LocalDate,

    @Comment("전기일")
    @Column(name = "posting_date")
    val postingDate: LocalDate,

    @Comment("발행일")
    @Column(name = "entry_date")
    val entryDate: LocalDate,

    @Comment("기록 유형")
    @Column(name = "record_type")
    @Convert(converter = BalanceRecordTypeConverter::class)
    val recordType: BalanceRecordType,      // OPENING, TRANSACTION, ADJUSTMENT 등 구분 관리

    @Comment("변경 금액")
    @Column(name = "change_amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val changeAmount: BigDecimal,

    @Comment("변경 후 잔액")
    @Column(name = "balance_after_change", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val balanceAfterChange: BigDecimal,

    @Comment("변경 후 차변 누적액")
    @Column(name = "accumulated_debit_after_change", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedDebitAfterChange: BigDecimal,    // 변경 후 차변 누적액

    @Comment("변경 후 대변 누적액")
    @Column(name = "accumulated_credit_after_change", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedCreditAfterChange: BigDecimal,   // 변경 후 대변 누적액

    @Comment("기록 시간")
    @Column(name = "record_time")
    val recordTime: OffsetDateTime = OffsetDateTime.now(),
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as AccountBalanceRecord

        return accountKey == other.accountKey
    }

    override fun hashCode(): Int {
        return accountKey.hashCode()
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "id='" + id + '\'' +
                ", companyCode='" + accountKey.companyCode + '\'' +
                ", accountCode='" + accountKey.accountCode + '\'' +
                ", accountNature=" + accountNature + '\'' +
                ", docItemId='" + docItemId + '\'' +
                ", documentDate=" + documentDate + '\'' +
                ", postingDate=" + postingDate + '\'' +
                ", entryDate=" + entryDate + '\'' +
                ", recordType=" + recordType + '\'' +
                ", changeAmount=" + changeAmount + '\'' +
                ", balanceAfterChange=" + balanceAfterChange + '\'' +
                ", accumulatedDebitAfterChange=" + accumulatedDebitAfterChange + '\'' +
                ", accumulatedCreditAfterChange=" + accumulatedCreditAfterChange + '\'' +
                ", recordTime=" + recordTime + '\'' +
                '}'
    }

    fun toAccountBalance(newVersion:Long = Constants.DEFAULT_VERSION):AccountBalance{
        return AccountBalance(
            accountKey = accountKey,
//            version = newVersion,
            accountNature = accountNature,
            docItemId = docItemId,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            balance = balanceAfterChange,
            accumulatedDebit = accumulatedDebitAfterChange,
            accumulatedCredit = accumulatedCreditAfterChange,
            recordTime = recordTime
        )
    }

    fun toAccountBalanceRecordResult(): AccountBalanceRecordResult {
        return AccountBalanceRecordResult(
            id = id,
            companyCode = accountKey.companyCode,
            accountCode = accountKey.accountCode,
            accountNature = accountNature,
            docItemId = docItemId,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            recordType = recordType,
            changeAmount = changeAmount,
            balanceAfterChange = balanceAfterChange,
            accumulatedDebitAfterChange = accumulatedDebitAfterChange,
            accumulatedCreditAfterChange = accumulatedCreditAfterChange,
            recordTime = recordTime
        )
    }

}