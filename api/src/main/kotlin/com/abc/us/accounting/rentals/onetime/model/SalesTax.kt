package com.abc.us.accounting.rentals.onetime.model

import com.abc.us.accounting.config.AppConfig
import com.abc.us.accounting.config.SalesTaxConfig
import com.abc.us.accounting.iface.domain.model.SalesTaxType
import com.abc.us.accounting.iface.domain.model.TaxLine
import com.abc.us.accounting.supports.utils.BigDecimals.diff
import com.abc.us.accounting.supports.utils.BigDecimals.equalsWithScale
import com.abc.us.accounting.supports.utils.buildToString
import mu.KotlinLogging
import java.math.BigDecimal

data class SalesTax (
    val total: BigDecimal,
    val state: BigDecimal,
    val county: BigDecimal,
    val city: BigDecimal,
    val special: BigDecimal,
    val taxLines: List<TaxLine>?=null
){

    override fun toString(): String {
        return buildToString {
            add(
                "total" to total,
                "state" to state,
                "county" to county,
                "city" to city,
                "special" to special,
                "taxLines" to taxLines
            )
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * 판매세 계산
         * 단수차 보정로직 추가
         */
        fun of (config: SalesTaxConfig, tax:BigDecimal, taxLines:List<TaxLine>? = null):SalesTax {
            val stateTax = taxLines?.find { it.getTaxLineType() == SalesTaxType.STATE }?.price ?: BigDecimal.ZERO
            val countyTax = taxLines?.find { it.getTaxLineType() == SalesTaxType.COUNTY }?.price ?: BigDecimal.ZERO
            val cityTax = taxLines?.find { it.getTaxLineType() == SalesTaxType.CITY }?.price ?: BigDecimal.ZERO
            val sum = taxLines?.sumOf { it.price } ?: BigDecimal.ZERO

            val diff = tax.diff(sum)
            if ( diff != BigDecimal.ZERO ) {
                logger.warn{"salesTax diff:$diff, tax:$tax is not match with sum:$sum of taxLines:$taxLines"}

                if (!config.ignoreToleranceException) {
                    require(diff <= config.salesTaxTolerance ) {
                        "exceed salesTax tolerance limit(${config.salesTaxTolerance}), diff:$diff by tax:$tax, sum:$sum of taxLines:$taxLines"
                    }
                }
            }
            val specialTax = tax.minus(stateTax).minus(countyTax).minus(cityTax)

            return SalesTax(tax, stateTax, countyTax, cityTax, specialTax, taxLines)
        }
    }
}
