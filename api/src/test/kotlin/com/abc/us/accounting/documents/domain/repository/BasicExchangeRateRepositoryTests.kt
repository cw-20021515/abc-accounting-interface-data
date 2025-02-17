package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate


//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ImportAutoConfiguration(exclude = [SecurityAutoConfiguration::class, WebMvcAutoConfiguration::class])
//@Import(MockBeanHandler::class)
@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicExchangeRateRepositoryTests(
    private val exchangeRateRepository: ExchangeRateRepository
) : AnnotationSpec() {

    @Test
    fun `basic test with exchange rate`() {
        val exchangeRates = exchangeRateRepository.findAll()
        logger.info("exchangeRates size:${exchangeRates.size}")
        exchangeRates.size shouldBe 4

        val result = exchangeRateRepository.findExchangeRate("USD", "KRW", LocalDate.of(2024, 11, 12))
        result.isPresent shouldBe true

        val exchangeRate = result.get()
        logger.info("exchangeRate:$exchangeRate")
        exchangeRate shouldNotBe null

        exchangeRate.fromCurrency shouldBe "USD"
        exchangeRate.toCurrency shouldBe "KRW"
        exchangeRate.exchangeRateDate shouldBe LocalDate.of(2024, 11, 1)
        exchangeRate.exchangeRate shouldBe BigDecimal("1379.3").toScale(12)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

}