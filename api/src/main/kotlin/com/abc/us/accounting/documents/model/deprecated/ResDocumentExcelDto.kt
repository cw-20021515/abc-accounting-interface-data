package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "응답_회계_전표_조회")
//주생성자 사용
class ResDocumentExcelDto (
        @JsonIgnore
        @field:Schema(description = "총 개수", hidden = true)
        var totalCnt: Long = 0L                        /* 총 개수 */
      , var documentId: String? = null                 /* 전표ID */
      , var documentTypeCode: String? = null           /* 전표유형코드 */
      , var documentType: String? = null               /* 전표유형 */
      , var documentStatus: String? = null             /* 전표상태 */
      , var approvalStatus: String? = null             /* 승인상태 */
      , var companyCode: String? = null                /* 회사코드 */
      ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        var documentDate: LocalDate? = null  //증빙일(실제 거래발생일)
      , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        var postingDate: LocalDate? = null             /* 전기일(거래가 재무제표에 반영되는날짜) */
      , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        var entryDate: LocalDate? = null               /* 발행일(회계기록이 시스템에 입력된날짜) */
      , var currency: String? = null                   /* 통화(USD) */
      , var amount: Double? = null                     /* 금액 */
      , var description: String? = null                /* 설명 */
      , var reference: String? = null                  /* 참조사유 */
      , var createId: String? = null                   /* 생성자ID */
      , var referenceDocumentType: String? = null      /* 참조전표유형코드 */
      , var referenceDocumentId: String? = null        /* 참조전표ID */
      , var bizTransactionTypeId: String? = null       /* 비즈거래유형코드 */
      , var bizTransactionId: String? = null           /* 비즈거래ID */
      , var reversalDocumentId: String? = null         /* 역분개전표ID */
      , var reversalReasonCode: String? = null         /* 역분개전표사유코드 */
      , var reversalReason: String? = null             /* 역분개전표사유 */
      , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        var searchTime:OffsetDateTime?=null //조회시간
      , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        var syncTime:OffsetDateTime?=null //동기화시간
    /*******************************
     ******* 이하 엑셀다운로드 관련 ******/
      , var documentItemId   : String? = null /*전표항목번호*/
      , var accountCode      : String? = null /*계정코드*/
      , var accountName      : String? = null /*계정명*/
      , var remark           : String? = null /*적요*/
      , var debitAmount      : Double? = null /*차변('D')*/
      , var creditAmount     : Double? = null /*대변('C')*/
      , var refDocumentItemId: String? = null /*참조전표항목ID*/
      , var costCenter       : String? = null /*코스트센터*/
      , var profitCenter     : String? = null /*손익센터*/
      , var segment          : String? = null /*세그먼트*/
      , var project          : String? = null /*프로젝트*/
      , var customerId       : String? = null /*고객ID*/
      , var orderId          : String? = null /*주문번호*/
      , var orderItemId      : String? = null /*주문아이템ID*/
      , var contractId       : String? = null /*계약ID */
      , var serialNumber     : String? = null /*시리얼번호*/
      , var salesType        : String? = null /*판매유형*/
      , var salesItem        : String? = null /*판매항목*/
      , var rentalCode       : String? = null /*렌탈코드*/
      , var channelId        : String? = null /*채널ID*/
      , var referralCode     : String? = null /*레퍼럴코드 */
      , var vendorId         : String? = null /*거래처ID */
      , var payoutId         : String? = null /*지급ID */
      , var invoiceId        : String? = null /*인보이스 */
      , var purchaseOrderId  : String? = null /* PO */
      , var materialId       : String? = null /*자재ID */
      , var materialType     : String? = null /*자재유형 */
      , var materialCategory : String? = null /*카테고리 */
      , var materialSeries   : String? = null /*품목  */
      , var installType      : String? = null /*설치유형 */
      , var filterType       : String? = null /*필터 */
      , var featureType      : String? = null /*기능군 */
)