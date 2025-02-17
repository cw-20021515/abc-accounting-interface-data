package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.master.domain.entity.RentalDistributionRule

data class RentalAssetInstallationData(
    val installation: CollectInstallation,
    val contract: CollectContract,
    val inventoryValue: CollectInventoryValuation,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val rentalDistributionRule: RentalDistributionRule,
    val order: CollectOrder
)
