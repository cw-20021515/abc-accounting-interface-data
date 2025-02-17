package com.abc.us.accounting.supports.mapper.enums

enum class OffsetDateTimeField(var date:String, var time:String) {
    BUDGET_USAGE_DATE("budgetUsageDate", "budgetUsageTime"),
    DOCUMENT_DATE("documentDate", "documentTime")
}