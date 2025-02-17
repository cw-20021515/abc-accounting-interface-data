package com.abc.us.accounting.supports.visitor

import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.YearMonth

class YearMonthVisitor(
    private val startYearMonth: YearMonth,
    private val endYearMonth: YearMonth
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun visit(block : (LocalDateTime, LocalDateTime)->Unit) {
        require(!startYearMonth.isAfter(endYearMonth)) { "startYearMonth cannot be after endYearMonth" }
        var current = startYearMonth
        while (!current.isAfter(endYearMonth)) {
            val startOfMonth = current.atDay(1).atStartOfDay() // 월의 첫째 날의 시작 시간
            val endOfMonth = current.plusMonths(1).atDay(1).atStartOfDay() // 월의 마지막 날의 다음날 0시 0분 0초
            block(startOfMonth,endOfMonth)
            logger.debug { "Processing: $startOfMonth ~ $endOfMonth" } // 실제 로직을 여기에 추가
            current = current.plusMonths(1) // 다음 달로 이동
        }
    }
}