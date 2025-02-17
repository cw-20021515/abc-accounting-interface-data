package com.abc.us.accounting.iface.domain.type.oms

enum class IfContractStatus(val value: String) {

    PENDING_CONFIRMATION("PENDING_CONFIRMATION"),
    ACTIVE("ACTIVE"),
    BREACHED_CONTRACT("BREACHED_CONTRACT"),
    TRANSFER_TO_COLLECTION("TRANSFER_TO_COLLECTION"),
    CONTRACT_ENDED("CONTRACT_ENDED"),
    CONTRACT_CANCELLED("CONTRACT_CANCELLED"),
    CONTRACT_WITHDRAWN("CONTRACT_WITHDRAWN");
    companion object {
        fun fromName(name: String): IfContractStatus? = IfContractStatus.entries.find { it.name == name }
    }
}