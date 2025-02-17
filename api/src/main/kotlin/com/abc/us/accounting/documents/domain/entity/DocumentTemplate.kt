package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.rentals.master.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@Table (
    name = "document_template",
)
class DocumentTemplate  (
    @Id
    @EmbeddedId
    val docTemplateKey: DocumentTemplateKey,

    @Comment("심볼")
    @Column(name="symbol", nullable = false)
    val symbol: String,

    @Comment("한글 텍스트")
    @Column(name = "kor_text", nullable = false)
    val korText:String,

    @Comment("영문 텍스트")
    @Column(name = "eng_text", nullable = false)
    val engText: String?,

    @Comment("카테고리")
    @Column(name = "biz_category", nullable = false)
    @Enumerated(EnumType.STRING)
    val bizCategory: BizCategory,

    @Comment("판매유형")
    @Column(name = "biz_system", nullable = false)
    @Enumerated(EnumType.STRING)
    val bizSystem: BizSystemType,

    @Comment("비즈니스 프로세스")
    @Column(name = "biz_process", nullable = false)
    @Enumerated(EnumType.STRING)
    val bizProcess: BizProcessType,

    @Comment("비즈니스 이벤트")
    @Column(name = "biz_event", nullable = false)
    @Enumerated(EnumType.STRING)
    val bizEvent: BizEventType,

    @Comment("회계 이벤트 유형")
    @Column(name = "account_event_category")
    @Enumerated(EnumType.STRING)
    val accountEventCategory: AccountEventCategory? = null,

    @Comment("회계 이벤트")
    @Column(name = "account_event")
    @Enumerated(EnumType.STRING)
    val accountEvent: AccountEventType? = null,


    @Comment("사용여부")
    @Column(name = "is_active", nullable = false)
    @Convert(converter = YesNoConverter::class)
    val isActive: Boolean,

    @Comment("비즈니스 이벤트 순서")
    @Column(name = "biz_event_order")
    val bizEventOrder:Int,


    @Comment("전표유형")
    @Column(name = "document_type")
    @Convert(converter = DocumentTypeNameConverter::class)
    val documentType: DocumentType,

    @Comment("처리유형")
    @Column(name = "processing_type")
    @Enumerated(EnumType.STRING)
    val processingType: ProcessingType,

    @Comment("주문항목 상태")
    @Column(name = "order_item_status")
    @Enumerated(EnumType.STRING)
    val orderItemStatus: OrderItemStatus?,

    @Comment("서비스 플로우 상태")
    @Column(name = "service_flow_type")
    @Enumerated(EnumType.STRING)
    val serviceFlowType: ServiceFlowType?,

    @Comment("서비스 플로우 상태")
    @Column(name = "service_flow_status")
    @Enumerated(EnumType.STRING)
    val serviceFlowStatus: ServiceFlowStatus?,

    @Comment("청구 상태")
    @Column(name = "charge_status")
    @Enumerated(EnumType.STRING)
    val chargeStatus: OmsChargeStatus,

    @Comment("계약 상태")
    @Column(name = "contract_status")
    @Enumerated(EnumType.STRING)
    val contractStatus: ContractStatus?,

    // 물류 상태 추가 필요
    @Comment("물류 상태")
    @Column(name = "logistics_status")
//    @Enumerated(EnumType.STRING)
    val logisticsStatus: String?,


    @Comment("생성 일시")
    var createTime: OffsetDateTime = OffsetDateTime.now(),
    ) {

    override fun toString(): String {
    return this.javaClass.simpleName + "{" +
            "company_code='" + docTemplateKey.companyCode + '\'' +
            ", doc_template_code='" + docTemplateKey.docTemplateCode + '\'' +
            ", symbol='" + symbol + '\'' +
            ", bizCategory='" + bizCategory + '\'' +
            ", bizSystem=" + bizSystem + '\'' +
            ", bizProcess=" + bizProcess + '\'' +
            ", bizEventType=" + bizEvent + '\'' +
            ", korText='" + korText + '\'' +
            ", engText='" + engText + '\'' +
            ", bizEventOrder=" + bizEventOrder + '\'' +
            ", documentType=" + documentType + '\'' +
            ", processingType=" + processingType + '\'' +
            ", orderItemStatus=" + orderItemStatus + '\'' +
            ", serviceFlowType=" + serviceFlowType + '\'' +
            ", serviceFlowStatus=" + serviceFlowStatus + '\'' +
            ", billingStatus=" + chargeStatus + '\'' +
            ", contractStatus=" + contractStatus + '\'' +
            ", logisticsStatus='" + logisticsStatus + '\'' +
            ", createTime=" + createTime + '\'' +
            '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentTemplate) return false

        return EqualsBuilder()
            .append(docTemplateKey, other.docTemplateKey)
            .append(bizCategory, other.bizCategory)
            .append(bizSystem, other.bizSystem)
            .append(bizProcess, other.bizProcess)
            .append(bizEvent, other.bizEvent)
            .append(korText, other.korText)
            .append(engText, other.engText)
            .append(documentType, other.documentType)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(docTemplateKey)
            .append(bizCategory)
            .append(bizSystem)
            .append(bizProcess)
            .append(bizEvent)
            .append(korText)
            .append(engText)
            .append(documentType)
            .toHashCode()
    }

    fun toDocumentOriginRequest(bizTxId: String): DocumentOriginRequest {
        return DocumentOriginRequest(
            docTemplateCode = docTemplateKey.docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizProcess = bizProcess,
            bizEvent = bizEvent,
            accountingEvent = accountEvent.toString()
        )
    }
}