package com.abc.us.accounting.documents.domain

import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.entity.FiscalRule
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class FiscalTests (
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec() {

    @Test
    fun `basic fiscal year and month test`() {
        // normal case (1~12월 회계기간)
        val fiscalRule = FiscalRule()
        val date = LocalDate.of(2021, 4, 1)
        val fiscalYear = fiscalRule.getFiscalYear(date)
        val fiscalMonth = fiscalRule.getFiscalMonth(date)
        val fiscalYearMonth = fiscalRule.from(date)
        val fiscalYearRange = fiscalRule.getFiscalYearRange(fiscalYear)

        val expectedFiscalYear = 2021
        val expectedFiscalMonth = 4
        fiscalYear shouldBe expectedFiscalYear
        fiscalMonth shouldBe expectedFiscalMonth
        fiscalYearMonth shouldBe FiscalYearMonth(expectedFiscalYear, expectedFiscalMonth)

        fiscalYearRange shouldBe Pair(LocalDate.of(expectedFiscalYear, 1, 1), LocalDate.of(expectedFiscalYear, 12, 31))

    }

    @Test
    fun `april fiscal year and month test`() {
        // 4월 회계법인 (4월 ~ 3월 회계기간)
        val fiscalRule = FiscalRule(startMonth = Month.APRIL)

        val date = LocalDate.of(2021, 4, 1)
        val fiscalYear = fiscalRule.getFiscalYear(date)
        val fiscalMonth = fiscalRule.getFiscalMonth(date)
        val fiscalYearMonth = fiscalRule.from(date)
        val fiscalYearRange = fiscalRule.getFiscalYearRange(fiscalYear)

        fiscalYear shouldBe 2021
        fiscalMonth shouldBe 1
        fiscalYearMonth shouldBe FiscalYearMonth(2021, 1)
        fiscalYearRange shouldBe Pair(LocalDate.of(2021, 4, 1), LocalDate.of(2022, 3, 31))

    }

    @Test
    fun `april fiscal year and month test2`() {
        // 4월 회계법인 (4월 ~ 3월 회계기간)
        val fiscalRule = FiscalRule(startMonth = Month.APRIL)

        run {
            val date = LocalDate.of(2021, 4, 1)
            val yearMonth = YearMonth.from(date)
            val fiscalYear = fiscalRule.from(yearMonth)

            yearMonth.year shouldBe 2021
            yearMonth.monthValue shouldBe 4
            fiscalYear shouldBe FiscalYearMonth(2021, 1)
            fiscalRule.toYearMonth(fiscalYear) shouldBe yearMonth
        }

        run {
            val date = LocalDate.of(2021, 1, 1)
            val yearMonth = YearMonth.from(date)
            val fiscalYear = fiscalRule.from(yearMonth)

            yearMonth.year shouldBe 2021
            yearMonth.monthValue shouldBe 1
            fiscalYear shouldBe FiscalYearMonth(2020, 10)
            fiscalRule.toYearMonth(fiscalYear) shouldBe yearMonth
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}