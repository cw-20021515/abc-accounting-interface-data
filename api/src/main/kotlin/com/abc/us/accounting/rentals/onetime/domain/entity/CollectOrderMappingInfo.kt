package com.abc.us.accounting.rentals.onetime.domain.entity

import com.abc.us.accounting.collects.domain.entity.collect.*


data class CollectOrderItemWithExtraInfo (
    val collectOrderItem: CollectOrderItem,
    val customerId: String? = null,
    val channelId: String? = null,
    val referralCode: String? = null,
)

data class CollectOrderMappingInfo (
    val collectOrderItem: CollectOrderItem,
    val collectMaterial: CollectMaterial? = null,
    val collectServiceFlow: CollectServiceFlow? = null,
    val collectInstallation: CollectInstallation? = null,
    val collectInventoryValuation: CollectInventoryValuation? = null,
) {
    constructor(
        collectOrderItem: CollectOrderItem,
        collectMaterial: CollectMaterial?= null
    ) : this(collectOrderItem, collectMaterial, null)

    constructor(
        collectOrderItem: CollectOrderItem,
        collectMaterial: CollectMaterial? = null,
        collectServiceFlow: CollectServiceFlow? = null,
    ) : this(collectOrderItem, collectMaterial, collectServiceFlow, null, null)
}

