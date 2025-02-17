package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Embeddable
data class Money (
    val amount: BigDecimal,
    val currency: Currency
) {

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "amount='" + amount + '\'' +
                ", currency=" + currency.currencyCode + '\'' +
                '}'
    }

    fun currencyCode(): String = currency.currencyCode


    operator fun times(rate: BigDecimal): Money = convert(rate, currency.currencyCode)

    fun convert(rate: BigDecimal, toCurrency: String): Money {
        return of(amount.multiply(rate), toCurrency)
    }

    fun toScale(scale: Int = Constants.EXCHANGE_RATE_SCALE,
                roundingMode: RoundingMode = RoundingMode.valueOf(Constants.MATH_ROUNDING_MODE)): Money {
        return Money(amount.setScale(scale, roundingMode), currency)
    }


    companion object {
        fun of(amount: BigDecimal, currency: String) = Money(amount.toScale(), Currency.getInstance(currency))

        fun of(amount:Double, currency: String) = Money(BigDecimal(amount).toScale(), Currency.getInstance(currency) )

        fun of(amount:Float, currency: String) = Money(BigDecimal(amount.toDouble()).toScale(), Currency.getInstance(currency))

        fun of(amount:Long, currency: String) = Money(BigDecimal(amount).toScale(), Currency.getInstance(currency))
    }
}
