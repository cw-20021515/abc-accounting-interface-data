package com.abc.us.accounting.rentals.lease.domain.type

import com.fasterxml.jackson.annotation.JsonCreator

enum class RentalAssetDepreciationMethod(
    val symbol: String,
    val description: String
) {
    STRAIGHT_LINE("SL", "정액법");

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(
            value: String
        ): RentalAssetDepreciationMethod? {
            return (
                entries.find { it.symbol == value } ?:
                entries.find { it.name == value }
            )
        }
    }
}
