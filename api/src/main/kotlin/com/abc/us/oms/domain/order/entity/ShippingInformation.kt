package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import java.time.LocalDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "shipping_information", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ShippingInformation(
    @Column(name = "service_flow_id")
    val serviceFlowId: String,

    @Column(name = "company_name")
    val companyName: String? = null,

    @Column(name = "company_code")
    val companyCode: String? = null,

    @Column(name = "tracking_id")
    val trackingId: String,

    @Column(name = "tracking_url")
    val trackingUrl: String? = null,

    @Column(name = "shipment_date")
    val shipmentDate: LocalDateTime? = null,

    @Column(name = "delivery_date")
    val deliveryDate: LocalDateTime? = null,

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDate? = null,

) : AuditTimeEntity()
