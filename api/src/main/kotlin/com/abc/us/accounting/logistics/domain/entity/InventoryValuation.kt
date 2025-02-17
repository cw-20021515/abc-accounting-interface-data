package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter

/**
 * 기말 자재별 전체 재고자산가치평가 테이블
 */
@Entity
@Table(name = "inventory_valuation")
@Comment("기말 자재별 전체 재고자산가치평가")
class InventoryValuation(

    @Comment("재고자산평가 ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("재고원가계산 ID")
    @Column(name = "costing_id")
    val costingId: Long? = null,

    @Comment("기말 평가 일시")
    @Column(name = "closing_time", nullable = false)
    val closingTime: OffsetDateTime,

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

    @Comment("자재 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int = 0,

    @Comment("자산 평가액")
    @Column(name = "total_value", nullable = false, precision = 38, scale = 4)
    val totalValue: BigDecimal,

    @Comment("레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
)
