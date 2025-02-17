package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.domain.type.RelationTypeConverter
import com.abc.us.accounting.documents.model.DocumentItemRelationResult
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "document_item_relation")
class DocumentItemRelation(
    @Id
    val id: String,

    @Column(name = "doc_item_id")
    val docItemId: String,

    @Column(name = "ref_doc_item_id")
    val refDocItemId: String,

    @Column(name = "relation_type")
    @Convert(converter = RelationTypeConverter::class)
    val relationType: RelationType,

    @Column(name = "reason")
    val reason: String? = null,

    @Column(name = "amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val amount: BigDecimal,

    @Column(name = "ref_amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val refAmount: BigDecimal,

    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),
) {
        override fun toString(): String {
            return this.javaClass.simpleName + "{" +
                    "id='" + id + '\'' +
                    ", docItemId=" + docItemId + '\'' +
                    ", refDocItemId=" + refDocItemId + '\'' +
                    ", relationType=" + relationType + '\'' +
                    ", reason=" + reason + '\'' +
                    ", refAmount=" + refAmount + '\'' +
                    ", amount=" + amount + '\'' +
                    ", createTime=" + createTime + '\'' +
                    '}'
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            if (other !is DocumentItemRelation) return false

            return EqualsBuilder()
                .append(id, other.id)
                .append(docItemId, other.docItemId)
                .append(refDocItemId, other.refDocItemId)
                .append(relationType, other.relationType)
                .append(reason, other.reason)
                .append(refAmount, other.refAmount)
                .append(amount, other.amount)
                .append(createTime, other.createTime)
                .isEquals
        }

        override fun hashCode(): Int {
            return HashCodeBuilder(17, 37)
                .append(id)
                .append(docItemId)
                .append(refDocItemId)
                .append(relationType)
                .append(reason)
                .append(refAmount)
                .append(amount)
                .append(createTime)
                .toHashCode()
        }

        fun toResult(): DocumentItemRelationResult {
            return DocumentItemRelationResult(
                docItemRelationId = id,
                docItemId = docItemId,
                refDocItemId = refDocItemId,
                relationType = relationType,
                reason = reason,
                amount = amount,
                refAmount = refAmount,
                createTime = createTime
            )
        }
}
