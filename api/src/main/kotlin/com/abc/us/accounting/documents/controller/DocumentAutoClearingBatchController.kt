package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.DocumentAutoClearingService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/accounting/v1/document/batch")
class DocumentAutoClearingBatchController(
    private val autoClearingService : DocumentAutoClearingService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/auto-clearing")
    fun autoClearing(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<List<DocumentResult>>  {

        logger.info { "DOCUMENT[auto-clearing] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
        val tzCode = TimeZoneCode.fromCode(timezone)

        val context = DocumentServiceContext.SAVE_DEBUG
        val results = autoClearingService.processAutoClearing(
            context,
            companyCode,
            tzCode.convertTime(startDateTime, TimeZoneCode.UTC)
        )
        return ResponseEntity.ok(results)
    }
}