package com.abc.us.accounting.rentals.lease.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class RentalAssetPriceData @JsonCreator constructor(
    @JsonProperty("productPrice")
    val productPrice: BigDecimal,
    @JsonProperty("servicePrice")
    val servicePrice: BigDecimal
)
