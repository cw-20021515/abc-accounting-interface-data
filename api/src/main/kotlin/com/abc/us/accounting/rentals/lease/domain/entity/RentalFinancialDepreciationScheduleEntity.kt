package com.abc.us.accounting.rentals.lease.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "RENTAL_FINANCIAL_DEPRECIATION_SCHEDULE")
class RentalFinancialDepreciationScheduleEntity(

    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("테이블 ID")
    var id: String? = null,

    @Comment("그룹 ID")
    var txId: String? = null,

    @Comment("주문 아이템 ID")
    var orderItemId: String? = null,

    @Comment("계약 ID")
    var contractId: String? = null,

    @Comment("금융상각 회차")
    var depreciationCount: Int? = null,

    @Comment("년월 (예: 2024.01)")
    var depreciationYearMonth: String? = null,

    @Comment("청구 년월 (예: 2024.01)")
    var depreciationBillYearMonth: String? = null,

    @Comment("통화 (예: USD)")
    var currency: String? = null,

    @Comment("렌탈료")
    var depreciationRentalAmount: BigDecimal? = null,

    @Comment("장부 금액")
    var depreciationBookValue: BigDecimal? = null,

    @Comment("현재 가치(PV)")
    var depreciationPresentValue: BigDecimal? = null,

    @Comment("현 할차")
    var depreciationCurrentDifference: BigDecimal? = null,

    @Comment("이자 수익")
    var depreciationInterestIncome: BigDecimal? = null,

    @Comment("생성 시간")
    var createTime: OffsetDateTime? = null
)
