package com.abc.us.accounting.collects.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Payment(
    val paymentId: String,
    val arId: String,
    val totalPrice: BigDecimal,
    val paymentMethod: String,
    val transactionId: String?,
    val cardNumber: String?,
    val cardType: String?,
    val installmentMonths: Int,
    val paymentTime: OffsetDateTime,
    val billingAddress: Location?,
    val arItems: List<AccountsReceivableItem>,
)