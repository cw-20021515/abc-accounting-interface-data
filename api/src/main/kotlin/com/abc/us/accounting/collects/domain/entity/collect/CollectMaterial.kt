package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.*
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectMaterial(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String?=null,

    @Embedded
    val relation : EmbeddableRelation? = null,

    @Comment("자재ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("품목코드")
    @Column(name = "material_series_code")
    var materialSeriesCode: String?=null,

    @Comment("자재 이름")
    @Column(name = "material_name")
    val materialName: String,

    @Comment("모델명")
    @Column(name = "material_model_name")
    var materialModelName: String? =  null,

    @Comment("자재 브랜드 이름")
    @Column(name = "material_brand_name")
    var materialBrandName: String,

    @Comment("자재유형")
    @Column(name = "material_type")
    @Enumerated(EnumType.STRING)
    val materialType: MaterialType,

    @Comment("제품군")
    @Column(name = "material_category_code")
    @Enumerated(EnumType.STRING)
    val materialCategoryCode: MaterialCategoryCode,

    @Comment("상품구분")
    @Column(name = "product_type")
    @Enumerated(EnumType.STRING)
    val productType: ProductType,

    @Comment("설치유형")
    @Column(name = "installation_type")
    //@Convert(converter = InstallationTypeConverter::class)
    @Enumerated(EnumType.STRING)
    val installationType: InstallationType? = null,

    @Comment("필터유형")
    @Column(name = "filter_type")
//    @Convert(converter = FilterTypeConverter::class)
    @Enumerated(EnumType.STRING)
    val filterType: FilterType? = null,

    @Comment("자재 주요 기능 속성 코드")
    @Column(name = "feature_code")
    //@Convert(converter = FeatureCodeConverter::class)
    @Enumerated(EnumType.STRING)
    val featureCode: FeatureCode? = null,

    @Comment("갱신일시")
    @Column(name = "description")
    var description: String?=null,

    @IgnoreHash
    @Comment("생성일시")
    @Column(name = "create_time")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("갱신일시")
    @Column(name = "update_time")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("계정의 활성화 상태")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,
) {
    override fun toString(): String {
        return "CollectMaterial(hashCode=$hashCode, createTime=$createTime, updateTime=$updateTime, isActive=$isActive, relation=$relation, materialId=$materialId, materialSeriesCode=$materialSeriesCode, materialName=$materialName, materialModelName=$materialModelName, materialBrandName=$materialBrandName, materialType=$materialType, materialCategoryCode=$materialCategoryCode, productType=$productType, installationType=$installationType, filterType=$filterType, featureCode=$featureCode, description=$description)"
    }


    fun withMaterialName(materialName: String): CollectMaterial {
        return this.copy(materialName = materialName)
    }
}
