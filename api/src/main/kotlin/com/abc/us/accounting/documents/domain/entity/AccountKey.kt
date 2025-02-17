package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.CompanyCode
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.io.Serializable


@Embeddable
class AccountKey(
    @Comment("회사 코드")
    @Column(name = "company_code")
    @Enumerated(EnumType.STRING)
    val companyCode: CompanyCode,           // 회사 코드

    @Comment("계정 코드")
    @Column(name = "account_code")
    val accountCode: String,                // 계정 코드
): Serializable {
    companion object{
        fun of (companyCode: CompanyCode, accountCode: String):AccountKey {
            return AccountKey(companyCode, accountCode)
        }
    }


    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                " companyCode='" + companyCode + '\'' +
                " accountCode='" + accountCode + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountKey) return false

        return EqualsBuilder()
            .append(companyCode, other.companyCode)
            .append(accountCode, other.accountCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(companyCode)
            .append(accountCode)
            .toHashCode()
    }
}
