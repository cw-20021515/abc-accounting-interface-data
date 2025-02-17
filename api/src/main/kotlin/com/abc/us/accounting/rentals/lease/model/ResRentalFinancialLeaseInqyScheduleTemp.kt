package com.abc.us.accounting.rentals.lease.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class ResRentalFinancialLeaseInqyScheduleTemp(
    val contractId: String? = null,
    val contractDate: LocalDate? = null,
    val baseDate: LocalDate? = null,
    val depreciationCount: Int? = null,
    val initialBookValue: BigDecimal? = null,
    val depreciationBookValue: BigDecimal? = null,
    val rentalAmountForGoods: BigDecimal? = null,
    val depreciationPresentValue: BigDecimal? = null,
    val depreciationCurrentDifference: BigDecimal? = null,
    val depreciationInterestIncome: BigDecimal? = null,
    val cumulativeInterestIncome: BigDecimal? = null,
    val orderItemId: String? = null,
    val customerId: String? = null,
    val serialNumber: String? = null,
    val materialId: String? = null,
    val modelName: String? = null,
    var materialCategory: String? = null,  // 카테고리(MaterialCategoryCode)
    val createTime: OffsetDateTime? = null,
    var totalCnt: Long? = 0,
    var rentalEventType: String = "",
    var orderId: String = "",
    var materialSeriesCode: String = "",
    var contractEndDate: LocalDate? = null,
    var contractPeriod: Int? = null,
    var rentalAmount: BigDecimal? = null,
    var initialPresentValue: BigDecimal? = null,
    var initialCurrentDifference: BigDecimal? = null,
    var interestRate: Double? = null,
)