package com.abc.us.accounting.iface.domain.type.oms

enum class IfMaterialFeatureCode(val symbol: String, val description: String) {
    COLD_PURIFIED("CP", "냉정"),
    COLD_HOT_PURIFIED("CHP","냉온정"),
    COLD_PURIFIED_ICE("CPI", "냉정얼음"),
    COLD_HOT_PURIFIED_ICE("CHPI", "냉온정얼음"),
    COLD_HOT_PURIFIED_ICE_SPARKLING("CHPIS", "냉온정얼음"),
    HOT_PURIFIED("HP", "온정"),
    PURIFIED("P", "정"),
    UNKNOWN("UNKNOWN", "알수없음");
}
