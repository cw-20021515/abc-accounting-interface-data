package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.iface.domain.entity.oms.IfMaterial

data class RentalDistributionMappingInfo(
    val rentalDistributionMaster : RentalDistributionMaster,
    val rentalCodeMaster: RentalCodeMaster,
    val rentalPricingMaster: RentalPricingMaster,
    val material: IfMaterial
)
