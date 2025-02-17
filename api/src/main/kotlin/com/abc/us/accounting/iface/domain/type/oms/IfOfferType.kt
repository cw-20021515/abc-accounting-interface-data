package com.abc.us.accounting.iface.domain.type.oms

enum class IfOfferType(val value: String) {

    GIFT("GIFT"),
    PAYBACK("PAYBACK");
    companion object {
        fun fromName(name: String): IfOfferType? = IfOfferType.entries.find { it.name == name }
    }
}