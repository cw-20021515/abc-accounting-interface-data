package com.abc.us.accounting.rentals.lease.utils

import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetDepreciationMethod
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RentalAssetDepreciationMethodConverter: AttributeConverter<RentalAssetDepreciationMethod, String> {
    override fun convertToDatabaseColumn(attribute: RentalAssetDepreciationMethod?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): RentalAssetDepreciationMethod? {
        return dbData?.let { symbol ->
            RentalAssetDepreciationMethod.entries.find { it.symbol == symbol }
        }
    }
}