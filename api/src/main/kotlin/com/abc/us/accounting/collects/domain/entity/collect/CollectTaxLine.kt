package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.SalesTaxType
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectTaxLine(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Embedded
    var relation : EmbeddableRelation? = null,

    var price: BigDecimal,
    val rate: BigDecimal,
    val title: String,

    @Comment("세금 유형")
    @Enumerated(EnumType.STRING)
    var salesTaxType : SalesTaxType,

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