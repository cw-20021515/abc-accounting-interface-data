package com.abc.us.accounting.rentals.lease.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RentalAssetDepreciationScheduleData @JsonCreator constructor(
    @JsonProperty("base")
    val base: RentalAssetDepreciationScheduleBaseData?,
    @JsonProperty("depreciationSchedule")
    val depreciationSchedule: List<RentalAssetDepreciationScheduleItemData>
)
