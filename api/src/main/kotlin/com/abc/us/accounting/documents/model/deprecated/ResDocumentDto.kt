package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "응답_회계_전표_조회")
//주생성자 사용
class ResDocumentDto (

    //, @JsonIgnore
      @field:Schema(description = "총 개수", hidden = true)
      var totalCnt:Long = 0L                       /* 총 개수 */
    //  ,@JsonInclude(JsonInclude.Include.NON_NULL)  /* null 필드제외 */
    //  @field:Schema(description = "총 개수", hidden = true)
    //  var totalCnt:Long? = 0L

    , var documentId: String? = null               /* 전표ID */
    , var documentTypeCode: String? = null         /* 전표유형코드 */
    , var documentType: String? = null             /* 전표유형 */
    , var documentStatus: String? = null           /* 전표상태 */
    , var approvalStatus: String? = null           /* 승인상태 */
    , var companyCode: String? = null              /* 회사코드 */
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
      var documentDate: LocalDate? = null            /* 증빙일(실제 거래발생일) */
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
      var postingDate: LocalDate? = null             /* 전기일(거래가 재무제표에 반영되는날짜) */
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
      var entryDate: LocalDate? = null               /* 발행일(회계기록이 시스템에 입력된날짜) */
    , var currency: String? = null                 /* 통화(USD) */
    , var amount: Double? = null                   /* 금액 */
    , var remark: String? = null                   /* 설명(적요,비고) */
    , var reference: String? = null                /* 참조사유 */
    , var createId: String? = null                 /* 생성자ID */
    , var referenceDocumentType: String? = null    /* 참조전표유형코드 */
    , var referenceDocumentId: String? = null      /* 참조전표ID */
    , var bizTransactionTypeId: String? = null     /* 비즈거래유형코드 */
    , var bizTransactionId: String? = null         /* 비즈거래ID */
    , var reversalDocumentId: String? = null       /* 역분개전표ID */
    , var reversalReasonCode: String? = null       /* 역분개전표사유코드 */
    , var reversalReason: String? = null           /* 역분개전표사유 */
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var searchTime: OffsetDateTime? = null         /* 조회시간 */
    , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var syncTime: OffsetDateTime? = null           /* 동기화시간 */

    /*****************************
    ,var billingCount: Int? = null //회차(기간)
    ,var accountBalance: Double? = null //계정과목잔액
    ,@Convert(converter = YesNoConverter::class)
    var isCompleted: Boolean? = null
    **********************************/
)