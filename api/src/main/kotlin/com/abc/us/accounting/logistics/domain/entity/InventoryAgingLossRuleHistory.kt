package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAgingAdjustmentMethod
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 보유 기간에 따른 재고자산가치평가 규칙 변경(History) 테이블
 */
@Entity
@Table(name = "inventory_aging_loss_rule_history")
@Comment("보유 기간에 따른 재고자산가치평가 규칙 변경(History)")
class InventoryAgingLossRuleHistory(

    @Comment("변경 이력 레코드 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_key", nullable = false)
    val auditKey: Int = 0,

    @Comment("변경 전 Aging 규칙 ID (고유 식별자)")
    @Column(name = "rule_id")
    val ruleId: Long? = null,

    @Comment("변경 전 자재 ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("변경 전 자재 등급")
    @Column(name = "grade", nullable = false)
    @Enumerated(EnumType.STRING)
    val grade: IfInventoryAssetGradeType,

    @Comment("변경 전 보유기간 범위 최소값")
    @Column(name = "min_aging_days", nullable = false)
    val minAgingDays: Int,

    @Comment("변경 전 보유기간 범위 최대값")
    @Column(name = "max_aging_days")
    val maxAgingDays: Int? = null,

    @Comment("변경 전 조정 방식 (예: Percentage, Fixed)")
    @Column(name = "adjustment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    val adjustmentMethod: IfInventoryAgingAdjustmentMethod,

    @Comment("변경 전 가격 조정 값 (정률 또는 정액)")
    @Column(name = "adjustment_value", nullable = false, precision = 38, scale = 4)
    val adjustmentValue: BigDecimal,

    @Comment("변경전 통화 코드")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("변경 전 수정 사용자")
    @Column(name = "update_user", nullable = false, length = 32)
    val updateUser: String,

    @Comment("변경 전 수정 일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime,

    @Comment("변경 전 레코드 활성화 여부")
    @Column(name = "is_active", nullable = false, length = 1)
    val isActive: String
)
