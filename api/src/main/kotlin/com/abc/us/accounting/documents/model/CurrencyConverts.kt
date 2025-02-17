package com.abc.us.accounting.documents.model

import com.abc.us.accounting.documents.domain.entity.Money
import java.time.LocalDate


data class CurrencyConversionResult(
    val fromMoney: Money,
    val toMoney: Money,
    val exchangeRateId:String?=null,
    val exchangeRateDate: LocalDate? = null
)
