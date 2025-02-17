package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.iface.domain.entity.oms.IfChargeInvoice
import com.abc.us.accounting.iface.domain.entity.oms.IfChargeItem
import com.abc.us.accounting.iface.domain.entity.oms.IfInvoice

data class RentalCharge(
    val chargeItems: List<IfChargeItem>,
    val chargeInvoice: IfChargeInvoice,
    val invoice: IfInvoice
)
