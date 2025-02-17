package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.collects.domain.type.ItemCreateCategoryEnum
import com.abc.us.accounting.collects.domain.type.ItemCreateTypeEnum
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboItemTemplate (

    @Id
    @Comment("고유 식별자")
    val templateId: String,

    @Comment("생성 필요한 회사 코드")
    val companyCode: String,

    @Comment("아이템 카테고리")
    @Enumerated(EnumType.STRING)
    val createCategory: ItemCreateCategoryEnum,

    @Comment("아이템 세부 항목")
    @Enumerated(EnumType.STRING)
    val createType: ItemCreateTypeEnum,

    @Comment("QBO 에 등록될 경우 생성된 연관 유형(예 : item.NonInventory")
    val associatedType: String,

    @Comment("자산 계정 과목 코드")
    val assetAccountCode: String? = null,

    @Comment("자산 계정 과목 이름")
    val assetAccountName: String? = null,

    @Comment("매출 계정 과목 코드")
    val incomeAccountCode: String? = null,

    @Comment("매출 계정 과목 이름")
    val incomeAccountName: String? = null,

    @Comment("매출원가 계정 과목 코드")
    val expenseAccountCode: String? = null,

    @Comment("매출원가 계정 과목 이름")
    val expenseAccountName: String? = null,

    @Comment("관리 단위 (SKU)")
    val managementUnit: String? = null,

    @Comment("전표 유형")
    val documentType: String? = null,
) {

}
