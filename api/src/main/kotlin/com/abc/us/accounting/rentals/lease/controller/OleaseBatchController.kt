package com.abc.us.accounting.rentals.lease.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.service.OleaseBatchService
import com.abc.us.accounting.rentals.lease.service.OleasePostingBatchService
import com.abc.us.accounting.rentals.lease.service.v2.OleaseBatchServiceV2
import com.abc.us.accounting.rentals.lease.service.v2.OleasePostingBatchServiceV2
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * 배치 호출 순서
 *
 * 1. 자산등록 배치(registrationBatch)
 * 2. 감가상각 배치(depreciationBatch)
 * 3. 전표 배치
 * - (운용리스) 제품출고(COOR002)
 * - (운용리스) 설치완료-렌탈자산 인식(COOR003)
 * - (운용리스) 설치완료-재고가액 확정(COOR004)
 * - (운용리스) 청구(COCP001)
 * - [운용리스:상각] 렌탈자산 감가상각(CODP001)
 * - (운용리스) 수납(COCP002)
 * - (운용리스) 입금(COCP003)
 * - [운용리스:서비스매출] 필터배송(COSS001)
 *
 * 개발중
 * - transferToCollection
 * - disposalBatch
 */

@RestController
@RequestMapping("/accounting/v1/rentals/operating-lease/batch/processing")
class OleaseBatchController(
    private val oleasePostingBatchService: OleasePostingBatchService,
    private val oleaseBatchService: OleaseBatchService,
    private val oleasePostingBatchServiceV2: OleasePostingBatchServiceV2,
    private val oleaseBatchServiceV2: OleaseBatchServiceV2
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun logging(action: String,
                startDateTime: LocalDateTime,
                endDateTime: LocalDateTime,
                companyCode: CompanyCode) {

        logger.info { "O.LEASE-PROCESSING[${action}] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
    }

    @PostMapping("/test")
    fun test(
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
        logger.info { "operating-lease-batch-test FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            oleasePostingBatchService.test(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
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
            oleasePostingBatchServiceV2.test(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
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

    @PostMapping("/test-range")
    fun testRange(
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
        logger.info { "operating-lease-batch-test FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
        val tzCode = TimeZoneCode.fromCode(timezone)
        var now = tzCode.convertTime(startDateTime, TimeZoneCode.UTC)
        val end = tzCode.convertTime(endDateTime, TimeZoneCode.UTC)
        while (now.isBefore(end)) {
            val startTime = now
            val endTime = RentalUtil.getLastDate(startTime)
            if (version == "v1") {
                oleasePostingBatchService.test(
                    startTime,
                    endTime,
                    companyCode,
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
                oleasePostingBatchServiceV2.test(
                    startTime,
                    endTime,
                    companyCode,
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
            now = now.plusMonths(1)
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                  Daily Batch API
    //------------------------------------------------------------------------------------------------------------------
    /**
     * 운용리스 > 자산등록 배치
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
        logging("자산등록",startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            oleaseBatchService.registrationBatch(
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
            oleaseBatchServiceV2.registrationBatch(
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
     * 운용리스 > 자산 상태 변경 배치
     */
    @PostMapping("/change-state")
    fun changeStateBatch(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
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
        logging("자산 상태 변경",startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        oleaseBatchServiceV2.changeStateBatch(
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

    /**
     * 운용리스 > 감가상각 배치
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
        logging("감가상각",startDateTime,endDateTime,companyCode)
        if (version == "v1") {
            oleaseBatchService.depreciationBatch(
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
            oleaseBatchServiceV2.depreciationBatch(
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