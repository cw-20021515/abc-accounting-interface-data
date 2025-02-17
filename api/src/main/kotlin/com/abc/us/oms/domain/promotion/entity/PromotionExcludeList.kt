package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "promotion_exclude_list", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionExcludeList(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "promotion_id")
    var promotionId: String,
    @Column(name = "exclude_promotion_id")
    var excludePromotionId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", insertable = false, updatable = false)
    var promotion: Promotion? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exclude_promotion_id", referencedColumnName = "id", insertable = false, updatable = false)
    var excludePromotion: Promotion? = null,
) : AuditTimeOnlyEntity()
