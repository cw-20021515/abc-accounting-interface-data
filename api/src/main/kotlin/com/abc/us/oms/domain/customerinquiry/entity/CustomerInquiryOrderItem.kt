package com.abc.us.oms.domain.customerinquiry.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.order.entity.OrderItem
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "customer_inquiry_order_item", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerInquiryOrderItem(
    @Column(name = "customer_inquiry_id")
    val customerInquiryId: String,
    @Column(name = "order_item_id")
    val orderItemId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", referencedColumnName = "id", insertable = false, updatable = false)
    var orderItem: OrderItem? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_inquiry_id", referencedColumnName = "id", insertable = false, updatable = false)
    var customerInquiry: CustomerInquiry? = null,
) : AuditTimeEntity()
