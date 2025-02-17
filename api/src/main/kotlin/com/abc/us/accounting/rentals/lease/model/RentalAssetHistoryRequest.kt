package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.master.domain.type.MaterialCategoryCode
import java.time.LocalDate

data class RentalAssetHistoryRequest(
    val baseDate: LocalDate? = null,
    val contractFromDate: LocalDate? = null,
    val contractToDate: LocalDate? = null,
    val customerId: String? = null,
    val orderIdFrom: String? = null,
    val orderIdTo: String? = null,
    val serialNumber: String? = null,
    val materialId: String? = null,
    val materialCategory: MaterialCategoryCode? = null,
    val current: Int = 1,
    val size: Int = 10
)