package com.abc.us.accounting.collects.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaxLine(
    val title: String,
    val rate: BigDecimal,
    val price: BigDecimal
)