package com.abc.us.accounting.commons.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.*
enum class CountryCode(
    val code: String,
    val alpha3Code: String,
    val numericCode: String,
    val engName: String,
    val regionCode: String,
    val currencyCode: String,
    val dialingCode: String
) {
    // Asia
    KR("KR", "KOR", "410", "Korea, Republic of", "AS", "KRW", "82"),
    JP("JP", "JPN", "392", "Japan", "AS", "JPY", "81"),
    CN("CN", "CHN", "156", "China", "AS", "CNY", "86"),
    SG("SG", "SGP", "702", "Singapore", "AS", "SGD", "65"),
    HK("HK", "HKG", "344", "Hong Kong", "AS", "HKD", "852"),
    TW("TW", "TWN", "158", "Taiwan", "AS", "TWD", "886"),
    TH("TH", "THA", "764", "Thailand", "AS", "THB", "66"),
    VN("VN", "VNM", "704", "Vietnam", "AS", "VND", "84"),
    ID("ID", "IDN", "360", "Indonesia", "AS", "IDR", "62"),
    MY("MY", "MYS", "458", "Malaysia", "AS", "MYR", "60"),

    // North America
    US("US", "USA", "840", "United States", "NA", "USD", "1"),
    CA("CA", "CAN", "124", "Canada", "NA", "CAD", "1"),
    MX("MX", "MEX", "484", "Mexico", "NA", "MXN", "52"),

    // Europe
    GB("GB", "GBR", "826", "United Kingdom", "EU", "GBP", "44"),
    DE("DE", "DEU", "276", "Germany", "EU", "EUR", "49"),
    FR("FR", "FRA", "250", "France", "EU", "EUR", "33"),
    IT("IT", "ITA", "380", "Italy", "EU", "EUR", "39"),
    ES("ES", "ESP", "724", "Spain", "EU", "EUR", "34"),
    NL("NL", "NLD", "528", "Netherlands", "EU", "EUR", "31"),
    CH("CH", "CHE", "756", "Switzerland", "EU", "CHF", "41"),

    // Oceania
    AU("AU", "AUS", "036", "Australia", "OC", "AUD", "61"),
    NZ("NZ", "NZL", "554", "New Zealand", "OC", "NZD", "64"),

    // South America
    BR("BR", "BRA", "076", "Brazil", "SA", "BRL", "55"),
    AR("AR", "ARG", "032", "Argentina", "SA", "ARS", "54"),
    CL("CL", "CHL", "152", "Chile", "SA", "CLP", "56");

    // Locale 생성
    fun toLocale(): Locale = Locale.of(code)

    // Currency 생성
    fun getCurrency(): Currency = Currency.getInstance(currencyCode)

    companion object {
        fun fromCode(code: String): CountryCode {
            return entries.find { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid country code: $code")
        }

        fun fromAlpha3Code(alpha3Code: String): CountryCode {
            return entries.find { it.alpha3Code.equals(alpha3Code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid alpha-3 code: $alpha3Code")
        }

        fun fromNumericCode(numericCode: String): CountryCode {
            return entries.find { it.numericCode == numericCode }
                ?: throw IllegalArgumentException("Invalid numeric code: $numericCode")
        }

        fun byRegion(regionCode: String): List<CountryCode> {
            return entries.filter { it.regionCode.equals(regionCode, ignoreCase = true) }
        }
    }
}

// JPA Converter
@Converter(autoApply = true)
class CountryCodeConverter : AttributeConverter<CountryCode, String> {
    override fun convertToDatabaseColumn(attribute: CountryCode?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): CountryCode? {
        return dbData?.let { CountryCode.fromCode(it) }
    }
}
