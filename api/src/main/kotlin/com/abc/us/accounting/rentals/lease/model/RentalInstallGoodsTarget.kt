package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster

class RentalInstallGoodsTarget (
    val contract: CollectContract,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val history: RentalFinancialDepreciationHistoryEntity,
    val installation: CollectInstallation
)