package com.abc.us.accounting.collects.helper.builder

//import com.abc.us.accounting.collects.domain.entity.collect.CollectChargeItem
//import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
//import com.abc.us.accounting.collects.domain.entity.collect.CollectPrice
//import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
//import com.abc.us.generated.models.*
//import java.math.BigDecimal

class PriceDetailBuilder {

//    companion object {
//        fun makePriceDetail(chargeItem: OmsBillingChargeItem, priceDetail: OmsChargePriceDetail): CollectPrice {
//            return CollectPrice().apply {
//                relation = EmbeddableRelation().apply {
//                    entity = CollectChargeItem::class.simpleName
//                    field = "charge_item_id"
//                    value = chargeItem.chargeItemId
//                }
//                discountPrice = priceDetail.discountPrice?.let { BigDecimal(it) }
//                itemPrice = priceDetail.itemPrice?.let { BigDecimal(it) }
//                prepaidAmount = priceDetail.prepaidAmount?.let { BigDecimal(it) }
//                //tax = priceDetail.tax?.let { BigDecimal(it) }
//                currency = priceDetail.currency
//            }
//        }
//
//        fun makePriceDetailFromPayment(omsPayment: OmsBillingPayment?): MutableList<CollectPrice>? {
//            return omsPayment?.let { payment ->
//                payment.chargeItems?.let { chargeItems ->
//                    makePriceDetailFromChargeItems(chargeItems)
//                }
//            }
//        }
//
//        fun makePriceDetailFromChargeItems(omsChargeItems: List<OmsBillingChargeItem>?): MutableList<CollectPrice>? {
//            return omsChargeItems?.let { chargeItems ->
//                val priceDetails = mutableListOf<CollectPrice>()
//                chargeItems.forEach { chargeItem ->
//                    chargeItem.priceDetail?.let { priceDetail ->
//                        priceDetails.add(makePriceDetail(chargeItem, priceDetail))
//                    }
//                }
//                priceDetails
//            }
//        }
//
//        fun makePrice(price: RetailPrice): CollectPrice {
//            return CollectPrice().apply {
//                totalPrice = price.totalPrice?.let { BigDecimal(it) }
//                discountPrice = price.discountPrice?.let { BigDecimal(it) }
//                itemPrice = price.itemPrice?.let { BigDecimal(it) }
//                prepaidAmount = price.prepaidAmount?.let { BigDecimal(it) }
//                tax = price.tax?.let { BigDecimal(it) }
//                currency = price.currency
//                registrationPrice = price.registrationPrice?.let { BigDecimal(it) }
//            }
//        }
//
//        fun makePriceFromOrderItem(itemView: OrderItemView): CollectPrice? {
//            return itemView.price?.let { price ->
//                makePrice(price).apply {
//                    relation = EmbeddableRelation().apply {
//                        entity = CollectChargeItem::class.simpleName
//                        field = "order_item_id"
//                        value = itemView.orderItemId
//                    }
//                }
//            }
//        }
//
//        fun makePriceFromOrder(orderView: OrderView): CollectPrice? {
//            return orderView.payment?.let { payment ->
//                payment.price?.let { price ->
//                    makePrice(price).apply {
//                        relation = EmbeddableRelation().apply {
//                            entity = CollectOrder::class.simpleName
//                            field = "order_id"
//                            value = orderView.orderId
//                        }
//                    }
//                }
//            }
//        }

//        fun buildFromCharges(omsCharges: List<OmsBillingCharge>): MutableList<CollectPrice> {
//            val priceDetails = mutableListOf<CollectPrice>()
//
//            omsCharges.forEach { omsCharge ->
//                val priceDetailFromChargeItem = makePriceDetailFromChargeItems(omsCharge.chargeItems)
//                val priceDetailFromPayment = makePriceDetailFromPayment(omsCharge.payment)
//
//                priceDetailFromChargeItem?.let { priceDetails.addAll(it) }
//                priceDetailFromPayment?.let { priceDetails.addAll(it) }
//            }
//            return priceDetails
//        }
//
//        fun buildFromOrderItems(orderItems: MutableList<OrderItemView>): MutableList<CollectPrice> {
//
//            val prices = mutableListOf<CollectPrice>()
//            orderItems.forEach { orderItem ->
//                makePriceFromOrderItem(orderItem)?.let { prices.add(it) }
//            }
//            return prices
//        }
//
//        fun buildFromOrders(originOrders: MutableList<OrderView>): MutableList<CollectPrice> {
//
//            val prices = mutableListOf<CollectPrice>()
//            originOrders.forEach { order ->
//                makePriceFromOrder(order)?.let {
//                    prices.add(it)
//                }
//            }
//            return prices
//        }
//    }
}