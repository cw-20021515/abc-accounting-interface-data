package com.abc.us.accounting.collects.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PriceDetail(
    val discountPrice: BigDecimal,
    val itemPrice: BigDecimal,
    val prepaidAmount: BigDecimal,
    val tax: BigDecimal,
    val currency: String,
    val taxLines: List<TaxLine>,
    val promotions: List<Promotion>
)