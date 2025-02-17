package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "원장_전표상세_항목(LineItems)")
class ResDocumentRelatedItemsDto(
     var documentId           : String? =null  /*전표ID*/
    ,var documentTypeCode     : String? =null  /*전표유형코드*/
    ,var documentType         : String? =null  /*전표유형*/
    ,var companyCode          : String? =null  /*회사코드*/
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var postingDate: LocalDate? = null        /*전기일(거래가 재무제표에 반영되는날짜)*/
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var entryDate: LocalDate? = null          /*발행일(회계기록이 시스템에 입력된날짜)*/
    ,var referenceDocumentType: String? =null  /*참조전표유형*/
    ,var referenceDocumentId  : String? =null  /*참조전표ID*/
    ,var bizTransactionTypeId : String? =null  /*비즈거래유형(소스유형)*/
    ,var bizTransactionId     : String? =null  /*비즈거래ID(소스거래ID)*/
)