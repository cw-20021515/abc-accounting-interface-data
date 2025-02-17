package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.type.oms.IfChargeStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 청구 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_charge")
@Comment("청구 정보(HISTORY)")
class IfCharge(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("청구ID")
    @Column(name = "charge_id", nullable = false)
    val chargeId: String,

    @Comment("청구상태")
    @Column(name = "charge_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val chargeStatus: IfChargeStatus,

    @Comment("직전 청구상태")
    @Column(name = "last_charge_status")
    @Enumerated(EnumType.STRING)
    val lastChargeStatus: IfChargeStatus? = null,

    @Comment("청구회차")
    @Column(name = "billing_cycle", nullable = false)
    val billingCycle: Int,

    @Comment("대상월")
    @Column(name = "target_month")
    val targetMonth: String?,

    @Comment("계약ID")
    @Column(name = "contract_id", nullable = false)
    val contractId: String,

    @Comment("시작일")
    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Comment("종료일")
    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
