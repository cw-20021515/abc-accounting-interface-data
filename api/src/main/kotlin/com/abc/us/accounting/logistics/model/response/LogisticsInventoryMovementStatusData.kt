package com.abc.us.accounting.logistics.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

data class LogisticsInventoryMovementStatusData @JsonCreator constructor(
    @JsonProperty("movementId")
    val movementId: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("movementDate")
    val movementDate: LocalDate?,
    @JsonProperty("warehouseId")
    val warehouseId: String?,
    @JsonProperty("warehouseName")
    val warehouseName: String?,
    @JsonProperty("materialId")
    val materialId: String?,
    @JsonProperty("movementCategory")
    val movementCategory: String?,
    @JsonProperty("movementGroup")
    val movementGroup: String?,
    @JsonProperty("movementType")
    val movementType: String?,
    @JsonProperty("quantity")
    val quantity: Int?,
    @JsonProperty("unitPrice")
    val unitPrice: BigDecimal?,
    @JsonProperty("amount")
    val amount: BigDecimal?,
    @JsonProperty("inventoryQuantity")
    val inventoryQuantity: Int?,
    @JsonProperty("inventoryUnitPrice")
    val inventoryUnitPrice: BigDecimal?,
    @JsonProperty("inventoryValue")
    val inventoryValue: BigDecimal?,
    @JsonProperty("inventoryCostId")
    val inventoryCostId: String?,
    @JsonProperty("materialName")
    val materialName: String?,
    )
