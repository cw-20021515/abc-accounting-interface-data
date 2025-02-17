package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule
import com.abc.us.accounting.supports.NumberUtil
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

@JsonIgnoreProperties(value = ["serialNumber", "depreciationDate"], allowSetters = true)
data class RentalAssetDepreciationScheduleItemData(
    @JsonProperty("depreciationCount")
    val depreciationCount: Int,
    @JsonProperty("depreciationPeriod")
    val depreciationPeriod: String,
    @JsonProperty("currency")
    val currency: String,
    @JsonProperty("beginningBookValue")
    val beginningBookValue: BigDecimal,
    @JsonProperty("depreciationExpense")
    val depreciationExpense: BigDecimal,
    @JsonProperty("endingBookValue")
    val endingBookValue: BigDecimal,
    @JsonProperty("accumulatedDepreciation")
    val accumulatedDepreciation: BigDecimal,
    @JsonProperty("serialNumber")
    val serialNumber: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("depreciationDate")
    val depreciationDate: LocalDate?
) {
    companion object {
        fun from(
            data: RentalAssetDepreciationSchedule
        ): RentalAssetDepreciationScheduleItemData {
            return RentalAssetDepreciationScheduleItemData(
                depreciationCount = data.depreciationCount,
                depreciationPeriod = data.depreciationDate.toString().substring(0, 7),
                currency = data.currency,
                beginningBookValue = NumberUtil.setScale(data.beginningBookValue, 2),
                depreciationExpense = NumberUtil.setScale(data.depreciationExpense, 2),
                endingBookValue = NumberUtil.setScale(data.endingBookValue, 2),
                accumulatedDepreciation = NumberUtil.setScale(data.accumulatedDepreciation, 2),
                serialNumber = data.serialNumber,
                depreciationDate = data.depreciationDate
            )
        }
    }

    fun toRentalAssetDepreciationSchedule(
    ): RentalAssetDepreciationSchedule {
        return RentalAssetDepreciationSchedule(
            serialNumber = serialNumber!!,
            depreciationCount = depreciationCount,
            depreciationDate = depreciationDate!!,
            currency = currency,
            beginningBookValue = beginningBookValue,
            depreciationExpense = depreciationExpense,
            endingBookValue = endingBookValue,
            accumulatedDepreciation = accumulatedDepreciation
        )
    }
}
