package com.abc.us.accounting.logistics.model.request

import com.abc.us.accounting.logistics.domain.type.CostHistory
import com.abc.us.accounting.rentals.master.domain.type.MaterialCategoryCode
import com.abc.us.accounting.rentals.master.domain.type.MaterialType
import com.abc.us.generated.models.WarehouseType
import java.time.LocalDate

data class LogisticsInventoryCostStatusRequest(
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val materialType: MaterialType? = null,
    val materialSeriesName: String? = null,
    val materialId: String? = null,
    val materialModelName: String? = null,
    val warehouseId: String? = null,
    val costHistory: CostHistory? = null,
    val current: Int = 1,
    val size: Int = 10
)