package com.abc.us.oms.domain.serviceflow.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

//@Entity
//@Table(name = "service_location", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceLocation(
    @Column(name = "service_flow_id")
    var serviceFlowId: String,
    @Column(name = "branch_id")
    var branchId: String? = null,
    @Column(name = "warehouse_id")
    var warehouseId: String? = null,
    @Column(name = "last_name")
    var lastName: String,
    @Column(name = "first_name")
    var firstName: String,
    @Column(name = "address1")
    var address1: String,
    @Column(name = "address2")
    var address2: String,
    @Column(name = "zipcode")
    var zipcode: String,
    @Column(name = "city")
    var city: String,
    @Column(name = "state")
    var state: String,
    @Column(name = "phone")
    var phone: String,
    @Column(name = "email")
    var email: String,
    @Column(name = "latitude")
    var latitude: Double? = null,
    @Column(name = "longitude")
    var longitude: Double? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_flow_id", referencedColumnName = "id", updatable = false, insertable = false)
    var serviceFlow: ServiceFlow?,
    @OneToMany(mappedBy = "serviceLocation", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var additionalInfos: MutableList<ServiceLocationAdditionalInfo>? = null,
    @Id
    @Column(name = "id")
    var id: String? = null,
)
