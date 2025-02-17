package com.abc.us.accounting.collects.oms.value

import com.abc.us.generated.models.AccountType
import com.abc.us.generated.models.CustomerStatus
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmsCustomer(

    @get:JsonProperty("id")
    val id: String,

    @get:JsonProperty("firstName")
    val firstName: String,

    @get:JsonProperty("lastName")
    val lastName: String,

    @get:JsonProperty("email")
    val email: String,

    @get:JsonProperty("phone")
    val phone: String,

    @get:JsonProperty("userId")
    val userId: String? = null,

    @get:JsonProperty("channelCustomerId")
    val channelCustomerId: String? = null,

    @get:JsonProperty("customerStatus")
    val customerStatus: CustomerStatus? = null,

    @get:JsonProperty("accountType")
    val accountType: AccountType? = null,

    @get:JsonProperty("createTime")
    val createTime: java.time.OffsetDateTime? = null,

    @get:JsonProperty("updateTime")
    val updateTime: java.time.OffsetDateTime? = null
)