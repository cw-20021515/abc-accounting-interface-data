package com.abc.us.accounting.iface.domain.type.oms

enum class IfChargeStatus(val value: String) {

    CREATED("CREATED"),
    SCHEDULED("SCHEDULED"),
    PENDING("PENDING"),
    OVERDUE("OVERDUE");
    companion object {
        fun fromName(name: String): IfChargeStatus? = IfChargeStatus.entries.find { it.name == name }
    }
}