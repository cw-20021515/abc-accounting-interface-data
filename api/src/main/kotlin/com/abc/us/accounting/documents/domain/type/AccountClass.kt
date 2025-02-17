package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.math.BigDecimal


enum class AccountClass(val code: String, val engName:String, val korName: String,
                        val codePrefix: Int,
                        val natualAccountSide: AccountSide,
                        val statementCategory: StatementCategory
) {
    ASSET("A","Asset", "자산", 1,
        AccountSide.DEBIT,
        StatementCategory.BALANCE_SHEET
    ),
    LIABILITY("L", "Liability", "부채", 2,
        AccountSide.CREDIT,
        StatementCategory.BALANCE_SHEET
    ),
    EQUITY("E", "Equity", "자본", 3,
        AccountSide.CREDIT,
        StatementCategory.BALANCE_SHEET
    ),
    REVENUE("R", "Revenue", "수익", 4,
        AccountSide.CREDIT,
        StatementCategory.INCOME_STATEMENT
    ),
    EXPENSE("E", "Expense", "비용", 5,
        AccountSide.DEBIT,
        StatementCategory.INCOME_STATEMENT
    ),
    ;

    fun getAccountCodeRange(): IntRange {
        return codePrefix * 1000000 until (codePrefix + 1) * 1000000
    }


    fun calculateAmount(amount: BigDecimal, accountSide:AccountSide): BigDecimal {
        return when {
            accountSide.isIncrease(this.natualAccountSide) -> amount
            else -> amount.negate()
        }
    }
}


@Converter
class AccountClassNameConverter : AttributeConverter<AccountClass, String> {
    override fun convertToDatabaseColumn(attribute: AccountClass?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): AccountClass? {
        return dbData?.let { name ->
            AccountClass.entries.find { it.name == name }
        }
    }
}
