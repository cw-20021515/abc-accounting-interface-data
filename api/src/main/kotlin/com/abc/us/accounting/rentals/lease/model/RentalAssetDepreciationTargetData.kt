package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule

data class RentalAssetDepreciationTargetData(
    val installation: CollectInstallation,
    val contract: CollectContract,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val schedule: RentalAssetDepreciationSchedule
)
