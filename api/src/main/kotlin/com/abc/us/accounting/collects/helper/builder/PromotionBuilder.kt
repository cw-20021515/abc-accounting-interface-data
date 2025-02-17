package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.collect.CollectPromotion
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.generated.models.OrderItemPromotion
import com.abc.us.generated.models.OrderItemView
import java.math.BigDecimal

class PromotionBuilder {

    companion object {
        fun makePromotion(orderItemId: String, promotion: OrderItemPromotion): CollectPromotion {
            return CollectPromotion().apply {
                relation = EmbeddableRelation().apply {
                    entity = CollectOrderItem::class.simpleName
                    field = "order_item_id"
                    value = orderItemId
                }
                promotionId = promotion.promotionId
                promotionName = promotion.promotionName
                promotionDescription = promotion.promotionDescription
                startDate = promotion.startDate
                endDate = promotion.endDate
                discountPrice = promotion.discountPrice?.let { price -> BigDecimal(price) }
                // TODO : promotion 기능 추가시 주석 해제
//            promotion.promotion?.let { p ->
//                promotionCycles = p.promotionCycles
//                promotionType = p.promotionType
//
//                p.offer?.let { o ->
//                    offerType = o.offerType
//                    materialId = o.materialId
//                }
//                p.discount?.let { d ->
//                    discountTargetType = d.discountTargetType
//                    discountType = d.discountType
//                    amount = d.amount?.let { BigDecimal(it) }
//                    rate = d.rate?.let { BigDecimal(it) }
//                }
//            }
            }//.apply { hashCode = toEntityHash() }
        }

        fun build(orderItems: MutableList<OrderItemView>): MutableList<CollectPromotion> {

            val converts = mutableListOf<CollectPromotion>()
            orderItems.forEach { orderItem ->
                orderItem.promotions?.let { list ->
                    list.forEach { promotion ->
                        converts.add(makePromotion(orderItem.orderItemId, promotion))
                    }
                }

            }
            return converts
        }
    }
}