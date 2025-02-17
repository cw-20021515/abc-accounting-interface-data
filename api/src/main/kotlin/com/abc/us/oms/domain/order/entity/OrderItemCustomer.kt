package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.customer.entity.Customer
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "order_item_customer", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderItemCustomer(
    @Column(name = "installation_information_id")
    var installationInformationId: String,

    @Column(name = "customer_id")
    var customerId: String,

    @Column(name = "customer_type")
    var customerType: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "installation_information_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
    )
    var installationInformation: InstallationInformation?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false)
    var customer: Customer? = null,
) : AuditTimeEntity()
