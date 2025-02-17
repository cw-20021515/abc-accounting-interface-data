package com.abc.us.accounting.rentals.lease.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import com.abc.us.accounting.rentals.lease.model.v2.RentalAssetInstallationDataV2 as RentalAssetInstallationDataV2

data class RentalAssetData @JsonCreator constructor(
    @JsonProperty("serialNumber")
    val serialNumber: String,
    @JsonProperty("materialId")
    val materialId: String,
    @JsonProperty("acquisitionCost")
    val acquisitionCost: BigDecimal,
    @JsonProperty("contractId")
    val contractId: String,
    @JsonProperty("contractDate")
    val contractDate: LocalDate?,
    @JsonProperty("contractStatus")
    val contractStatus: String,
    @JsonProperty("orderId")
    val orderId: String,
    @JsonProperty("orderItemId")
    val orderItemId: String,
    @JsonProperty("customerId")
    val customerId: String,
    @JsonProperty("installationDate")
    val installationDate: LocalDate?
) {
    companion object {
        fun from(
            item: RentalAssetHistoryItemData
        ): RentalAssetData {
            return RentalAssetData(
                serialNumber = item.serialNumber,
                materialId = item.materialId,
                acquisitionCost = item.acquisitionCost,
                contractId = item.contractId,
                contractDate = item.contractDate,
                contractStatus = item.contractStatus,
                orderId = item.orderId,
                orderItemId = item.orderItemId,
                customerId = item.customerId,
                installationDate = null
            )
        }

        fun from(
            data: RentalAssetInstallationData
        ): RentalAssetData {
            return RentalAssetData(
                serialNumber = data.installation.serialNumber!!,
                materialId = data.contract.materialId!!,
                acquisitionCost = data.inventoryValue.stockAvgUnitPrice,
                contractId = data.contract.contractId,
                contractDate = data.contract.startDate,
                contractStatus = data.contract.contractStatus!!,
                orderId = data.contract.orderId!!,
                orderItemId = data.contract.orderItemId!!,
                customerId = data.contract.customerId!!,
                installationDate = data.installation.installationTime?.toLocalDate()
            )
        }

        fun from(
            data: RentalAssetInstallationDataV2
        ): RentalAssetData {
            return RentalAssetData(
                serialNumber = data.serviceFlow.serialNumber!!,
                materialId = data.orderItem.materialId,
                acquisitionCost = data.inventoryValue.stockAvgUnitPrice,
                contractId = data.contract.contractId,
                contractDate = data.contract.startDate,
                contractStatus = data.contract.contractStatus.toString(),
                orderId = data.orderItem.orderId,
                orderItemId = data.contract.orderItemId,
                customerId = data.contract.customerId,
                installationDate = data.serviceFlow.createTime.toLocalDate()
            )
        }
    }
}