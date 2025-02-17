package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster

data class RentalProductShippedTarget(
    val serviceFlow: CollectServiceFlow,
    val contract: CollectContract,
    val inventoryValue: CollectInventoryValuation,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val rentalCodeMaster: RentalCodeMaster
)
