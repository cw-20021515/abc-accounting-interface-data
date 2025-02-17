package com.abc.us.accounting.iface.domain.entity.logistics

import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 발주 품목 정보 테이블 (History)
 */
@Entity
@Table(name = "if_purchase_order_item")
@Comment("발주 품목 정보(History)")
class IfPurchaseOrderItem(

    @Id
    @Comment("고유 식별자")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("발주 ID")
    @Column(name = "purchase_order_id", nullable = false)
    val purchaseOrderId: String,

    @Comment("발주 품목 ID")
    @Column(name = "purchase_order_item_id", nullable = false)
    val purchaseOrderItemId: String,

    @Comment("자재 ID")
    @Column(name = "material_id")
    val materialId: String? = null,

    @Comment("발주 수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("발주 단가")
    @Column(name = "unit_price", nullable = false, precision = 38, scale = 4)
    val unitPrice: BigDecimal,

    @Comment("통화 코드")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("원본 레코드가 최초 생성 시 create_time, 수정 시 update_time 입력")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
