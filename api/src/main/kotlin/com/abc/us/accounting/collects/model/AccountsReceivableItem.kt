package com.abc.us.accounting.collects.model

import com.abc.us.accounting.collects.domain.type.ChargeItemEnum
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AccountsReceivableItem(
    val arItemId: String,
    val arItemType: ChargeItemEnum,
    val serviceFlowId: String?,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val isTaxExempt: Boolean,
    val createTime: OffsetDateTime,
    val priceDetail: PriceDetail?
)