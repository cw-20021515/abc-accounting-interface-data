package com.abc.us.oms.domain.serviceflow.entity

import com.abc.us.oms.domain.common.config.AbstractEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "service_location_additional_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceLocationAdditionalInfo(
    @Column(name = "service_location_id")
    val serviceLocationId: String,
    @Column(name = "info_type", nullable = false)
    val infoType: String,
    @Column(name = "info_value", nullable = false)
    val infoValue: String,
    @Column(name = "custom_value")
    val customValue: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_location_id", referencedColumnName = "id", updatable = false, insertable = false)
    val serviceLocation: ServiceLocation? = null,
) : AbstractEntity()
