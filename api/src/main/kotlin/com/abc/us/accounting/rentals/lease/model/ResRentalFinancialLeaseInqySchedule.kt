package com.abc.us.accounting.rentals.lease.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(description = "응답 금융 리스 상각 스케줄 현황")
data class ResRentalFinancialLeaseInqySchedule(

    @Schema(description = "계약 ID")
    val contractId: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "계약일")
    val contractDate: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "기준일")
    val baseDate: LocalDate? = null,

    @Schema(description = "청구 회차")
    val depreciationCount: Int? = null,

    @Schema(description = "최초 장부 금액")
    val initialBookValue: BigDecimal? = null,

    @Schema(description = "장부 금액")
    val depreciationBookValue: BigDecimal? = null,

    @Schema(description = "렌탈료(재화)", example = "37.08100")
    val rentalAmountForGoods: BigDecimal? = null,

    @Schema(description = "현재 가치(PV)")
    val depreciationPresentValue: BigDecimal? = null,

    @Schema(description = "현 할차")
    val depreciationCurrentDifference: BigDecimal? = null,

    @Schema(description = "이자 수익")
    val depreciationInterestIncome: BigDecimal? = null,

    @Schema(description = "이자 수익 누계액")
    val cumulativeInterestIncome: BigDecimal? = null,

    @Schema(description = "주문 아이템 ID")
    val orderItemId: String? = null,

    @Schema(description = "고객 ID")
    val customerId: String? = null,

    @Schema(description = "일련 번호")
    val serialNumber: String? = null,

    @Schema(description = "자재 ID")
    val materialId: String? = null,

    @Schema(description = "모델명")
    val modelName: String? = null,

    @Schema(description = "렌탈이벤트타입")
    val rentalEventType: String,

    @Schema(description = "카테고리(자재 분류 코드)", example = "WATER_PURIFIER")
    var materialCategory: String? = null,  // 카테고리(MaterialCategoryCode)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "생성 시간")
    val createTime: OffsetDateTime? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "총 개수", hidden = true)
    var totalCnt: Long? = 0
) {
    companion object {
        fun from(
            data: ResRentalFinancialLeaseInqyScheduleTemp
        ): ResRentalFinancialLeaseInqySchedule {
            return ResRentalFinancialLeaseInqySchedule(
                contractId = data.contractId,
                contractDate = data.contractDate,
                baseDate = data.baseDate,
                depreciationCount = data.depreciationCount,
                initialBookValue = data.initialBookValue,
                depreciationBookValue = data.depreciationBookValue,
                rentalAmountForGoods = data.rentalAmountForGoods,
                depreciationPresentValue = data.depreciationPresentValue,
                depreciationCurrentDifference = data.depreciationCurrentDifference,
                depreciationInterestIncome = data.depreciationInterestIncome,
                cumulativeInterestIncome = data.cumulativeInterestIncome,
                orderItemId = data.orderItemId,
                customerId = data.customerId,
                serialNumber = data.serialNumber,
                materialId = data.materialId,
                modelName = data.modelName,
                rentalEventType = data.rentalEventType,
                materialCategory = data.materialCategory,
                createTime = data.createTime,
                totalCnt = data.totalCnt
            )
        }
    }
}