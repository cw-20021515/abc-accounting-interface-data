package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.material.entity.Material
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate

//@Entity
//@Table(name = "installation_filter", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class InstallationFilter(
    @Column(name = "installation_information_id")
    val installationInformationId: String,

    @Column(name = "material_id")
    val materialId: String,

    @Column(name = "serial_number")
    val serialNumber: String,

    @Column(name = "next_shipping_due_date")
    val nextShippingDueDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "installation_information_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
    )
    var installationInformation: InstallationInformation?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "id", insertable = false, updatable = false)
    var material: Material?,

    @OneToOne(
        mappedBy = "installationFilter",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var filterPairing: FilterPairing? = null,

    @OneToMany(
        mappedBy = "installationFilter",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var filterDeliveries: MutableList<FilterDelivery> = mutableListOf(),

//    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)

) : AuditTimeEntity()
