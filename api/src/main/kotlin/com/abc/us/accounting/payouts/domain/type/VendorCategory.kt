package com.abc.us.accounting.payouts.domain.type

enum class VendorCategory(val symbol : String, val description : String) {

    CORPORATION("C","법인 사업자"),
    SOLE_PROPRIETORSHIP("S","개인 사업자"),
    FREELANCER("F","자유 계약직");
}