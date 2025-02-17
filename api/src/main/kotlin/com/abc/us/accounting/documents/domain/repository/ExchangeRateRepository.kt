package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.ExchangeRate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface ExchangeRateRepository: JpaRepository<ExchangeRate, String> {

    @Query(value = """
        select a 
        from ExchangeRate a 
        where a.exchangeRateDate = (
            select max(r.exchangeRateDate)
            from ExchangeRate r
            where r.exchangeRateDate <= :baseDate
        ) and a.fromCurrency = :fromCurrency and a.toCurrency = :toCurrency
    """
    )
    fun findExchangeRate(fromCurrency:String, toCurrency:String, baseDate: LocalDate = LocalDate.now()):Optional<ExchangeRate>

    @Query(value = """
        select a 
        from ExchangeRate a 
        where a.exchangeRateDate = (
            select max(r.exchangeRateDate)
            from ExchangeRate r
            where r.exchangeRateDate <= :baseDate
        )
    """
    )
    fun findAllByBaseDate(baseDate: LocalDate = LocalDate.now()):List<ExchangeRate>
}