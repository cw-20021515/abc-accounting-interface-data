package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.taxline.entity.TaxLine
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDateTime

//@Entity
//@Table(name = "payment", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Payment(
    @Column(name = "order_id")
    var orderId: String,
    @Column(name = "payment_method")
    var paymentMethod: String,
    @Column(name = "transaction_id")
    var transactionId: String,
    @Column(name = "payout_id")
    var payoutId: String? = null,
    @Column(name = "payment_time")
    var paymentTime: LocalDateTime? = null,
    @Column(name = "last_name")
    var lastName: String? = null,
    @Column(name = "first_name")
    var firstName: String? = null,
    @Column(name = "address1")
    var address1: String? = null,
    @Column(name = "address2")
    var address2: String? = null,
    @Column(name = "zipcode")
    var zipcode: String? = null,
    @Column(name = "city")
    var city: String? = null,
    @Column(name = "state")
    var state: String? = null,
    @Column(name = "phone")
    var phone: String? = null,
    @Column(name = "email")
    var email: String? = null,
    @Column(name = "remark")
    var remark: String? = null,
    @Column(name = "item_price")
    var itemPrice: Double = 0.0,
    @Column(name = "discount_price")
    var discountPrice: Double = 0.0,
    @Column(name = "registration_price")
    var registrationPrice: Double = 0.0,
    @Column(name = "total_price")
    var totalPrice: Double = 0.0,
    @Column(name = "prepaid_amount")
    var prepaidAmount: Double = 0.0,
    @Column(name = "tax")
    var tax: Double = 0.0,
    @Column(name = "currency")
    var currency: String = "USD",
    @Column(name = "card_number")
    var cardNumber: String? = null,
    @Column(name = "card_type")
    var cardType: String? = null,
    @Column(name = "installment_months")
    var installmentMonths: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", updatable = false, insertable = false)
    var order: Order?,

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val taxLines: MutableList<TaxLine> = mutableListOf(),


    @Id
    @Column(name = "id")
    var id: String? = null,
)
