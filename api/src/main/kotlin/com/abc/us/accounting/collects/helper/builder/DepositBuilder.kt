package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectDeposit
import com.abc.us.accounting.collects.model.SourceDeposit
import com.abc.us.accounting.supports.entity.toEntityId
import com.abc.us.generated.models.OmsBillingCharge
import java.math.BigDecimal
import java.time.OffsetDateTime

class DepositBuilder {
    companion object {
        fun makeMockup(omsCharge : OmsBillingCharge) : CollectDeposit? {
            return CollectDeposit().apply {
                createTime =OffsetDateTime.now()
                updateTime = OffsetDateTime.now()
                depositId = toEntityId()
                currency = "USD"
                amount = BigDecimal(omsCharge.totalPrice).toString()   //정산의 총 금액
                adjustmentsFeeAmount = "0.00" //조정 수수료 금액
                adjustmentsGrossAmount = "0.00" //조정 총 금액
                chargesFeeAmount = "0.00" //조정 총 금액
                chargesGrossAmount = "0.00" //조정 총 금액
                refundsFeeAmount = "0.00" //조정 총 금액
                refundsGrossAmount = "0.00" //조정 총 금액
                reservedFundsFeeAmount = "0.00" //조정 총 금액
                reservedFundsGrossAmount = "0.00" //조정 총 금액
                retriedDepositsFeeAmount = "0.00" //조정 총 금액
                retriedDepositsGrossAamount = "0.00" //조정 총 금액
                salesFeeAmount = "0.00" //조정 총 금액
                salesGrossAmount = "0.00" //조정 총 금액
                fees = "0.00" //조정 총 금액
                gross = "0.00" //조정 총 금액
                net = "0.00" //조정 총 금액
            }
        }
        fun makeDeposit(source : SourceDeposit)  : CollectDeposit? {
            return CollectDeposit().apply {
//                createTime =
//                updateTime
//                depositId = toEntityId()
//                currency =
//                    amount = BigDecimal(charge.totalPrice).toString()   //정산의 총 금액
//                adjustmentsFeeAmount = "0.00" //조정 수수료 금액
//                adjustmentsGrossAmount = "0.00" //조정 총 금액
//                chargesFeeAmount = "0.00" //조정 총 금액
//                chargesGrossAmount = "0.00" //조정 총 금액
//                refundsFeeAmount = "0.00" //조정 총 금액
//                refundsGrossAmount = "0.00" //조정 총 금액
//                reservedFundsFeeAmount = "0.00" //조정 총 금액
//                reservedFundsGrossAmount = "0.00" //조정 총 금액
//                retriedDepositsFeeAmount = "0.00" //조정 총 금액
//                retriedDepositsGrossAamount = "0.00" //조정 총 금액
//                salesFeeAmount = "0.00" //조정 총 금액
//                salesGrossAmount = "0.00" //조정 총 금액
//                fees = "0.00" //조정 총 금액
//                gross = "0.00" //조정 총 금액
//                net = "0.00" //조정 총 금액
            }
        }
        fun mockup(omsCharges : List<OmsBillingCharge>) :  MutableList<CollectDeposit> {

            val deposits = mutableListOf<CollectDeposit>()
            omsCharges.forEach { charge ->
                makeMockup(charge)?.let { deposits.add(it) }
            }
            return deposits
        }
        fun build(omsCharges : List<OmsBillingCharge>) :  MutableList<CollectDeposit> {

            val deposits = mutableListOf<CollectDeposit>()
            omsCharges.forEach { charge ->
                //makeDeposit(charge)?.let { deposits.add(it) }
            }
            return deposits
        }
    }
}