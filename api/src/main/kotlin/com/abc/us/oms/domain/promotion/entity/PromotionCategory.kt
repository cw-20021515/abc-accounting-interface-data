package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

//@Entity
//@Table(name = "promotion_category", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionCategory(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "name")
    @Comment(value = "프로모션 카테고리 명")
    var name: String,
    @Column(name = "parent_id")
    @Comment(value = "부모 카테고리 ID")
    var parentId: String?,
    @Column(name = "depth")
    @Comment(value = "카테고리 깊이(리프기준)")
    var depth: String = "1",
) : AuditTimeOnlyEntity()
