package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class OpenItemStatus(val code: String, val value:String, val engText:String, val korText: String) {
    OPEN("O", "Open", "Open", "미결"),
    CLEARED("C", "Cleared", "Cleared","반제"),
    NONE("N", "-", "None", "-"),
    ;

    companion object {
        fun of(code: String): OpenItemStatus {
            for (entry in entries) {
                if (entry.code == code) {
                    return entry
                }
            }
            throw IllegalArgumentException("code not found in enum, code:$code")
        }
    }
}


@Converter
class OpenItemStatusConverter : AttributeConverter<OpenItemStatus, String> {
    override fun convertToDatabaseColumn(attribute: OpenItemStatus?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): OpenItemStatus? {
        return dbData?.let { code ->
            OpenItemStatus.entries.find { it.code == code }
        }
    }
}



