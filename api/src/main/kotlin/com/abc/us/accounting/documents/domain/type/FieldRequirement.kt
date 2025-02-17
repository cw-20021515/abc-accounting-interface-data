package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class FieldRequirement(val symbol: String, val engName:String, val korName:String, val description: String, val sortOrder:Int) {
    REQUIRED("R", "Required", "필수", "필수 입력 항목", 1),
    OPTIONAL("O", "Optional", "선택", "선택 입력 항목", 2),
    CONDITIONAL("C", "Conditional", "조건부", "특정 조건에서 필수 입력", 3),
    NOT_ALLOWED("N", "Not Allowed", "허용 안됨", "해당 용도에서 사용하지 않음", 4),
    ;

    fun isAcceptable():Boolean {
        return when(this) {
            REQUIRED, OPTIONAL -> true
            else -> false
        }
    }
}


@Converter
class FieldRequirementNameConverter : AttributeConverter<FieldRequirement, String> {
    override fun convertToDatabaseColumn(attribute: FieldRequirement?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): FieldRequirement? {
        return dbData?.let { symbol ->
            FieldRequirement.entries.find { it.name == symbol }
        }
    }
}