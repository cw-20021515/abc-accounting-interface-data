package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster

data class MonthEndTarget(
    val installation: CollectInstallation,
    val contract: CollectContract,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val schedule: RentalFinancialDepreciationScheduleEntity
)
