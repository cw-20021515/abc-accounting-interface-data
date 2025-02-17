package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime
import kotlin.jvm.Transient
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus as MasterServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType as MasterServiceFlowType

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectServiceFlow (
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Comment("서비스 아이디")
    var serviceFlowId: String?=null,

    @Comment("주문 아이디")
    var orderId: String?=null,

    @Comment("주문 아이템 아이디")
    var orderItemId: String?=null,

    @Comment("")
    @Enumerated(EnumType.STRING)
    var serviceType: MasterServiceFlowType,

    @Comment("")
    @Enumerated(EnumType.STRING)
    var serviceStatus: MasterServiceFlowStatus,

    @Comment("고객서비스 아이디")
    var customerServiceId: String? = null,

    @Comment("고객서비스 티켓 아이디")
    var customerServiceTicketId: String? = null,

    @Comment("추가 정산 빌링 아이디")
    var billingId: String? = null,

    @Comment("작업아이디")
    var workId: String? = null,

    @IgnoreHash
    @Comment("등록 일시")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("취소 일시")
    var cancelTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @IgnoreHash
    @Transient
    var location : CollectLocation? = null,

    @IgnoreHash
    @Transient
    var receipts : MutableList<CollectReceipt> = mutableListOf()
){

    override fun toString(): String {
        return "CollectServiceFlow(hashCode=$hashCode, createTime=$createTime, updateTime=$updateTime, cancelTime=$cancelTime, isActive=$isActive, serviceFlowId=$serviceFlowId, orderId=$orderId, orderItemId=$orderItemId, serviceType=$serviceType, serviceStatus=$serviceStatus, customerServiceId=$customerServiceId, customerServiceTicketId=$customerServiceTicketId, billingId=$billingId, workId=$workId)"
    }
}