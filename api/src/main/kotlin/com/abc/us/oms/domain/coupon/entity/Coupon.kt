package com.abc.us.oms.domain.coupon.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.promotion.entity.Promotion
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDateTime

//@Entity
//@Table(name = "coupon", schema = "public", catalog = "abc_oms")
//@SQLRestriction(
//    value = "is_deleted = false",
//)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Coupon(
    @Id
    val id: String,
    @Column(name = "coupon_name")
    var couponName: String = "",
    @Column(name = "promotion_id")
    var promotionId: String? = null,
    @Column(name = "is_issue")
    var isIssue: Boolean = false,
    @Column(name = "is_multiple_issue")
    var isMultipleIssue: Boolean,
    @Column(name = "is_multiple_use")
    var isMultipleUse: Boolean,
    @Column(name = "is_simultaneous_use")
    var isSimultaneousUse: Boolean? = false,
    @Column(name = "period")
    var period: String?,
    @Column(name = "coupon_code_type")
    var couponCodeType: String? = null,
    @Column(name = "start_date")
    var startDate: LocalDateTime? = null,
    @Column(name = "end_date")
    var endDate: LocalDateTime? = null,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,
    @Column(name = "coupon_description")
    var couponDescription: String? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", updatable = false, insertable = false)
    var promotion: Promotion? = null,
    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var couponCode: MutableList<CouponCode> = mutableListOf(),
) : AuditTimeOnlyEntity()
