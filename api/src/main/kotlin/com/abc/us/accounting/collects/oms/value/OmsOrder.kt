package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

//@JsonInclude(JsonInclude.Include.NON_NULL)
//data class OmsOrder(
//    @get:JsonProperty("channelOrderItemId")
//    val channelOrderId: String?=null,
//
//    @get:JsonProperty("channelId")
//    val channelId: String?=null,
//
//    @get:JsonProperty("orderProductType")
//    var orderProductType: String? = null,
//
//    @get:JsonProperty("referrerCode")
//    var referrerCode: String? = null,
//
//    @get:JsonProperty("orderCreateTime")
//    val orderCreateTime: LocalDateTime? = null,
//
//    @get:JsonProperty("orderUpdateTime")
//    var orderUpdateTime: LocalDateTime? = null,
//
//    @get:JsonProperty("createTime")
//    var createTime: LocalDateTime?=null,
//
//    @get:JsonProperty("updateTime")
//    var updateTime: LocalDateTime?=null,
//
//    @get:JsonProperty("orderItems")
//    var orderItems: MutableList<OmsOrderItem> = mutableListOf(),
//
////    @get:JsonProperty("deliveryAddress")
////    var deliveryAddress: DeliveryAddress? = null,
////
////    @get:JsonProperty("payment")
////    var payment: Payment? = null,
//
//    @get:JsonProperty("customerId")
//    var customerId: String?=null,
//
//    @get:JsonProperty("customer")
//    val customer: OmsCustomer? = null,
//
//    @get:JsonProperty("channel")
//    val channel: OmsChannel? = null,
//
//    @get:JsonProperty("id")
//    val id: String?=null,
//) {
//}