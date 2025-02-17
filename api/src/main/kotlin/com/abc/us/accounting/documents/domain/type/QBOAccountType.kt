package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * QBO Account Type
 * 참조링크: https://developer.intuit.com/app/developer/qbo/docs/api/accounting/all-entities/account
 * @param value: String
 * @param ui: String
 * @param accountClass: AccountClass
 * @return QboAccountType
 * @see AccountClass
 */
enum class QBOAccountType(val value: String, val ui:String, val accountClass: AccountClass) {
    BANK("Bank", "Bank", AccountClass.ASSET),
    ACCOUNTS_RECEIVABLE("Accounts Receivable", "Accounts Receivable (A/R)", AccountClass.ASSET),
    OTHER_CURRENT_ASSET("Other Current Asset", "Other Current Assets", AccountClass.ASSET),
    FIXED_ASSET("Fixed Asset", "Fixed assets", AccountClass.ASSET),
    OTHER_ASSET("Other Asset", "Other Assets", AccountClass.ASSET),
    ACCOUNTS_PAYABLE("Accounts Payable", "Accounts Payable (A/P)", AccountClass.LIABILITY),
    CREDIT_CARD("Credit Card", "Credit Card", AccountClass.LIABILITY),
    OTHER_CURRENT_LIABILITY("Other Current Liability", "Other Current Liabilities", AccountClass.LIABILITY),
    LONG_TERM_LIABILITY("Long Term Liability", "Long Term Liabilities", AccountClass.LIABILITY),
    EQUITY("Equity", "Equity", AccountClass.EQUITY),
    INCOME("Income", "Income", AccountClass.REVENUE),
    COST_OF_GOODS_SOLD("Cost of Goods Sold", "Cost of Goods Sold", AccountClass.EXPENSE),
    EXPENSE("Expense", "Expenses", AccountClass.EXPENSE),
    OTHER_INCOME("Other Income", "Other Income", AccountClass.REVENUE),
    OTHER_EXPENSE("Other Expense", "Other Expense", AccountClass.EXPENSE),
;

    companion object {
        fun fromValue(description: String): QBOAccountType {
            return values().first { it.value == description }
        }
        fun fromUI(description: String): QBOAccountType {
            return values().first { it.ui == description }
        }
    }
}


@Converter
class QBOAccountTypeConverter : AttributeConverter<QBOAccountType, String> {
    override fun convertToDatabaseColumn(attribute: QBOAccountType?): String? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(dbData: String?): QBOAccountType? {
        return dbData?.let { engName ->
//            QBOAccountType.valueOf(engName)
            QBOAccountType.entries.find { it.value == engName }
        }
    }
}
