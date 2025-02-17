package com.abc.us.accounting.iface.domain.type.oms

import com.fasterxml.jackson.annotation.JsonProperty

enum class IfMaterialProductType(val value: kotlin.String) {

    @JsonProperty("PRIMARY") PRIMARY("PRIMARY"),
    @JsonProperty("SECONDARY") SECONDARY("SECONDARY"),
    @JsonProperty("NOT_FOR_SALE") NOT_FOR_SALE("NOT_FOR_SALE")
}