package com.abc.us.accounting.logistics.domain.type

enum class MovementCategory(val code: String, val description: String) {
    //PAYOUT("PAYOUT","미지급금"),
    //BEGINNING("BEGINNING", "기초"),
    //ENDING("ENDING", "기말"),

    //PURCHASE("PURCHASE", "구매"),
    //INBOUND("INBOUND", "입고"),
    //OUTBOUND("OUTBOUND", "출고"),
    //INVENTORY("INVENTORY", "재고"),
    //TRANSFER("TRANSFERS", "운송"),
    //OTHER("OTHER", "기타")

    INBOUND("INBOUND", "입고"),
    OUTBOUND("OUTBOUND", "출고"),
    IN_TRANSIT("IN_TRANSIT", "운송중")
}

