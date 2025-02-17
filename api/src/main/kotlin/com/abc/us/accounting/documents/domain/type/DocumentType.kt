package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


/*
    * DocumentTypeCode: SAP Document Type과 같은 코드 사용
    * 참조링크: https://help.sap.com/docs/SAP_S4HANA_CLOUD/0fa84c9d9c634132b7c4abb9ffdd8f06/cbe758a0da7847a59c0cf1a8f595f438.html
    * 추후 코드 확장 예정
    *
    * @param symbol: String
    * @param engName: String
    * @param korName: String
    * @param description: String
    * @param allowAccountTypes: List<AccountTypeCode>
*/

enum class DocumentType(val code: String, val engName:String, val korName: String, val description: String, val allowAccountTypes: List<AccountType>) {
    ACCOUNTING_DOCUMENT("SA", "G/L Account Document", "G/L 계정 전기", "일반 G/L 전표",
                        listOf(AccountType.GENERAL, AccountType.SALES, AccountType.COGS)),

    JOURNAL_ENTRY("AB", "Journal Entry", "일반전표","모든 계정과목 허용, SA의 역분개전표로 사용됨",
        listOf(AccountType.GENERAL, AccountType.ASSET, AccountType.CUSTOMER, AccountType.VENDOR, AccountType.MATERIAL, AccountType.SALES, AccountType.COGS)),

    ASSET_POSTING("AA", "Asset Posting", "저산 전기", "자산회계전표",       listOf(AccountType.ASSET)),
    DEPRECIATION_POSTING("AF", "Depreciation Posting", "감가상각 전기", "감가상각전표", listOf(AccountType.ASSET)),
    NET_ASSET_POSTING("AN", "Net Asset Posting", "순자산 전기", "순자산전표", listOf(AccountType.ASSET)),

    CUSTOMER_DOCUMENT("DA", "Customer Document", "고객전표", "고객전표", listOf(AccountType.CUSTOMER)),
    CUSTOMER_INVOICE("DR", "Customer Invoice", "고객청구", "미수금 전표", listOf(AccountType.CUSTOMER)),
    CUSTOMER_PAYMENT("DZ", "Customer Payment", "고객수납", "고객수납", listOf(AccountType.CUSTOMER)),
    CUSTOMER_CREDIT_MEMO("DG", "Customer Credit Memo", "고객신용메모", "고객신용메모", listOf(AccountType.CUSTOMER)),
    CUSTOMER_INTERESTS("DV", "Customer Interests", "고객이자", "고객이자", listOf(AccountType.CUSTOMER)),

    VENDOR_DOCUMENT("KA", "Vendor Document", "거래처전표", "거래처 전표", listOf(AccountType.VENDOR)),
    VENDOR_INVOICE("KR", "Vendor Invoice", "거래처송장", "미지급금 전표", listOf(AccountType.VENDOR)),
    VENDOR_PAYMENT("KZ", "Vendor Payment", "거래처지급", "거래처지급", listOf(AccountType.VENDOR)),
    VENDOR_CREDIT_MEMO("KG", "Vendor Credit Memo", "거래처 대변 메모", "거래처 대변 메모", listOf(AccountType.VENDOR)),
    NET_VENDORS("KN", "Net Vendors", "순거래처", "순거래처", listOf(AccountType.VENDOR)),

    INVENTORY_POSTING("SE", "Inventory Posting", "재고전기", "재고전기", listOf(AccountType.MATERIAL)),
    INVENTORY_DOCUMENT("WI", "Inventory Document", "재고전표", "재고전표", listOf(AccountType.MATERIAL)),
    GOODS_ISSUE("WA", "Goods Issue", "상품출고", "상품출고", listOf(AccountType.MATERIAL)),
    GOODS_RECEIPT("WE", "Goods Receipt", "상품입고", "상품입고", listOf(AccountType.MATERIAL)),
    GOODS_TRANSFER("WL", "Goods Issue/Delivery", "상품이동", "상품이동", listOf(AccountType.MATERIAL)),
;

    override fun toString(): String {
        return "$name($code)"
    }
}



@Converter
class DocumentTypeCodeConverter : AttributeConverter<DocumentType, String> {
    override fun convertToDatabaseColumn(attribute: DocumentType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentType? {
        return dbData?.let { symbol ->
            DocumentType.values().find { it.code == symbol }
        }
    }
}



@Converter
class DocumentTypeNameConverter : AttributeConverter<DocumentType, String> {
    override fun convertToDatabaseColumn(attribute: DocumentType?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentType? {
        return dbData?.let { name ->
            DocumentType.entries.find { it.name == name }
        }
    }
}
