package com.abc.us.accounting.logistics.domain.type

enum class MovingAverageMethod(val code: String, val description: String) {
    MONTHLY("monthly", "월말 계산"),

    TRIGGERING("TRIGGERING", "이벤트 발생 계산"),

    END("END", "끝")
}
