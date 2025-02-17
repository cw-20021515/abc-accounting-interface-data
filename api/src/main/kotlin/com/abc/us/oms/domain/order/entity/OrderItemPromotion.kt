package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.promotion.entity.Promotion
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "order_item_promotion", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderItemPromotion(
    @Column(name = "order_item_id")
    val orderItemId: String,

    @Column(name = "promotion_id")
    val promotionId: String,

    @Column(name = "discount_price")
    val discountPrice: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", referencedColumnName = "id", insertable = false, updatable = false)
    var orderItem: OrderItem? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", insertable = false, updatable = false)
    var promotion: Promotion? = null,
) : AuditTimeEntity()
