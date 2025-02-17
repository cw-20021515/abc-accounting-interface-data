package com.abc.us.accounting.payouts.domain.type

enum class ApprovalStatus(val symbol: String,val description : String) {
    INIT("I","초기 상태"),
    SUBMITTED("S","승인 요청"),
    REJECTED("R","반려됨"),
    APPROVED("A","승인됨");
}