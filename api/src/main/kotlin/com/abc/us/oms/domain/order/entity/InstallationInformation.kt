package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDateTime

//@Entity
//@Table(name = "installation_information", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class InstallationInformation(
    @Column(name = "order_item_id")
    var orderItemId: String? = null,
    @Column(name = "address1")
    var address1: String,
    @Column(name = "address2")
    var address2: String,
    @Column(name = "zipcode")
    var zipcode: String,
    @Column(name = "city")
    var city: String,
    @Column(name = "state")
    var state: String,
    @Column(name = "latitude")
    var latitude: Double? = null,
    @Column(name = "longitude")
    var longitude: Double? = null,
    @Column(name = "serial_number")
    var serialNumber: String? = null,
    @Column(name = "installation_time")
    var installationTime: LocalDateTime? = null,
    @Column(name = "warranty_start_time")
    var warrantyStartTime: LocalDateTime? = null,
    @Column(name = "warranty_end_time")
    var warrantyEndTime: LocalDateTime? = null,
    @Column(name = "technician_id")
    var technicianId: String? = null,
    @Column(name = "service_flow_id")
    val serviceFlowId: String? = null,
    @Column(name = "branch_id")
    var branchId: String? = null,
    @Column(name = "warehouse_id")
    var warehouseId: String? = null,
    @Column(name = "water_type")
    var waterType: String? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "order_item_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var orderItem: OrderItem?,
    @OneToMany(
        mappedBy = "installationInformation",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var orderItemCustomers: MutableList<OrderItemCustomer> = mutableListOf(),

    @OneToMany(
        mappedBy = "installationInformation",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var installationFilters: MutableList<InstallationFilter> = mutableListOf(),
) : AuditTimeEntity()
