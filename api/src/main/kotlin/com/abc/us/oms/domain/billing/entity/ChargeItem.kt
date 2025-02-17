package com.abc.us.oms.domain.billing.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

//@Entity
//@Table(name = "charge_item", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ChargeItem(
    @Column(name = "charge_item_type")
    val chargeItemType: String,

    @Column(name = "service_flow_id")
    val serviceFlowId: String? = null,

    @Column(name = "quantity")
    val quantity: Int = 1,

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

    @Column(name = "charge_id")
    val chargeId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "charge_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    val charge: Charge? = null,

    @Id
    @Column(name = "id")
    val id: String,

) : AuditTimeOnlyEntity()
