package com.abc.us.oms.domain.serviceflow.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.taxline.entity.TaxLine
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

//@Entity
//@Table(name = "service_payment", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServicePayment(
    @Column(name = "service_flow_id")
    var serviceFlowId: String,
    @Column(name = "payment_method")
    var paymentMethod: String? = null,
    @Column(name = "transaction_id")
    var transactionId: String? = null,
    @Column(name = "payout_id")
    var payoutId: String? = null,
    @Column(name = "item_price")
    var itemPrice: Double = 0.0,
    @Column(name = "registration_price")
    var registrationPrice: Double = 0.0,
    @Column(name = "discount_price")
    var discountPrice: Double = 0.0,
    @Column(name = "prepaid_amount")
    var prepaidAmount: Double = 0.0,
    @Column(name = "currency")
    var currency: String = "USD",
    @Column(name = "total_price")
    var totalPrice: Double = 0.0,
    @Column(name = "tax")
    var tax: Double = 0.0,
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
    @Column(name = "card_number")
    var cardNumber: String? = null,
    @Column(name = "card_type")
    var cardType: String? = null,
    @Column(name = "installment_months")
    var installmentMonths: Int? = null,
    @Column(name = "service_billing_type")
    var serviceBillingType: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_flow_id", referencedColumnName = "id", updatable = false, insertable = false)
    var serviceFlow: ServiceFlow?,
    @OneToMany(mappedBy = "servicePayment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var chargeItems: MutableList<ServicePaymentChargeItem> = mutableListOf(),
    @OneToMany(mappedBy = "servicePayment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var taxLines: MutableList<TaxLine> = mutableListOf(),
) : AuditTimeEntity()
