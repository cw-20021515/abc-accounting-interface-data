package com.abc.us.accounting.collects.domain.type

enum class ItemCreateCategoryEnum(val symbol: String, val description: String) {
    RENTAL("R", "렌탈 공통"),
    PURCHASE("P", "일시불"),
    MEMBERSHIP("M", "멤버십"),
    OPERATING_LEASE("OL", "운용리스"),
    FINANCIAL_LEASE("FL", "금융리스"),
    AFTER_SERVICE("AS", "고객서비스"),
    PURCHASE_CONSUME("PC", "소모품 구매"),
    PURCHASE_FILTER("PF", "필터"),
    PURCHASE_PRODUCT("PRD", "제품"),
    PURCHASE_PART("PART", "부품"),
    NONE("N", "기타")
}
