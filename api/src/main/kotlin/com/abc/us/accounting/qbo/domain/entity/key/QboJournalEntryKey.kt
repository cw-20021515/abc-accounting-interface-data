package com.abc.us.accounting.qbo.domain.entity.key

import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable

@Embeddable
data class QboJournalEntryKey (
    val qboId: String,
    val docId: String,
    var companyCode : String,
)  : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is QboJournalEntryKey)
            return false

        return EqualsBuilder()
            .append(qboId, other.qboId)
            .append(docId, other.docId)
            .append(companyCode, other.companyCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(qboId)
            .append(docId)
            .append(companyCode)
            .toHashCode()
    }
}
