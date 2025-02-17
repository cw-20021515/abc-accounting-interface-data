package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectShipping(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Comment("주문 아이디")
    var shippingId: String,

    @Column(name = "service_flow_id")
    val serviceFlowId: String,

    @Column(name = "company_name")
    var companyName: String? = null,

    @Column(name = "company_code")
    var companyCode: String? = null,

    @Column(name = "tracking_id")
    val trackingId: String,

    @Column(name = "tracking_url")
    var trackingUrl: String? = null,

    @Column(name = "shipment_date")
    var shipmentDate: LocalDateTime? = null,

    @Column(name = "delivery_date")
    var deliveryDate: LocalDateTime? = null,

    @Column(name = "estimated_delivery_date")
    var estimatedDeliveryDate: LocalDate? = null,

    @IgnoreHash
    @Comment("등록 일시")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,
) {
}