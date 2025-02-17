package com.abc.us.accounting.payouts.domain.type

enum class CostCenterCategory(val value : String, val description : String) {
    FUNCTION("FUNCTION", "기능별 구분"),
    RESPONSIBILITY("RESPONSIBILITY", "책임 수준별 구분"),
    PROCESS("PROCESS", "프로세스 활동 기반 구분"),
    PROFITABILITY("PROFITABILITY", "투자 및 수익 기여도별 구분"),
    PROJECT("PROJECT", "프로젝트별 구분");
}