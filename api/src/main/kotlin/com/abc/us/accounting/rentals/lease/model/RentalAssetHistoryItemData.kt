package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule
import com.fasterxml.jackson.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@JsonIgnoreProperties(value = ["eventType", "installationDate"], allowSetters = true)
data class RentalAssetHistoryItemData @JsonCreator constructor(
    @JsonProperty("serialNumber")
    val serialNumber: String,
    @JsonProperty("materialId")
    val materialId: String,
    @JsonProperty("modelName")
    val modelName: String,
    @JsonProperty("depreciationCount")
    val depreciationCount: Int?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("depreciationDate")
    val depreciationDate: LocalDate?,
    @JsonProperty("acquisitionCost")
    val acquisitionCost: BigDecimal,
    @JsonProperty("depreciationExpense")
    val depreciationExpense: BigDecimal?,
    @JsonProperty("accumulatedDepreciation")
    val accumulatedDepreciation: BigDecimal?,
    @JsonProperty("bookValue")
    val bookValue: BigDecimal,
    @JsonProperty("contractId")
    val contractId: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("contractDate")
    val contractDate: LocalDate,
    @JsonProperty("contractStatus")
    val contractStatus: String,
    @JsonProperty("orderId")
    val orderId: String,
    @JsonProperty("orderItemId")
    val orderItemId: String,
    @JsonProperty("customerId")
    val customerId: String,
    @JsonProperty("eventType")
    val eventType: String?,
    @JsonProperty("installationDate")
    val installationDate: LocalDate?,
    @JsonProperty("schedule")
    val schedule: RentalAssetDepreciationSchedule?
)
