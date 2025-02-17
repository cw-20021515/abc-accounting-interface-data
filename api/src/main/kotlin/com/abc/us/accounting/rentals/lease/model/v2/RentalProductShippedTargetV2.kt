package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster

data class RentalProductShippedTargetV2(
    val serviceFlow: IfServiceFlow,
    val contract: IfContract,
    val orderItem: IfOrderItem,
    val inventoryValue: CollectInventoryValuation,
    val material: IfMaterial,
    val channel: IfChannel?,
    val rentalCodeMaster: RentalCodeMaster
)
