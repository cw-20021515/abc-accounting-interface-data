package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.type.ChargeItemEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
data class CollectChargeItem (
    @Id
    @IgnoreHash
    @Comment("entity 고유 식별자")
    var hashCode: String? = null,

    @Embedded
    var price : EmbeddablePrice,

//    @Embedded
//    var relation : EmbeddableRelation,

    var chargeId: String,

    var invoiceId: String?=null,

    var chargeItemId: String,

    @Enumerated(EnumType.STRING)
    var chargeItemType: ChargeItemEnum,

    var serviceFlowId: String? = null,

    var quantity: Int? = null,

//    var totalPrice: BigDecimal? = null

//    var isTaxExempt: Boolean = false

    var receiptId: String? = null, // Payment를 ID로 참조

    @IgnoreHash
    @Comment("생성일시")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("갱신일시")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

) {

}