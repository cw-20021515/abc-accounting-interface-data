package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime


@Entity
@Table(name = "document_item_history")
class DocumentItemHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @Column(name = "doc_item_id")
    val docItemId: String,

    @Comment("버전정보(Optimistic Lock)")
//    @Version
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
    val exchangeRateId: String?,

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
) {

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updateTime = OffsetDateTime.now()
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "id='" + id + '\'' +
                ", docId='" + docId + '\'' +
                ", lineNumber=" + lineNumber + '\'' +
                ", accountCode='" + accountCode + '\'' +
                ", accountSide=" + accountSide + '\'' +
                ", companyCode=" + companyCode + '\'' +
                ", originalCurrency='" + txMoney.currency + '\'' +
                ", originalAmount=" + txMoney.amount + '\'' +
                ", currency='" + money.currency + '\'' +
                ", amount=" + money.amount + '\'' +
                ", exchangeRateId='" + exchangeRateId + '\'' +
                ", text='" + text + '\'' +
                ", createTime=" + createTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updateTime=" + updateTime + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentItemHistory) return false
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
}

