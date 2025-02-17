package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.documents.domain.entity.Account
import com.abc.us.accounting.iface.domain.type.oms.IfMaterialCategoryCode
import com.abc.us.accounting.iface.domain.type.oms.IfMaterialProductType
import com.abc.us.accounting.iface.domain.type.oms.IfMaterialShippingMethodType
import com.abc.us.accounting.iface.domain.type.oms.IfMaterialType
import com.abc.us.accounting.supports.utils.buildToString
import com.abc.us.accounting.supports.utils.toStringByReflection
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment



/**
 * 자재 정보 테이블
 */
@Entity
@Table(name = "if_material")
@Comment("자재 정보")
class IfMaterial(

    @Comment("자재ID")
    @Id
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("자재유형")
    @Column(name = "material_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val materialType: IfMaterialType,

    @Comment("모델이름")
    @Column(name = "material_model_name")
    val materialModelName: String? = null,

    @Comment("모델이름(prefix)")
    @Column(name = "material_model_name_prefix")
    val materialModelNamePrefix: String? = null,

    @Comment("자재이름")
    @Column(name = "material_name", nullable = false)
    val materialName: String,

    @Comment("시리즈코드")
    @Column(name = "material_series_code")
    val materialSeriesCode: String? = null,

    @Comment("시리즈명")
    @Column(name = "material_series_name")
    val materialSeriesName: String? = null,

    @Comment("카테고리코드")
    @Column(name = "material_category_code", nullable = false)
    @Enumerated(EnumType.STRING)
    val materialCategoryCode: IfMaterialCategoryCode,

    @Comment("카테고리명")
    @Column(name = "material_category_name")
    val materialCategoryName: String? = null,

    @Comment("제조사 생산코드 (SAP)")
    @Column(name = "manufacturer_code", nullable = false)
    val manufacturerCode: String,

    @Comment("브랜드명")
    @Column(name = "brand_name")
    val brandName: String? = null,

    @Comment("제품유형")
    @Column(name = "product_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val productType: IfMaterialProductType,

    @Comment("특징")
    @Column(name = "feature_code", nullable = true)
//    @Convert(converter = IfMaterialFeatureCodeConverter::class)
//    @Enumerated(EnumType.STRING)
//    val featureCode: IfMaterialFeatureCode = IfMaterialFeatureCode.UNKNOWN,
    val featureCode : String? = null,
    @Comment("필터유형")
    @Column(name = "filter_type", nullable = true)
//    @Convert(converter = IfMaterialFilterTypeConverter::class)
//    @Enumerated(EnumType.STRING)
//    val filterType: IfMaterialFilterType = IfMaterialFilterType.UNKNOWN,
    val filterType : String? = null,

    @Comment("설치유형")
    @Column(name = "installation_type", nullable = true)
//    @Convert(converter = IfMaterialInstallationTypeConverter::class)
//    @Enumerated(EnumType.STRING)
//    val installationType: IfMaterialInstallationType = IfMaterialInstallationType.UNKNOWN ,
    val installationType : String? = null,



    @Comment("배송유형")
    @Column(name = "shipping_method_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val shippingMethodType: IfMaterialShippingMethodType,

    @Comment("설명")
    @Column(name = "description", nullable = false)
    val description: String,

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
) {

    override fun toString(): String {
        return buildToString {
            add(
                "materialId" to materialId,
                "materialType" to materialType,
                "materialModelName" to materialModelName,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is IfMaterial) return false

        return EqualsBuilder()
            .append(materialId, other.materialId)
            .append(materialType, other.materialType)
            .append(materialModelName, other.materialModelName)
            .append(materialModelNamePrefix, other.materialModelNamePrefix)
            .append(materialName, other.materialName)
            .append(materialSeriesCode, other.materialSeriesCode)
            .append(materialSeriesName, other.materialSeriesName)
            .append(materialCategoryCode, other.materialCategoryCode)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(materialId)
            .append(materialType.name)
            .append(materialModelName)
            .toHashCode()
    }
}
