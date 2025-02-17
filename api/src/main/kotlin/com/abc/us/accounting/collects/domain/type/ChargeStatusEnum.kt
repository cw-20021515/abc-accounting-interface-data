package com.abc.us.accounting.collects.domain.type

enum class ChargeStatusEnum(val value: kotlin.String) {

    CREATED("CREATED"),
    SCHEDULED("SCHEDULED"),
    PENDING("PENDING"),
    PAID("PAID"),
    UNPAID("UNPAID"),
    OVERDUE("OVERDUE")
}

