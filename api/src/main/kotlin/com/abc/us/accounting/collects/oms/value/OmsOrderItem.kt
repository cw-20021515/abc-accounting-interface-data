package com.abc.us.accounting.collects.oms.value

import com.abc.us.generated.models.OrderItemType
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//data class OmsOrderItem(
//
//    @get:JsonProperty("channelOrderItemId")
//    val channelOrderItemId: String?=null,
//
//    @get:JsonProperty("orderItemId")
//    val orderItemId: String?=null,
//
//
//    @get:JsonProperty("orderId")
//    val orderId: String?=null,
//
//    @get:JsonProperty("channelOrderId")
//    val channelOrderId: String?=null,
//
//    @get:JsonProperty("orderItemStatusCode")
//    val orderItemStatusCode: String?=null,
//
//    @get:JsonProperty("orderItemType")
//    val orderItemType: OrderItemType?=null,
//
//    @get:JsonProperty("materialId")
//    val materialId: String?=null,
//
//    @get:JsonProperty("quantity")
//    val quantity: Int?=null,
//
//    @get:JsonProperty("itemPrice")
//    val itemPrice: Double?=null,
//
//    @get:JsonProperty("registrationPrice")
//    val registrationPrice: Double?=null,
//
//    @get:JsonProperty("discountPrice")
//    val discountPrice: Double?=null,
//
//    @get:JsonProperty("totalPrice")
//    val totalPrice: Double?=null,
//
//    @get:JsonProperty("tax")
//    val tax: Double?=null,
//
//
//    @get:JsonProperty("currency")
//    val currency: String? = null,
//
//    @get:JsonProperty("shippingInformationId")
//    val shippingInformationId: String? = null,
//
//    @get:JsonProperty("createTime")
//    val createTime: LocalDateTime? = null,
//
//    @get:JsonProperty("updateTime")
//    val updateTime: LocalDateTime? = null,
//
//    @get:JsonProperty("order")
//    val order: OmsOrder? = null,
//
////    @get:JsonProperty("material")
////    val material: Material? = null,
//
//    @get:JsonProperty("contracts")
//    val contracts: MutableList<OmsContract>? = null,
//
//    @get:JsonProperty("installationInformation")
//    var installationInformation: OmsInstallationInformation? = null,
//
////    @get:JsonProperty("shippingInformation")
////    var shippingInformation: OmsShippingInformation? = null,
//
////    @get:JsonProperty("orderItemPromotions")
////    val orderItemPromotions: List<OrderItemPromotion>? = null,
//
////    @get:JsonProperty("taxLines")
////    var taxLines: MutableList<TaxLine> = mutableListOf(),
//
//    @get:JsonProperty("id")
//    val id: String?=null,
//)
//
