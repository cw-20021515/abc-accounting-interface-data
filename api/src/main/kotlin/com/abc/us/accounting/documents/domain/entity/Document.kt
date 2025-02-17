package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.Persistable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Document Entity
 * Optimistic Lock으로 동시성 처리
 * 중복방지기능 설정:
 */
@Entity
@Table (
    name = "document",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_doc_hash",
            columnNames = ["doc_hash"]
        )
    ]
)
class Document(
    @Id
    @Comment("전표ID")
    @Column(name = "id")
    private val _id: String,

    @Comment("버전정보(Optimistic Lock)")
    @Version
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

    @Comment("증빙일")
    @Column(name = "document_date")
    val documentDate: LocalDate,

    @Comment("전기일")
    @Column(name = "posting_date")
    val postingDate: LocalDate,

    @Comment("발행일")
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
    var money: Money,

    @Column(name = "reference")
    val reference: String? = null,

    @Column(name = "text")
    val text: String? = null,

    @Column(name = "is_deleted")
    @Convert(converter = YesNoConverter::class)
    val isDeleted: Boolean = false,

    val createTime: OffsetDateTime = OffsetDateTime.now(),
    val createdBy: String = Constants.APP_NAME,
    var updateTime: OffsetDateTime = OffsetDateTime.now(),
    var updatedBy: String = Constants.APP_NAME,

    @Transient
    var items : MutableList<DocumentItem> = mutableListOf(),

    @Transient
    private var _isNew: Boolean = true,

    @Transient
    var roundingDifference : BigDecimal = BigDecimal.ZERO,

    ): Persistable<String> {
    override fun getId(): String = _id
    override fun isNew(): Boolean = _isNew
    @PostPersist
    @PostLoad
    fun markNotNew() {
        _isNew = false
    }

    override fun toString(): String {
        return "Document{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", docType=" + docType +
                ", docHash='" + docHash + '\'' +
                ", documentDate=" + documentDate +
                ", postingDate=" + postingDate +
                ", entryDate=" + entryDate +
                ", fiscal=" + fiscalYearMonth +
                ", docStatus=" + docStatus +
                ", workflowStatus=" + workflowStatus +
                ", workflowId='" + workflowId + '\'' +
                ", companyCode=" + companyCode +
                ", txCurrency=" + txMoney.currency +
                ", txAmount=" + txMoney.amount +
                ", currency=" + money.currency +
                ", amount=" + money.amount +
                ", text='" + text + '\'' +
                ", createTime=" + createTime +
                ", createdBy='" + createdBy + '\'' +
                ", updateTime=" + updateTime +
                ", updatedBy='" + updatedBy + '\'' +
                '}'
    }

    fun calculateDocHash(): String {
        return Hashs.hash(
            docType,
            documentDate,
            postingDate,
            companyCode,
            txMoney,
            text,
            money,
            reference,
        )
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false

        // id 없이 중복인지 확인 필요
        return EqualsBuilder()
            .append(id, other.id)
            .append(docType, other.docType)
            .append(docHash, other.docHash)
            .append(documentDate, other.documentDate)
            .append(postingDate, other.postingDate)
            .append(entryDate, other.entryDate)
            .append(fiscalYearMonth, other.fiscalYearMonth)
            .append(docStatus, other.docStatus)
            .append(workflowStatus, other.workflowStatus)
            .append(workflowId, other.workflowId)
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
            .append(docType)
            .append(docHash)
            .append(documentDate)
            .append(postingDate)
            .append(entryDate)
            .append(fiscalYearMonth)
            .append(docStatus)
            .append(workflowStatus)
            .append(workflowId)
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

    fun copy(newVersion: Long=-1L, docHash:String?=null): Document{
        val copied = Document(
            _id = this.id,
            version = if (newVersion != -1L) newVersion else Constants.DEFAULT_VERSION,
            docType = this.docType,
            docHash = docHash ?: this.docHash,
            documentDate = this.documentDate,
            postingDate = this.postingDate,
            entryDate = this.entryDate,
            fiscalYearMonth =  this.fiscalYearMonth,
            docStatus = docStatus,
            workflowStatus = this.workflowStatus,
            workflowId = this.workflowId,
            companyCode = this.companyCode,
            txMoney = this.txMoney,
            money = this.money,
            reference = this.reference,
            text = text,
            isDeleted = this.isDeleted,
            createTime = this.createTime,
            createdBy = this.createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = this.updatedBy,
            _isNew = this._isNew
        )
        return copied
    }

    fun copy(newText: String?): Document{
        return Document(
            _id = this.id,
            version = this.version,
            docType = this.docType,
            docHash = this.docHash,
            documentDate = this.documentDate,
            postingDate = this.postingDate,
            entryDate = this.entryDate,
            fiscalYearMonth =  this.fiscalYearMonth,
            docStatus = docStatus,
            workflowStatus = this.workflowStatus,
            workflowId = this.workflowId,
            companyCode = this.companyCode,
            txMoney = this.txMoney,
            money = this.money,
            reference = this.reference,
            text = newText,
            isDeleted = this.isDeleted,
            createTime = this.createTime,
            createdBy = this.createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = this.updatedBy,
            _isNew = this._isNew
        )
    }

    fun copy(claimStatus: DocumentStatus): Document{
        // claimStatus에 따라서 다음 상태로 변경
        val newStatus = docStatus.transit(claimStatus)

        return Document(
            _id = this.id,
            version = this.version,
            docType = this.docType,
            docHash = this.docHash,
            documentDate = this.documentDate,
            postingDate = this.postingDate,
            entryDate = this.entryDate,
            fiscalYearMonth =   this.fiscalYearMonth,
            docStatus = newStatus,
            workflowStatus = this.workflowStatus,
            workflowId = this.workflowId,
            companyCode = this.companyCode,
            txMoney =  this.txMoney,
            money = this.money,
            reference = this.reference,
            text = this.text,
            isDeleted = this.isDeleted,
            createTime = this.createTime,
            createdBy = this.createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = this.updatedBy,
            _isNew = this._isNew
        )
    }

    fun copy(money: Money): Document{
        return Document(
            _id = this.id,
            version = this.version,
            docType = this.docType,
            docHash = this.docHash,
            documentDate = this.documentDate,
            postingDate = this.postingDate,
            entryDate = this.entryDate,
            fiscalYearMonth =   this.fiscalYearMonth,
            docStatus = this.docStatus,
            workflowStatus = this.workflowStatus,
            workflowId = this.workflowId,
            companyCode = this.companyCode,
            txMoney = this.txMoney,
            money = money,
            reference = this.reference,
            text = this.text,
            isDeleted = this.isDeleted,
            createTime = this.createTime,
            createdBy = this.createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = this.updatedBy,
            _isNew = this._isNew
        )
    }

    fun toHistory(): DocumentHistory {
        return DocumentHistory(
            docId = id,
            version = version,
            docHash = docHash,
            docType = docType,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYearMonth = fiscalYearMonth,
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            reference = reference,
            text = text,
            isDeleted = isDeleted,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        )
    }

    fun reverse(
        docId:String,
        docType:DocumentType,
        docHash:String,
        documentDate: LocalDate,
        postingDate: LocalDate,
        docStatus: DocumentStatus,
        workflowStatus: WorkflowStatus,
        workflowId: String?,
        createTime: OffsetDateTime,
        createdBy: String,
        fiscalYearMonth: FiscalYearMonth
    ): Document {
        val reverseReference = id
        val reverseText = "$text Reversing"

        return Document(
            _id = docId,
            version = Constants.DEFAULT_VERSION,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = LocalDate.now(),
            fiscalYearMonth = fiscalYearMonth,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            reference = reverseReference,
            text = reverseText,
            isDeleted = false,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = createdBy
        )
    }
    fun toResult(): DocumentResult {
        return DocumentResult(
            docId = id,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYear = fiscalYearMonth.year,
            fiscalMonth = fiscalYearMonth.month,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            workflowId = workflowId,
            companyCode = companyCode,
            txCurrency = txMoney.currencyCode(),
            txAmount = txMoney.amount,
            currency = money.currencyCode(),
            amount = money.amount,
            reference = reference,
            text = text,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        )
    }
}
