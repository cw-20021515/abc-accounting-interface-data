package com.abc.us.accounting.iface.domain.type.oms

enum class IfOrderItemType(val value: String) {

    RENTAL("RENTAL"),
    PURCHASE("PURCHASE"),
    ONETIME("ONETIME"), // 임시
    AUTO_ORDER("AUTO_ORDER"),
    NONE("NONE");
    companion object {
        fun fromName(name: String): IfOrderItemType? = IfOrderItemType.entries.find { it.name == name }
    }
}
