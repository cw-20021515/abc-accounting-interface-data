package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class RelationType(val code: String, val description: String) {
//    NONE("NO", "None"),
    CLEARING("CL", "Clearing"),
    PARTIAL_CLEARING("PC", "Partial Clearing"),
    REVERSING("RV", "Reversing"),
    OFFSETTING("OF", "Offsetting"),
    ;

    fun docItemStatus():DocumentItemStatus {
        return when(this) {
//            NONE -> DocumentItemStatus.NORMAL
            CLEARING -> DocumentItemStatus.CLEARING
            PARTIAL_CLEARING -> DocumentItemStatus.PARTIAL
            REVERSING -> DocumentItemStatus.REVERSAL
            OFFSETTING -> DocumentItemStatus.NORMAL
        }
    }

    fun refDocItemStatus(): DocumentItemStatus {
        return when(this) {
//            NONE -> DocumentItemStatus.NORMAL
            CLEARING -> DocumentItemStatus.CLEARED
            PARTIAL_CLEARING -> DocumentItemStatus.NORMAL
            REVERSING -> DocumentItemStatus.REVERSED
            OFFSETTING -> DocumentItemStatus.NORMAL
        }
    }
}


@Converter
class RelationTypeConverter : AttributeConverter<RelationType, String> {
    override fun convertToDatabaseColumn(attribute: RelationType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): RelationType? {
        return dbData?.let { code ->
            RelationType.entries.find { it.code == code }
        }
    }
}
