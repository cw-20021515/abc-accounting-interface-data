package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmsInstallationInformation(
    @get:JsonProperty("orderItemId")
    var orderItemId: String? = null,

    @get:JsonProperty("address1")
    var address1: String? = null,

    @get:JsonProperty("address2")
    var address2: String? = null,

    @get:JsonProperty("zipcode")
    var zipcode: String? = null,

    @get:JsonProperty("city")
    var city: String? = null,

    @get:JsonProperty("state")
    var state: String? = null,

    @get:JsonProperty("latitude")
    var latitude: Double? = null,

    @get:JsonProperty("longitude")
    var longitude: Double? = null,

    @get:JsonProperty("serialNumber")
    var serialNumber: String? = null,

    @get:JsonProperty("installationTime")
    var installationTime: LocalDateTime? = null,

    @get:JsonProperty("warrantyStartTime")
    var warrantyStartTime: LocalDateTime? = null,

    @get:JsonProperty("warrantyEndTime")
    var warrantyEndTime: LocalDateTime? = null,

//    @get:JsonProperty("technicianId")
//    var technicianId: String? = null,

    @get:JsonProperty("serviceFlowId")
    val serviceFlowId: String? = null,
    @get:JsonProperty("branchId")
    var branchId: String? = null,

    @get:JsonProperty("warehouseId")
    var warehouseId: String? = null,

//    @get:JsonProperty("companyName")
//    var waterType: String? = null,

//    @get:JsonProperty("companyName")
//    var orderItem: OmsOrderItem?,

//    @get:JsonProperty("orderItemCustomers")
//    var orderItemCustomers: MutableList<OrderItemCustomer> = mutableListOf(),
//
//    @get:JsonProperty("installationFilters")
//    var installationFilters: MutableList<InstallationFilter> = mutableListOf(),

    ) {
}