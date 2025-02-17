package com.abc.us.accounting.iface.domain.entity.oms

import io.hypersistence.utils.hibernate.type.json.JsonType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type

/**
 * 서비스 결제 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_service_charge_payment")
@Comment("서비스 결제정보(HISTORY)")
class IfServiceChargePayment(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("결제")
    @Column(name = "payment_id", nullable = false)
    val paymentId: String,

    @Comment("거래유형")
    @Column(name = "transaction_type", nullable = false)
    val transactionType: String,

    @Comment("거래ID")
    @Column(name = "transaction_id", nullable = false)
    val transactionId: String,

    @Comment("서비스 청구 ID")
    @Column(name = "service_charge_id", nullable = false)
    val serviceChargeId: String,

    @Comment("결제방법 (렌탈/현장수납?)")
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,

    @Comment("결제시간")
    @Column(name = "payment_time")
    val paymentTime: OffsetDateTime? = null,

    @Comment("거래통화")
    @Column(name = "currency")
    val currency: String? = null,

    @Comment("결제금액(세금포함)")
    @Column(name = "total_price", nullable = false, precision = 38, scale = 4)
    val totalPrice: BigDecimal,

    @Comment("판매세")
    @Column(name = "tax", nullable = false, precision = 38, scale = 4)
    val tax: BigDecimal,

    @Comment("결제금액(세금미포함)")
    @Column(name = "subtotal_price", nullable = false, precision = 38, scale = 4)
    val subtotalPrice: BigDecimal,

    @Comment("개별상품가격")
    @Column(name = "item_price", nullable = false, precision = 38, scale = 4)
    val itemPrice: BigDecimal,

    @Comment("할인가격")
    @Column(name = "discount_price", nullable = false, precision = 38, scale = 4)
    val discountPrice: BigDecimal,

    @Comment("청구항목")
    @Type(JsonType::class)
    @Column(name = "charge_items", columnDefinition = "json", nullable = false)
    val chargeItems: MutableMap<String,Any> = mutableMapOf(),

    @Comment("판매세정보")
    @Type(JsonType::class)
    @Column(name = "tax_lines", columnDefinition = "json", nullable = false)
    val taxLines: MutableMap<String,Any> = mutableMapOf(),

    @Comment("주소")
    @Type(JsonType::class)
    @Column(name = "address", columnDefinition = "json", nullable = false)
    val address: MutableMap<String,Any> = mutableMapOf(),

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
