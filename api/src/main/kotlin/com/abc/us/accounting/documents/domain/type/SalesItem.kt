package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionCode

/**
 * 판매항목
 */
enum class SalesItem (val code:String, val engName:String, val korName:String, val group:SalesItemGroup) {
    TOTAL(RentalDistributionCode.T01.name, "Total", "전체", SalesItemGroup.TOTAL),
    PRODUCT(RentalDistributionCode.M01.name, "Product", "재화", SalesItemGroup.PRODUCT),
    REPLACEMENT_1(RentalDistributionCode.R01.name, "Replacement 1", "교체1", SalesItemGroup.REPLACEMENT),
    REPLACEMENT_2(RentalDistributionCode.R02.name, "Replacement 2", "교체2", SalesItemGroup.REPLACEMENT),
    REPLACEMENT_3(RentalDistributionCode.R03.name, "Replacement 3", "교체3", SalesItemGroup.REPLACEMENT),
    SERVICE(RentalDistributionCode.S01.name, "Service", "서비스", SalesItemGroup.SERVICE),

    INTEREST_INCOME("I01", "Interest Income", "이자수익", SalesItemGroup.FEE),

    REGISTRATION_FEE("F01", "Registration Fee", "등록비", SalesItemGroup.FEE),
    INSTALLATION_FEE("F02", "Installation Fee", "설치비", SalesItemGroup.FEE),
    DISMANTLING_FEE("F03", "Dismantling Fee", "해체비", SalesItemGroup.FEE),

    LATE_FEE("P01", "Late Fee", "연체료", SalesItemGroup.PENALTY),
    PENALTY_FEE("P02", "Penalty Fee", "위약금", SalesItemGroup.PENALTY),
    LOST_FEE("P03", "Lost Fee", "분실료", SalesItemGroup.PENALTY)
}

enum class SalesItemGroup (val code:String, val engName:String, val korName:String) {
    TOTAL("T", "Total", "전체(안분)"),
    PRODUCT("M", "Product", "재화"),
    REPLACEMENT("R", "Replacement", "교체"),
    SERVICE("S", "Service", "서비스"),
    FEE("F", "Fee", "수수료"),
    PENALTY("P", "Penalty", "벌금"),
}
