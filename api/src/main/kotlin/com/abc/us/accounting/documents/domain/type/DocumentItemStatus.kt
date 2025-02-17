package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.domain.type.DocumentStatus.INITIAL
import com.abc.us.accounting.documents.exceptions.DocumentException
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class DocumentItemStatus(val code: String, val value:String, val engText:String, val korText: String) {
    INITIAL("IN", "Initial", "Initial Document Item", "초기상태 전표항목"),
    NORMAL("NO", "Normal", "Normal Document Item","정상전표항목"),
    PARTIAL("CP", "Partial", "Partial Cleared Document Item", "원전표-부분 반제 항목"),
    CLEARED("CD", "Cleared", "Cleared Document Item", "원전표 - 반제 항목"),
    CLEARING("CL", "Clearing", "Clearing Document Item", "반제전표 - 반제항목"),
    REVERSED("RD", "Reversed", "Reversed Document Item", "원번표 - 피 역분개 항목"),
    REVERSAL("RL", "Reversal", "Reversal Document Item", "역분개전표 - 역분개 항목"),
    ;

    fun canTransit(next: DocumentItemStatus): Boolean {
        val transits: List<DocumentItemStatus> = when (this) {
            INITIAL -> listOf(NORMAL, REVERSAL, CLEARING)
            NORMAL -> listOf(PARTIAL, CLEARED, CLEARING, REVERSED)
            PARTIAL -> listOf(NORMAL, CLEARED, REVERSED)
            CLEARED -> listOf(NORMAL)
            CLEARING -> listOf(REVERSED)
            else -> listOf()
        }
        return transits.contains(next)
    }

    fun transit(desiredStatus: DocumentItemStatus): DocumentItemStatus {
        if (!canTransit(desiredStatus)) {
            throw DocumentException.DocumentItemStatusTransitionException(this, desiredStatus)
        }
        return desiredStatus
    }

    fun getOpenItemStatus(isOpenItemMgmt:Boolean = false): OpenItemStatus {
        if (!isOpenItemMgmt) {
            return OpenItemStatus.NONE
        }

        return when (this) {
            NORMAL -> OpenItemStatus.OPEN
            PARTIAL -> OpenItemStatus.OPEN
            CLEARED -> OpenItemStatus.CLEARED
            CLEARING -> OpenItemStatus.CLEARED
            REVERSED -> OpenItemStatus.NONE
            REVERSAL -> OpenItemStatus.NONE
            else -> OpenItemStatus.OPEN
        }

    }

    companion object {
        fun of(code: String): DocumentItemStatus {
            for (status in entries) {
                if (status.code == code) {
                    return status
                }
            }
            throw IllegalArgumentException("code not found in enum, code:$code")
        }


        /**
         * 처음에 가능한 전표상태를 확인한다.
         */
        fun verify(claimStatus: DocumentItemStatus): DocumentItemStatus {
            return DocumentItemStatus.INITIAL.transit(claimStatus)
        }
    }
}


@Converter
class DocumentItemStatusConverter : AttributeConverter<DocumentItemStatus, String> {
    override fun convertToDatabaseColumn(attribute: DocumentItemStatus?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentItemStatus? {
        return dbData?.let { symbol ->
            DocumentItemStatus.values().find { it.code == symbol }
        }
    }
}



