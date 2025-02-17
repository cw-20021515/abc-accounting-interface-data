package com.abc.us.accounting.collects.helper

typealias OmsApiCustomerType = com.abc.us.generated.models.AccountType
typealias OmsApiCustomerStatus = com.abc.us.generated.models.CustomerStatus

typealias OmsApiOrder = com.abc.us.generated.models.OrderView
typealias OmsEntityOrder = com.abc.us.oms.domain.order.entity.Order

typealias OmsApiCustomer = com.abc.us.generated.models.Customer
typealias OmsEntityCustomer = com.abc.us.oms.domain.customer.entity.Customer

typealias OmsOrderItem = com.abc.us.generated.models.OrderItem
typealias OmsApiOrderItem = com.abc.us.generated.models.OrderItemView
typealias OmsEntityOrderItem = com.abc.us.oms.domain.order.entity.OrderItem

typealias OmsApiContract = com.abc.us.generated.models.ContractView
typealias OmsEntityContract = com.abc.us.oms.domain.contract.entity.Contract

typealias OmsApiCharge = com.abc.us.generated.models.OmsBillingCharge
typealias OmsEntityCharge = com.abc.us.oms.domain.billing.entity.Charge

typealias OmsApiChargeItem = com.abc.us.generated.models.OmsBillingChargeItem
typealias OmsEntityChargeItem = com.abc.us.oms.domain.billing.entity.ChargeItem

@JvmInline
value class OmsApiCustomerMutableList(val items: MutableList<OmsApiCustomer>) {
    fun forEach(action: (OmsApiCustomer) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsEntityCustomerMutableList(val items: MutableList<OmsEntityCustomer>) {
    fun forEach(action: (OmsEntityCustomer) -> Unit) {
        items.forEach(action)
    }
}


@JvmInline
value class OmsApiOrderMutableList(val items: MutableList<OmsApiOrder>) {
    fun forEach(action: (OmsApiOrder) -> Unit) {
        items.forEach(action)
    }
}
@JvmInline
value class OmsEntityOrderMutableList(val items: MutableList<OmsEntityOrder>) {
    fun forEach(action: (OmsEntityOrder) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsOrderItemMutableList(val items: MutableList<OmsOrderItem>) {
    fun forEach(action: (OmsOrderItem) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsEntityOrderItemMutableList(val items: MutableList<OmsEntityOrderItem>) {
    fun forEach(action: (OmsEntityOrderItem) -> Unit) {
        items.forEach(action)
    }
}
@JvmInline
value class OmsApiOrderItemMutableList(val items: MutableList<OmsApiOrderItem>) {
    fun forEach(action: (OmsApiOrderItem) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsEntityChargeMutableList(val items: MutableList<OmsEntityCharge>) {
    fun forEach(action: (OmsEntityCharge) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsApiChargeMutableList(val items: MutableList<OmsApiCharge>) {
    fun forEach(action: (OmsApiCharge) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsEntityChargeItemMutableList(val items: MutableList<OmsEntityChargeItem>) {
    fun forEach(action: (OmsEntityChargeItem) -> Unit) {
        items.forEach(action)
    }
}
@JvmInline
value class OmsApiChargeItemMutableList(val items: MutableList<OmsApiChargeItem>) {
    fun forEach(action: (OmsApiChargeItem) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsApiContractMutableList(val items: MutableList<OmsApiContract>) {
    fun forEach(action: (OmsApiContract) -> Unit) {
        items.forEach(action)
    }
}

@JvmInline
value class OmsEntityContractMutableList(val items: MutableList<OmsEntityContract>) {
    fun forEach(action: (OmsEntityContract) -> Unit) {
        items.forEach(action)
    }
}
