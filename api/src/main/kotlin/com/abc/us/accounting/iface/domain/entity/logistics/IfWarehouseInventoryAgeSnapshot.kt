package com.abc.us.accounting.iface.domain.entity.logistics

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 창고의 자재별 재고 보유 기간 정보 테이블
 */
@Entity
@Table(name = "if_warehouse_inventory_age_snapshot")
@Comment("창고의 자재별 재고 보유 기간 정보")
class IfWarehouseInventoryAgeSnapshot(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("창고 ID")
    @Column(name = "warehouse_id", nullable = false)
    val warehouseId: String,

    @Comment("자재 ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("자재 등급")
    @Column(name = "grade", nullable = false)
    @Enumerated(EnumType.STRING)
    val grade: IfInventoryAssetGradeType,

    @Comment("보유기간 범위 최소값")
    @Column(name = "min_aging_days", nullable = false)
    val minAgingDays: Int,

    @Comment("보유기간 범위 최대값")
    @Column(name = "max_aging_days")
    val maxAgingDays: Int? = null,

    @Comment("자재 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("측정 기준 일시(스냅샷 일시)")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
