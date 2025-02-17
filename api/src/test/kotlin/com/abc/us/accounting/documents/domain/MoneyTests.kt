package com.abc.us.accounting.documents.domain

import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.TimeLogger
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.Money
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class MoneyTests (
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec() {


    @Test
    fun `basic usd to krw`() {
        val usdMoney = Money.of(100.0, "USD").toScale(Constants.ACCOUNTING_SCALE)

        val toKRWRate = BigDecimal(1406.40).toScale(Constants.EXCHANGE_RATE_SCALE)
        val krwMoney = usdMoney.convert(toKRWRate, "KRW").toScale(Constants.ACCOUNTING_SCALE)

        usdMoney.amount shouldBe BigDecimal(100.0).toScale(Constants.ACCOUNTING_SCALE)
        usdMoney.currency shouldBe Currency.getInstance("USD")

        krwMoney.amount shouldBe BigDecimal(140640.0).toScale(Constants.ACCOUNTING_SCALE)
        krwMoney.currency shouldBe Currency.getInstance("KRW")
    }

    @Test
    fun `basic krw to usd`() {
        val krwMoney = Money.of(1000, "KRW").toScale(Constants.ACCOUNTING_SCALE)

        val toUSDRate = BigDecimal(0.00071103527).toScale(Constants.EXCHANGE_RATE_SCALE)
        logger.info("toUSDRate:$toUSDRate")
        val usdMoney = krwMoney.convert(toUSDRate, "USD").toScale(Constants.ACCOUNTING_SCALE)

        krwMoney.amount shouldBe BigDecimal(1000.0).toScale(Constants.ACCOUNTING_SCALE)
        krwMoney.currency shouldBe Currency.getInstance("KRW")

        usdMoney.amount shouldBe BigDecimal(0.7110).toScale(Constants.ACCOUNTING_SCALE)
        usdMoney.currency shouldBe Currency.getInstance("USD")

    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

}
