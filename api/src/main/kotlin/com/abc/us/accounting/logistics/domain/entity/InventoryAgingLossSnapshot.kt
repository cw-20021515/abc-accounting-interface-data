package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAgingAdjustmentMethod
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 보유 기간에 따른 기말 재고자산가치평가 결과 테이블
 */
@Entity
@Table(name = "inventory_aging_loss_snapshot")
@Comment("보유 기간에 따른 기말 재고자산가치평가 결과")
class InventoryAgingLossSnapshot(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("Aging 규칙 ID")
    @Column(name = "rule_id")
    val ruleId: Long? = null,

    @Comment("기말 평가 일시")
    @Column(name = "closing_time", nullable = false)
    val closingTime: OffsetDateTime,

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

    @Comment("통화 코드")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("기존 자재 원가(inventory_costing)")
    @Column(name = "original_price", nullable = false, precision = 38, scale = 4)
    val originalPrice: BigDecimal,

    @Comment("조정 방식 (예: Percentage, Fixed)")
    @Column(name = "adjustment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    val adjustmentMethod: IfInventoryAgingAdjustmentMethod,

    @Comment("가격 조정 값 (정률 또는 정액)")
    @Column(name = "adjustment_value", nullable = false, precision = 38, scale = 4)
    val adjustmentValue: BigDecimal,

    @Comment("감가된 원가")
    @Column(name = "adjusted_price", nullable = false, precision = 38, scale = 4)
    val adjustedPrice: BigDecimal,

    @Comment("자재 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("감가전 자산 평가액")
    @Column(name = "total_original_value", nullable = false, precision = 38, scale = 4)
    val totalOriginalValue: BigDecimal,

    @Comment("감가된 자산 평가액")
    @Column(name = "total_adjusted_value", nullable = false, precision = 38, scale = 4)
    val totalAdjustedValue: BigDecimal,

    @Comment("레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    val isActive: String
)
