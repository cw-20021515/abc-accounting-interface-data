package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

//@JsonInclude(JsonInclude.Include.NON_NULL)
//class OmsContractDocument(
//    @get:JsonProperty("id")
//    var id: String? = null,
//
//    @get:JsonProperty("contractId")
//    var contractId: String? = null,
//
//    @get:JsonProperty("channelContractId")
//    var channelContractId: String? = null,
//
//    @get:JsonProperty("customerId")
//    var customerId: String? = null,
//
//    @get:JsonProperty("orderItemId")
//    var orderItemId: String? = null,
//
//    @get:JsonProperty("contract")
//    var contract: OmsContract? = null,
//
//    @get:JsonProperty("revision")
//    var revision: Int? = null,
//
//    @get:JsonProperty("mappingData")
//    var mappingData: String? = null,
//
//    @get:JsonProperty("fileUrl")
//    var fileUrl: String? = null,
//
//    @get:JsonProperty("createTime")
//    val createTime: LocalDateTime? = null,
//
//    @get:JsonProperty("updateTime")
//    var updateTime: LocalDateTime? = null,
//) {
//}