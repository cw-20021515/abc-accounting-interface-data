package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


enum class AccountType(val code: String, val engName:String, val korName: String, val description: String) {
    GENERAL("G", "General","일반", "일반 총계정원장에 포함되는 전통적인 회계의 계정과목 코드"),
    CUSTOMER("C", "Customer", "고객", "고객/판매거래처 계정"),
    VENDOR("V", "Vendor","거래처", "공급업체/구매거래처 계정"),
    ASSET("A", "Asset","자산", "고정자산 관련계정(렌탈자산 포함)"),
    MATERIAL("M", "Material","자재", "재고자산/자재 관련 계정"),
    SALES("S", "Sales","매출", "매출/수익 관련 계정"),
    COGS("O", "COGS","원가", "매출원가/원가 관련 계정"),
    ;

    /**
     * 현재는 CUSTOMER, SALES만 사용, 수정 필요할지는 추후 검토
     */
    fun customerIdRequired(): Boolean {
        return when(this){
            CUSTOMER, SALES -> true
            else -> false
        }
    }

    /**
     * 현재는 VENDOR만 사용, 수정 필요 할지는 추후 검토
     */
    fun vendorIdRequired(): Boolean {
        return when(this){
            VENDOR -> true
            else -> false
        }
    }
}



@Converter
class AccountTypeNameConverter : AttributeConverter<AccountType, String> {
    override fun convertToDatabaseColumn(attribute: AccountType?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): AccountType? {
        return dbData?.let { name ->
            AccountType.values().find { it.name == name }
        }
    }
}
