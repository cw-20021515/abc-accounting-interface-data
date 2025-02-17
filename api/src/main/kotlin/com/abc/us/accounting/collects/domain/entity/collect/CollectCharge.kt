package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.type.ChargeStatusEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.jvm.Transient

@Entity
data class CollectCharge (
    @Id
    @IgnoreHash
    @Comment("entity 고유 식별자")
    var hashCode: String? = null,

    val chargeId: String,

    @Embedded
    val price : EmbeddablePrice,

    @Enumerated(EnumType.STRING)
    val chargeStatus: ChargeStatusEnum,

    val billingCycle: Int,

    var targetMonth: String? = null,

    val contractId: String,

    var invoiceId: String?=null,

    var receiptId: String? = null,

    var startDate: LocalDate? = null,

    var endDate: LocalDate?=null,

    @IgnoreHash
    @Comment("생성일시")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("갱신일시")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @IgnoreHash
    @Transient
    var chargeItems : MutableList<CollectChargeItem> = mutableListOf(),

    @IgnoreHash
    @Transient
    var receipt : CollectReceipt?=null
) {

}