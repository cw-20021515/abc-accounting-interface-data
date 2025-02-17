package com.abc.us.oms.domain.promotion.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

//@Entity
//@Table(name = "promotion_plan", schema = "public", catalog = "abc_oms")
//@SQLRestriction(
//    value = "is_deleted = false",
//)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionPlan(
    @Id
    val id: String,
    @Column(name = "promotion_plan_name")
    var promotionPlanName: String? = null,
    @Column(name = "promotion_plan_description")
    var promotionPlanDescription: String? = null,
    @Column(name = "is_active")
    var isActive: Boolean = false,
    @Column(name = "start_date")
    var startDate: LocalDateTime? = null,
    @Column(name = "end_date")
    var endDate: LocalDateTime? = null,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,
    @OneToMany(mappedBy = "promotionPlan", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var promotions: MutableList<Promotion> = mutableListOf(),
) : AuditTimeOnlyEntity()
