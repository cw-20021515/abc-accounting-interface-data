package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.math.BigDecimal

/**
 * AccountSide: 차변/대변 구분 코드
 */
enum class AccountSide(val code: String, val engName:String, val korName: String) {
    DEBIT("D", "Debit","차변"),
    CREDIT("C", "Credit", "대변"),
    ;

    fun reverse(): AccountSide {
        return when(this) {
            DEBIT -> CREDIT
            CREDIT -> DEBIT
        }
    }
    
    fun isIncrease (accountSide: AccountSide): Boolean =
        when(accountSide) {
            DEBIT -> this == DEBIT
            CREDIT -> this == CREDIT
        }

    /**
     * 금액 계산, 차변 계정과목은 차변 일때 +, 대변 계정과목은 대변 일때 +, 다른 경우는 -
     */
    fun calculateAmount (amount: BigDecimal, accountSide: AccountSide): BigDecimal =
        when {
            isIncrease(accountSide) -> amount.toScale()
            else -> amount.negate().toScale()
        }

    /**
     * 차변에 대한 금액 계산, 대변은 0
     */
    fun debitAmount (amount: BigDecimal, accountSide: AccountSide): BigDecimal =
        when {
            accountSide == DEBIT -> amount.toScale()
            else -> BigDecimal.ZERO.toScale()
        }

    /**
     * 대변에 대한 금액 계산, 차변은 0
     */
    fun creditAmount (amount: BigDecimal, accountSide: AccountSide): BigDecimal =
        when {
            accountSide == CREDIT -> amount.toScale()
            else -> BigDecimal.ZERO.toScale()
        }

    companion object {
        fun of(code: String): AccountSide {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown AccountSide code: $code")
        }
    }
}




@Converter
class AccountSideConverter : AttributeConverter<AccountSide, String> {
    override fun convertToDatabaseColumn(attribute: AccountSide?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): AccountSide? {
        return dbData?.let { symbol ->
            AccountSide.entries.find { it.code == symbol }
        }
    }
}

