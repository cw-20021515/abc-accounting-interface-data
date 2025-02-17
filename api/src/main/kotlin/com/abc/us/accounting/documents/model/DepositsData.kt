package com.abc.us.accounting.documents.model

import java.math.BigDecimal
import java.time.LocalDate

data class DepositsData(
    val receiptDate: LocalDate? = null,
    val receiptId: String? = null,
    val receiptType: String? = null,
    val receiptStatus: String? = null,
    val receiptProvider: String? = null,
    val receiptMethod: String? = null,
    val currency: String? = null,
    val receiptAmount: BigDecimal? = null,
    val fee: BigDecimal? = null,
    val depositAmount: BigDecimal? = null,
    val differenceAmount: BigDecimal? = null,
    val depositDate: LocalDate? = null,
    val depositId: String? = null,
    val billId: String? = null,
    val orderId: String? = null,
    val customerId: String? = null,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val remark: String? = null
)
