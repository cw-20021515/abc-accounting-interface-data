package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectChargeItem
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.type.ChargeItemEnum
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.generated.models.ChargeItemType
import com.abc.us.generated.models.OmsBillingCharge
import com.abc.us.generated.models.OmsBillingChargeItem
import com.abc.us.generated.models.OmsBillingPayment
import com.abc.us.oms.domain.billing.entity.Charge
import com.abc.us.oms.domain.billing.entity.ChargeItem
import com.abc.us.oms.domain.billing.entity.ChargePayment
import java.math.BigDecimal
import java.time.ZoneOffset

class ChargeItemBuilder {
    companion object {
        private fun convertChargeItemType(type: ChargeItemType): ChargeItemEnum {
            return when (type) {
                ChargeItemType.SERVICE_FEE -> ChargeItemEnum.SERVICE_FEE
                ChargeItemType.INSTALLATION_FEE -> ChargeItemEnum.INSTALLATION_FEE
                ChargeItemType.DISMANTILING_FEE -> ChargeItemEnum.DISMANTILING_FEE
                ChargeItemType.REINSTALLATION_FEE -> ChargeItemEnum.REINSTALLATION_FEE
                ChargeItemType.TERMINATION_PENALTY -> ChargeItemEnum.TERMINATION_PENALTY
                ChargeItemType.LATE_FEE -> ChargeItemEnum.LATE_FEE
                ChargeItemType.LOSS_FEE -> ChargeItemEnum.LOSS_FEE
                ChargeItemType.PART_COST -> ChargeItemEnum.PART_COST
                ChargeItemType.RENTAL_FEE -> ChargeItemEnum.RENTAL_FEE
                ChargeItemType.RELOCATION_FEE -> ChargeItemEnum.RELOCATION_FEE
            }
        }

        private fun convertChargeItemType(type: String): ChargeItemEnum {
            return when (type) {
                "SERVICE_FEE" -> ChargeItemEnum.SERVICE_FEE
                "INSTALLATION_FEE" -> ChargeItemEnum.INSTALLATION_FEE
                "DISMANTILING_FEE" -> ChargeItemEnum.DISMANTILING_FEE
                "REINSTALLATION_FEE" -> ChargeItemEnum.REINSTALLATION_FEE
                "TERMINATION_PENALTY" -> ChargeItemEnum.TERMINATION_PENALTY
                "LATE_FEE" -> ChargeItemEnum.LATE_FEE
                "LOSS_FEE" -> ChargeItemEnum.LOSS_FEE
                "PART_COST" -> ChargeItemEnum.PART_COST
                "RENTAL_FEE" -> ChargeItemEnum.RENTAL_FEE
                "RELOCATION_FEE" -> ChargeItemEnum.RELOCATION_FEE
                else -> ChargeItemEnum.NONE
            }
        }



        // payment 정보 내에 cargeItem 있지만 charge 에 있는 것 하고 동일할 것 같아서 일단은 SKIP
//        fun toChargeItemsFromPayment(omsPayment: OmsBillingPayment?) : MutableList<CollectChargeItem> {
//            val chargeItems = mutableListOf<CollectChargeItem>()
//            omsPayment?.let { payment ->
//                payment.chargeItems?.let { items ->
//                    items.forEach { item ->
//                        val chargeItem = makeChargeItem(payment.chargeId!!,item)
//                        chargeItem.apply {
//                            relation = EmbeddableRelation().apply {
//                                entity = CollectReceipt::class.simpleName
//                                field = "recept_id"
//                                value = omsPayment.invoiceId
//                            }
//                        }
//                        chargeItems.add(chargeItem)
//                    }
//                }
//            }
//            return chargeItems
//        }

        fun buildWithCharge(charge : Charge , chargeItem : ChargeItem) : CollectChargeItem {

            val price = EmbeddablePrice(totalPrice = chargeItem.totalPrice.let { BigDecimal(it) },
                                        currency = chargeItem.currency)
                .apply {
                    discountPrice = BigDecimal(chargeItem.discountPrice)
                    itemPrice = BigDecimal(chargeItem.itemPrice)
                    prepaidAmount = chargeItem.prepaidAmount?.let { BigDecimal(it) }
                    isTaxExempt = chargeItem.isTaxExempt ?: false
                }

            val chargeItem = CollectChargeItem(price = price,
                                               chargeId = charge.id,
                                               chargeItemId = chargeItem.id,
                                               chargeItemType = convertChargeItemType(chargeItem.chargeItemType)).apply {
                serviceFlowId = chargeItem.serviceFlowId
                quantity = chargeItem.quantity
                createTime = chargeItem.createTime?.toOffset()
            }
            return chargeItem
        }
        fun buildWithCharge(omsCharge : OmsBillingCharge , chargeItem : OmsBillingChargeItem) : CollectChargeItem {

            val price = EmbeddablePrice(totalPrice = chargeItem.totalPrice.let { BigDecimal(it) },
                                        currency = chargeItem.priceDetail.currency)
                .apply {
                    discountPrice = BigDecimal(chargeItem.priceDetail.discountPrice)
                    itemPrice = BigDecimal(chargeItem.priceDetail.itemPrice)
                    prepaidAmount = chargeItem.priceDetail.prepaidAmount?.let { BigDecimal(it) }
                    isTaxExempt = chargeItem.isTaxExempt ?: false
                }

            val chargeItem = CollectChargeItem(price = price,
                                               chargeId = omsCharge.chargeId,
                                               chargeItemId = chargeItem.chargeItemId,
                                               chargeItemType = convertChargeItemType(chargeItem.chargeItemType)).apply {
                serviceFlowId = chargeItem.serviceFlowId
                quantity = chargeItem.quantity
                createTime = chargeItem.createTime
            }
            return chargeItem
        }


        fun buildWithChargePayment(payment : ChargePayment, chargeItem : ChargeItem) : CollectChargeItem {
            val price = EmbeddablePrice(totalPrice = chargeItem.totalPrice.let { BigDecimal(it) },
                                        currency = chargeItem.currency)
                .apply {
                    discountPrice = BigDecimal(chargeItem.discountPrice)
                    itemPrice = BigDecimal(chargeItem.itemPrice)
                    prepaidAmount = chargeItem.prepaidAmount?.let { BigDecimal(it) }
                    isTaxExempt = chargeItem.isTaxExempt ?: false
                }
            val chargeItem = CollectChargeItem(price = price,
                                               chargeId = payment.chargeId,
                                               chargeItemId = chargeItem.id,
                                               chargeItemType = convertChargeItemType(chargeItem.chargeItemType)).apply {
                serviceFlowId = chargeItem.serviceFlowId
                quantity = chargeItem.quantity
                createTime = chargeItem.createTime?.toOffset()
                invoiceId = payment.invoiceId
            }
            return chargeItem
        }
        fun buildWithPayment(payment : OmsBillingPayment, chargeItem : OmsBillingChargeItem) : CollectChargeItem {
            val price = EmbeddablePrice(totalPrice = chargeItem.totalPrice.let { BigDecimal(it) },
                                        currency = chargeItem.priceDetail.currency)
                .apply {
                    discountPrice = BigDecimal(chargeItem.priceDetail.discountPrice)
                    itemPrice = BigDecimal(chargeItem.priceDetail.itemPrice)
                    prepaidAmount = chargeItem.priceDetail.prepaidAmount?.let { BigDecimal(it) }
                    isTaxExempt = chargeItem.isTaxExempt ?: false
                }
            val chargeItem = CollectChargeItem(price = price,
                                               chargeId = payment.chargeId,
                                               chargeItemId = chargeItem.chargeItemId,
                                               chargeItemType = convertChargeItemType(chargeItem.chargeItemType)).apply {
                serviceFlowId = chargeItem.serviceFlowId
                quantity = chargeItem.quantity
                createTime = chargeItem.createTime
                invoiceId = payment.invoiceId
            }
            return chargeItem
        }

        fun build(charge: OmsBillingCharge) : MutableList<CollectChargeItem> {
            val chargeItems = mutableListOf<CollectChargeItem>()
            charge.chargeItems.forEach { chargeItem->
                chargeItems.add(buildWithCharge(charge,chargeItem))
            }

            charge.payment?.let {
                it.chargeItems.forEach { chargeItem ->
                    chargeItems.add(buildWithPayment(it,chargeItem))
                }
            }
            return chargeItems
        }

        fun build(charge : Charge) : MutableList<CollectChargeItem> {
            val chargeItems = mutableListOf<CollectChargeItem>()
            charge.chargeItems.forEach { chargeItem->
                chargeItems.add(buildWithCharge(charge,chargeItem))
            }
            //
            // TODO hschoi --> OMS 측 데이터 이상으로 보임 추후 다시 확인해봐야함
//            charge.chargePayment?.let {
//                it.chargeItems.forEach { chargeItem ->
//                    chargeItems.add(buildWithChargePayment(it,chargeItem))
//                }
//            }
            return chargeItems
        }
        fun builds(omsCharges : List<OmsBillingCharge>) : MutableList<CollectChargeItem> {
            val chargeItems = mutableListOf<CollectChargeItem>()
            omsCharges.forEach { omsCharge ->

                // collect charge items
                val chargeItemsFromCharge = build(omsCharge)
                //val chargeItemsFromPayment = toChargeItemsFromPayment(omsCharge.payment)

                chargeItems.addAll(chargeItemsFromCharge)
                //chargeItems.addAll(chargeItemsFromPayment)
            }
            return chargeItems
        }
    }
}