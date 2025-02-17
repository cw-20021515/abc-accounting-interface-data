package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.material.entity.Material
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "promotion_material", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionMaterial(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "material_id")
    var materialId: String,
    @Column(name = "promotion_id")
    var promotionId: String,
    @Column(name = "sequence")
    var sequence: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", insertable = false, updatable = false)
    var promotion: Promotion? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "id", insertable = false, updatable = false)
    var material: Material? = null,
) : AuditTimeOnlyEntity()
