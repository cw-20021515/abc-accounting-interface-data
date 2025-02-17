package com.abc.us.accounting.collects.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class Location {
    var locationId: String? = null
    var branchId: String? = null
    var warehouseId: String? = null
    var city: String? = null
    var country: String? = null
    var zipCode: String? = null
    var state: String? = null
    var countryCode: String? = null
    var county: String? = null
    var address1: String? = null
    var address2: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var createTime: OffsetDateTime? = null
    var updateTime: OffsetDateTime? = null
}