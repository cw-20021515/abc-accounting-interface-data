package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.DocumentAttributeTypeConverter
import com.abc.us.accounting.documents.model.DocumentItemAttributeResult
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.time.OffsetDateTime

@Embeddable
data class DocumentItemAttributeId(
    @Column(name = "doc_item_id")
    val docItemId: String,

    @Column(name = "attribute_type")
    @Convert(converter = DocumentAttributeTypeConverter::class)
    val attributeType: DocumentAttributeType
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentItemAttributeId) return false

        return EqualsBuilder()
            .append(docItemId, other.docItemId)
            .append(attributeType, other.attributeType)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(docItemId)
            .append(attributeType)
            .toHashCode()
    }
}


@Entity
@Table(name = "document_item_attribute")
class DocumentItemAttribute(
    @EmbeddedId
    val attributeId: DocumentItemAttributeId,

    @Column(name = "value")
    val value: String,

    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),
) {

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                " docItemId='" + attributeId.docItemId + '\'' +
                " attributeType='" + attributeId.attributeType + '\'' +
                ", value=" + value + '\'' +
                ", createTime=" + createTime + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentItemAttribute) return false

        return EqualsBuilder()
            .append(attributeId, other.attributeId)
            .append(value, other.value)
            .append(createTime, other.createTime)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(attributeId)
            .append(value)
            .append(createTime)
            .toHashCode()
    }

    fun copy(): DocumentItemAttribute{
        return DocumentItemAttribute(
            attributeId = this.attributeId,
            value = this.value
        )
    }

    fun toResult(): DocumentItemAttributeResult {
        return DocumentItemAttributeResult(
            docItemId = attributeId.docItemId,
            type = attributeId.attributeType,
            value = value
        )
    }
}
