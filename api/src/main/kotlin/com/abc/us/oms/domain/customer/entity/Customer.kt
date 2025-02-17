package com.abc.us.oms.domain.customer.entity

import com.abc.us.oms.domain.contract.entity.Contract
import com.abc.us.oms.domain.customerservice.entity.CustomerService
import com.abc.us.oms.domain.order.entity.Order
import com.abc.us.oms.domain.order.entity.OrderItemCustomer
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "customer", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Customer(
    @Column(name = "user_id")
    var userId: String? = null,
    @Column(name = "channel_customer_id")
    var channelCustomerId: String? = null,
    @Column
    var email: String,
    @Column
    var phone: String,
    @Column(name = "account_type")
    var accountType: String,
    @Column(name = "customer_status")
    var customerStatus: String,
    @Column(name = "first_name")
    var firstName: String? = null,
    @Column(name = "last_name")
    var lastName: String? = null,
    @Column(name = "referrer_code")
    var referrerCode: String? = null,
    @CreatedBy
    @Column(name = "create_user")
    var createUser: String = "test",
    @LastModifiedBy
    @Column(name = "update_user")
    var updateUser: String = "test",
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var contracts: Set<Contract>? = null,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var orders: Set<Order>? = null,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customerServices: Set<CustomerService>? = null,
    @OneToMany(
        mappedBy = "customer",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var orderItemCustomers: MutableList<OrderItemCustomer> = mutableListOf(),
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    var createTime: LocalDateTime? =null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
    @Id
    @Column
    val id: String,
) {
    companion object
}
