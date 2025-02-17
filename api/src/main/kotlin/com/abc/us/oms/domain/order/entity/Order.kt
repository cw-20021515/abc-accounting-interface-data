package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.channel.entity.Channel
import com.abc.us.oms.domain.customer.entity.Customer
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "order", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Order(
    @Column(name = "channel_order_id")
    val channelOrderId: String,

    @Column(name = "channel_id")
    val channelId: String,

    @Column(name = "order_product_type")
    var orderProductType: String? = null,

    @Column(name = "referrer_code")
    var referrerCode: String? = null,

    @Column(name = "order_create_time")
    val orderCreateTime: LocalDateTime? = null,

    @Column(name = "order_update_time")
    var orderUpdateTime: LocalDateTime? = null,

    @Column(name = "create_time", columnDefinition = "timestamp(3)")
    @CreatedDate
    var createTime: LocalDateTime? = null,

    @Column(name = "update_time", columnDefinition = "timestamp(3)")
    @LastModifiedDate
    var updateTime: LocalDateTime? = null,

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var orderItems: MutableList<OrderItem> = mutableListOf(),

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var deliveryAddress: DeliveryAddress? = null,

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var payment: Payment? = null,

    @Column(name = "customer_id")
    var customerId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customer: Customer? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", referencedColumnName = "id", updatable = false, insertable = false)
    val channel: Channel? = null,

    @Id
    @Column(name = "id")
    val id: String,
)
