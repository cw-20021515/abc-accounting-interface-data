package com.abc.us.accounting.payouts.domain.entity.key

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable

@Embeddable
class IdCompanyKey(
    @Column(name = "id")
    val id: String,

    @Column(name = "company_code")
    val companyCode: String,
): Serializable {
    companion object{
        fun of (id: String, paymentId: String):IdCompanyKey {
            return IdCompanyKey(id, paymentId)
        }
    }


    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                " id='" + id + '\'' +
                " companyCode='" + companyCode + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdCompanyKey) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(companyCode, other.companyCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(companyCode)
            .toHashCode()
    }
}