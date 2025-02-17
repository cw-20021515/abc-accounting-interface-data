package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.domain.type.RelationTypeConverter
import com.abc.us.accounting.documents.model.DocumentRelationResult
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Entity
@Table(name = "document_relation")
class DocumentRelation(
    @Id
    val id: String,

    @Column(name = "doc_id")
    val docId: String,

    @Column(name = "ref_doc_id")
    val refDocId: String,

    @Column(name = "relation_type")
    @Convert(converter = RelationTypeConverter::class)
    val relationType: RelationType,

    @Column(name = "reason")
    val reason: String? = null,

    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),
) {
    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "id='" + id + '\'' +
                ", docId=" + docId + '\'' +
                ", relationType=" + relationType + '\'' +
                ", refDocId=" + refDocId + '\'' +
                ", reason=" + reason + '\'' +
                ", createTime=" + createTime + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentRelation) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(docId, other.docId)
            .append(relationType, other.relationType)
            .append(refDocId, other.refDocId)
            .append(reason, other.reason)
            .append(createTime, other.createTime)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(docId)
            .append(relationType)
            .append(refDocId)
            .append(reason)
            .append(createTime)
            .toHashCode()
    }

    fun toResult(): DocumentRelationResult {
        return DocumentRelationResult(
            docRelationId = id,
            docId = docId,
            refDocId = refDocId,
            relationType = relationType,
            reason = reason,
            createTime = createTime
        )
    }
//
//
//    data class DocumentRelationResult (
//        val docRelationId: String,
//        val docId: String,
//        val refDocId: String,
//        val relationType: RelationType,
//        val createTime: OffsetDateTime,
//        val createdBy: String,
//        val updateTime: OffsetDateTime,
//        val updatedBy: String
//    )
}
