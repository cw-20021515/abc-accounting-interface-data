package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.documents.model.DepositsData
import com.abc.us.accounting.documents.model.DepositsRequest
import com.abc.us.accounting.documents.service.DepositsService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounting/v1/receipts/deposits")
class DepositsController(
    private val depositsService: DepositsService
) {

    @Operation(
        summary = "입금현황 조회",
        description = "입금현황 조회"
    )
    @GetMapping("")
    fun deposits(
        @ModelAttribute req: DepositsRequest
    ): ResponseEntity<ApiPageResponse<List<DepositsData>>> {
        val data = depositsService.findDeposits(req)
        return ResponseEntity.ok(ApiPageResponse(ResHeader(), data))
    }

    @Operation(
        summary = "입금현황 엑셀 다운로드",
        description = "입금현황 엑셀 다운로드"
    )
    @GetMapping("/rawdata/download")
    fun depositsExcelDownload(
        @ModelAttribute req: DepositsRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        depositsService.depositsExcelDownload(req, response)
        return ResponseEntity.ok(ApiResponse(ResHeader(), null))
    }
}