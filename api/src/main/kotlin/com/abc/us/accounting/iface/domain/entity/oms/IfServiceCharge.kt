package com.abc.us.accounting.iface.domain.entity.oms

import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 서비스 청구 정보 테이블 (HISTORY)
 */
//@Entity
@Table(name = "if_service_charge")
@Comment("서비스 청구(HISTORY)")
class IfServiceCharge(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("서비스 청구 ID")
    @Column(name = "service_charge_id", nullable = false)
    val serviceChargeId: String,

    @Comment("서비스 청구상태")
    @Column(name = "charge_status", nullable = false)
    val chargeStatus: String,

    @Comment("직전 서비스 청구상태")
    @Column(name = "last_charge_status")
    val lastChargeStatus: String? = null,

    @Comment("서비스 플로우 ID")
    @Column(name = "service_flow_id", nullable = false)
    val serviceFlowId: String,

    @Comment("서비스 빌링유형")
    @Column(name = "service_billing_type", nullable = false)
    val serviceBillingType: String,

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
