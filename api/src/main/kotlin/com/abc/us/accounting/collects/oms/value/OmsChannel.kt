package com.abc.us.accounting.collects.oms.value

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmsChannel(
    @get:JsonProperty("id")
    val id: String?=null,

    @get:JsonProperty("channelType")
    val channelType: String?=null,

    @get:JsonProperty("channelName")
    val channelName: String?=null,

    @get:JsonProperty("channelDetail")
    val channelDetail: String? = null,

    @get:JsonProperty("createTime")
    val createTime: LocalDateTime?=null,

    @get:JsonProperty("updateTime")
    var updateTime: LocalDateTime?=null,
) {
}
