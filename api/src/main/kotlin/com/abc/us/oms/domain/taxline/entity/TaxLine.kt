package com.abc.us.oms.domain.taxline.entity

import com.abc.us.oms.domain.common.config.AbstractEntity
import com.abc.us.oms.domain.contract.entity.ContractPaymentInformation
import com.abc.us.oms.domain.order.entity.OrderItem
import com.abc.us.oms.domain.order.entity.Payment
import com.abc.us.oms.domain.serviceflow.entity.ServicePayment
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "tax_line")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaxLine(

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val rate: Double,

    @Column(nullable = false)
    val price: Double? = 0.0,

    @Column(name = "payment_id")
    val paymentId: String? = null,

    @Column(name = "service_payment_id")
    val servicePaymentId: String? = null,

    @Column(name = "contract_payment_information_id")
    val contractPaymentInformationId: String? = null,

    @Column(name = "order_item_id")
    val orderItemId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "order_item_id",
        referencedColumnName = "id",
        updatable = false,
        insertable = false,
    )
    val orderItem: OrderItem? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "id", updatable = false, insertable = false)
    val payment: Payment? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_payment_id", referencedColumnName = "id", updatable = false, insertable = false)
    val servicePayment: ServicePayment? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "contract_payment_information_id",
        referencedColumnName = "id",
        updatable = false,
        insertable = false,
    )
    val contractPaymentInformation: ContractPaymentInformation? = null,
) : AbstractEntity()
