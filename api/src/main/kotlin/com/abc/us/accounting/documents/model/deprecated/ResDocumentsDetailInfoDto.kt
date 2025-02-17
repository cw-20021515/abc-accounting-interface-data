package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "응답_회계_전표_상세_조회")
class ResDocumentsDetailInfoDto(
     var documentId: String? = null             /*전표ID(번호)*/
    ,var documentType: String? = null           /*전표유형*/
    ,var documentStatus: String? = null         /*전표상태*/
    ,var approvalStatus: String? = null         /*승인상태*/
    ,var workFlowId: String? = null             /*워크플로우ID*/
    ,var companyCode: String? = null            /*회사코드*/
    ,var fiscalYear: String? = null             /*회계연도*/
    ,var fiscalMonth: String? = null           /*회계월*/
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var documentDate: LocalDate? = null         /*증빙일*/
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var postingDate: LocalDate? = null          /*전기일*/
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var entryDate: LocalDate? = null           /*발행일*/
    ,var createId: String? = null               /*생성자*/
    ,var reference: String? = null              /*참조 */
    ,var description: String? = null            /*설명*/
    ,var referenceDocumentType: String? = null  /*참조전표유형*/
    ,var referenceDocumentId: String? = null    /*참조전표ID*/
    ,var bizTransactionTypeId: String? = null   /*비즈거래유형(소스유형)*/
    ,var bizTransactionId: String? = null       /*비즈거래ID(소스거래ID)*/
    ,var reversalDocumentId: String? = null     /*역분개전표ID*/
    ,var reversalReason: String? = null         /*역분개사유*/
    ,var lineItems: List<ResDocumentLineItemsDto>? = null //원장_전표상세_항목(LineItems)
    ,var totalDebitAmount  : Double? =null     /*차변('D')*/
    ,var totalCreditAmount : Double? =null     /*대변('C')*/
    ,var relatedDocuments: List<ResDocumentRelatedItemsDto>? = null //원장_전표상세_항목(RelatedDocuments)
    ,var relatedDocumentsSumAmt: List<ResDocumentRelatedItemsDto>? = null //원장_전표상세_항목(RelatedDocuments)
    ,var notes: List<ResDocumentNotesDto>? = null //원장_전표상세_노트
    ,var attachments: List<ResDocumentAttachmentsDto>? = null //원장_전표상세_항목(RelatedDocuments)
)