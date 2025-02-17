package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAgingAdjustmentMethod
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 보유 기간에 따른 재고자산가치평가 규칙 테이블
 */
@Entity
@Table(name = "inventory_aging_loss_rule")
@Comment("보유 기간에 따른 재고자산가치평가 규칙")
class InventoryAgingLossRule(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("자재 ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("규칙 이름")
    @Column(name = "rule_name")
    val ruleName: String? = null,

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

    @Comment("조정 방식 (예: Percentage, Fixed)")
    @Column(name = "adjustment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    val adjustmentMethod: IfInventoryAgingAdjustmentMethod,

    @Comment("가격 조정 값 (정률 또는 정액)")
    @Column(name = "adjustment_value", nullable = false, precision = 38, scale = 4)
    val adjustmentValue: BigDecimal,

    @Comment("통화 코드")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("생성 사용자")
    @Column(name = "create_user", nullable = false, length = 32)
    val createUser: String,

    @Comment("생성 일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정 사용자")
    @Column(name = "update_user", nullable = false, length = 32)
    val updateUser: String,

    @Comment("수정 일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime,

    @Comment("레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    val isActive: String
)
