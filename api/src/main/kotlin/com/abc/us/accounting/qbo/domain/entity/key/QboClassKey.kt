package com.abc.us.accounting.qbo.domain.entity.key

import jakarta.persistence.Embeddable
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable



@Embeddable
data class QboClassKey (
    val qboId: String,
    val classId: String
)  : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QboClassKey) return false

        return EqualsBuilder()
            .append(qboId, other.qboId)
            .append(classId, other.classId)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(qboId)
            .append(classId)
            .toHashCode()
    }
}
