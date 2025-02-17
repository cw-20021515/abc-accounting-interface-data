package com.abc.us.accounting.iface.domain.type.oms

enum class IfMaterialInstallationType(val symbol: String, val description: String) {
    COUNTER_TOP("CT", "카운터탑"),
    BUILT_IN("BI", "빌트인"),
    FREE_STAND("FS", "스탠트"),
    UNKNOWN("UNKNOWN", "알수없음");
}