package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.BillingTypeEnum
import com.abc.us.accounting.collects.domain.type.ReceiptMethodEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.jvm.Transient

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectReceipt(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Embedded
    var relation : EmbeddableRelation,

    @Embedded
    var name : EmbeddableName,

    @Embedded
    var location : EmbeddableLocation,

    @Embedded
    var price : EmbeddablePrice,

//    var itemPrice: BigDecimal?=null
//
//    var discountPrice: BigDecimal?=null
//
//    var registrationPrice: BigDecimal?=null
//
//    var totalPrice: BigDecimal?=null
//
//    var prepaidAmount: BigDecimal? = null
//
//    var tax: BigDecimal?=null
//
//    var currency: String?=null


    @Comment("청구ID")
    var chargeId: String?=null,

    @Comment("청구서 아이디")
    var invoiceId: String?=null,

    @Comment("결제 정보ID")
    var receiptId: String?=null,

    @Comment("지급 ID - shopify payments 등 에서 제공하는 지급 ID")
    var depositId: String?=null,

    @Enumerated(EnumType.STRING)
    var receiptMethod: ReceiptMethodEnum?=null,

    @Enumerated(EnumType.STRING)
    var billingType : BillingTypeEnum? = null,

    var transactionId: String? = null,

    var subscriptionReceiptDay: Int?=null,

    var receiptTime: OffsetDateTime?=null,

    var monthlyTotalPrice: BigDecimal?=null,

    var monthlyDiscountPrice: BigDecimal?=null,

    var itemMonthlyPrice: BigDecimal?=null,

    var monthlyTax: BigDecimal?=null,

    var cardNumber: String? = null,

    var cardType: String? = null,

    var remark: String? = null,

    @Column(name = "installment_months")
    var installmentMonths: Int? = null,

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

    @IgnoreHash
    @Transient
    var taxLines : MutableList<CollectTaxLine> = mutableListOf()
){

}