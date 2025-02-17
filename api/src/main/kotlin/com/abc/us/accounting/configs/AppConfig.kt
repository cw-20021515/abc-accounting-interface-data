package com.abc.us.accounting.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.math.RoundingMode

@Configuration
@ConfigurationProperties(prefix = "abc.accounting")
data class AppConfig (
    var name: String = "",
    var version: String = ""
){

}

@Configuration
@ConfigurationProperties(prefix = "abc.accounting.math")
data class MathConfig (
    var precision: Int = Constants.ACCOUNTING_PRECISION,
    var scale: Int = Constants.ACCOUNTING_SCALE,
    var ratioScale: Int = Constants.RATIO_SCALE,
    var percentageScale:Int = Constants.PERCENTAGE_SCALE,
    var exchangeRateScale:Int = Constants.EXCHANGE_RATE_SCALE,
    var qboScale:Int = Constants.QBO_SCALE,
    val roundingMode: String = Constants.MATH_ROUNDING_MODE
){
    fun getRoundingMode(): RoundingMode {
        return RoundingMode.valueOf(roundingMode)
    }
}

@Configuration
@ConfigurationProperties(prefix = "abc.accounting.salestax")
data class SalesTaxConfig (
    var salesTaxTolerance : BigDecimal = Constants.SALES_TAX_TOLERANCE,
    var ignoreToleranceException: Boolean = true,
)

@Configuration
@ConfigurationProperties(prefix = "abc.accounting.documents.persist")
data class DocumentsConfig (
    var enable : Boolean = false
)


@Configuration
@ConfigurationProperties(prefix = "abc.accounting.account-balance")
data class AccountBalanceConfig (
    var enable : Boolean = false
)


@Configuration
@ConfigurationProperties(prefix = "abc.accounting.rentals.onetime")
data class OnetimeConfig (
    var enableTaxline: Boolean = false,
)
