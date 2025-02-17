package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmsShippingInformation(
    @get:JsonProperty("serviceFlowId")
    val serviceFlowId: String? = null,

    @get:JsonProperty("companyName")
    val companyName: String? = null,

    @get:JsonProperty("companyCode")
    val companyCode: String? = null,

    @get:JsonProperty("trackingId")
    val trackingId: String? = null,

    @get:JsonProperty("trackingUrl")
    val trackingUrl: String? = null,

    @get:JsonProperty("shipmentDate")
    val shipmentDate: LocalDateTime? = null,

    @get:JsonProperty("deliveryDate")
    val deliveryDate: LocalDateTime? = null,

    @get:JsonProperty("estimatedDeliveryDate")
    val estimatedDeliveryDate: LocalDate? = null,

    @get:JsonProperty("createTime")
    var createTime: LocalDateTime?=null,

    @get:JsonProperty("updateTime")
    var updateTime: LocalDateTime?=null
) {
}