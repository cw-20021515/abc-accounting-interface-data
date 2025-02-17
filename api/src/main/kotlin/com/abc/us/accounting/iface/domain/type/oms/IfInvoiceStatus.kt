package com.abc.us.accounting.iface.domain.type.oms

enum class IfInvoiceStatus(val value: String) {

    ISSUED("ISSUED"),
    EXPIRED("EXPIRED");
    companion object {
        fun fromName(name: String): IfInvoiceStatus? = IfInvoiceStatus.entries.find { it.name == name }
    }
}