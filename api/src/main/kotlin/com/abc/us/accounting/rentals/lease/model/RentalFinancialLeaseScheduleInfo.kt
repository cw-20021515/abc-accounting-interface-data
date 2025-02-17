package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime

@Schema(name = "응답_금융 리스_렌탈스케쥴_등록")
data class RentalFinancialLeaseScheduleInfo(
    @Schema(description = "청구 회차")
    val depreciationCount: Int? = 0,

    @Schema(description = "년월 (예: 2024.01)")
    val depreciationYearMonth: String,

    @Schema(description = "청구 년월 (예: 2024.01)")
    val depreciationBillYearMonth: String? = null,

    @Schema(description = "통화 (예: USD)")
    val currency: String = "USD",

    @Schema(description = "렌탈료(재화)", defaultValue = "0.0")
    val depreciationRentalAmount: BigDecimal? = BigDecimal("0.0"),

    @Schema(description = "장부 금액", defaultValue = "0.0")
    val depreciationBookValue: BigDecimal = BigDecimal("0.0"),

    @Schema(description = "현재 가치", defaultValue = "0.0")
    val depreciationPresentValue: BigDecimal = BigDecimal("0.0"),

    @Schema(description = "현 할차", defaultValue = "0.0")
    val depreciationCurrentDifference: BigDecimal = BigDecimal("0.0"),

    @Schema(description = "이자 수익", defaultValue = "0.0")
    val depreciationInterestIncome: BigDecimal? = null,

    @Schema(description = "렌탈료", defaultValue = "0.0")
    val initFirstRentalAmount: BigDecimal? = null,
    @Schema(description = "렌탈료(재화)", defaultValue = "0.0")
    val initFirstRentalAmountForGoods: BigDecimal? = null,
    @Schema(description = "렌탈료(서비스-선수금)", defaultValue = "0.0")
    val initFirstRentalAmountForService: BigDecimal? = null,

    @Schema(description = "그룹 ID", defaultValue = "0.0")
    var txId:String? = null,

){
    fun fromItems(fLeaseId:String, reqOrderItemId:String, reqContractId:String, rentalFinancialDepreciationScheduleEntity : RentalFinancialDepreciationScheduleEntity) : RentalFinancialDepreciationScheduleEntity {
        var request = this
        return rentalFinancialDepreciationScheduleEntity.apply {
            txId = fLeaseId
            orderItemId = reqOrderItemId
            contractId = reqContractId
            depreciationCount = request.depreciationCount
            depreciationYearMonth = request.depreciationYearMonth
            depreciationBillYearMonth = request.depreciationBillYearMonth
            currency = request.currency
            depreciationRentalAmount = request.depreciationRentalAmount
            depreciationBookValue = request.depreciationBookValue
            depreciationPresentValue = request.depreciationPresentValue
            depreciationCurrentDifference = request.depreciationCurrentDifference
            depreciationInterestIncome = request.depreciationInterestIncome

            createTime = createTime?:OffsetDateTime.now()
        }
    }
}
