package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

//@JsonInclude(JsonInclude.Include.NON_NULL)
//data class OmsContractPaymentInformation(
//    @get:JsonProperty("contractId")
//    var contractId: String?=null,
//
//    @get:JsonProperty("paymentMethod")
//    var paymentMethod: String?=null,
//
//    @get:JsonProperty("transactionId")
//    var transactionId: String? = null,
//
//    @get:JsonProperty("subscriptionPaymentDay")
//    var subscriptionPaymentDay: Int?=null,
//
//    @get:JsonProperty("lastName")
//    var lastName: String? = null,
//
//    @get:JsonProperty("firstName")
//    var firstName: String? = null,
//
//    @get:JsonProperty("address1")
//    var address1: String? = null,
//
//    @get:JsonProperty("address2")
//    var address2: String? = null,
//
//    @get:JsonProperty("zipcode")
//    var zipcode: String? = null,
//
//    @get:JsonProperty("city")
//    var city: String? = null,
//
//    @get:JsonProperty("state")
//    var state: String? = null,
//
//    @get:JsonProperty("phone")
//    var phone: String? = null,
//
//    @get:JsonProperty("email")
//    var email: String? = null,
//
//    @get:JsonProperty("cardNumber")
//    var cardNumber: String? = null,
//
//    @get:JsonProperty("cardType")
//    var cardType: String? = null,
//
//    @get:JsonProperty("monthlyTotalPrice")
//    var monthlyTotalPrice: Double?=null,
//
//    @get:JsonProperty("monthlyDiscountPrice")
//    var monthlyDiscountPrice: Double?=null,
//
//    @get:JsonProperty("itemMonthlyPrice")
//    var itemMonthlyPrice: Double?=null,
//
//    @get:JsonProperty("monthlyTax")
//    var monthlyTax: Double?=null,
//
//    @get:JsonProperty("currency")
//    var currency: String?=null,
//
////    @get:JsonProperty("taxLines")
////    var taxLines: MutableList<TaxLine> = mutableListOf(),
//
//    @get:JsonProperty("taxLines")
//    var contract: OmsContract?=null,
//
//    @get:JsonProperty("id")
//    var id: String?=null,
//
//    ) {
//}