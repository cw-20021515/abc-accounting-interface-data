package com.abc.us.accounting.rentals.lease.domain.type

enum class RentalAssetEventType(val description: String) {
    REGISTRATION("자산등록"),
    DEPRECIATION("감가상각"),
    CONTRACT_ENDED("계약종료"),
    CONTRACT_WITHDRAWN("주문철회"),
    CONTRACT_CANCELLED("해지"),
    TEMP_CLOSING_START("가해약 등록"),
    TEMP_CLOSING_END("가해약 취소"),
    TRANSFER_TO_COLLECTION("추심전환"),
    TRANSITION_TO_COMMITMENT("약정전환");

    companion object {
        fun fromName(
            name: String
        ): RentalAssetEventType?{
            return entries.find {
                it.name == name
            }
        }
    }
}