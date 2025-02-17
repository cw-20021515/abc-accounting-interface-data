package com.abc.us.accounting.qbo.domain.entity.key

import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable


@Embeddable
data class QboAccountKey (
    val qboId: String,
    val accountCode: String,
    val accountName: String,
)  : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QboAccountKey) return false

        return EqualsBuilder()
            .append(qboId, other.qboId)
            .append(accountCode, other.accountCode)
            .append(accountName, other.accountName)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(qboId)
            .append(accountCode)
            .append(accountName)
            .toHashCode()
    }
}
