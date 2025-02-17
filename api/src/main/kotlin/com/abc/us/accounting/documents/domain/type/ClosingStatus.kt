package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException

enum class ClosingStatus{
    INIT,        // 초기 상태
    OPEN,        // 열림 (전표 입력/수정 가능)
    CLOSING,     // 마감 중 (
    CLOSED       // 마감 완료
    ;

    private fun canTransit(claim: ClosingStatus): Boolean {
        val transits: List<ClosingStatus> = when (this) {
            INIT -> listOf(OPEN)
            OPEN -> listOf(CLOSING, CLOSED)
            CLOSING -> listOf(CLOSED)
            CLOSED -> listOf(OPEN)
        }
        return transits.contains(claim)
    }

    fun transit(claim: ClosingStatus): ClosingStatus {
        if (!canTransit(claim)) {
            throw DocumentException.ClosingStatusTransitionException(this, claim)
        }
        return claim
    }
}
