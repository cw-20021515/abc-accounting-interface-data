package com.abc.us.accounting.logistics.model.response

import com.abc.us.generated.models.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

data class LogisticsInventoryCostStatusData @JsonCreator constructor(
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("costUpdateDate")
    val costUpdateDate: LocalDate?,
    @JsonProperty("inventoryCostId")
    val inventoryCostId: String?,
    @JsonProperty("materialId")
    val materialId: String?,
    @JsonProperty("warehouseId")
    val warehouseId: String?,
    @JsonProperty("warehouseName")
    val warehouseName: String?,
    @JsonProperty("inventoryCost")
    val inventoryCost: BigDecimal?,
    @JsonProperty("materialName")
    val materialName: String?,
    @JsonProperty("modelName")
    val modelName: String?,
    @JsonProperty("materialType")
    val materialType: MaterialType?,
    @JsonProperty("materialCategory")
    val materialCategory: MaterialCategoryCode?,
    @JsonProperty("installType")
    val installType: MaterialAttributeInstallationTypeCode?,
    @JsonProperty("filterType")
    val filterType: MaterialAttributeFilterTypeCode?,
    @JsonProperty("featureType")
    val featureType: MaterialAttributeKeyFeatureCode?,
    )