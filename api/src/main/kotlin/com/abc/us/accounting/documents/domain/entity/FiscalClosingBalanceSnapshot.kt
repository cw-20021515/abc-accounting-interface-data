package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.DocumentDateType
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * 계정 잔액 스냅샷 (계정별 잔액을 저장하는 테이블)
 * 향후 보완 예정
 * 계정마감을 고려해서 보완예정
 */

@Entity
@Table(
    name = "fiscal_closing_balance_snapshot",
)
@Comment("월별 마감 잔액 스냅샷")
class FiscalClosingBalanceSnapshot (
    @Id
    @Comment("ID")
    @Column(name = "id")
    val id: Long,                       // 자동으로 생성되는 값 (IdGenerator 이용)

    @Embedded
    val fiscalKey: FiscalKey,

    @Comment("계정 코드")
    @Column(name = "account_code")
    val accountCode: String,

    @Comment("계정 차대구분")
    @Column(name = "account_nature", nullable = false)
    val accountNature: AccountSide,

    @Comment("잔액")
    @Column(name = "balance", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val balance: BigDecimal,

    @Comment("차변 누계액")
    @Column(name = "accumulated_debit", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedDebit: BigDecimal,

    @Comment("대변 누계액")
    @Column(name = "accumulated_credit", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val accumulatedCredit: BigDecimal,

    @Comment("스냅샷 날짜")
    @Column(name = "snapshot_date")
    val snapshotDate: LocalDate,

    @Comment("스냅샷 유형")
    @Column(name = "snapshot_type")
    @Enumerated(EnumType.STRING)
    val snapshotType: DocumentDateType,     // 현재는 postingDate만 있음

    @Comment("생성 시간")
    val createdTime: OffsetDateTime = OffsetDateTime.now()
){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FiscalClosingBalanceSnapshot) return false

        if (fiscalKey != other.fiscalKey) return false
        if (accountCode != other.accountCode) return false
        if (accountNature != other.accountNature) return false
        if (balance != other.balance) return false
        if (accumulatedDebit != other.accumulatedDebit) return false
        if (accumulatedCredit != other.accumulatedCredit) return false
        if (snapshotDate != other.snapshotDate) return false
        if (snapshotType != other.snapshotType) return false
        return true
    }

    override fun hashCode(): Int {
        return Hashs.hash(fiscalKey, accountCode, accountNature, balance, accumulatedDebit, accumulatedCredit, snapshotDate, snapshotType).toInt()
    }

    override fun toString(): String {
        return "FiscalClosingBalanceSnapshot(id=$id, fiscalKey=$fiscalKey, accountCode='$accountCode', accountNature=$accountNature, balance=$balance, accumulatedDebit=$accumulatedDebit, accumulatedCredit=$accumulatedCredit, snapshotDate=$snapshotDate, snapshotType=$snapshotType, createdTime=$createdTime)"
    }

}