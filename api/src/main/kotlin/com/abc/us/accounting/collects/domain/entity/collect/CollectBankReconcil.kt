package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.type.DepositType
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
class CollectBankReconcil {
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    @Comment("입금 ID")
    var reconcilId: String? = null

    @Comment("입금 라인 번호")
    var reconcilLineNumber : Int = 1

    @Comment("입금 통화")
    var currency : String? = null

    @Comment("입금 수행 계정 코드")
    var reconcilToAccountCode : String? = null

    @Comment("입금 총액")
    var totalAmount : BigDecimal = BigDecimal(0.0)

    @Comment("입금 라인별 총액")
    var lineAmount : BigDecimal = BigDecimal(0.0)

    @Enumerated(EnumType.STRING)
    val detailType : DepositType = DepositType.NONE

    @Comment("결제 방식")
    val paymentMethod : String? = null

    @Comment("계정 코드")
    val accountCode : String? = null

    @Comment("연결된 고객")
    val entityName: String? = null

    @Comment("연결된 엔터티의 고유 ID.")
    val entityValue: String? = null

    @Comment("연결된 거래의 고유 ID")
    val txnId : String? = null

    @Comment("연결된 거래의 유형 (예: Payment, CreditMemo).")
    val txnType : String? = null

    @Comment("거래 날짜. QuickBooks에 기록된 입금 날짜.")
    val txnDate : LocalDate? = null

    @Comment("세금 코드")
    val taxCode : String? = null

    @Comment("입금 라인별 총액")
    var totalTax : BigDecimal = BigDecimal(0.0)

    @Comment("입금 라인별 총액")
    val privateNote : String? = null

    @IgnoreHash
    @Comment("빌링 시스템 내 생성 시간")
    var createTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("빌링 시스템 내 업데이트 시간")
    var updateTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("고객 활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
}