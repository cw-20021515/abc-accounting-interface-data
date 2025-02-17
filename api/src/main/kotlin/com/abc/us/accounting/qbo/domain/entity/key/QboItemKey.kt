package com.abc.us.accounting.qbo.domain.entity.key

import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.io.Serializable

@Embeddable
data class QboItemKey (
    @Comment("QBO 등록 ID")
    val qboId: String,

    @Comment("사업장 ID")
    val companyCode: String,

    )  : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QboItemKey) return false

        return EqualsBuilder()
            .append(qboId, other.qboId)
            .append(companyCode, other.companyCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(qboId)
            .append(companyCode)
            .toHashCode()
    }
}
