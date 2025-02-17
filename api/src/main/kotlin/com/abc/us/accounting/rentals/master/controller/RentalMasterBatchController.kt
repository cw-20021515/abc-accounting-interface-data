package com.abc.us.accounting.rentals.master.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.rentals.master.service.RentalDistributionRuleService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/accounting/v1/rentals/master/batch/processing")
class RentalMasterBatchController @Autowired constructor(
    private val distributionRuleService: RentalDistributionRuleService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    //------------------------------------------------------------------------------------------------------------------
    //                  Monthly Batch API
    //------------------------------------------------------------------------------------------------------------------
    // TODO : hschoi --> ERROR: duplicate key value violates unique constraint "rental_distribution_rule_pkey" 발생중
    @PostMapping("/distribution-rule")
    fun ruleGenerate(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam("orderItemId", required = false) orderItemId:String? = null,
        @RequestParam(required = false) test: Boolean = false,
    ) {
        logger.info { "RENTAL-MASTER-PROCESSING[distribution rule 생성}] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
        val tzCode = TimeZoneCode.fromCode(timezone)
        distributionRuleService.generate(tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),)
    }

}