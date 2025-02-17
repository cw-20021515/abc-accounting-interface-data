package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.AccountSideConverter
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


@Entity
@Table(
    name = "account_balance",
)
@Comment("계정 잔액")
class AccountBalance (
    @EmbeddedId
    var accountKey: AccountKey,

//    @Version
//    val version: Long = Constants.DEFAULT_VERSION,
//
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

    @Comment("변경 후 잔액")
    @Column(name = "balance", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val balance: BigDecimal,

    @Comment("변경 후 차변 누적액")
    @Column(name = "accumulated_debit", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedDebit: BigDecimal,    // 변경 후 차변 누적액

    @Comment("변경 후 대변 누적액")
    @Column(name = "accumulated_credit", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedCredit: BigDecimal,   // 변경 후 대변 누적액

    @Comment("기록 시간")
    @Column(name = "record_time")
    val recordTime: OffsetDateTime = OffsetDateTime.now(),
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as AccountBalance

        return accountKey == other.accountKey
    }

    override fun hashCode(): Int {
        return accountKey.hashCode()
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                ", companyCode='" + accountKey.companyCode + '\'' +
                ", accountCode='" + accountKey.accountCode + '\'' +
//                ", version=" + version +'\'' +
                ", accountNature=" + accountNature + '\'' +
                ", docItemId='" + docItemId + '\'' +
                ", documentDate=" + documentDate + '\'' +
                ", postingDate=" + postingDate + '\'' +
                ", entryDate=" + entryDate + '\'' +
                ", balance=" + balance + '\'' +
                ", accumulatedDebit=" + accumulatedDebit + '\'' +
                ", accumulatedCredit=" + accumulatedCredit + '\'' +
                ", recordTime=" + recordTime + '\'' +
                '}'
    }

}