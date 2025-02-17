package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "응답_회계_원장_조회")
//주생성자 사용
class ResLedgerDto (
     @JsonInclude(JsonInclude.Include.NON_NULL)  /* null 필드제외 */
     @field:Schema(description = "총 개수", hidden = true)
     var totalCnt:Long? = 0L                         /* 총 개수 */
    ,var documentStatus: String? = null //전표상태(전체,미결,반제)
    ,var documentId: String? = null        //전표ID
    ,var documentTypeCode: String? = null  //전표유형코드
    ,var documentType: String? = null      //전표유형
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var documentDate: LocalDate? = null  //증빙일(실제 거래발생일)
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var postingDate: LocalDate? = null   //전기일(거래가 재무제표에 반영되는날짜)
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
     var entryDate: LocalDate? = null     //발행일(회계기록이 시스템에 입력된날짜)
    ,var documentItemId   : String? = null //전표항목ID
    ,var companyCode      : String? = null //회사코드
    ,var accountCode      : String? = null //계정코드
    ,var accountName      : String? = null //계정명
    ,var remark           : String? = null /*적요*/
    ,var currency         : String? = null //통화(USD)
    ,var debitAmount      : Double? = null /*차변('D')*/
    ,var creditAmount     : Double? = null /*대변('C')*/
    ,var balance          : Double? = null //잔액
    ,var costCenter       : String? = null /*코스트센터*/
    ,var profitCenter     : String? = null /*손익센터*/
    ,var segment          : String? = null /*세그먼트*/
    ,var project          : String? = null /*프로젝트*/
    ,var customerId       : String? = null /*고객ID*/
    ,var orderId          : String? = null /*주문번호*/
    ,var orderItemId      : String? = null /*주문아이템ID*/
    ,var serialNumber     : String? = null /*시리얼번호*/
    ,var salesType        : String? = null /*판매유형(AccountingLeaseType)*/
    ,var salesItem        : String? = null /*판매항목*/
    ,var rentalCode       : String? = null /*렌탈코드*/
    ,var channelId        : String? = null /*채널ID*/
    ,var referralCode     : String? = null /*레퍼럴코드 */
    ,var vendorId         : String? = null /*거래처ID */
    ,var payoutId         : String? = null /*지급ID */
    ,var invoiceId        : String? = null /*인보이스 */
    ,var purchaseOrderId  : String? = null /*PO */
    ,var materialId       : String? = null /*자재ID */
    ,var materialType     : String? = null /*자재유형 */
    ,var materialCategory : String? = null /*카테고리 */
    ,var installType      : String? = null /*설치유형 */
    ,var filterType       : String? = null /*필터 */
    ,var featureType      : String? = null /*기능군 */
    ,var contractId       : String? = null /*계약ID */
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
     var searchTime:OffsetDateTime?=null //조회시간
    ,@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
     var syncTime:OffsetDateTime?=null //동기화시간

)