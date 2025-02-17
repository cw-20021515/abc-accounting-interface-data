package com.abc.us.accounting.collects.domain.type


enum class MaterialType(val symbol:String, val description:String) {
    PRODUCT("PRODUCT", "제품"),
    PART("PART", "부품"),
    FILTER("FILTER", "필터"),
    CONSUMABLE("CONSUMABLE", "소모품"),
    NONE("NONE", "분류안됨"),
}

enum class MaterialCategoryCode (val symbol:String, val materialType: MaterialType) {
    WATER_PURIFIER("WP", MaterialType.PRODUCT),
    WATER_PURIFIER_FILTER("WPF", MaterialType.FILTER),
    WATER_PURIFIER_PART("WPP", MaterialType.PART),
    WATER_PURIFIER_CONSUMABLE("WPC", MaterialType.CONSUMABLE),
    NONE("NONE", MaterialType.NONE),
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

    PURIFIED("PURIFIED", "정"),
    FAUCET("FAUCET",""),
    PUMP("PUMP",""),
    VALVE("VALVE",""),
    TUBING("TUBING",""),
    FITTING("FITTING",""),
    REED("REED",""),
    BIMETAL("BIMETAL",""),
    CONNECTORS("CONNECTORS",""),
    DESIGN("DESIGN",""),

    COLD_PURIFIED("CP", "냉정"),
    COLD_HOT_PURIFIED("CHP","냉온정"),
    COLD_HOT_PURIFIED_ICE("CHPI", "냉온정얼음"),
    COLD_HOT_PURIFIED_ICE_SPARKLING("CHPIS", "냉온정얼음"),
    HOT_PURIFIED("HP", "온정"),
    POWER_BOARD_ASSEMBLY("PBA","POWER_BOARD_ASSEMBLY"),
    SEDIMENT_REMOVAL("SR",""),
    MICRO_PARTICLE_REMOVAL("MPR",""),
    CHLORINE_REMOVAL("CR",""),
    PESTICIDE_REMOVAL("PR",""),
    VOC_REMOVAL("VOCR",""),
    HEAVY_METAL_REMOVAL("HMR",""),
    BACTERIA_VIRUS_REMOVAL("BVR",""),
    MINERAL_MAINTENANCE("MM",""),
    MINERAL_ADDITION("MA",""),
    MOTOR_ASSEMBLY("MA",""),

    PRESSURE_SWITCH("PS",""),
    POWER_SAVING("PS",""),

    FLOW_RESTRICTOR("FR",""),
    FILTER_REPLACEMENT("FR",""),

    ANTIOXIDANT_EFFECT("AE",""),
    PH_BALANCE("PHB",""),
    TASTE_IMPROVEMENT("TI",""),
    WATER_SOFTENING("WS",""),
    WATER_TANK("WT",""),
    WIFI_ASSEMBLY("WA",""),


    WATER_LEAK_DETECTOR("WLD",""),
    ELECTRONIC_CONTROL_UNIT("ECU",""),
    SENSOR_ASSEMBLY("SA",""),

    DISPLAY_PANEL("DP",""),
    HEATING_ELEMENT("HE",""),
    COOLING_UNIT("CU",""),

    CLEANING_KIT("CK",""),
    UV_LAMP("UVL",""),
    FILTER_O_RING("FOR",""),
    SEDIMENT_FILTER_CARTRIDGE("SFC",""),
    ACTIVATED_CARBON_CARTRIDGE("ACC",""),
    IRON_SILVER("IS",""),
    PEBBLE_GRAY("PG",""),
    BRONZE_BEIGE("BB",""),
    ICY_BLUE("IB",""),
    QUARTZ_BROWN("QB",""),
    PORCELAIN_WHITE("PW",""),
    MYSTIC_GRAY("MG",""),
    SAND_BEIGE("SB",""),
    TERRACOTTA_PINK("TP",""),
    HAZY_BLUE("HB",""),
    SELF_STERILIZATION("SS",""),
    PUMP_TYPE("PT",""),
    GENERAL_TYPE("GT",""),
    NOT_IN_USE("NIU",""),
    COMPACT_SIZE("CS",""),
    IOCARE_IOT("IIOT",""),

    REVERSE_OSMOSIS("RO",""),
    NANO_TRAP("NT",""),
    COUNTER_TOP("CT",""),
    BUILT_IN("BI",""),
    FREE_STAND("FS","")
}

enum class ProductType(val symbol: String, val description: String) {
    PRIMARY("PRIMARY", "주상품"),
    SECONDARY("SECONDARY","별매품"),
    NON_SALEABLE("NON_SALEABLE", "비매품"),
    NONE("NONE","분류실패")
}