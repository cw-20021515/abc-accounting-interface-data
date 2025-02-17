package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

//@Entity
//@Table(name = "filter_delivery")
@JsonInclude(JsonInclude.Include.NON_NULL)
class FilterDelivery(
    @Column(name = "installation_filter_id")
    val installationFilterId: String,

    @Column(name = "serial_number")
    val serialNumber: String,

    @Column(name = "filter_cycle")
    val filterCycle: Int,

    @Column(name = "shipping_information_id")
    val shippingInformationId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "installation_filter_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    val installationFilter: InstallationFilter? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shipping_information_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    val shippingInformation: ShippingInformation? = null,

) : AuditTimeEntity()
