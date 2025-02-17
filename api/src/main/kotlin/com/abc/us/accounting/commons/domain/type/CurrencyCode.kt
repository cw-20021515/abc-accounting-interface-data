package com.abc.us.accounting.commons.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

enum class CurrencyCode(
    val code: String,
    val numericCode: String,
    val decimalPlaces: Int,
    val symbol: String,
    val engName: String
) {
    // Major Currencies
    USD("USD", "840", 2, "$", "US Dollar"),
    EUR("EUR", "978", 2, "€", "Euro"),
    JPY("JPY", "392", 0, "¥", "Japanese Yen"),
    GBP("GBP", "826", 2, "£", "British Pound Sterling"),
    KRW("KRW", "410", 0, "₩", "Korean Won"),
    CNY("CNY", "156", 2, "¥", "Chinese Yuan"),

    // Asian Currencies
    SGD("SGD", "702", 2, "S$", "Singapore Dollar"),
    HKD("HKD", "344", 2, "HK$", "Hong Kong Dollar"),
    TWD("TWD", "901", 2, "NT$", "New Taiwan Dollar"),
    THB("THB", "764", 2, "฿", "Thai Baht"),

    // Other Major Currencies
    AUD("AUD", "036", 2, "A$", "Australian Dollar"),
    CAD("CAD", "124", 2, "C$", "Canadian Dollar"),
    CHF("CHF", "756", 2, "Fr", "Swiss Franc"),
    ;

    val currency: Currency
        get() = Currency.getInstance(code)

    companion object {
        fun fromCode(code: String): CurrencyCode {
            return entries.find { it.code == code.uppercase() }
                ?: throw IllegalArgumentException("Invalid currency code: $code")
        }

        fun fromNumericCode(numericCode: String): CurrencyCode {
            return entries.find { it.numericCode == numericCode }
                ?: throw IllegalArgumentException("Invalid numeric code: $numericCode")
        }
    }

    // 통화 포맷팅 메서드들
    fun format(amount: BigDecimal): String {
        return NumberFormat.getCurrencyInstance().apply {
            currency = this@CurrencyCode.currency
        }.format(amount)
    }

    fun formatWithoutSymbol(amount: BigDecimal): String {
        return amount.setScale(decimalPlaces, RoundingMode.HALF_UP)
            .toString()
    }
}

// JPA Converter
@Converter(autoApply = true)
class CurrencyCodeConverter : AttributeConverter<CurrencyCode, String> {
    override fun convertToDatabaseColumn(attribute: CurrencyCode?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): CurrencyCode? {
        return dbData?.let { CurrencyCode.fromCode(it) }
    }
}
