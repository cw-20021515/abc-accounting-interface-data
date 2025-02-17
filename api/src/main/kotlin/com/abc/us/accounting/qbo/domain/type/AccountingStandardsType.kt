package com.abc.us.accounting.qbo.domain.type

enum class AccountingStandardsType(val code: String, val description: String) {
    GAAP("GAAP", "일반 회계 원칙"),  // 일반 회계 원칙
    IFRS("IFRS", "국제회계기준"),
    // 국제회계기준
}
