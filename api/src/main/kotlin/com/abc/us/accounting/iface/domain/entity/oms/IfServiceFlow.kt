package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowStatus
import com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

/**
 * 서비스 플로우 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_service_flow")
@Comment("서비스 플로우 정보(HISTORY)")
class IfServiceFlow(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("서비스플로우ID")
    @Column(name = "service_flow_id", nullable = false)
    val serviceFlowId: String,

    @Comment("서비스플로우상태")
    @Column(name = "service_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val serviceStatus: IfServiceFlowStatus,

    @Comment("직전 서비스플로우상태")
    @Column(name = "last_service_status")
    @Enumerated(EnumType.STRING)
    val lastServiceStatus: IfServiceFlowStatus? = null,

    @Comment("서비스유형")
    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val serviceType: com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType,

    @Comment("설치ID")
    @Column(name = "install_id")
    val installId: String? = null,

    @Comment("시리얼번호")
    @Column(name = "serial_number")
    val serialNumber: String? = null,

    @Comment("브랜치 ID")
    @Column(name = "branch_id")
    val branchId: String? = null,

    @Comment("창고 ID")
    @Column(name = "warehouse_id")
    val warehouseId: String? = null,

    @Comment("테크니션ID")
    @Column(name = "technician_id")
    val technicianId: String? = null,

    @Comment("주문항목ID")
    @Column(name = "order_item_id", nullable = false)
    val orderItemId: String,

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
