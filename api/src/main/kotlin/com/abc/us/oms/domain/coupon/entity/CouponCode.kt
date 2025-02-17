package com.abc.us.oms.domain.coupon.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "coupon_code", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CouponCode(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "coupon_id")
    var couponId: String,
    @Column(name = "coupon_code")
    var couponCode: String,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "coupon_id", referencedColumnName = "id", updatable = false, insertable = false)
    var coupon: Coupon? = null,
    @OneToMany(mappedBy = "couponCode", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var couponIssue: MutableList<CouponIssue>? = null,
) : AuditTimeOnlyEntity()
