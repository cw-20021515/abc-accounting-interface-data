package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "promotion_offer", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionOffer(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "promotion_id")
    var promotionId: String,
    @Column(name = "material_id")
    var materialId: String,
    @Column(name = "type")
    var type: String,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", updatable = false, insertable = false)
    var promotion: Promotion? = null,
) : AuditTimeOnlyEntity()
