package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.model.RentalAssetHistoryItemData

data class RentalChangeStateTarget(
    val eventType: RentalAssetEventType,
    val rentalAsset: RentalAssetHistoryItemData,
    val orderItem: IfOrderItem
)
