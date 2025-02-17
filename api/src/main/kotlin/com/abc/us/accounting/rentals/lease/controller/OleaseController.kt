package com.abc.us.accounting.rentals.lease.controller

import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.rentals.lease.model.RentalAssetHistoryItemData
import com.abc.us.accounting.rentals.lease.model.RentalAssetHistoryRequest
import com.abc.us.accounting.rentals.lease.service.v2.OleaseServiceV2
import com.abc.us.accounting.supports.ExceptionUtil
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounting/v1/rentals/operating-lease")
class OleaseController(
    private val oleaseService: OleaseServiceV2
) {

    /**
     * 운용리스 > 렌탈자산 현황
     */
    @GetMapping("")
    fun getRentalAssetHistory(
        @ModelAttribute req: RentalAssetHistoryRequest
    ): ResponseEntity<ApiPageResponse<List<RentalAssetHistoryItemData>>> {
        val data = oleaseService.getRentalAssetDepreciationHistory(req)
        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    /**
     * 운용리스 > 렌탈자산 현황 엑셀 다운로드
     */
    @GetMapping("/download")
    fun getRentalAssetHistoryExcelDownload(
        @ModelAttribute req: RentalAssetHistoryRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        oleaseService.getRentalAssetDepreciationHistoryExcelDownload(
            req,
            response
        )
        return ResponseEntity.ok(
            ApiResponse(
                ResHeader(),
                null
            )
        )
    }

    /**
     * 운용리스 > 렌탈자산 감가상각 스케줄
     */
    @GetMapping("/depreciation-schedule/{serialNumber}")
    fun getRentalAssetDepreciationSchedule(
        @PathVariable(value = "serialNumber") serialNumber: String
    ): ResponseEntity<out Any> {
        val data = oleaseService.getRentalAssetDepreciationSchedule(serialNumber)
            ?: return ExceptionUtil.notFound404()
        return ResponseEntity.ok(
            ApiResponse(
                ResHeader(),
                data
            )
        )
    }

    /**
     * 운용리스 > 렌탈자산 감가상각 스케줄 엑셀 다운로드
     */
    @GetMapping("/depreciation-schedule/{serialNumber}/download")
    fun getRentalAssetDepreciationScheduleExcelDownload(
        @PathVariable(value = "serialNumber") serialNumber: String,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        oleaseService.getRentalAssetDepreciationScheduleExcelDownload(
            serialNumber,
            response
        )
        return ResponseEntity.ok(
            ApiResponse(
                ResHeader(),
                null
            )
        )
    }
}