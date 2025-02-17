package com.abc.us.accounting.collects.model

import org.hibernate.annotations.Comment
import java.time.LocalDate

class SourceDeposit {

    @Comment("외부 거래 ID - 카드사에서 제공하는 거래 ID 등")
    var transactionId : String?=null

    @Comment("입금 ID - shopify payments 등 에서 제공하는 지급 ID")
    var depositId : String?=null

    @Comment("정산 금액의 통화 코드")
    var currency : String?=null

    @Comment("정산이 처리된 날짜를 ISO 8601 형식")
    var depositDate : LocalDate?= null

    @Comment("정산의 총 금액")
    var amount : String? = null

    @Comment("조정 수수료 금액")
    var adjustmentsFeeAmount: String? = null

    @Comment("조정 총 금액")
    var adjustmentsGrossAmount: String? = null

    @Comment("청구 수수료 금액")
    var chargesFeeAmount: String? = null

    @Comment("청구 수수료 금액")
    var chargesGrossAmount: String? = null

    @Comment("환불 수수료 금액")
    var refundsFeeAmount: String? = null

    @Comment("환불 총 금액")
    var refundsGrossAmount: String? = null

    @Comment("예약된 자금의 수수료 금액")
    var reservedFundsFeeAmount: String? = null

    @Comment("예약된 자금의 총 금액")
    var reservedFundsGrossAmount: String? = null

    @Comment("재시도된 정산의 수수료 금액")
    var retriedDepositsFeeAmount: String? = null

    @Comment("재시도된 정산의 총 금액")
    var retriedDepositsGrossAamount: String? = null

    @Comment("판매 수수료 금액")
    var salesFeeAmount: String? = null

    @Comment("판매 총 금액")
    var salesGrossAmount: String? = null

    @Comment("총 수수료 금액")
    var fees: String? = null

    @Comment("총 수익 금액")
    var gross: String? = null

    @Comment("총 순이익 금액")
    var net: String? = null
}