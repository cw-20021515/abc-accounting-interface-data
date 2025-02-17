package com.abc.us.accounting.collects.domain.entity.embeddable

import com.abc.us.accounting.supports.entity.Hashable
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Comment

@Embeddable
class EmbeddableRelation  : Hashable {
    @Comment("entity 이름")
    var entity: String? = null

    @Comment("entity field")
    var field: String? = null

    @Comment("entity value")
    var value: String? = null
    override fun hashValue(): String {
        val builder = StringBuilder()
        val inputStr = builder
            .append(entity).append("|")
            .append(field).append("|")
            .append(value)
            .toString()
        return Hashs.sha256Hash(inputStr)
    }
}