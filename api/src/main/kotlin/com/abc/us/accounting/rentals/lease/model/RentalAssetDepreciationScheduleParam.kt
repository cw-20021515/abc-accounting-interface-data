package com.abc.us.accounting.rentals.lease.model

import java.math.BigDecimal
import java.time.LocalDate

data class RentalAssetDepreciationScheduleParam(
    val bookValue: BigDecimal,
    val usefulLife: Int,
    val installationDate: LocalDate,
    val currency: String,
    val salvageValue: BigDecimal,
    val serialNumber: String
)
