package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Embeddable
data class FiscalKey(
    @Comment("회사 코드")
    @Column(name = "company_code")
    @Enumerated(EnumType.STRING)
    val companyCode: CompanyCode,

    @Comment("회계 연도/월")
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "fiscal_year", column = Column(name = "fiscal_year")),
        AttributeOverride(name = "fiscal_month", column = Column(name = "fiscal_month"))
    )
    val fiscalYearMonth: FiscalYearMonth
){
    companion object {
        fun of(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth): FiscalKey {
            return FiscalKey(
                companyCode = companyCode,
                fiscalYearMonth = fiscalYearMonth
            )
        }

        fun of(companyCode: String, fiscalYearMonth: String): FiscalKey {
            return FiscalKey(
                companyCode = CompanyCode.valueOf(companyCode),
                fiscalYearMonth = FiscalYearMonth.parse(fiscalYearMonth)
            )
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FiscalKey) return false

        if (companyCode != other.companyCode) return false
        if (fiscalYearMonth != other.fiscalYearMonth) return false

        return true
    }

    override fun hashCode(): Int {
        return Hashs.hash(companyCode, fiscalYearMonth).hashCode()
    }

}

