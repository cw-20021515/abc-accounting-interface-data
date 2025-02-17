package com.abc.us.accounting.iface.domain.type.oms

enum class IfPromotionType(val value: String) {

    DISCOUNT("DISCOUNT"),
    OFFER("OFFER");
    companion object {
        fun fromName(name: String): IfPromotionType? = IfPromotionType.entries.find { it.name == name }
    }
}