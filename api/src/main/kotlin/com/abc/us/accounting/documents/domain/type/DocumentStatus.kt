package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


/**
 * 전표 상태
 *
 * INITIAL 상태는 추가 고민 필요
 */
enum class DocumentStatus(val code: String, val value:String, val engText:String, val korText: String) {
    INITIAL("IN", "Initial", "Initial Document", "초기상태 전표"),
    NORMAL("NO", "Normal", "Normal Document","정상전표"),
    DRAFT("DR", "Draft", "Draft Document", "임시전표(임시저장으로 생성)"),
    REVIEW("RV", "Review", "Review Document", "검토중 전표"),
    REVERSED("RD", "Reversed", "Reversed Document", "역분개된(취소된) 전표"),
    REVERSAL("RL", "Reversal", "Reversal Document", "역분개 전표"),
    ;

    val isDraft: Boolean
        get() = DRAFT == this

    /**
     * 전표 상태가 정상적으로 처리 가능한 상태인지 확인한다.
     */
    val isAcceptable: Boolean
        get() = when (this) {
            NORMAL, REVERSED, REVERSAL -> true
            else -> false
        }

    fun canTransit(next: DocumentStatus): Boolean {
        val transits: List<DocumentStatus> = when (this) {
            INITIAL -> listOf(DRAFT, NORMAL, REVIEW, REVERSAL)
            NORMAL -> listOf(REVERSED)
            DRAFT -> listOf(DRAFT, REVIEW, NORMAL)          // 임시전표는 임시전표/검토전표/정상전표로 전환 가능
            REVIEW -> listOf(DRAFT, NORMAL)                 // 검토전표는 임시전표/정상전표로 전환 가능
            else -> listOf()
        }
        return transits.contains(next)
    }

    fun transit(desiredStatus: DocumentStatus): DocumentStatus {
        if (!canTransit(desiredStatus)) {
            throw DocumentException.DocumentStatusTransitionException(this, desiredStatus)
        }
        return desiredStatus
    }

    companion object {
        fun of(code: String): DocumentStatus {
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
        fun verify(claimStatus: DocumentStatus): DocumentStatus {
            return INITIAL.transit(claimStatus)
        }
    }
}


@Converter
class DocumentStatusConverter : AttributeConverter<DocumentStatus, String> {
    override fun convertToDatabaseColumn(attribute: DocumentStatus?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentStatus? {
        return dbData?.let { code ->
            DocumentStatus.values().find { it.code == code }
        }
    }
}


