package com.abc.us.accounting.documents.domain.type

enum class ClearingReason(val code: String, val descriptionEn: String, val descriptionKo: String) {
    AUTOMATIC_CLEARING("C01", "Automatic Clearing", "자동반제"),
    MANUAL_CLEARING("C02", "Manual Clearing", "수동반제"),
    PARTIAL_PAYMENT("C03", "Partial Clearing", "부분반제"),
    BANK_STATEMENT("C04", "Bank Statement", "은행명세"),
    CUSTOMER_PAYMENT("C05", "Customer Payment", "고객결제"),
    DOWN_PAYMENT("C06", "Down Payment", "선급금"),
    EXCHANGE_RATE_DIFFERENCE("C07", "Exchange Rate Difference", "환율차이"),
    VENDOR_PAYMENT("C08", "Vendor Payment", "거래처 결제"),
    CREDIT_MEMO("C09", "Credit Memo", "크레딧 메모"),
    REVERSAL("C10", "Reversal", "역분개"),
    INTEREST("C11", "Interest", "이자"),
    CASH_DISCOUNT("C12", "Cash Discount", "할인"),
    WRITE_OFF("C13", "Write-Off", "대손처리"),
    REIMBURSEMENT("C14", "Reimbursement", "환급"),
    SPECIAL_GL_TRANSACTION("C15", "Special G/L Transaction", "특별 G/L 거래"),
    TAX("C16", "Tax", "세금"),
    UNPLANNED_PAYMENT("C17", "Unplanned Payment", "예상치 못한 결제"),
    VENDOR_DOWN_PAYMENT("C18", "Vendor Down Payment", "거래처 선급금"),
    WITHHOLDING_TAX("C19", "Withholding Tax", "원천징수"),
    OTHER("C20", "Other", "기타");

    companion object {
        fun getDescriptionEnByCode(code: String): String? {
            return entries.find { it.code == code }?.descriptionEn
        }
    }
}
