package com.abc.us.accounting.logistics.domain.type

enum class CostHistory(val code: String, val description: String) {
    COST_HISTORY_INCLUDE("COST_HISTORY_INCLUDE", "포함"),
    COST_HISTORY_EXCLUDE("COST_HISTORY_EXCLUDE", "미포함"),
}