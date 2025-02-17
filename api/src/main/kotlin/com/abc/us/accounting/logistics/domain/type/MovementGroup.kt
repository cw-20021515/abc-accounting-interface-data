package com.abc.us.accounting.logistics.domain.type

enum class MovementGroup(val code: String, val description: String) {
    //INVENTORY("INVENTORY", "재고"),

    //PURCHASE("PURCHASE", "구매"),
    //SALES("SALES", "판매"),

    //OTHER("OTHER", "기타"),

    //SERVICE_ORDER("OUTBOUND_SERVICE_ORDER", "서비스오더"),  //OUTBOUND_OTHER("OUTBOUND_OTHER", "기타 출고"),

    //IN_TRANSIT("IN_TRANSIT", "운송중"),

    //VALUATION("VALUATION", "원가계산"),

    //ENDING_INVENTORY("ENDING_INVENTORY", "기말재고"),
    //END("END", "끝")

    INBOUND_PURCHASE("INBOUND_PURCHASE", "구매 입고"),
    INBOUND_RETURN("INBOUND_RETURN", "반환 입고"),
    INBOUND_TRANSFER("INBOUND_TRANSFER", "이동 입고"),
    INBOUND_ADJUSTMENT("INBOUND_ADJUSTMENT", "조정 입고"),
    INBOUND_OTHER("INBOUND_OTHER", "기타 입고"),

    OUTBOUND_SALES("OUTBOUND_SALES", "판매 출고"),
    OUTBOUND_RETURN("OUTBOUND_RETURN", "반품 출고"),
    OUTBOUND_TRANSFER("OUTBOUND_TRANSFER", "이동 출고"),
    OUTBOUND_SCRAP("OUTBOUND_SCRAP", "폐기 출고"),
    OUTBOUND_CONSUMABLE("OUTBOUND_CONSUMABLE", "소모품 출고"),
    OUTBOUND_OTHER("OUTBOUND_OTHER", "기타 출고"),

    IN_TRANSIT("IN_TRANSIT", "운송중")
}
