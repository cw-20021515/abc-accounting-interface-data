package com.abc.us.accounting.rentals.lease.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.service.FleaseBatchService
import com.abc.us.accounting.rentals.lease.service.v2.FleaseBatchServiceV2
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * 1. 제품 출고      : bookingConfirmedBatch
 * 2. 설치 완료(매출-재화, 매출-원가, 매출-재고가액 확정) : installBatch(GOODS, COGS, INVENTORY)
 * 3. 상각        : depreciationBatch
 * 4. 배치(월말)-청구, 수납, 입금 : postingBatch(BILLING, PAYMENT, DEPOSIT)
 * 5. 필터 교체    : filterShippedBatch

 */
@RestController
@Tag(name = "렌탈 금융상각 스케줄(배치) API")
@RequestMapping("/accounting/v1/rentals/financial-lease/batch/processing")
class FleaseBatchController(
    private val fleaseBatchService: FleaseBatchService,
    private val fleaseBatchServiceV2: FleaseBatchServiceV2
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun logging(action: String,
                startDateTime: LocalDateTime,
                endDateTime: LocalDateTime,
                companyCode: CompanyCode) {

        logger.info { "F.LEASE-PROCESSING[${action}] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                  Daily Batch API
    //------------------------------------------------------------------------------------------------------------------

    /**
     * 금융리스 > 자산등록 배치
     */
    @PostMapping("/registration")
    fun registrationBatch(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam("version", required = false, defaultValue = "v1") version: String = "v1",
        @RequestParam("companyCodes", required = false) companyCodes: List<CompanyCode> = listOf(),
        @RequestParam("docTemplateCodes", required = false) docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        @RequestParam("orderIds", required = false) orderIds: List<String> = listOf(),
        @RequestParam("orderItemIds", required = false) orderItemIds: List<String> = listOf(),
        @RequestParam("customerIds", required = false) customerIds: List<String> = listOf(),
        @RequestParam("materialIds", required = false) materialIds: List<String> = listOf(),
        @RequestParam("serviceFlowIds", required = false) serviceFlowIds: List<String> = listOf(),
        @RequestParam("contractIds", required = false) contractIds: List<String> = listOf(),
        @RequestParam("bisSystems", required = false) bisSystems: List<BizSystemType> = listOf(),
        @RequestParam("bizTxIds", required = false) bizTxIds: List<String> = listOf(),
        @RequestParam("bizProcesses", required = false) bizProcesses: List<BizProcessType> = listOf(),
        @RequestParam("bizEvents", required = false) bizEvents: List<BizEventType> = listOf(),
        @RequestParam("accountingEvents", required = false) accountingEvents: List<String> = listOf(),
        @RequestParam(required = false) test: Boolean = false
    ) {
        logging("금융리스:자산등록",startDateTime,endDateTime,companyCode)

        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            fleaseBatchService.registrationBatch(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCodes,
                docTemplateCodes,
                orderIds,
                orderItemIds,
                customerIds,
                materialIds,
                serviceFlowIds,
                contractIds,
                bisSystems,
                bizTxIds,
                bizProcesses,
                bizEvents,
                accountingEvents
            )
        } else {
            fleaseBatchServiceV2.registrationBatch(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCodes,
                docTemplateCodes,
                orderIds,
                orderItemIds,
                customerIds,
                materialIds,
                serviceFlowIds,
                contractIds,
                bisSystems,
                bizTxIds,
                bizProcesses,
                bizEvents,
                accountingEvents
            )
        }

    }

    //------------------------------------------------------------------------------------------------------------------
    //                  Monthly Batch API
    //------------------------------------------------------------------------------------------------------------------
    /**
     * 금융리스 > 상각 배치
     */
    @PostMapping("/depreciation")
    fun depreciationBatch(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam("version", required = false, defaultValue = "v1") version: String = "v1",
        @RequestParam("companyCodes", required = false) companyCodes: List<CompanyCode> = listOf(),
        @RequestParam("docTemplateCodes", required = false) docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        @RequestParam("orderIds", required = false) orderIds: List<String> = listOf(),
        @RequestParam("orderItemIds", required = false) orderItemIds: List<String> = listOf(),
        @RequestParam("customerIds", required = false) customerIds: List<String> = listOf(),
        @RequestParam("materialIds", required = false) materialIds: List<String> = listOf(),
        @RequestParam("serviceFlowIds", required = false) serviceFlowIds: List<String> = listOf(),
        @RequestParam("contractIds", required = false) contractIds: List<String> = listOf(),
        @RequestParam("bisSystems", required = false) bisSystems: List<BizSystemType> = listOf(),
        @RequestParam("bizTxIds", required = false) bizTxIds: List<String> = listOf(),
        @RequestParam("bizProcesses", required = false) bizProcesses: List<BizProcessType> = listOf(),
        @RequestParam("bizEvents", required = false) bizEvents: List<BizEventType> = listOf(),
        @RequestParam("accountingEvents", required = false) accountingEvents: List<String> = listOf(),
    ) {
        logging("상각",startDateTime,endDateTime,companyCode)
        if(version == "v1") {
            fleaseBatchService.depreciationBatch(
                TimeZoneCode.fromCode(timezone).toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCodes,
                docTemplateCodes,
                orderIds,
                orderItemIds,
                customerIds,
                materialIds,
                serviceFlowIds,
                contractIds,
                bisSystems,
                bizTxIds,
                bizProcesses,
                bizEvents,
                accountingEvents
            )
        } else {
            fleaseBatchServiceV2.depreciationBatch(
                TimeZoneCode.fromCode(timezone).toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCodes,
                docTemplateCodes,
                orderIds,
                orderItemIds,
                customerIds,
                materialIds,
                serviceFlowIds,
                contractIds,
                bisSystems,
                bizTxIds,
                bizProcesses,
                bizEvents,
                accountingEvents
            )
        }
    }
}
