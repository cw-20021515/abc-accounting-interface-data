package com.abc.us.accounting.logistics.controller

import com.abc.us.accounting.logistics.model.request.LogisticsInventoryCostStatusRequest
import com.abc.us.accounting.logistics.model.request.LogisticsInventoryMovementStatusRequest
import com.abc.us.accounting.logistics.model.response.LogisticsInventoryCostStatusData
import com.abc.us.accounting.logistics.model.response.LogisticsInventoryMovementStatusData
import com.abc.us.accounting.logistics.service.LogisticsService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ResHeader
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounting/v1/logistics")
class LogisticsController(
    private val logisticsService: LogisticsService
) {

    @GetMapping("/inventory-cost-status")
    fun getInventoryCostStatus(
        @ModelAttribute req: LogisticsInventoryCostStatusRequest
    ): ResponseEntity<ApiPageResponse<List<LogisticsInventoryCostStatusData>>> {
        val data = logisticsService.getInventoryCostStatus(req)
        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    @GetMapping("/inventory-cost-status/download")
    fun downloadInventoryCostStatus(
        @ModelAttribute req: LogisticsInventoryCostStatusRequest
    ): ResponseEntity<InputStreamResource> {
        val data = logisticsService.downloadInventoryCostStatus(req)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_cost_status.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(data)
    }

    @GetMapping("/inventory-movement-status")
    fun getInventoryMovementStatus(
        @ModelAttribute req: LogisticsInventoryMovementStatusRequest
    ): ResponseEntity<ApiPageResponse<List<LogisticsInventoryMovementStatusData>>> {
        val data = logisticsService.getInventoryMovementStatus(req)
        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    @GetMapping("/inventory-movement-status/download")
    fun downloadInventoryMovementStatus(
        @ModelAttribute req: LogisticsInventoryMovementStatusRequest
    ): ResponseEntity<InputStreamResource> {
        val data = logisticsService.downloadInventoryMovementStatus(req)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_movement_status.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(data)
    }

}