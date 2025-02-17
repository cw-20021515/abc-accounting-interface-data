package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.domain.entity.FiscalRule
import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import java.time.LocalDate
import java.time.Month
import java.util.*


enum class CompanyCode(val code: String, val engName: String, val korName:String) {
    N100("N100", "NECOA Holdings", "네코아 지주회사"),
    N200("N200","NECOA", "네코아"),
    N300("N300", "NECOA Tech", "네코아 테크"),


    T100("T100", "[Test] NECOA Holdings", "[테스트] 네코아 지주회사"),
    T200("T200","[Test] NECOA", "[테스트] 네코아"),
    T300("T300", "[Test] NECOA Tech", "[테스트] 네코아 테크"),
    ;

    fun isTestCompany(): Boolean {
        return this.name.startsWith("T")
    }

    fun isNewAccountCode(): Boolean {
        return isTestCompany()
    }

    fun isSalesCompany(): Boolean {
        return this == N200 || this == T200
    }

    companion object {
        fun of(code: String): CompanyCode {
            for (companyCode in CompanyCode.entries) {
                if (companyCode.code == code) {
                    return companyCode
                }
            }
            throw IllegalArgumentException("code not found in enum, code:$code")
        }

        fun toCompanyCodeNames():List<String> {
            return CompanyCode.entries.map { entry -> entry.name }
        }

        fun randomSalesCompany(): CompanyCode{
            val list = listOf(N200, T200)
            return list.random()
        }
    }
}