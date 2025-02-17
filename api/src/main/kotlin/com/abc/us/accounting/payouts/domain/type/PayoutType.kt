package com.abc.us.accounting.payouts.domain.type

enum class PayoutType(val symbol : String, val description : String) {

    VENDOR("V","업체 비용"),
    EMPLOYEE("E","개인 비용"),
}