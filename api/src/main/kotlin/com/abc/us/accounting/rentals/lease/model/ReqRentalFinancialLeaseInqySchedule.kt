package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.master.domain.type.MaterialCategoryCode
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import java.math.BigDecimal
import java.time.LocalDate

@Schema(name = "요청_금융 리스_렌탈스케쥴_검색조회")
class ReqRentalFinancialLeaseInqySchedule(

    @Schema(description = "페이징", defaultValue = "1")
    var current: Int = 1,

    @Schema(description = "페이지 크기", defaultValue = "50")
    var size: Int = 50,

    @Schema(description = "주문 아이템 ID", example = "98765")
    val orderItemId: String? = null,           // 주문 항목 ID

    @Schema(description = "기준일", example = "2024-01-01")
    val baseDate: LocalDate? = null,           // 기준일

    @Schema(description = "계약일 시작", example = "2024-01-01")
    val contractFromDate: LocalDate? = null,   // 계약일 시작

    @Schema(description = "계약일 종료", example = "2024-12-31")
    val contractToDate: LocalDate? = null,     // 계약일 종료

    @Schema(description = "고객 ID", example = "CUST12345")
    val customerId: String? = null,            // 고객 ID

    @Schema(description = "계약 ID", example = "CONT123456")
    val contractId: String? = null,            // 계약 ID

    @Schema(description = "시리얼 번호", example = "SN12345678")
    val serialNumber: String? = null,          // 시리얼 번호

    @Schema(description = "자재 ID", example = "MAT123")
    val materialId: String? = null,            // 자재 ID

    @Schema(description = "카테고리(자재 분류 코드)", example = "WATER_PURIFIER")
    val materialCategory: MaterialCategoryCode? = null,  // 카테고리(MaterialCategoryCode)
){
    fun transformRes(res: Page<ResRentalFinancialLeaseInqySchedule>): List<List<Any>> {
        return res.content.map { it ->
            listOf(
                it.contractId ?: "",                                    // 계약 ID
                it.contractDate?.toString() ?: "",                      // 계약일
                it.baseDate?.toString() ?: "",                          // 기준일
                it.depreciationCount ?: 0,                              // 청구 회차
                it.initialBookValue ?: 0.0,                             // 최초 장부 금액
                it.depreciationBookValue ?: BigDecimal.ZERO,            // 장부 금액
                it.rentalAmountForGoods ?: BigDecimal.ZERO,             // 렌탈료(재화)
                it.depreciationPresentValue ?: BigDecimal.ZERO,         // 현재 가치(PV)
                it.depreciationCurrentDifference ?: BigDecimal.ZERO,    // 현 할차
                it.depreciationInterestIncome ?: BigDecimal.ZERO,                   // 이자 수익
                it.cumulativeInterestIncome ?: BigDecimal.ZERO,         // 이자 수익 누계액
                it.orderItemId ?: "",                                   // 주문 아이템 ID
                it.customerId ?: "",                                    // 고객 ID
                it.serialNumber ?: "",                                  // 일련 번호
                it.materialId ?: "",                                    // 자재 ID
                it.modelName ?: ""                                      // 모델명
            )
        }
    }

    fun transformHeaderRes(): List<String> {
        return listOf(
            "계약 ID",                      // Contract ID
            "계약일",                       // Contract Date
            "기준일",                       // Base Date
            "청구 회차",                    // Bill Cycle
            "최초 장부 금액",                // Initial Book Value
            "장부 금액",                    // Book Value
            "렌탈료(재화)",                  // Rental Amount for Goods
            "현재 가치(PV)",                // Present Value
            "현 할차",                     // Current Difference
            "이자 수익",                   // Interest Income
            "이자 수익 누계액",              // Cumulative Interest Income
            "주문 아이템 ID",               // Order Item ID
            "고객 ID",                    // Customer ID
            "일련 번호",                   // Serial Number
            "자재 ID",                    // Material ID
            "모델명"                       // Model Name
        )
    }
}