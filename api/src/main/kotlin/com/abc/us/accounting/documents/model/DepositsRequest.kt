package com.abc.us.accounting.documents.model

import java.time.LocalDate

data class DepositsRequest(
    val periodType: String? = null,
    val periodFromDate: LocalDate? = null,
    val periodToDate: LocalDate? = null,
    val receiptType: String? = null,
    val receiptStatus: String? = null,
    val receiptMethod: String? = null,
    val orderId: String? = null,
    val customerId: String? = null,
    val depositId: String? = null,
    val current: Int = 1,
    val size: Int = 10
)
