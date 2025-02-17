package com.abc.us.accounting.documents.model.deprecated

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "원장_전표상세_항목(LineItems)")
class ResDocumentLineItemsDto(
     var documentItemId   : String? =null     /*전표항목번호_lpad(4)*/
    ,var companyCode      : String? =null     /*회사코드*/
    ,var accountCode      : String? =null     /*계정코드*/
    ,var accountName      : String? =null     /*계정명*/
    ,var remark           : String? =null     /*적요*/
    ,var currency         : String? =null     /*통화*/
    ,var debitAmount      : Double? =null     /*차변('D')*/
    ,var creditAmount     : Double? =null     /*대변('C')*/
    ,var documentStatus   : String? =null     /*전표상태_*/
    ,var refDocumentItemId: String? =null     /*참조전표항목ID*/
    ,var costCenter       : String? =null     /*코스트센터*/
    ,var profitCenter     : String? =null     /*손익센터*/
    ,var segment          : String? =null     /*세그먼트*/
    ,var project          : String? =null     /*프로젝트*/
    ,var id               : String? =null     /*ACCOUNTING_PROCESSING_DATA_ID*/
    ,var documentId       : String? =null     /*전표ID*/
    ,var customerId       : String? =null     /*고객ID*/
    ,var orderId          : String? =null     /*주문번호*/
    ,var orderItemId      : String? =null     /*주문아이템ID*/
    ,var contractId       : String? =null     /*계약ID */
    ,var serialNumber     : String? =null     /*시리얼번호*/
    ,var salesType        : String? =null     /*판매유형*/
    ,var salesItem        : String? =null     /*판매항목*/
    ,var rentalCode       : String? =null     /*렌탈코드*/
    ,var contractPeriod   : String? =null     /*계약기간 */
    ,var contractPeriodNo : String? =null     /*계약회차 */
    ,var channelId        : String? =null     /*채널ID*/
    ,var referralCode     : String? =null     /*레퍼럴코드 */
    ,var vendorId         : String? =null     /*거래처ID */
    ,var payoutId         : String? =null     /*지급ID */
    ,var invoiceId        : String? =null     /*인보이스 */
    ,var purchaseOrderId  : String? =null     /*PO */
    ,var materialId       : String? =null     /*자재ID */
    ,var materialType     : String? =null     /*자재유형 */
    ,var materialCategory : String? =null     /*카테고리 */
    ,var materialSeries   : String? =null     /*품목 */
    ,var installType      : String? =null     /*설치유형 */
    ,var filterType       : String? =null     /*필터  */
    ,var featureType      : String? =null     /*기능군 */
)