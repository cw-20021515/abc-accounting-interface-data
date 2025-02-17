package com.abc.us.oms.domain.customerservice.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "customer_service_ticket", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerServiceTicket(
    @Column(name = "channel_customer_service_ticket_id")
    var channelCustomerServiceTicketId: String? = null,
    @Column(name = "work_type")
    var workType: String? = null,
    @Column(name = "reason_type")
    var reasonType: String? = null,
    @Column(name = "reason_type_code")
    var reasonTypeCode: String? = null,
    @Column(name = "reason_group")
    var reasonGroup: String? = null,
    @Column(name = "reason_group_code")
    var reasonGroupCode: String? = null,
    @Column(name = "reason_detail")
    var reasonDetail: String? = null,
    @Column(name = "reason_detail_code")
    var reasonDetailCode: String? = null,
    @Column(name = "urgency")
    var urgency: String? = null,
    @Column(name = "remark")
    var remark: String? = null,
    @Column(name = "order_id")
    var orderId: String,
    @Column(name = "order_item_id")
    var orderItemId: String,
    @Column(name = "ticket_status_code")
    var ticketStatusCode: String,
    @Column(name = "branch_id")
    var branchId: String? = null,
    @Column(name = "warehouse_id")
    var warehouseId: String? = null,
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
    @Column(name = "latitude")
    var latitude: Double? = null,
    @Column(name = "longitude")
    var longitude: Double? = null,
    @Column(name = "slot_type")
    var slotType: String? = null,
    @Column(name = "booking_date")
    var bookingDate: LocalDate? = null,
    @Column(name = "customer_service_id")
    var customerServiceId: String,
    @Column(name = "confirm_time")
    var confirmTime: LocalDateTime? = null,
    @Column(name = "complete_time")
    var completeTime: LocalDateTime? = null,
    @Column(name = "reference_work_id")
    var referenceWorkId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_service_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customerService: CustomerService? = null,
    @Id
    @Column(name = "id")
    val id: String = "",//SnowflakeId.generateId().toBase62(),

) : AuditTimeOnlyEntity()
