package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.DiscountTypeEnum
import com.abc.us.accounting.collects.domain.type.OffetTypeEnum
import com.abc.us.accounting.collects.domain.type.PromotionDiscountTargetTypeEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectPromotion {
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    @Embedded
    var relation : EmbeddableRelation? = null

    var promotionId: String? = null

    var promotionName: String? = null

    var promotionDescription: String? = null

    var startDate: OffsetDateTime? = null

    var endDate: OffsetDateTime? = null

    var discountPrice: BigDecimal? = null

    @Enumerated(EnumType.STRING)
    val discountTargetType : PromotionDiscountTargetTypeEnum? = null

    @Enumerated(EnumType.STRING)
    val discountType : DiscountTypeEnum? = null

    var promotionCycles: String? = null

    var promotionType : String? = null

    @Comment("사은품 타입")
    @Enumerated(EnumType.STRING)
    var offerType : OffetTypeEnum? = null

    @Comment("사은품 ID")
    var materialId : String? = null

    @Comment("할인금액")
    var amount : BigDecimal? = null

    @Comment("할인율")
    var rate : BigDecimal? = null

    @IgnoreHash
    @Comment("등록 일시")
    var createTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

}