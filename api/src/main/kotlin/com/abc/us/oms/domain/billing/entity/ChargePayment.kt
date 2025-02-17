package com.abc.us.oms.domain.billing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDateTime

//@Entity
//@Table(name = "charge_payment", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ChargePayment(
    @Id
    @Column(name = "id", nullable = false)
    val id: String = "",//SnowflakeId.generateId().toBase62(),

    @Column(name = "charge_id")
    val chargeId: String,

    @Column(name = "invoice_id")
    val invoiceId: String,

    @Column(name = "payment_method")
    val paymentMethod: String,

    @Column(name = "transaction_id")
    val transactionId: String,

    @Column(name = "payout_id")
    var payoutId: String? = null,

    @Column(name = "payment_time")
    val paymentTime: LocalDateTime,

    @Column(name = "last_name")
    val lastName: String? = null,

    @Column(name = "first_name")
    val firstName: String? = null,

    @Column(name = "address1")
    val address1: String? = null,

    @Column(name = "address2")
    val address2: String? = null,

    @Column(name = "zipcode")
    val zipcode: String? = null,

    @Column(name = "city")
    val city: String? = null,

    @Column(name = "state")
    val state: String? = null,

    @Column(name = "phone")
    val phone: String? = null,

    @Column(name = "email")
    val email: String? = null,

    @Column(name = "remark")
    val remark: String? = null,

    @Column(name = "total_price")
    val totalPrice: Double = 0.0,

    @Column(name = "charge_items")
    @Type(JsonType::class)
    val chargeItems: String,

    @Column(name = "card_number")
    val cardNumber: String? = null,

    @Column(name = "installment_months")
    val installmentMonths: Int? = null,

    @Column(name = "card_type")
    val cardType: String? = null,

    @Column(name = "currency")
    val currency: String = "USD",

    @Column(name = "tax")
    val tax: Double,

    @Type(JsonType::class)
    @Column(name = "tax_lines")
    val taxLines: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "charge_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    val charge: Charge? = null,
)
