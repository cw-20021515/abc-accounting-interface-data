package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.type.oms.IfChargeItemType
import io.hypersistence.utils.hibernate.type.json.JsonType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type

/**
 * 요금 청구 항목 정보 테이블
 */
@Entity
@Table(name = "if_charge_item")
@Comment("요금 청구 항목 정보")
class IfChargeItem(

    @Comment("청구항목ID")
    @Id
    @Column(name = "charge_item_id", nullable = false)
    val chargeItemId: String,

    @Comment("청구항목유형")
    @Column(name = "charge_item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val chargeItemType: IfChargeItemType,

    @Comment("수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("전체금액(세금미포함)")
    @Column(name = "subtotal_price", nullable = false, precision = 10, scale = 4)
    val subtotalPrice: BigDecimal,

    @Comment("아이템금액")
    @Column(name = "item_price", nullable = false, precision = 10, scale = 4)
    val itemPrice: BigDecimal,

    @Comment("할인금액")
    @Column(name = "discount_price", nullable = false, precision = 10, scale = 4)
    val discountPrice: BigDecimal,

    @Comment("선급금액")
    @Column(name = "prepaid_amount", nullable = false, precision = 10, scale = 4)
    val prepaidAmount: BigDecimal,

    @Comment("프로모션")
    @Type(JsonType::class)
    @Column(name = "promotions", columnDefinition = "json")
    val promotions: List<Map<String, Any>> = listOf(),

    @Comment("거래통화")
    @Column(name = "currency", nullable = false)
    val currency: String,

    @Comment("면세여부")
    @Column(name = "is_tax_exempt", nullable = false)
    val isTaxExempt: Boolean,

    @Comment("청구ID")
    @Column(name = "charge_id", nullable = false)
    val chargeId: String,

    @Comment("서비스플로우ID")
    @Column(name = "service_flow_id")
    val serviceFlowId: String? = null,

    @Comment("자재ID")
    @Column(name = "material_id")
    val materialId: String? = null,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
