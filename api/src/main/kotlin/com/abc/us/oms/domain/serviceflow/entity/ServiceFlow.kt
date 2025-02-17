package com.abc.us.oms.domain.serviceflow.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "service_flow", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceFlow(
    @Id
    @Column(name = "id", nullable = false)
    val id: String = "",//SnowflakeId.generateId().toBase62(),

    @Column(name = "service_type", nullable = true)
    var serviceType: String,
    @Column(name = "order_id", nullable = true)
    var orderId: String,
    @Column(name = "order_item_id", nullable = true)
    var orderItemId: String,
    @Column(name = "customer_service_id", nullable = true)
    var customerServiceId: String? = null,
    @Column(name = "customer_service_ticket_id", nullable = true)
    var customerServiceTicketId: String? = null,
    @Column(name = "booking_id")
    var bookingId: String? = null,
    @Column(name = "outbound_delivery_id")
    var outboundDeliveryId: String? = null,
    @Column(name = "billing_id")
    var billingId: String? = null,
    @Column(name = "work_id")
    var workId: String? = null,
    @Column(name = "service_status_code")
    var serviceStatusCode: String = "SERVICE_CREATED",
    @OneToOne(mappedBy = "serviceFlow", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var serviceSlot: ServiceSlot? = null,
    @OneToOne(mappedBy = "serviceFlow", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var serviceLocation: ServiceLocation? = null,
    @OneToMany(mappedBy = "serviceFlow", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var servicePayments: MutableList<ServicePayment> = mutableListOf(),
    @CreatedDate
    @Column(name = "service_create_time")
    var serviceCreateTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "service_update_time")
    var serviceUpdateTime: LocalDateTime? = null,
    @Column(name = "service_cancel_time")
    var serviceCancelTime: LocalDateTime? = null

) : AuditTimeOnlyEntity()
