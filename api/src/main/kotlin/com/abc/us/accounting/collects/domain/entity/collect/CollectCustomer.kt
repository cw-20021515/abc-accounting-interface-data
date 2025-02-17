package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.ChannelTypeEnum
import com.abc.us.accounting.collects.domain.type.CustomerStatusEnum
import com.abc.us.accounting.collects.domain.type.CustomerTypeEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.abc.us.oms.domain.contract.entity.Contract
import com.abc.us.oms.domain.customerservice.entity.CustomerService
import com.abc.us.oms.domain.order.entity.Order
import com.abc.us.oms.domain.order.entity.OrderItemCustomer
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.Transient

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectCustomer (
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Comment("고객 식별자 (userId)")
    var customerId: String,

    @Embedded
    var name : EmbeddableName,

    @Embedded
    var relation : EmbeddableRelation? = null,

    @Comment("채널 고객 식별자")
    var channelCustomerId: String? = null,

    @Comment("고객 상태")
    @Enumerated(EnumType.STRING)
    var customerStatus: CustomerStatusEnum? = null,

    @Comment("채널 유형")
    @Enumerated(EnumType.STRING)
    var channelType: ChannelTypeEnum? = null,

    @Comment("고객 유형 oms --> accountType 으로 매칭됨")
    @Enumerated(EnumType.STRING)
    var customerType: CustomerTypeEnum? = null,

    @Comment("세금 부과 여부")
    @Convert(converter = YesNoConverter::class)
    var isTaxLiability: Boolean = true,

//    @Comment("고객 회원 여부")
//    @Convert(converter = YesNoConverter::class)
//    var isMember: Boolean = ,

    @Comment("고객 통화") // 기본값은 US
    var currency: Currency = Currency.getInstance(Locale.US),

    @Comment("설치 제품의 ID")
    var installationId : String? = null,

    @Comment("추천인 코드")
    var referrerCode : String? = null,

    @IgnoreHash
    @Comment("생성 시간")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("업데이트 시간")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("고객 활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @IgnoreHash
    @Transient
    var contracts: Set<Contract>? = null,

    @IgnoreHash
    @Transient
    var orders: Set<Order>? = null,

    @IgnoreHash
    @Transient
    var customerServices: Set<CustomerService>? = null,

    @IgnoreHash
    @Transient
    var orderItemCustomers: MutableList<OrderItemCustomer> = mutableListOf(),
){

}
