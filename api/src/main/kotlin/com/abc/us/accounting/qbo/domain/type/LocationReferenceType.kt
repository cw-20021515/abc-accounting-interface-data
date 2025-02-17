package com.abc.us.accounting.qbo.domain.type


enum class LocationReferenceType(val symbol: String) {
    //    TAX("TAX"),
    VENDOR("VENDOR"),  // 협력 업체
    CUSTOMER("CUSTOMER"),  // 구매 고객
    SALES("SALES"),  // 판매처
    COMPANY("COMPANY"),
    // 미신사 법인
}
