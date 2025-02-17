package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.OrderProductTypeEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime
import kotlin.jvm.Transient

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectOrder(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Embedded
    var relation : EmbeddableRelation? = null,

    @Comment("주문 아이디")
    var orderId: String,

    var customerId: String,

    var channelId: String?=null,

    //var deliveryLocationId : String? = null

    var channelOrderId: String?=null,

    @Enumerated(EnumType.STRING)
    var orderProductType: OrderProductTypeEnum? = null,

    var referrerCode: String? = null,

    @IgnoreHash
    var orderCreateTime: OffsetDateTime? = null,

    @IgnoreHash
    var orderUpdateTime: OffsetDateTime? = null,

    var receiptId : String? = null,

    @Comment("배송 정보 ID")
    var shippingId: String?=null,

    @IgnoreHash
    @Comment("생성시간")
    var createTime: OffsetDateTime?=null,

    @IgnoreHash
    @Comment("수정시간")
    var updateTime: OffsetDateTime?= null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

//    @IgnoreHash
//    @Transient
//    var orderItems : MutableList<CollectOrderItem> = mutableListOf(),

    @IgnoreHash
    @Transient
    var channel : CollectChannel?=null,

    @IgnoreHash
    @Transient
    var customer : CollectCustomer?=null,

    @IgnoreHash
    @Transient
    var deliveryAddress : CollectLocation? = null,

    @IgnoreHash
    @Transient
    var receipt : CollectReceipt? = null,

    @IgnoreHash
    @Transient
    var shipping: CollectShipping? = null,
)