package com.abc.us.accounting.rentals.lease.model

import java.math.BigDecimal
import java.time.LocalDate

data class RentalAssetDepreciationData(
    val depreciationCount: Int? = null,
    val depreciationDate: LocalDate? = null,
    val depreciationExpense: BigDecimal? = null,
    val accumulatedDepreciation: BigDecimal? = null,
    val bookValue: BigDecimal? = null
)
