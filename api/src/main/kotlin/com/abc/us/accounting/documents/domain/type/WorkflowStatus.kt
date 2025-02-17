package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


enum class WorkflowStatus(val code: String, val value:String, val engText:String, val korText: String) {
    INITIAL("IN", "Initial", "Initial Document", "초기상태 전표"),
    SUBMITTED("SB", "Submitted", "Submitted Document", "제출된 전표"),
    WAITING("WT", "Waiting", "Waiting Document", "대기중 전표"),
    REJECTED("RJ", "Rejected", "Rejected Document", "반려된 전표"),
    APPROVED("AP", "Approved", "Approved Document", "승인된 전표"),
    ;


    fun canTransit(next: WorkflowStatus): Boolean {
        val transits: List<WorkflowStatus> = when (this) {
            INITIAL -> listOf(SUBMITTED)
            SUBMITTED -> listOf(REJECTED, WAITING, APPROVED)
            WAITING -> listOf(SUBMITTED)
            else -> listOf()
        }
        return transits.contains(next)
    }

    fun transit(desiredStatus: WorkflowStatus): WorkflowStatus {
        if (!canTransit(desiredStatus)) {
            throw DocumentException.WorkflowStatusTransitionException(this, desiredStatus)
        }
        return desiredStatus
    }

    companion object {
        fun of(code: String): WorkflowStatus {
            for (status in entries) {
                if (status.code == code) {
                    return status
                }
            }
            throw IllegalArgumentException("code not found in enum, code:$code")
        }
    }
}



@Converter
class WorkflowStatusConverter : AttributeConverter<WorkflowStatus, String> {
    override fun convertToDatabaseColumn(attribute: WorkflowStatus?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): WorkflowStatus? {
        return dbData?.let { code ->
            WorkflowStatus.values().find { it.code == code }
        }
    }
}


