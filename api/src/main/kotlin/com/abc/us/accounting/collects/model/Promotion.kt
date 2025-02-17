package com.abc.us.accounting.collects.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Promotion(
    val promotionId: String,
    val promotionName: String,
    val promotionDescription: String,
    val startDate: OffsetDateTime,
    val endDate: OffsetDateTime,
    val discountPrice: BigDecimal
)