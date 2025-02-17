package com.abc.us.oms.domain.customerservice.entity

import com.abc.us.oms.domain.customer.entity.Customer
import com.abc.us.oms.domain.customerinquiry.entity.CustomerInquiry
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "customer_service", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerService(
    @Column(name = "channel_id")
    var channelId: String? = null,
    @Column(name = "channel_customer_service_id")
    var channelCustomerServiceId: String? = null,
    @Column(name = "customer_id")
    var customerId: String? = null,
    @Column(name = "customer_service_type")
    var customerServiceType: String,
    @Column(name = "customer_inquiry_id")
    var customerInquiryId: String? = null,
    @OneToMany(mappedBy = "customerService", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var customerServiceTickets: MutableList<CustomerServiceTicket> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customer: Customer? = null,
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    var createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_inquiry_id", updatable = false, insertable = false)
    var customerInquiry: CustomerInquiry? = null,
    @Id
    @Column(name = "id")
    val id: String,
)
