package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class BalanceRecordType(val code: String, val description: String) {
    OPENING_BALANCE("O", "Opening Balance"),    // 개시 잔액
    BALANCE_ADJUSTMENT("A", "Balance Adjustment"),         // 잔액을 다시 맞춰야 하는 경우
    DOCUMENT_CREATED("C", "Document Created"),    // 전표 생성
    DOCUMENT_MODIFIED("M", "Document Modified"),    // 전표 수정
    DOCUMENT_REVERSE("R", "Document Reverse"),    // 전표 취소
    DOCUMENT_DELETED("D", "Document Deleted"),    // 전표 삭제
}


@Converter
class BalanceRecordTypeConverter : AttributeConverter<BalanceRecordType, String> {
    override fun convertToDatabaseColumn(attribute: BalanceRecordType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BalanceRecordType? {
        return dbData?.let { code ->
            BalanceRecordType.entries.find { it.code == code }
        }
    }
}

//
//enum class BalanceSnapshotType {
//    DOCUMENT_DATE_BALANCE,    // 거래일 기준 잔액
//    POSTING_DATE_BALANCE,     // 전기일 기준 잔액
//    CURRENT_BALANCE          // 현재 잔액
//}