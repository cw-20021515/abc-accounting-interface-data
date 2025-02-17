package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.model.*
import com.abc.us.accounting.iface.domain.type.oms.IfPaymentMethod
import com.abc.us.accounting.iface.domain.type.oms.IfTransactionType
import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 청구 결제 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_charge_payment")
@Comment("청구 결제 정보(HISTORY)")
class IfChargePayment(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("결제ID")
    @Column(name = "payment_id", nullable = false)
    val paymentId: String,

    @Comment("거래유형(Shopify Payments의 거래유형(결제))")
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val transactionType: IfTransactionType,

    @Comment("청구ID")
    @Column(name = "charge_id", nullable = false)
    val chargeId: String,

    @Comment("인보이스ID")
    @Column(name = "invoice_id")
    val invoiceId: String? = null,

    @Comment("거래ID(Shopify)")
    @Column(name = "transaction_id")
    val transactionId: String? = null,

    @Comment("결제수단")
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    val paymentMethod: IfPaymentMethod? = null,

    @Comment("결제시간")
    @Column(name = "payment_time")
    val paymentTime: OffsetDateTime? = null,

    @Comment("결제금액(세금포함)")
    @Column(name = "total_price", nullable = false, precision = 38, scale = 4)
    val totalPrice: BigDecimal,

    @Comment("판매세")
    @Column(name = "tax", nullable = false, precision = 38, scale = 4)
    val tax: BigDecimal,

    @Comment("결제금액(세금미포함)")
    @Column(name = "subtotal_price", nullable = false, precision = 38, scale = 4)
    val subtotalPrice: BigDecimal,

    @Comment("청구항목")
    @Convert(converter = IfOmsBillingChargeItemConverter::class)
    @Column(name = "charge_items", nullable = false)
    val chargeItems: List<IfOmsBillingChargeItem>,

    @Comment("판매세")
    @Convert(converter = TaxLinesConverter::class)
    @Column(name = "tax_lines", columnDefinition = "json", nullable = false)
    val taxLines: List<TaxLine> = listOf(),

    @Comment("할부개월수")
    @Column(name = "installment_months")
    val installmentMonths: Int? = null,

    @Comment("거래통화")
    @Column(name = "currency", nullable = false)
    val currency: String,

    @Comment("주소정보")
    @Convert(converter = PaymentAddressConverter::class)
    @Column(name = "address", columnDefinition = "json", nullable = false)
    val address: PaymentAddress,
)
