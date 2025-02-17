package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class DocumentAttributeCategory (val symbol: String, val engName:String, val korName: String, val description: String) {
    ASSIGNMENT("S", "Assignment", "할당", "할당"),
    ATTRIBUTE("T", "Attribute", "속성", "속성"),
}



@Converter
class DocumentAttributeCategoryConverter : AttributeConverter<DocumentAttributeCategory, String> {
    override fun convertToDatabaseColumn(attribute: DocumentAttributeCategory?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentAttributeCategory? {
        return dbData?.let { name ->
            DocumentAttributeCategory.entries.find { it.name == name }
        }
    }
}