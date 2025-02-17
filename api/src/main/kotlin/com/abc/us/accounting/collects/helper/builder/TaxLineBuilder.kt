package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.collect.CollectTaxLine
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.SalesTaxType
import com.abc.us.accounting.collects.helper.OmsApiOrderItemMutableList
import com.abc.us.accounting.collects.helper.OmsApiOrderMutableList
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.OmsBillingCharge
import com.abc.us.generated.models.OmsBillingPayment
import com.abc.us.generated.models.TaxLineProperties
import com.abc.us.oms.domain.billing.entity.ChargePayment
import com.abc.us.oms.domain.taxline.entity.TaxLine
import mu.KotlinLogging
import java.math.BigDecimal


class TaxLineBuilder {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
        fun parseTaxLine(title : String, block : (SalesTaxType)->CollectTaxLine) : CollectTaxLine {
            val hasLocationKeywords = listOf("State", "City", "County")
            val hasSpecialKeywords = listOf("District", "Purpose", "Special","Mta")

            val parts = title.split(" ")
            if (parts.size < 2)
                return block(SalesTaxType.NONE)

            val locationName = parts.takeWhile { it !in hasLocationKeywords && it !in hasSpecialKeywords }.joinToString(" ")
            val remainingWords = parts.dropWhile { it !in hasLocationKeywords && it !in hasSpecialKeywords }.joinToString(" ")

            var salesTaxType = when {
                remainingWords.contains("State", ignoreCase = true) -> SalesTaxType.STATE
                remainingWords.contains("City", ignoreCase = true) -> SalesTaxType.CITY
                remainingWords.contains("County", ignoreCase = true) -> SalesTaxType.COUNTY
                else -> SalesTaxType.NONE
            }
            if(salesTaxType == SalesTaxType.NONE) {
                salesTaxType = when {
                        hasSpecialKeywords.any { remainingWords.contains(it, ignoreCase = true) } -> SalesTaxType.SPECIAL
                        else -> SalesTaxType.NONE
                    }
            }
            return block(salesTaxType)
        }

        fun buildFromLine(r :EmbeddableRelation,taxLine : TaxLine) :  CollectTaxLine {

            return parseTaxLine(taxLine.title) {taxType->
                CollectTaxLine(title = taxLine.title,
                               rate = BigDecimal(taxLine.rate),
                               price = taxLine.price?.let { BigDecimal(it) }?:BigDecimal(0.00),
                               salesTaxType=taxType).apply {
                    relation = r
                }
            }
        }
        fun buildFromProperty(r :EmbeddableRelation, taxLine : TaxLineProperties) : CollectTaxLine {

            return parseTaxLine(taxLine.title) {taxType->
                CollectTaxLine(title = taxLine.title,
                               rate = BigDecimal(taxLine.rate),
                               price = taxLine.price?.let { BigDecimal(it) }?:BigDecimal(0.00),
                               salesTaxType=taxType).apply {
                    relation = r
                }
            }
        }

        fun makeTaxLinesFromPayment(omsPayment: OmsBillingPayment?) :MutableList<CollectTaxLine>? {

            val taxLines = mutableListOf<CollectTaxLine>()
            omsPayment?.let {  payment ->
                payment.taxLines?.let { lines ->
                    lines.forEach { line ->
                        val relatio = EmbeddableRelation().apply {
                            entity = CollectCharge::class.simpleName
                            field = "charge_id"
                            value = payment.chargeId
                        }
                        val taxLine = buildFromProperty(relatio,line)
                        taxLines.add(taxLine)
                    }
                }
            }
            return taxLines
        }

        fun buildFromCharges(omsCharges : List<OmsBillingCharge>) : MutableList<CollectTaxLine> {

            val taxLines = mutableListOf<CollectTaxLine>()
            omsCharges.forEach { omsCharge ->
                // collect tax lines
                //val taxLinesFromChargeItemsPriceDetail = makeTaxLinesFromChargeItems(omsCharge.chargeItems)
                val taxLinesFromPaymentChargeItems = makeTaxLinesFromPayment(omsCharge.payment)
                //taxLinesFromChargeItemsPriceDetail?.let { taxLines.addAll(it) }
                taxLinesFromPaymentChargeItems?.let { taxLines.addAll(it) }
            }
            return taxLines
        }

        fun build(payment : OmsBillingPayment) : MutableList<CollectTaxLine> {
            val taxLines = mutableListOf<CollectTaxLine>()
            payment.taxLines?.let {lines ->
                lines.forEach { line->
                    val taxLine = buildFromProperty(EmbeddableRelation().apply {
                        entity = CollectCharge::class.simpleName
                        field = "charge_id"
                        value = payment.chargeId
                    },line)
                    taxLines.add(taxLine)
                }
            }
            return taxLines
        }
//        fun build(payment : ChargePayment) : MutableList<CollectTaxLine> {
//            val taxLines = mutableListOf<CollectTaxLine>()
//
//            val lines = payment.taxLines?.let { converter.toObj(it,TaxLine::class.java) }
//
//            lines?.let {
//                it.forEach { line->
//                    val taxLine = buildFromProperty(EmbeddableRelation().apply {
//                        entity = CollectCharge::class.simpleName
//                        field = "charge_id"
//                        value = payment.chargeId
//                    }
//                        ,line)
//                    taxLines.add(taxLine)
//                }
//            }
//            return taxLines
//        }


        fun build(orderItems: OmsApiOrderItemMutableList): MutableList<CollectTaxLine> {

            val taxLines = mutableListOf<CollectTaxLine>()
            orderItems.forEach { orderItem ->
                orderItem.price.let {rentalPrice ->
                    rentalPrice.taxLines?.let { lines ->
                        lines.forEach { line->
                            val taxLine = buildFromProperty(EmbeddableRelation().apply {
                                entity = CollectOrderItem::class.simpleName
                                field = "order_item_id"
                                value = orderItem.orderItemId
                            },line)
                            taxLines.add(taxLine)
                        }
                    }
                }
            }
            return taxLines
        }

        fun build(orders: OmsApiOrderMutableList): MutableList<CollectTaxLine> {
            val taxLines = mutableListOf<CollectTaxLine>()
            orders.forEach { order ->
                order.orderItems.forEach { orderItem ->
                    orderItem.contract?.let {
                        it.contractPaymentInfo.price.taxLines?.let {lines ->
                            lines.forEach {taxLine ->
                                val relation = EmbeddableRelation().apply {
                                    entity = CollectOrder::class.simpleName
                                    field = "contract_id"
                                    value = it.contractId
                                }
                                taxLines.add(buildFromProperty(relation,taxLine))
                            }
                        }
                    }
                }
                order.payment?.let { payment ->
                    payment.price.let { price ->
                        price.taxLines?.let { lines ->
                            lines.forEach { line ->
                                val relation = EmbeddableRelation().apply {
                                    entity = CollectOrder::class.simpleName
                                    field = "order_id"
                                    value = order.orderId
                                }
                                taxLines.add(buildFromProperty(relation,line))
                            }
                        }
                    }
                }
            }
            return taxLines
        }
    }
}