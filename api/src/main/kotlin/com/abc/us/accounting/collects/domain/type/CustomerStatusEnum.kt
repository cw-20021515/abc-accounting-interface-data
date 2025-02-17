package com.abc.us.accounting.collects.domain.type

enum class CustomerStatusEnum(val symbol: String) {
    ACTIVE("ACTIVE"),
    CONTRACT("CONTRACT"),
    BANKRUPT("BANKRUPT"),
    DECREASED("DECREASED"),
    DECEASED("DECEASED"),
    NONE("NONE")
}
