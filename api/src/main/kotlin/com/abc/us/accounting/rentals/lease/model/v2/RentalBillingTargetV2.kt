package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.master.domain.entity.RentalDistributionRule
import com.abc.us.accounting.rentals.master.domain.entity.RentalPricingMaster

data class RentalBillingTargetV2(
    val charge: IfCharge,
    val chargeItems: List<IfChargeItem>,
    val invoice: IfInvoice,
    val contract: IfContract,
    val orderItem: IfOrderItem,
    val serviceFlows: List<IfServiceFlow>,
    val material: IfMaterial,
    val channel: IfChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val rentalPricingMaster: RentalPricingMaster,
    val rentalDistributionRule: RentalDistributionRule
)
