package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable

enum class ContractPricingType(val symbol: String, val description: String): Serializable {
    NO_COMMITMENT("NOC", "무약정"),
    NEW_COMMITMENT("NWC", "신규 약정"),
    TRANSITION_TO_COMMITMENT("TTC", "약정 전환"),
    COMMITMENT_RENEWAL_1("CR1", "재약정1차"),
    COMMITMENT_RENEWAL_2("CR2", "재약정2차"),
    PREVIOUS_CONTRACT("PRC", "기존 계약가"),
}
