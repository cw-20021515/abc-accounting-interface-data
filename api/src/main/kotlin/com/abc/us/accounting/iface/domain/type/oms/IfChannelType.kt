package com.abc.us.accounting.iface.domain.type.oms

enum class IfChannelType(val value: String) {

    ONLINE_MALL("ONLINE_MALL");
    companion object {
        fun fromName(name: String): IfChannelType? = IfChannelType.entries.find { it.name == name }
    }
}