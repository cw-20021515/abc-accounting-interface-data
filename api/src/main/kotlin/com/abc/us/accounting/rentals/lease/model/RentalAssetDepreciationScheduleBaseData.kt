package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetDepreciationMethod
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate

data class RentalAssetDepreciationScheduleBaseData(
    val materialId: String? = null,
    val modelName: String? = null,
    val materialSeriesCode: String? = null,
    val orderId: String? = null,
    val orderItemId: String? = null,
    val contractId: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val orderDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val installationDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val contractDate: LocalDate? = null,
    val depreciationMethod: RentalAssetDepreciationMethod? = null,
    val usefulLife: Int? = null,
    val salvageValue: BigDecimal? = null
)
