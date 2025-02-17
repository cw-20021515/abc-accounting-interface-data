package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import java.time.LocalDate

data class RentalAssetMakeHistoryData(
    val eventType: RentalAssetEventType,
    val rentalAssetData: RentalAssetData,
    val depreciationData: RentalAssetDepreciationData = RentalAssetDepreciationData(),
    val baseDate: LocalDate
)
