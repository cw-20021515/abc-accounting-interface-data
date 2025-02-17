package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.master.domain.entity.RentalPricingMaster

data class RentalPaymentTarget(
    val receipt: CollectReceipt,
    val contract: CollectContract,
    val installation: CollectInstallation,
    val material: CollectMaterial,
    val channel: CollectChannel?,
    val charge: CollectCharge,
    val rentalCodeMaster: RentalCodeMaster,
    val rentalPricingMaster: RentalPricingMaster
)
