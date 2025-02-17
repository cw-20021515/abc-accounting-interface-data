package com.abc.us.accounting.iface.domain.type.oms

enum class IfMaterialFilterType(val symbol: String, val description: String) {
    NANO_TRAP("NT", "나노트랩"),
    REVERSE_OSMOSIS("RO", "RO"),
    UNKNOWN("UNKNOWN", "알수없음");
}