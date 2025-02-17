package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.ExchangeRate
import com.abc.us.accounting.documents.domain.entity.Money
import com.abc.us.accounting.documents.domain.repository.ExchangeRateRepository
import com.abc.us.accounting.documents.model.CurrencyConversionResult
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class ExchangeRateService(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val cachedCurrencies:MutableList<ExchangeRate> = mutableListOf()
) {
    private val lock: Any = Any()

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        construct()
    }

    // 1시간 단위 갱신
    @Scheduled(cron = "0 0 * * * *")
    private fun reload() {
        construct()
    }

    private final fun construct(baseDate: LocalDate = LocalDate.now()) {
        synchronized(lock)  {
            try {
                val replace = exchangeRateRepository.findAllByBaseDate(baseDate)
                cachedCurrencies.clear()
                cachedCurrencies.addAll(replace)
                logger.info("construct exchange rate:${cachedCurrencies} data done, by baseDate:$baseDate, cachedCurrencies:$cachedCurrencies")
            }catch(ex:Exception){
                logger.warn("exception occurred by construct exchange rate:${ex.message}", ex)
                throw ex
            }
        }
    }

    fun getExchangeRate(fromCurrency:String, toCurrency: String, date: LocalDate=LocalDate.now()): ExchangeRate {
        if (fromCurrency == toCurrency) {
            logger.trace { "getExchangeRate, fromCurrency:$fromCurrency, toCurrency:$toCurrency - currency is same!!" }
            return ExchangeRate.of(fromCurrency)
        }
        synchronized(lock) {
            if (cachedCurrencies.isNotEmpty()) {
                val result = cachedCurrencies.find { it.fromCurrency == fromCurrency && it.toCurrency == toCurrency && it.exchangeRateDate == date }
                if (result != null) {
                    return result
                } else {
                    logger.debug { "No exchange rate for $fromCurrency to $toCurrency" }
                }
            } else {
                logger.debug { "getExchangeRate, cachedCurrencies is empty, $cachedCurrencies" }
            }
        }
        logger.debug { "getExchangeRate for $fromCurrency to $toCurrency by ExchangeRateRepository" }
        val result = exchangeRateRepository.findExchangeRate(fromCurrency, toCurrency, date)
        if(result.isEmpty) {
            throw IllegalArgumentException ( "Exchange rate not found from $fromCurrency to $toCurrency")
        }

        return result.get()
    }


    fun convertCurrency(money: Money, toCurrency: String, date:LocalDate=LocalDate.now()): CurrencyConversionResult {
        if (money.currency.currencyCode == toCurrency) {
            logger.trace { "convertCurrency, currency convert ignored by $money to $toCurrency - currency is same!!" }
            return CurrencyConversionResult(money, money)
        }
        synchronized(lock) {
            if (cachedCurrencies.isNotEmpty()) {
                val result = cachedCurrencies.find { it.fromCurrency == money.currencyCode() && it.toCurrency == toCurrency && it.exchangeRateDate == date }
                if (result != null) {
                    return result.toConversionResult(money)
                } else{
                    logger.debug { "No exchange rate found for $money to $toCurrency" }
                }
            } else {
                logger.debug { "convertCurrency, cachedCurrencies is empty, $cachedCurrencies" }
            }
        }

        logger.debug { "convert exchange rate for $money to $toCurrency, by ExchangeRateRepository" }
        val result = exchangeRateRepository.findExchangeRate(money.currency.currencyCode, toCurrency, date)
        if (result.isEmpty) {
            throw IllegalArgumentException("Exchange rate not found from ${money.currency.currencyCode} to $toCurrency")
        }
        val exchangeRate = result.get()
        return exchangeRate.toConversionResult(money)
    }
}