package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.master.domain.entity.RentalDistributionRule

data class RentalPaymentTargetV2(
    val chargePayment: IfChargePayment,
    val contract: IfContract,
    val orderItem: IfOrderItem,
    val serviceFlows: List<IfServiceFlow>,
    val material: IfMaterial,
    val channel: IfChannel?,
    val charge: IfCharge,
    val invoice: IfInvoice,
    val rentalCodeMaster: RentalCodeMaster,
    val rentalDistributionRule: RentalDistributionRule
)
