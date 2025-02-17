package com.abc.us.accounting.logistics.model.request

import com.abc.us.accounting.rentals.master.domain.type.MaterialType
import java.time.LocalDate

data class LogisticsInventoryMovementStatusRequest(
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val materialType: MaterialType? = null,
    val materialSeriesName: String? = null,
    val materialId: String? = null,
    val materialModelName: String? = null,
    val warehouseId: String? = null,
    val current: Int = 1,
    val size: Int = 10
)