package com.abc.us.accounting.rentals.lease.controller

import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseInqySchedule
import com.abc.us.accounting.rentals.lease.model.ResRentalFinancialLeaseInqySchedule
import com.abc.us.accounting.rentals.lease.service.FleaseService
import com.abc.us.accounting.rentals.lease.service.v2.FleaseServiceV2
import com.abc.us.accounting.supports.ExceptionUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "렌탈 금융상각 스케줄 API")
@RequestMapping("/accounting/v1/rentals/financial-lease")
class FleaseController(
    private val fleaseService: FleaseServiceV2,
) {

    /**
     * 렌탈 금융상각 스케줄 현황 리스트
     */
    @Operation(summary = "렌탈 금융상각 스케줄 현황 리스트")
    @GetMapping("")
    fun getFinancialLeaseScheduleList(
        @ModelAttribute financialsLease: ReqRentalFinancialLeaseInqySchedule
    ): ResponseEntity<ApiPageResponse<Page<ResRentalFinancialLeaseInqySchedule>>> {
        val scheduleInfo = fleaseService.getFinancialLeaseScheduleList(financialsLease)
        return ResponseEntity.ok(ApiPageResponse(ResHeader(), scheduleInfo))
    }

    /**
     * 렌탈 금융상각 스케줄 다운로드
     */
    @Operation(summary = "렌탈 금융상각 현황 다운로드")
    @GetMapping("/download")
    fun getFinancialLeaseScheduleInfoDownload(
        @ModelAttribute financialsLease: ReqRentalFinancialLeaseInqySchedule,
        response: HttpServletResponse,
    ) {
        fleaseService.getRentalFinancialLeaseExcelDownload(financialsLease, response)
    }


    /**
     * [화면] 렌탈 금융상각 스케줄 상세조회
     * http://localhost:8080/accounting/v1/rentals/financial-lease/depreciation-schedule/{contractId}
     */
    @Operation(summary = "렌탈 금융상각 스케줄 상세조회")
    @GetMapping("/depreciation-schedule/{contractId}")
    fun getFinancialLeaseScheduleInfo(
        @PathVariable(value = "contractId") contractId: String,
    ): ResponseEntity<out Any> {
        val scheduleInfo = fleaseService.getFinancialLeaseScheduleInfo(contractId)
            ?: return ExceptionUtil.notFound404()
        return ResponseEntity.ok(ApiResponse(ResHeader(), scheduleInfo))
    }

    /**
     * [화면] 렌탈 금융상각 스케줄 다운로드
     */
    @Operation(summary = "렌탈 금융상각 스케줄 다운로드")
    @GetMapping("/depreciation-schedule/{contractId}/download")
    fun getFinancialLeaseScheduleInfoDownload(
        @PathVariable(value = "contractId") contractId: String,
        response: HttpServletResponse,
    ) {
        fleaseService.getRentalFinancialLeaseScheduleExcelDownload(contractId, response)
    }
}
