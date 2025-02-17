package com.abc.us.accounting.rentals.master.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


enum class MaterialType(val symbol:String, val description:String) {
    PRODUCT("PRODUCT", "제품"),
    PART("PART", "부품"),
    FILTER("FILTER", "필터"),
    CONSUMABLE("CONSUMABLE", "소모품"),
}

enum class MaterialCategoryCode (val symbol:String, val materialType: MaterialType) {
    WATER_PURIFIER("WP", MaterialType.PRODUCT),
    WATER_PURIFIER_FILTER("WPF", MaterialType.FILTER),
    WATER_PURIFIER_PART("WPP", MaterialType.PART),
    WATER_PURIFIER_CONSUMABLE("WPC", MaterialType.CONSUMABLE),
}

enum class InstallationType(val symbol: String, val description: String) {
    COUNTER_TOP("CT", "카운터탑"),
    BUILT_IN("BI", "빌트인"),
    FREE_STAND("FS", "스탠트")
}

enum class FilterType(val symbol: String, val description: String) {
    NANO_TRAP("NT", "나노트랩"),
    REVERSE_OSMOSIS("RO", "RO"),
}

enum class FeatureCode(val symbol: String, val description: String) {
    COLD_PURIFIED("CP", "냉정"),
    COLD_HOT_PURIFIED("CHP","냉온정"),
    COLD_PURIFIED_ICE("CPI", "냉정얼음"),
    COLD_HOT_PURIFIED_ICE("CHPI", "냉온정얼음"),
    COLD_HOT_PURIFIED_ICE_SPARKLING("CHPIS", "냉온정얼음"),
    HOT_PURIFIED("HP", "온정"),
    PURIFIED("P", "정")
}

