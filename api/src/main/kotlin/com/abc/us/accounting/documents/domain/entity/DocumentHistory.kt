package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Document Entity
 * Optimistic Lock으로 동시성 처리
 * 중복방지기능 설정:
 */
@Entity
@Table (
    name = "document_history",
)
class DocumentHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @Comment("전표ID")
    @Column(name = "doc_id")
    val docId: String,

    @Comment("버전정보(Optimistic Lock)")
    val version: Long = Constants.DEFAULT_VERSION,

    // 중복체크 용으로 사용
    @Column(name = "doc_hash")
    val docHash: String,

    @Column(name = "doc_type")
    @Convert(converter = DocumentTypeCodeConverter::class)
    val docType: DocumentType,

    @Column(name = "doc_status")
    @Convert(converter = DocumentStatusConverter::class)
    val docStatus: DocumentStatus,

    @Column(name = "workflow_status")
    @Convert(converter = WorkflowStatusConverter::class)
    val workflowStatus: WorkflowStatus,

    @Column(name = "workflow_id")
    val workflowId: String? = null,


    @Column(name = "document_date")
    val documentDate: LocalDate,

    @Column(name = "posting_date")
    val postingDate: LocalDate,

    @Column(name = "entry_date")
    val entryDate: LocalDate,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "fiscal_year", column = Column(name = "fiscal_year")),
        AttributeOverride(name = "fiscal_month", column = Column(name = "fiscal_month"))
    )
    val fiscalYearMonth: FiscalYearMonth,


    @Column(name = "company_code")
    @Enumerated(EnumType.STRING)
    val companyCode: CompanyCode,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "currency", column = Column(name = "tx_currency")),
        AttributeOverride(name = "amount", column = Column(name = "tx_amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
    )
    var txMoney: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "currency", column = Column(name = "currency")),
        AttributeOverride(name = "amount", column = Column(name = "amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
    )
    var money: Money? = null,

    @Column(name = "reference")
    val reference: String? = null,

    @Column(name = "text")
    val text: String? = null,

    @Column(name = "is_deleted")
    @Convert(converter = YesNoConverter::class)
    val isDeleted: Boolean,

    @Column(name = "create_time", nullable = false, updatable = false)
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_by")
    val createdBy: String = Constants.APP_NAME,

    @Column(name = "update_time", nullable = false)
    var updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: String = Constants.APP_NAME,

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
                ", version=" + version + '\'' +
                ", docType=" + docType + '\'' +
                ", docStatus=" + docStatus + '\'' +
                ", workflowStatus=" + workflowStatus + '\'' +
                ", workflowId=" + workflowId + '\'' +
                ", documentDate=" + documentDate + '\'' +
                ", postingDate=" + postingDate + '\'' +
                ", entryDate=" + entryDate + '\'' +
                ", fiscalYearMonth=" + fiscalYearMonth + '\'' +
                ", companyCode=" + companyCode + '\'' +
                ", txMoney=" + txMoney + '\'' +
                ", money=" + money + '\'' +
                ", reference=" + reference + '\'' +
                ", text=" + text + '\'' +
                ", isDeleted=" + isDeleted + '\'' +
                ", createTime=" + createTime + '\'' +
                ", createdBy=" + createdBy + '\'' +
                ", updateTime=" + updateTime + '\'' +
                ", updatedBy=" + updatedBy + '\'' +
                '}'
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentHistory) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(docId, other.docId)
            .append(version, other.version)
            .append(docType, other.docType)
            .append(docStatus, other.docStatus)
            .append(workflowStatus, other.workflowStatus)
            .append(workflowId, other.workflowId)
            .append(documentDate, other.documentDate)
            .append(postingDate, other.postingDate)
            .append(entryDate, other.entryDate)
            .append(fiscalYearMonth, other.fiscalYearMonth)
            .append(companyCode, other.companyCode)
            .append(txMoney, other.txMoney)
            .append(money, other.money)
            .append(reference, other.reference)
            .append(text, other.text)
            .append(isDeleted, other.isDeleted)
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
            .append(version)
            .append(docType)
            .append(docStatus)
            .append(workflowStatus)
            .append(workflowId)
            .append(documentDate)
            .append(postingDate)
            .append(entryDate)
            .append(fiscalYearMonth)
            .append(companyCode)
            .append(txMoney)
            .append(money)
            .append(reference)
            .append(text)
            .append(isDeleted)
            .append(createTime)
            .append(createdBy)
            .append(updateTime)
            .append(updatedBy)
            .toHashCode()
    }
}
