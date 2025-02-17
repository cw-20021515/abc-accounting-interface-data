package com.abc.us.accounting.qbo.domain.entity.key

import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable

@Embeddable
data class QboCustomerKey (
    val qboId: String,
    val customerId: String,
    var companyCode : String,
)  : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QboCustomerKey) return false

        return EqualsBuilder()
            .append(qboId, other.qboId)
            .append(customerId, other.customerId)
            .append(companyCode, other.companyCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(qboId)
            .append(customerId)
            .append(companyCode)
            .toHashCode()
    }
}