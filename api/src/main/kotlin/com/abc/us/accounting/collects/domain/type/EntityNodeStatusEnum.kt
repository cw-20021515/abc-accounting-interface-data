package com.abc.us.accounting.collects.domain.type

enum class EntityNodeStatusEnum(val symbol: String) {
    ACTIVE("A"),  //  할증 - 어떤 원인으로 인한 비정상적인 비용(위약금,  등)
    INACTIVE("I"),  // 할인 - 어떤 원인으로 인한 금액 감소가 발생(프로모션 등)
    DELETED("D"),
    PENDING("P"),
}