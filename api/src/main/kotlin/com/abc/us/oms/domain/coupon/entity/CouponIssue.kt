package com.abc.us.oms.domain.coupon.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDateTime

//@Entity
//@Table(name = "coupon_issue", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CouponIssue(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "coupon_code_id")
    val couponCodeId: String? = null,
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "is_use")
    var isUse: Boolean = false,
    @Column(name = "coupon_issue_status_code")
    var couponIssueStatusCode: String,
    @Column(name = "start_time")
    val startTime: LocalDateTime,
    @Column(name = "end_time")
    val endTime: LocalDateTime,
    @Column(name = "use_time")
    val useTime: LocalDateTime? = null,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "coupon_code_id", referencedColumnName = "id", updatable = false, insertable = false)
    var couponCode: CouponCode? = null,
) : AuditTimeOnlyEntity()
