package com.abc.us.accounting.logistics.domain.entity

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 기말 재고 수량 테이블
 */
@Entity
@Table(name = "inventory_closing_stock")
@Comment("기말 재고 수량")
class InventoryClosingStock(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("기말 마감 일시")
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

    @Comment("기말 재고 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int = 0
)
