package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.model.CurrencyConversionResult
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


@Entity
@Table(name = "exchange_rate")
class ExchangeRate (
    @Id
    val id: String,

    @Column(name = "from_currency", nullable = false)
    val fromCurrency: String,

    @Column(name = "to_currency", nullable = false)
    val toCurrency: String,

    @Column(name = "exchange_rate_date", nullable = false)
    val exchangeRateDate: LocalDate,

    @Column(name = "exchange_rate", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.EXCHANGE_RATE_SCALE)
    val exchangeRate: BigDecimal,

    @Column(name = "create_time", nullable = false, updatable = false)
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime = OffsetDateTime.now()
) {

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                ", id=" + id + '\'' +
                ", fromCurrency=" + fromCurrency + '\'' +
                ", toCurrency=" + toCurrency + '\'' +
                ", exchangeRate=" + exchangeRate + '\'' +
                ", exchangeRateDate=" + exchangeRateDate + '\'' +
                ", createTime=" + createTime + '\'' +
                ", updateTime=" + updateTime + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is ExchangeRate) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(fromCurrency, other.fromCurrency)
            .append(toCurrency, other.toCurrency)
            .append(exchangeRateDate, other.exchangeRateDate)
            .append(exchangeRate, other.exchangeRate)
            .append(createTime, other.createTime)
            .append(updateTime, other.updateTime)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(fromCurrency)
            .append(toCurrency)
            .append(exchangeRateDate)
            .append(exchangeRate)
            .append(createTime)
            .append(updateTime)
            .toHashCode()
    }

    fun convert(money: Money): Money {
        val exchangeRateValue = exchangeRate.toScale(Constants.EXCHANGE_RATE_SCALE)
        val convertedMoney= money.convert(exchangeRateValue, toCurrency).toScale(Constants.ACCOUNTING_SCALE)

        return convertedMoney
    }

    fun toConversionResult(money: Money): CurrencyConversionResult {
        require( fromCurrency == money.currencyCode()) { "Invalid from currency, exchangeRage:${fromCurrency}, money:${money.currencyCode()}" }

        val exchangeRateId = id
        val exchangeRateDate = exchangeRateDate
        val convertedMoney = convert(money)

        return CurrencyConversionResult(money, convertedMoney, exchangeRateId, exchangeRateDate)
    }

    companion object    {
        fun of(currency:String): ExchangeRate {
            return ExchangeRate(
                id = "0",
                fromCurrency = currency,
                toCurrency = currency,
                exchangeRateDate = LocalDate.now(),
                exchangeRate = BigDecimal(1.0).toScale(Constants.EXCHANGE_RATE_SCALE)
            )
        }
    }

}
