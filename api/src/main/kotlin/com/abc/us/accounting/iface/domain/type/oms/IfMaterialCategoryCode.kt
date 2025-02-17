package com.abc.us.accounting.iface.domain.type.oms

//[자재 분류 코드] 자재 유형에 대한 상세 분류 코드
enum class IfMaterialCategoryCode(val symbol: String,val description: String) {
    WATER_PURIFIER("WP","정수기 제품"),
    WATER_PURIFIER_FILTER("WPF","정수기 필터"),
    WATER_PURIFIER_FILTER_SET("WPFS","정수기 필터 세트"),
    WATER_PURIFIER_PART("WPP","정수기 부품"),
    WATER_PURIFIER_CONSUMABLE("WPC","정수기 소모품");
}