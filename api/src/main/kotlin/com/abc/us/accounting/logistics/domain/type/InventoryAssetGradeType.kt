package com.abc.us.accounting.logistics.domain.type

import com.abc.us.accounting.documents.domain.type.DocumentType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class InventoryAssetGradeType(val code: String, val description: String) {
    GRADE_A("A", "Grade_A"),

    GRADE_B("B", "Grade_A")
}


@Converter
class InventoryAssetGradeTypeConverter : AttributeConverter<InventoryAssetGradeType, String> {
    override fun convertToDatabaseColumn(attribute: InventoryAssetGradeType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): InventoryAssetGradeType? {
        return dbData?.let { code ->
            InventoryAssetGradeType.entries.find { it.code == code }
        }
    }
}

