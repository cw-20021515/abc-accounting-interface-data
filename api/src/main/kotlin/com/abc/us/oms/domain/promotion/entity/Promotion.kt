package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.coupon.entity.Coupon
import com.abc.us.oms.domain.order.entity.OrderItemPromotion
import com.abc.us.oms.domain.promotiontarget.entity.PromotionTarget
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "promotion", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Promotion(
    @Id
    val id: String,
    @Column(name = "promotion_plan_id")
    var promotionPlanId: String? = null,
    @Column(name = "promotion_name")
    var promotionName: String? = null,
    @Column(name = "promotion_type")
    var promotionType: String,
    @Column(name = "promotion_cycles")
    var promotionCycles: List<String>? = null,
    @OneToOne(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotionOffer: PromotionOffer? = null,
    @OneToOne(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotionDiscount: PromotionDiscount? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_plan_id", referencedColumnName = "id", insertable = false, updatable = false)
    var promotionPlan: PromotionPlan? = null,
    @OneToOne(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotionTarget: PromotionTarget? = null,
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var orderItemPromotions: MutableList<OrderItemPromotion>? = mutableListOf(),
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotionMaterials: MutableList<PromotionMaterial>? = mutableListOf(),
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotionExcludeList: MutableList<PromotionExcludeList>? = mutableListOf(),
    @OneToOne(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var coupon: Coupon? = null,
) : AuditTimeOnlyEntity()
