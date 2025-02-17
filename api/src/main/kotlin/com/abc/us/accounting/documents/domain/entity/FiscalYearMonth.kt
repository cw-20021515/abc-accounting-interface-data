package com.abc.us.accounting.documents.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate
import java.time.YearMonth

@Embeddable
data class FiscalYearMonth(
    @Column(name = "fiscal_year")
    val year: Int,

    @Column(name = "fiscal_month")
    val month: Int
): Comparable<FiscalYearMonth> {
    init {
        require(month in 1..12) { "회계기간은 1에서 12 사이여야 합니다." }
    }

    override fun toString() = String.format("%04d-%02d", year, month)


    companion object {
        fun of(year: Int, month: Int): FiscalYearMonth {
            return FiscalYearMonth(year, month)
        }

        fun parse(fiscalYearMonth: String): FiscalYearMonth {
            val pattern = """(\d{4})-(\d{2})""".toRegex()
            val matchResult = pattern.matchEntire(fiscalYearMonth)
                ?: throw IllegalArgumentException("유효하지 않은 회계연도/기간 형식입니다. (예: 202401)")

            val (year, period) = matchResult.destructured
            return of(year.toInt(), period.toInt())
        }

        fun from(yearMonth: YearMonth, fiscalRule: FiscalRule): FiscalYearMonth {
            return fiscalRule.from(yearMonth)
        }

        fun from (date: LocalDate, fiscalRule: FiscalRule): FiscalYearMonth {
            return fiscalRule.from(date)
        }
    }

    fun toYearMonth(fiscalRule: FiscalRule):YearMonth{
        return fiscalRule.toYearMonth(this)
    }

    /**
     * 다음 회계기간을 반환
     */
    fun next(): FiscalYearMonth {
        return if (month == 12) {
            of(year + 1, 1)
        } else {
            of(year, month + 1)
        }
    }

    /**
     * 이전 회계기간을 반환
     */
    fun previous(): FiscalYearMonth {
        return if (month == 1) {
            of(year - 1, 12)
        } else {
            of(year, month - 1)
        }
    }

    /**
     * 특정 기간만큼 이동한 회계기간을 반환
     */
    fun plusMonths(months: Int): FiscalYearMonth {
        val totalFiscalMonths = year * 12 + (month - 1) + months
        val newFiscalYear = totalFiscalMonths / 12
        val newFiscalMonth = (totalFiscalMonths % 12) + 1
        return of(newFiscalYear, newFiscalMonth)
    }

    /**
     * 두 회계기간 사이의 기간 차이를 반환
     */
    fun monthsBetween(other: FiscalYearMonth): Int {
        return ((other.year * 12 + other.month) - (this.year * 12 + this.month))
    }

    /**
     * 특정 회계기간이 현재 회계기간 범위 내에 있는지 확인
     */
    fun contains(other: FiscalYearMonth): Boolean {
        return !this.isAfter(other) && !this.isBefore(other)
    }

    fun isBefore(other: FiscalYearMonth): Boolean {
        return this.compareTo(other) < 0
    }

    fun isAfter(other: FiscalYearMonth): Boolean {
        return this.compareTo(other) > 0
    }

    override fun compareTo(other: FiscalYearMonth): Int {
        return when {
            year != other.year -> year.compareTo(other.year)
            else -> month.compareTo(other.month)
        }
    }

    /**
     * 회계기간을 yyyyMM 형식의 문자열로 반환
     */
    fun toFiscalYearMonthString(): String {
        return String.format("%d%02d", year, month)
    }

    fun toLocalDateAtDay(fiscalRule: FiscalRule, dayOfMonth:Int = 1): LocalDate {
        val yearMonth = toYearMonth(fiscalRule)
        return yearMonth.atDay(dayOfMonth)
    }

}
