package com.abc.us.accounting.collects.domain.type

enum class SalesTaxType(val value: String, val description: String) {

    STATE("STATE","주"),
    CITY("CITY", "시"),
    COUNTY("COUNTY", "군"),
    SPECIAL("SPECIAL", "특별세"),
    NONE("NONE", ""),
}