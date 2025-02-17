package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class BizSystemType(val code:String, val description:String, val korText:String) {
    ABC_ACCOUNTING("AC", "abc-accounting", "ABC 회계"),
    ONETIME("OT", "One Time", "일시불"),
    OPERATING_LEASE("OL", "Operating Lease", "운용리스"),
    FINANCIAL_LEASE("FL", "Financial Lease", "금융리스"),
    ACCOUNT_RECEIVABLE("AR", "Account Receivable", "AR"),
    ACCOUNT_PAYABLE("AP", "Account Payable", "AP"),
    INVENTORY("IN", "Inventory", "재고자산"),
    LOGISTICS("LG", "Logistics", "물류"),
    ABC_RULE_ENGINE("RE", "abc-rule-engine", "자동처리엔진(TBD)"),
    ABC_ADMIN("AD", "ABC Admin", "수동"),
    ;

    override fun toString(): String {
        return this.name
    }

}

enum class BizCategory {
    CUSTOMER,
    VENDOR
}



@Converter
class BizSystemTypeConverter : AttributeConverter<BizSystemType, String> {
    override fun convertToDatabaseColumn(attribute: BizSystemType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BizSystemType? {
        return dbData?.let { code ->
            BizSystemType.entries.find { it.code == code }
        }
    }
}
