package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectMaterial
import com.abc.us.accounting.collects.domain.type.*
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.oms.domain.material.entity.Material
import mu.KotlinLogging
import java.time.ZoneOffset

class MaterialBuilder {
    companion object {
        private val logger = KotlinLogging.logger {}

        fun convertMaterialType(type : String) : MaterialType {
            return when(type) {
                "PRODUCT" -> MaterialType.PRODUCT
                "PART" -> MaterialType.PART
                "FILTER" -> MaterialType.FILTER
                "CONSUMABLE" -> MaterialType.CONSUMABLE
                else -> MaterialType.NONE
            }
        }

        fun build(material : Material) : CollectMaterial{
            val featureCode = material.attributes.firstOrNull{attr -> attr.type.equals("KEY_FEATURE")}?.code
            val filterType = material.attributes.firstOrNull{ attr -> attr.type.equals("FILTER_TYPE")}?.code
            val installationType = material.attributes.firstOrNull{attr -> attr.type.equals("INSTALLATION_TYPE")}?.code
            return CollectMaterial(
                materialId = material.id,
                materialName = material.name,
                materialBrandName = material.brandName?.let { it }?:run { "" },
                materialType = MaterialType.valueOf(material.type),
                materialCategoryCode = material.category?.let { MaterialCategoryCode.valueOf(it.code)}?:run { MaterialCategoryCode.NONE },
                productType = material.productType?.let { ProductType.valueOf(it)}?:run {  ProductType.NONE },
                featureCode = if ( featureCode != null) FeatureCode.valueOf(featureCode) else null,
                filterType = if ( filterType != null) FilterType.valueOf(filterType) else null,
                installationType = if ( installationType != null) InstallationType.valueOf(installationType) else null
            )
                .apply {
                    materialSeriesCode = material.series?.let { it.code }
                    materialModelName = material.modelName
                    createTime = material.createTime?.toOffset()
                    updateTime = material.updateTime?.toOffset()
                    description = material.description
                }

        }
    }

}