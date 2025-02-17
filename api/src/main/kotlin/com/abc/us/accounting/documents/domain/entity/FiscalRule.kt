package com.abc.us.accounting.documents.domain.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Embeddable
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

/**
 * 회계연도 규칙
 */
@Embeddable
data class FiscalRule (
    @Convert(converter = MonthConverter::class)
    val startMonth: Month = Month.JANUARY  // 회계연도 시작월 (예: 4월 = 4)
) {
    companion object{
        val DEFAULT = FiscalRule()
    }

    fun from(date: LocalDate): FiscalYearMonth {
        return FiscalYearMonth.of(getFiscalYear(date), getFiscalMonth(date))
    }

    fun from(yearMonth: YearMonth): FiscalYearMonth {
        val fiscal = yearMonth.minusMonths(startMonth.value.toLong() - 1)
        return FiscalYearMonth.of(fiscal.year, fiscal.monthValue)
    }

    fun toYearMonth(fiscalYearMonth: FiscalYearMonth): YearMonth {
        val yearMonth = YearMonth.of(fiscalYearMonth.year, fiscalYearMonth.month)
        return yearMonth.plusMonths(startMonth.value.toLong() - 1)
    }

    fun getFiscalYear(date: LocalDate): Int {
        val yearMonth = YearMonth.from(date)
        val fiscalYearMonth = yearMonth.minusMonths(startMonth.value.toLong() - 1) // 4월이 시작월인 경우 4월부터 3월까지의 yearMonth를
        return fiscalYearMonth.year
    }

    fun getFiscalMonth(date: LocalDate): Int {
        val yearMonth = YearMonth.from(date)
        val fiscalYearMonth = yearMonth.minusMonths(startMonth.value.toLong() - 1) // 4월이 시작월인 경우 4월부터 3월까지의 yearMonth를

        return fiscalYearMonth.monthValue
    }

    fun getFiscalYearRange(fiscalYear: Int): Pair<LocalDate, LocalDate> {
        val start = LocalDate.of(fiscalYear, startMonth, 1)
        val end = start.plusYears(1).minusDays(1)
        return Pair(start, end)
    }
}


@Converter(autoApply = true)
class MonthConverter : AttributeConverter<Month, Int> {
    override fun convertToDatabaseColumn(month: Month?): Int? {
        return month?.value
    }

    override fun convertToEntityAttribute(dbData: Int?): Month? {
        return dbData?.let { Month.of(it) }
    }
}