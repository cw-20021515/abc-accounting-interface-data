package com.abc.us.accounting.iface.domain.type.oms

enum class IfPromotionDiscountTargetType(val value: String) {

    SALES_PRICE("SALES_PRICE"),
    RENTAL_FEE("RENTAL_FEE"),
    INSTALLATION_FEE("INSTALLATION_FEE"),
    DISMANTLING_FEE("DISMANTLING_FEE");
    companion object {
        fun fromName(name: String): IfPromotionDiscountTargetType? = IfPromotionDiscountTargetType.entries.find { it.name == name }
    }
}