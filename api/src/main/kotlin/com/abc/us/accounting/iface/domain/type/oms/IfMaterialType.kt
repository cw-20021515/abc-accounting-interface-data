package com.abc.us.accounting.iface.domain.type.oms

enum class IfMaterialType(val symbol:String, val description:String) {
    PRODUCT("PRODUCT", "제품"),
    PART("PART", "부품"),
    FILTER("FILTER", "필터"),
    CONSUMABLE("CONSUMABLE", "소모품"),
    NONE("NONE", "분류안됨"),
}
