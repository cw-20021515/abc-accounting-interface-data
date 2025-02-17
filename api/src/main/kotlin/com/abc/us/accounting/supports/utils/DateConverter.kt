package com.abc.us.accounting.supports.utils

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object DateConverter {
    /**
     * 1. Date를 LocalDate로 변환
     */
    fun Date.toLocalDate(): LocalDate =
        this.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    /**
     * LocalDate 확장 함수
     */
    fun LocalDate.toDate(): Date =
        Date.from(
            this.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
        )


}