package com.abc.us.accounting.rentals.lease.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(description = "응답 금융 리스 상각 스케줄 정보")
data class ResRentalFinancialLeaseSchedule(

    @Schema(description = "금융 리스 고유 식별자")
    val fLeaseId: String? = null,

    @Schema(description = "주문 ID")
    val orderId: String? = null,

    @Schema(description = "주문 항목 ID")
    val orderItemId: String? = null,

    @Schema(description = "고객 ID")
    val customerId: String? = null,

    @Schema(description = "일련 번호")
    val serialNumber: String? = null,

    @Schema(description = "계약 ID")
    val contractId: String? = null,

    @Schema(description = "자재 ID")
    val materialId: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "기준일")
    val baseDate: LocalDate? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "계약 날짜")
    val contractDate: LocalDate? = null,

    @Schema(description = "자재 시리즈 코드")
    val materialSeriesCode: String? = null,

    @Schema(description = "모델명")
    val modelName: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "계약 종료일")
    val contractEndDate: LocalDate? = null,

    @Schema(description = "계약 기간")
    val contractPeriod: Int? = null,

    @Schema(description = "최초 장부 가치")
    val initialBookValue: Double? = null,

    @Schema(description = "이자율")
    val interestRate: Double? = null,

    @Schema(description = "대여금액")
    val rentalAmount: Double? = null,

    @Schema(description = "렌탈료(재화)", example = "37.08100")
    var rentalAmountForGoods: BigDecimal? = null,

    @Schema(description = "생성 시간")
    val createTime: OffsetDateTime? = null,

    @Schema(description = "최초 현재 가치(PV)", example = "1214.29")
    var initialPresentValue: BigDecimal? = null,

    @Schema(description = "최초 현 할차", example = "132.55")
    var initialCurrentDifference: BigDecimal? = null,

    @Schema(description = "렌탈 금융삼각 리스트")
    var depreciationSchedule:List<RentalFinancialLeaseScheduleInfo>? = mutableListOf()
)
