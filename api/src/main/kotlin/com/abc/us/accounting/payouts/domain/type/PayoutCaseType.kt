package com.abc.us.accounting.payouts.domain.type

import com.fasterxml.jackson.annotation.JsonProperty

enum class PayoutCaseType(val value: kotlin.String) {

    @JsonProperty("MATERIAL") MATERIAL("MATERIAL"),
    @JsonProperty("EMPLOYEE") EMPLOYEE("EMPLOYEE"),
    @JsonProperty("GENERAL") GENERAL("GENERAL")
}

