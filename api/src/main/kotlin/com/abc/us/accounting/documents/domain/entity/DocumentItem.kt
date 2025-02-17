package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentItemResult
import jakarta.persistence.*
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.springframework.data.domain.Persistable
import java.time.OffsetDateTime


@Entity
@Table(name = "document_item")
class DocumentItem(
    @Id
    @Column(name = "id")
    private val _id: String,

    @Comment("버전정보(Optimistic Lock)")
    @Version
    val version: Long = Constants.DEFAULT_VERSION,

    @Column(name = "doc_item_status")
    @Convert(converter = DocumentItemStatusConverter::class)
    val docItemStatus: DocumentItemStatus,

    @Column(name = "doc_id")
    val docId:String,

    @Column(name = "line_number")
    val lineNumber: Int,

    @Column(name = "account_code")
    val accountCode: String,

    @Comment("차대구분")
    @Column(name = "account_side")
    @Convert(converter = AccountSideConverter::class)
    val accountSide: AccountSide,

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

    @Column(name = "exchange_rate_id")
    val exchangeRateId: String? = null,

    @Column(name = "text")
    val text: String,

    @Column(name="doc_template_code")
    @Convert(converter = DocumentTemplateCodeConverter::class)
    val docTemplateCode:DocumentTemplateCode? = null,

    val costCenter: String,
    val profitCenter: String? = null,
    val segment:String? = null,
    val project: String? = null,
    val customerId: String? = null,
    val vendorId: String? = null,

    val createTime: OffsetDateTime = OffsetDateTime.now(),
    val createdBy: String = Constants.APP_NAME,
    var updateTime: OffsetDateTime = OffsetDateTime.now(),
    val updatedBy: String = Constants.APP_NAME,

    @Transient
    var attributes : MutableList<DocumentItemAttribute> = mutableListOf(),

    @Transient
    private var _isNew: Boolean = true
): Persistable<String> {
    override fun getId(): String = _id
    override fun isNew(): Boolean = _isNew

    @PostPersist
    @PostLoad
    fun markNotNew() {
        _isNew = false
    }

    fun toAccountKey():AccountKey{
        return AccountKey.of(companyCode, accountCode)
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "id='" + id + '\'' +
                ", docId='" + docId + '\'' +
                ", lineNumber=" + lineNumber + '\'' +
                ", docItemStatus=" + docItemStatus + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", accountCode='" + accountCode + '\'' +
                ", accountSide=" + accountSide + '\'' +
                ", originalCurrency='" + txMoney.currency + '\'' +
                ", originalAmount=" + txMoney.amount + '\'' +
                ", currency='" + money.currency + '\'' +
                ", amount=" + money.amount + '\'' +
                ", exchangeRateId='" + exchangeRateId + '\'' +
                ", text='" + text + '\'' +
                ", docTemplateCode=" + docTemplateCode + '\'' +
                ", customerId='" + customerId + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", cost_center='" + costCenter + '\'' +
                ", profit_center='" + profitCenter + '\'' +
                ", segment='" + segment + '\'' +
                ", project='" + project + '\'' +
                ", createTime=" + createTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updateTime=" + updateTime + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentItem) return false

        return id == other.id
                && docId == other.docId
                && lineNumber == other.lineNumber
                && accountCode == other.accountCode
                && accountSide == other.accountSide
                && companyCode == other.companyCode
                && txMoney == other.txMoney
                && money == other.money
                && exchangeRateId == other.exchangeRateId
                && text == other.text
                && createTime == other.createTime
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(docId)
            .append(lineNumber)
            .append(accountCode)
            .append(accountSide)
            .append(companyCode)
            .append(txMoney)
            .append(money)
            .append(exchangeRateId)
            .append(text)
            .append(createTime)
            .toHashCode()
    }

    fun toHistory(): DocumentItemHistory {
        return DocumentItemHistory(
            docItemId = getDocumentItemId( docId, lineNumber),
            version = version,
            docItemStatus = docItemStatus,
            docId = docId,
            lineNumber = lineNumber,
            accountCode = accountCode,
            accountSide = accountSide,
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            exchangeRateId = exchangeRateId,
            text = text,
            docTemplateCode = docTemplateCode,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = customerId,
            vendorId = vendorId,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        )
    }

    fun copy (newVersion:Long = -1L):DocumentItem {
        return DocumentItem(
            _id = this.id,
            version = if (newVersion == -1L) this.version else newVersion,
            docItemStatus = docItemStatus,
            docId = docId,
            lineNumber = lineNumber,
            accountCode = accountCode,
            accountSide = accountSide,
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            exchangeRateId = exchangeRateId,
            text = text,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = customerId,
            vendorId = vendorId,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
            _isNew = isNew
        )
    }

    fun copy (claimStatus: DocumentItemStatus):DocumentItem {
        // claimStatus에 따라서 다음 상태로 변경
        val newStatus = docItemStatus.transit(claimStatus)

        return DocumentItem(
            _id = this.id,
            version = this.version,
            docItemStatus = newStatus,
            docId = docId,
            lineNumber = lineNumber,
            accountCode = accountCode,
            accountSide = accountSide,
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            exchangeRateId = exchangeRateId,
            text = text,
            docTemplateCode = docTemplateCode,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = customerId,
            vendorId = vendorId,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
            _isNew = isNew
        )
    }

    fun reversal(docId:String, createTime:OffsetDateTime, createdBy:String): DocumentItem {
        return DocumentItem(
            _id = getDocumentItemId(docId, this.lineNumber),
            docItemStatus = DocumentItemStatus.REVERSAL,
            version = version,
            docId = docId,
            lineNumber = lineNumber,
            accountCode = accountCode,
            accountSide = accountSide.reverse(),
            companyCode = companyCode,
            txMoney = txMoney,
            money = money,
            exchangeRateId = exchangeRateId,
            text = text,
            docTemplateCode = docTemplateCode,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = customerId,
            vendorId = vendorId,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = createTime,
            updatedBy = createdBy,
            _isNew = isNew
        )
    }

    fun toResult(isOpenItemMgmt:Boolean): DocumentItemResult {
        val result = DocumentItemResult(
            docItemId = id,
            docId = docId,
            lineNumber = lineNumber,
            docItemStatus = docItemStatus,
            status = docItemStatus.getOpenItemStatus(isOpenItemMgmt),   // TODO: account에서 읽어서 처리
            companyCode = companyCode,
            accountCode = accountCode,
            accountSide = accountSide,
            txCurrency = txMoney.currency.currencyCode,
            txAmount = txMoney.amount,
            currency = money.currency.currencyCode,
            amount = money.amount,
            exchangeRateId = exchangeRateId,
            text = text,
            docTemplateCode = docTemplateCode,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = customerId,
            vendorId = vendorId,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy,
        )
        return result
    }


    companion object {
        fun getDocumentItemId(docId: String, sequence: Int): String {
            return String.format("%s-%03d", docId, sequence)
        }
    }
}

