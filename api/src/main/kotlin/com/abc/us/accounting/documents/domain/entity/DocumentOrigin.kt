package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentOriginResult
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "document_origin",
)
class DocumentOrigin(
    @Id
    @Column(name="doc_id")
    val docId: String,

    @Column(name = "doc_template_code")
    @Convert(converter = DocumentTemplateCodeConverter::class)
    val docTemplateCode: DocumentTemplateCode,

    @Column(name = "biz_system")
    @Convert(converter = BizSystemTypeConverter::class)
    val bizSystem: BizSystemType,

    @Column(name = "biz_tx_id")
    val bizTxId: String? = null,

    @Column(name = "biz_process")
    @Convert(converter = BizProcessTypeConverter::class)
    val bizProcess: BizProcessType? = null,

    @Column(name = "biz_event")
    @Convert(converter = BizEventTypeConverter::class)
    val bizEvent: BizEventType? = null,

    @Column(name = "accounting_event")
    val accountingEvent: String?=null,
){

    fun toResult():DocumentOriginResult {
        return DocumentOriginResult(
//            docOriginId = id.toString(),
            docId = docId,
            docTemplateCode = docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizProcess = bizProcess,
            bizEvent = bizEvent,
            accountingEvent = accountingEvent,
        )
    }

    fun toRequest():DocumentOriginRequest {
        return DocumentOriginRequest(
            docTemplateCode = docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizProcess = bizProcess,
            bizEvent = bizEvent,
            accountingEvent = accountingEvent,
        )
    }

}
