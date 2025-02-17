package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.type.ChargeStatusEnum
import com.abc.us.accounting.collects.helper.OmsApiChargeMutableList
import com.abc.us.accounting.collects.helper.OmsEntityCharge
import com.abc.us.accounting.collects.helper.OmsEntityChargeMutableList
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.generated.models.ChargeStatus
import com.abc.us.generated.models.OmsBillingCharge
import java.math.BigDecimal
import java.time.ZoneOffset

class ChargeBuilder {
    companion object {
        private fun convertChargeStatus(status: ChargeStatus?): ChargeStatusEnum {
            return when (status) {
                ChargeStatus.CREATED -> ChargeStatusEnum.CREATED
                ChargeStatus.SCHEDULED -> ChargeStatusEnum.SCHEDULED
                ChargeStatus.PENDING -> ChargeStatusEnum.PENDING
                ChargeStatus.PAID -> ChargeStatusEnum.PAID
                ChargeStatus.UNPAID -> ChargeStatusEnum.UNPAID
                ChargeStatus.OVERDUE -> ChargeStatusEnum.OVERDUE
                null -> TODO()
            }
        }
        private fun convertChargeStatus(status: String?): ChargeStatusEnum {
            return when (status) {
                "CREATED" -> ChargeStatusEnum.CREATED
                "SCHEDULED" -> ChargeStatusEnum.SCHEDULED
                "PENDING" -> ChargeStatusEnum.PENDING
                "PAID" -> ChargeStatusEnum.PAID
                "UNPAID" -> ChargeStatusEnum.UNPAID
                "OVERDUE" -> ChargeStatusEnum.OVERDUE
                null -> TODO()
                else -> {TODO()}
            }
        }

        fun build(charge: OmsBillingCharge): CollectCharge {
            val price = EmbeddablePrice(totalPrice = BigDecimal(charge.totalPrice),
                                        currency = "USD")
                .apply {
                    tax = charge.payment?.let { payment -> payment.tax?.let { BigDecimal(it) } }
                }
            return CollectCharge(price = price,
                                 chargeId = charge.chargeId,
                                 chargeStatus = convertChargeStatus(charge.chargeStatus),
                                 billingCycle = charge.billingCycle,
                                 contractId =  charge.contractId).apply {
                invoiceId = charge.payment?.invoiceId // Payment ID로 참조
                startDate = charge.startDate
                endDate =  charge.endDate
                createTime = charge.createTime
                updateTime = charge.updateTime
                targetMonth = charge.targetMonth
                chargeItems = ChargeItemBuilder.build(charge)
                receipt = charge.payment?.let { ReceiptBuilder.build(it) }
            }
        }

        fun build(charge: OmsEntityCharge): CollectCharge {
            val totalPrice = charge.chargePayment?.let {
                payment -> BigDecimal(payment.totalPrice)
            }?: run {
                BigDecimal.ZERO
            }

            val price = EmbeddablePrice(totalPrice = totalPrice,
                currency = "USD")
                .apply {
                    tax = charge.chargePayment?.let { payment -> payment.tax?.let { BigDecimal(it) } }
                }
            return CollectCharge(price = price,
                chargeId = charge.id,
                chargeStatus = convertChargeStatus(charge.chargeStatus),
                billingCycle = charge.billingCycle,
                contractId =  charge.contractId).apply {
                invoiceId = charge.chargePayment?.invoiceId // Payment ID로 참조
                startDate = charge.startDate
                endDate =  charge.endDate
                createTime = charge.createTime?.toOffset()
                updateTime = charge.updateTime?.toOffset()
                targetMonth = charge.targetMonth
                chargeItems = ChargeItemBuilder.build(charge)
                receipt = charge.chargePayment?.let { ReceiptBuilder.build(it) }
            }
        }
        fun builds(chages: OmsApiChargeMutableList): MutableList<CollectCharge> {
            val collectChargeList = mutableListOf<CollectCharge>()
            chages.forEach { charge ->
                collectChargeList.add(build(charge))
            }
            return collectChargeList
        }

        fun builds(charges: OmsEntityChargeMutableList): MutableList<CollectCharge> {
            val collectChargeList = mutableListOf<CollectCharge>()
            charges.forEach { charge ->
                collectChargeList.add(build(charge))
            }
            return collectChargeList
        }
    }
}