package com.abc.us.accounting.documents.domain.entity

import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDateTime
import java.time.OffsetDateTime


@Entity
@Table(name = "document_note")
class DocumentNote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "doc_id")
    val docId: String,

    @Column(name = "is_deleted")
    val isDeleted: Char,

    @Column(name = "contents")
    val contents: String,

    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String,

    @Column(name = "update_time")
    var updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String
) {

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updateTime = OffsetDateTime.now()
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "id='" + id + '\'' +
                ", docId=" + docId + '\'' +
                ", isDeleted=" + isDeleted + '\'' +
                ", contents=" + contents + '\'' +
                ", createTime=" + createTime + '\'' +
                ", createdBy=" + createdBy + '\'' +
                ", updateTime=" + updateTime + '\'' +
                ", updatedBy=" + updatedBy + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentNote) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(docId, other.docId)
            .append(isDeleted, other.isDeleted)
            .append(contents, other.contents)
            .append(createTime, other.createTime)
            .append(createdBy, other.createdBy)
            .append(updateTime, other.updateTime)
            .append(updatedBy, other.updatedBy)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(docId)
            .append(isDeleted)
            .append(contents)
            .append(createTime)
            .append(createdBy)
            .append(updateTime)
            .append(updatedBy)
            .toHashCode()
    }
}
