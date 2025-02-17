package com.abc.us.oms.domain.customerinquiry.entity

import com.abc.us.oms.domain.customer.entity.Customer
import com.abc.us.oms.domain.customercenter.entity.CustomerCenterAttachment
import com.abc.us.oms.domain.customerservice.entity.CustomerService
import com.abc.us.oms.domain.material.entity.Material
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "customer_inquiry", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerInquiry(
    @Column(name = "customer_inquiry_channel")
    var customerInquiryChannel: String,
    @Column(name = "customer_inquiry_type_value")
    var customerInquiryTypeValue: String? = null,
    @Column(name = "customer_inquiry_type_tag")
    var customerInquiryTypeTag: String? = null,
    @Column(name = "customer_inquiry_status")
    var customerInquiryStatus: String,
    @Column(name = "material_id")
    var materialId: String? = null,
    @Column(name = "subject")
    var subject: String,
    @Column(name = "content")
    var content: String? = null,
    @Column(name = "summary")
    var summary: String? = null,
    @Column(name = "customer_id")
    var customerId: String,
    @Column(name = "agent_id")
    var agentId: String? = null,
    @Column(name = "agent_first_name")
    var agentFirstName: String? = null,
    @Column(name = "agent_last_name")
    var agentLastName: String? = null,
    @Column(name = "agent_email")
    var agentEmail: String? = null,
    @Column(name = "customer_center_ticket_id")
    var customerCenterTicketId: String,
    @Column(name = "customer_center_ticket_url")
    var customerCenterTicketUrl: String,
    @OneToMany(mappedBy = "customerInquiry", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var attachments: MutableList<CustomerCenterAttachment> = mutableListOf(),
//    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "customerInquiry")
//    var customerCenterTicket: CustomerCenterTicket? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customer: Customer? = null,
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    var createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime?= null,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_inquiry_id",
        insertable = false,
        updatable = false,
    )
    var customerServices: MutableList<CustomerService> = mutableListOf(),
    @OneToMany(mappedBy = "customerInquiry", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var customerInquiryOrderItems: MutableList<CustomerInquiryOrderItem> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "id", updatable = false, insertable = false)
    val material: Material? = null,

    @Id
    @Column(name = "id")
    val id: String = "",//SnowflakeId.generateId().toBase62(),
)
