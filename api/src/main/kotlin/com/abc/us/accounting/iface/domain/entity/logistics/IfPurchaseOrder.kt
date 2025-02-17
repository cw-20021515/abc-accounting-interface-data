package com.abc.us.accounting.iface.domain.entity.logistics

import com.abc.us.accounting.iface.domain.type.logistics.IfPurchaseOrderStatus
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 발주 정보 테이블 (History)
 */
@Entity
@Table(name = "if_purchase_order")
@Comment("발주 정보(History)")
class IfPurchaseOrder(

    @Id
    @Comment("고유 식별자")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("발주 ID")
    @Column(name = "purchase_order_id", nullable = false)
    val purchaseOrderId: String,

    @Comment("구매 주문 상태")
    @Column(name = "purchase_order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val purchaseOrderStatus: IfPurchaseOrderStatus,

    @Comment("자재 공급자")
    @Column(name = "vendor_id")
    val vendorId: String? = null,

    @Comment("자재 수급자")
    @Column(name = "customer_id")
    val customerId: String? = null,

    @Comment("원본 레코드가 최초 생성 시 create_time, 수정 시 update_time 입력")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
