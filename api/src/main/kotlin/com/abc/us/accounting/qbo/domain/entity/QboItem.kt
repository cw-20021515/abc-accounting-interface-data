package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.collects.domain.type.ItemCreateCategoryEnum
import com.abc.us.accounting.collects.domain.type.ItemCreateTypeEnum
import com.abc.us.accounting.qbo.domain.entity.key.QboItemKey
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.OffsetDateTime


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboItem  (
    @Id
    @EmbeddedId
    val key: QboItemKey,

    @Comment("QBO 의 SKU에 할당될 ID")
    var materialId: String?= null,

    @Comment("item 생성 unique name")
    val displayName : String,

    @Comment("item 생성 템플릿 참조 ID")
    val templateId : String,

    @Comment("아이템 카테고리")
    @Enumerated(EnumType.STRING)
    val createCategory: ItemCreateCategoryEnum,

    @Comment("아이템 세부 항목")
    @Enumerated(EnumType.STRING)
    val createType: ItemCreateTypeEnum,


    @Comment("QBO 에 등록될 경우 생성된 연관 유형(예 : item.NonInventory")
    val associatedType: String,

    @Comment("자산 계정 코드")
    var assetAccountCode: String? = null,

    @Comment("자산 계정 이름")
    var assetAccountName: String? = null,

    @Comment("매출 계정 코드")
    var incomeAccountCode: String? = null,

    @Comment("매출 계정 이름")
    var incomeAccountName: String? = null,

    @Comment("매출원가 계정 코드")
    var expenseAccountCode: String? = null,

    @Comment("매출원가 계정 이름")
    var expenseAccountName: String? = null,

    @Comment("전표 유형")
    var documentType: String? = null,

    @Comment("관리 단위 (SKU)")
    var managementUnit: String? = null,

    @Comment("자재 설명")
    var description: String? = null,

    @Comment("제품 단가")
    var unitPrice: BigDecimal? = null,

    @Comment("제품 단가율")
    var ratePercent: BigDecimal? = null,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    val submitResult: String,

    @Comment("생성 시간")
    var createTime: OffsetDateTime? = null,

    @Comment("갱신 시간")
    var updateTime: OffsetDateTime? = null,
    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
) {

}