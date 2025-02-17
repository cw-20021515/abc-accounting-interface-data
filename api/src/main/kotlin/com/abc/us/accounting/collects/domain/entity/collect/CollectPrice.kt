//package com.abc.us.accounting.collects.domain.entity.collect
//
//import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
//import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
//import com.fasterxml.jackson.annotation.JsonInclude
//import jakarta.persistence.Convert
//import jakarta.persistence.Embedded
//import jakarta.persistence.Entity
//import jakarta.persistence.Id
//import org.hibernate.annotations.Comment
//import org.hibernate.annotations.CreationTimestamp
//import org.hibernate.type.YesNoConverter
//import java.math.BigDecimal
//import java.time.OffsetDateTime
//
//@Entity
//@JsonInclude(JsonInclude.Include.NON_NULL)
//class CollectPrice {
//    @Id
//    @IgnoreHash
//    @Comment("entity 비교 위한 code")
//    var hashCode: String? = null
//
//    @Embedded
//    var relation : EmbeddableRelation? = null
//
//    @Comment("실제 결제 총액")
//    var totalPrice: BigDecimal?=null
//
//    @Comment("할인 금액")
//    var discountPrice: BigDecimal?=null
//
//    @Comment("주문 Item 가격")
//    var itemPrice: BigDecimal?=null
//
//    @Comment("선불금액(포인트, 마일리지, 기 납부금액 등)")
//    var prepaidAmount: BigDecimal?=null
//
//    @Comment("세액")
//    var tax: BigDecimal?=null
//
//    @Comment("통화")
//    var currency: String? = null
//
//    @Comment("등록비")
//    var registrationPrice : BigDecimal?=null
//
//    @IgnoreHash
//    @Comment("등록 일시")
//    @CreationTimestamp
//    var createTime: OffsetDateTime? = null
//
//    @IgnoreHash
//    @Comment("수정 일시")
//    @CreationTimestamp
//    var updateTime: OffsetDateTime? = null
//
//    @IgnoreHash
//    @Comment("활성화 여부")
//    @Convert(converter = YesNoConverter::class)
//    var isActive: Boolean = true
//}