package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 자재별 재고 원가 계산 테이블
 */
@Entity
@Table(name = "inventory_costing")
@Comment("자재별 재고 원가 계산")
class InventoryCosting(

    @Comment("재고원가계산 ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("원가 계산 일시")
    @Column(name = "costing_time", nullable = false)
    val costingTime: OffsetDateTime,

    @Comment("재고 원가 결정 방법")
    @Column(name = "method", nullable = false)
    val method: String,

    @Comment("재고원가계산 시작 기준일")
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Comment("재고원가계산 종료 기준일")
    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,

    @Comment("CDC 창고 ID")
    @Column(name = "warehouse_id", nullable = false)
    val warehouseId: String,

    @Comment("자재 ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("자재 등급")
    @Column(name = "grade", nullable = false)
    @Enumerated(EnumType.STRING)
    val grade: IfInventoryAssetGradeType,

    @Comment("통화 코드 (예: USD)")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("자재 원가")
    @Column(name = "unit_cost", nullable = false, precision = 38, scale = 4)
    val unitCost: BigDecimal,

    @Comment("레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    val isActive: String
)
