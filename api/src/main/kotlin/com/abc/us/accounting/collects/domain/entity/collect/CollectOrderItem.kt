package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime
import kotlin.jvm.Transient

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectOrderItem (
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Embedded
    var relation : EmbeddableRelation? = null,

    @Embedded
    var price : EmbeddablePrice? = null,

    @Comment("주문아이디")
    var orderId: String,

    @Comment("주문상세 아이디")
    var orderItemId: String,

    @Comment("판매채널 주문 아이디")
    var channelOrderId: String?=null,

    @Comment("판매채널 주문상세 아이디")
    var channelOrderItemId: String?=null,

    @Enumerated(EnumType.STRING)
    var orderItemStatus: OrderItemStatus,

    @Enumerated(EnumType.STRING)
    var orderItemType: OrderItemType,

    @Comment("entity 비교 위한 code")
    var materialId: String?=null,

    @Comment("contract 식별 id")
    var contractId: String?=null,

    @Comment("install 식별 id")
    var installId: String?=null,

    @Comment("배송 정보 ID")
    var shippingId: String?=null,

    @Comment("수량")
    var quantity: Int?=null,

//    @Comment("entity 비교 위한 code")
//    var totalPrice: BigDecimal?=null

//    @Comment("entity 비교 위한 code")
//    var discountPrice: BigDecimal?=null
//
//    @Comment("entity 비교 위한 code")
//    var itemPrice: BigDecimal?=null

//    @Comment("entity 비교 위한 code")
//    var tax: BigDecimal?=null

//    @Comment("entity 비교 위한 code")
//    var currency: String? = null

//    @Comment("entity 비교 위한 code")
//    var registrationPrice: BigDecimal?=null

    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,


    @IgnoreHash
    @Transient
    var order : CollectOrder? = null,

//    @Transient
//    var shipping : MutableList<CollectShipping> = mutableListOf()

    @IgnoreHash
    @Transient
    var taxLines : MutableList<CollectTaxLine> = mutableListOf(),

    @IgnoreHash
    @Transient
    var installation : CollectInstallation? = null,

    @IgnoreHash
    @Transient
    var shipping: CollectShipping? = null,

    @IgnoreHash
    @Transient
    var material: CollectMaterial? = null,

    @IgnoreHash
    @Transient
    var customers : MutableList<CollectCustomer> = mutableListOf(),

    @IgnoreHash
    @Transient
    var contracts : MutableList<CollectContract> = mutableListOf(),

    @IgnoreHash
    @Transient
    var promotions : MutableList<CollectPromotion> = mutableListOf(),
) {

}