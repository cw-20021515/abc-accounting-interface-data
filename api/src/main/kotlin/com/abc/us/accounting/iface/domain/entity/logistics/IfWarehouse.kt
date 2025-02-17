package com.abc.us.accounting.iface.domain.entity.logistics

import com.abc.us.accounting.iface.domain.type.logistics.IfWarehouseType
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 창고 정보 테이블 (History)
 */
@Entity
@Table(name = "if_warehouse")
@Comment("창고 정보(History)")
class IfWarehouse(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("창고 ID")
    @Column(name = "warehouse_id", nullable = false)
    val warehouseId: String,

    @Comment("상위 창고 ID")
    @Column(name = "parent_warehouse_id")
    val parentWarehouseId: String? = null,

    @Comment("창고 이름")
    @Column(name = "name", nullable = false)
    val name: String,

    @Comment("창고 유형")
    @Column(name = "warehouse_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val warehouseType: IfWarehouseType,

    @Comment("Time Zone Id")
    @Column(name = "time_zone", nullable = false)
    val timeZone: String,

    @Comment("레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    val isActive: String,

    @Comment("원본 레코드가 최초 생성 시 create_time, 수정 시 update_time 입력")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
