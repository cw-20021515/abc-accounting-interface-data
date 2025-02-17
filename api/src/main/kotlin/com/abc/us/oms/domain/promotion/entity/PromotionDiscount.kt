package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "promotion_discount", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionDiscount(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "promotion_id")
    var promotionId: String,
    @Column(name = "type")
    var type: String,
    @Column(name = "target_type")
    var targetType: String,
    @Column(name = "amount")
    var amount: Double? = 0.0,
    @Column(name = "rate")
    var rate: Double? = 0.0,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", updatable = false, insertable = false)
    var promotion: Promotion? = null,
) : AuditTimeOnlyEntity()
