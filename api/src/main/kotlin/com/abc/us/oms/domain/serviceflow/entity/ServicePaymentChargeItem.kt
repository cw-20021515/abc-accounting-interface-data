package com.abc.us.oms.domain.serviceflow.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

//@Entity
//@Table(name = "service_payment_charge_item", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServicePaymentChargeItem(

    @Column(name = "charge_item_type")
    val chargeItemType: String,

    @Column(name = "service_flow_id")
    val serviceFlowId: String,

    @Column(name = "quantity")
    val quantity: Int,

    @Column(name = "total_price")
    val totalPrice: Double,

    @Column(name = "item_price")
    val itemPrice: Double,

    @Column(name = "discount_price")
    val discountPrice: Double,

    @Column(name = "prepaid_amount")
    val prepaidAmount: Double? = 0.0,

    @Type(JsonType::class)
    @Column(name = "promotions")
    val promotions: String? = null,

    @Column(name = "currency")
    val currency: String,

    @Column(name = "is_tax_exempt")
    val isTaxExempt: Boolean,

    @Column(name = "service_payment_id")
    val servicePaymentId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_payment_id", nullable = false, insertable = false, updatable = false)
    val servicePayment: ServicePayment? = null,
) : AuditTimeEntity()
