package com.abc.us.accounting.rentals.lease.domain.type

import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RentalFinancialEventType(
    private val value: String
) {
    FLEASE_REGISTRATION("FLEASE_REGISTRATION"),                                                 // 금융 리스 등록
    FLEASE_PRODUCT_SHIPPED(DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED.name),                         // 금융 리스 제품 출고
    FLEASE_INSTALL_COMPLETED(DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED.name),                       // 금융 리스 제품 출고
    FLEASE_GOODS_SALES_RECOGNITION(DocumentTemplateCode.FLEASE_SALES_RECOGNITION.name),         // 설치 완료(매출-재화)
    FLEASE_COGS_RECOGNITION(DocumentTemplateCode.FLEASE_COGS_RECOGNITION.name),                       // 설치 완료(매출-원가)
    FLEASE_INVENTORY_PRICE_DIFFERENCE(DocumentTemplateCode.FLEASE_PRICE_DIFFERENCE.name),   // 설치 완료(매출-재고가액 확정)
    FLEASE_DEPRECIATION(DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_INTEREST_INCOME.name),                               // 금융 리스 상각

    EXPIRED("EXPIRED"),                                         // 만료 됨 리스 계약이 정상적으로 종료된 상태
    TERMINATION("TERMINATION"),                                 // 리스 종료 후에도 자산이 여전히 사용되거나 유지되는 상태
    DISPOSAL("DISPOSAL"),                                       // 폐기
    EXCHANGE("EXCHANGE");                                       // 교환

    @JsonValue
    fun getValue(): String {
        return value
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(value: String): RentalFinancialEventType {
            return values().firstOrNull { it.name == value }
                ?: throw IllegalArgumentException("Unknown enum value: $value")
        }

        fun convertInstall(
            rentalFinancialEventType: RentalFinancialEventType
        ): List<RentalFinancialEventType> {
            // 각 이벤트 유형에 대한 비호환 이벤트 유형 설정 rentalFinancialEventType
            val incompatibleEventTypes = when (rentalFinancialEventType) {
                FLEASE_GOODS_SALES_RECOGNITION -> listOf(
                    FLEASE_COGS_RECOGNITION,
                    FLEASE_INVENTORY_PRICE_DIFFERENCE
                )
                FLEASE_COGS_RECOGNITION -> listOf(
                    FLEASE_GOODS_SALES_RECOGNITION,
                    FLEASE_INVENTORY_PRICE_DIFFERENCE
                )
                FLEASE_INVENTORY_PRICE_DIFFERENCE -> listOf(
                    FLEASE_GOODS_SALES_RECOGNITION,
                    FLEASE_COGS_RECOGNITION
                )
                else -> emptyList() // 다른 경우는 비호환 이벤트가 없음
            }

            return incompatibleEventTypes
        }
    }
}




