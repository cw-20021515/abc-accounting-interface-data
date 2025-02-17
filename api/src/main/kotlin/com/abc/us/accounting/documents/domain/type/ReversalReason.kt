package com.abc.us.accounting.documents.domain.type

enum class ReversalReason(val code: String, val descriptionEn: String, val descriptionKo: String) {
    WRONG_POSTING("R01", "Wrong posting", "잘못된 전기"),
    WRONG_PERIOD("R02", "Wrong period", "잘못된 기간"),
    WRONG_PRICE_CONDITIONS("R03", "Wrong price and conditions", "잘못된 가격조건"),
    RETURN("R04", "Return", "반환"),
    AUTO_REVERSE("R05", "Auto-reverse", "자동역분개");

    companion object {
        fun getDescriptionEnByCode(code: String): String? {
            return entries.find { it.code == code }?.descriptionEn
        }
    }
}
