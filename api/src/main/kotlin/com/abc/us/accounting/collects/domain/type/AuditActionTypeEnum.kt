package com.abc.us.accounting.collects.domain.type

enum class AuditActionTypeEnum(val symbol: String) {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    ALL("ALL")
}