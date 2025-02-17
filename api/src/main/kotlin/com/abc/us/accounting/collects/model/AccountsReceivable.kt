package com.abc.us.accounting.collects.model

import com.abc.us.accounting.collects.domain.entity.collect.CollectContract
import com.abc.us.accounting.collects.domain.type.ChargeStatusEnum
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AccountsReceivable (
    val arId: String,
    val arItems: MutableList<AccountsReceivableItem>,
    val billingCycle: Int,
    val targetMonth: String,
    val totalPrice: BigDecimal,
    val arStatus: ChargeStatusEnum,
    val createTime: OffsetDateTime,
    val updateTime: OffsetDateTime,
    val contract : CollectContract?,
    val payment: Payment?
)