package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.contract.entity.Contract
import com.abc.us.oms.domain.material.entity.Material
import com.abc.us.oms.domain.taxline.entity.TaxLine
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "order_item", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderItem(
    @Column(name = "channel_order_item_id")
    val channelOrderItemId: String,
    @Column(name = "order_id")
    val orderId: String,
    @Column(name = "channel_order_id")
    val channelOrderId: String,
    @Column(name = "sequence")
    var sequence: Int? = 0,
    @Column(name = "order_item_status_code")
    var orderItemStatusCode: String,
    @Column(name = "order_item_type")
    var orderItemType: String,
    @Column(name = "material_id")
    val materialId: String,
    @Column(name = "quantity")
    var quantity: Int = 0,
    @Column(name = "item_price")
    var itemPrice: Double = 0.0,
    @Column(name = "registration_price")
    var registrationPrice: Double = 0.0,
    @Column(name = "discount_price")
    var discountPrice: Double = 0.0,
    @Column(name = "total_price")
    var totalPrice: Double = 0.0,

    @Column(name = "tax")
    var tax: Double = 0.0,

    @Column(name = "currency")
    var currency: String = "USD",

    @Column(name = "shipping_information_id")
    val shippingInformationId: String? = null,

    @CreatedDate
    @Column(name = "create_time")
    val createTime: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", updatable = false, insertable = false)
    val order: Order? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "id", updatable = false, insertable = false)
    val material: Material? = null,


    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("create_time ASC")
    var contracts: MutableList<Contract>? = mutableListOf(),

    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var installationInformation: InstallationInformation? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_information_id", referencedColumnName = "id", updatable = false, insertable = false)
    var shippingInformation: ShippingInformation? = null,

    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var orderItemPromotions: MutableList<OrderItemPromotion> = mutableListOf(),

    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var taxLines: MutableList<TaxLine> = mutableListOf(),

    @Id
    @Column(name = "id")
    val id: String,
)
