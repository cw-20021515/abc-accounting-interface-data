package com.abc.us.oms.domain.order.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

//@Entity
//@Table(name = "delivery_address", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class DeliveryAddress(
    @Column(name = "order_id")
    var orderId: String,
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
    @Column(name = "remark")
    var remark: String? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "order_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var order: Order? = null,
    @Id
    @Column(name = "id")
    var id: String? = null,
)
