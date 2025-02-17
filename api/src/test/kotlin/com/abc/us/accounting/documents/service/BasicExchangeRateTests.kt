package com.abc.us.accounting.documents.service

import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.TimeLogger
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.Money
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicExchangeRateTests(
    private val exchangeRateService: ExchangeRateService,
    private val timeLogger: TimeLogger = TimeLogger()
): FunSpec({

    test("basic currency converter test") {
        timeLogger.measureAndLog {
            val usdMoney = Money.of(100.0, "USD")

            logger.info("usdMoney: $usdMoney")

            val result = exchangeRateService.convertCurrency(usdMoney, "USD")
            usdMoney shouldBe result.toMoney

            val result2 = exchangeRateService.convertCurrency(usdMoney, "KRW")
            val krwMoney = result2.toMoney
            val exRateId = result2.exchangeRateId
            val exRateDate = result2.exchangeRateDate

            krwMoney.currency.currencyCode shouldBe "KRW"
            krwMoney.amount shouldBe BigDecimal(140640.0).toScale(Constants.ACCOUNTING_SCALE)

            exRateId shouldBe "4"
            exRateDate shouldBe LocalDate.of(2024, 11, 15)
            logger.info("krwMoney: $krwMoney")

            val targetCurrency = "EUR"
            val exception = shouldThrow<IllegalArgumentException> {
                exchangeRateService.convertCurrency(usdMoney, targetCurrency)
            }
            exception.message shouldBe "Exchange rate not found from ${usdMoney.currency.currencyCode} to $targetCurrency"
            logger.info ("Exception message: ${exception.message}")
        }
    }

}) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
