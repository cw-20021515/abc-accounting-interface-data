package com.abc.us.accounting.iface.domain.type.oms

enum class IfDiscountType(val value: String) {

    FIXED("FIXED"),
    PERCENTAGE("PERCENTAGE");
    companion object {
        fun fromName(name: String): IfDiscountType? = IfDiscountType.entries.find { it.name == name }
    }
}