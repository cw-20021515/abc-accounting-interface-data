package com.abc.us.accounting.iface.domain.entity.logistics

import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryAssetGradeType
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryMovementCategory
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryMovementGroup
import com.abc.us.accounting.iface.domain.type.logistics.IfInventoryMovementType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

/**
 * 수불(재고 이동) 정보 테이블
 */
@Entity
@Table(name = "if_inventory_movement")
@Comment("수불(재고 이동) 정보")
class IfInventoryMovement(

    @Comment("고유 식별자")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("수불 발생 회사 코드")
    @Column(name = "company_code")
    val companyCode: String? = null,

    @Comment("입고 창고, 출고 출발지 창고 ID")
    @Column(name = "source_warehouse_id", nullable = false)
    val sourceWarehouseId: String,

    @Comment("출고 목적지 창고 ID(창고간 이동시)")
    @Column(name = "destination_warehouse_id")
    val destinationWarehouseId: String? = null,

    @Comment("창고간 이동 ID")
    @Column(name = "warehouse_transfer_id")
    val warehouseTransferId: String? = null,

    @Comment("수불 분류(MovementCategory)")
    @Column(name = "movement_category", nullable = false)
    @Enumerated(EnumType.STRING)
    val movementCategory: IfInventoryMovementCategory,

    @Comment("수불 그룹(MovementGroup)")
    @Column(name = "movement_group", nullable = false)
    @Enumerated(EnumType.STRING)
    val movementGroup: IfInventoryMovementGroup,

    @Comment("수불 유형(MovementType)")
    @Column(name = "movement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val movementType: IfInventoryMovementType,

    @Comment("입고 시 발주 품목 ID")
    @Column(name = "inbound_purchase_order_item_id")
    val inboundPurchaseOrderItemId: String? = null,

    @Comment("입고 시 B/L 번호(SAP Invoice의 순단가 확인용)")
    @Column(name = "inbound_bl_no")
    val inboundBlNo: String? = null,

    @Comment("자재 ID")
    @Column(name = "material_id")
    val materialId: String? = null,

    @Comment("자재 등급")
    @Column(name = "grade", nullable = false)
    @Enumerated(EnumType.STRING)
    val grade: IfInventoryAssetGradeType,


    @Comment("수불 발생 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("수불 발생 일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
