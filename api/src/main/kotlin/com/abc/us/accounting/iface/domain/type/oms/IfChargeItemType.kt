package com.abc.us.accounting.iface.domain.type.oms

enum class IfChargeItemType(val value: String) {

    SERVICE_FEE("SERVICE_FEE"),
    INSTALLATION_FEE("INSTALLATION_FEE"),
    DISMANTILING_FEE("DISMANTILING_FEE"),
    REINSTALLATION_FEE("REINSTALLATION_FEE"),
    TERMINATION_PENALTY("TERMINATION_PENALTY"),
    LATE_FEE("LATE_FEE"),
    LOSS_FEE("LOSS_FEE"),
    PART_COST("PART_COST"),
    RENTAL_FEE("RENTAL_FEE"),
    RELOCATION_FEE("RELOCATION_FEE");
    companion object {
        fun fromName(name: String): IfChargeItemType? = IfChargeItemType.entries.find { it.name == name }
    }
}