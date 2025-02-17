package com.abc.us.accounting.rentals.lease.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.service.LeasePostingBatchService
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.lease.service.OleasePostingBatchService
import com.abc.us.accounting.rentals.lease.service.v2.LeasePostingBatchServiceV2
import com.abc.us.accounting.rentals.lease.service.v2.OleasePostingBatchServiceV2
import com.abc.us.accounting.supports.visitor.YearMonthVisitor
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * 배치 호출 순서
 *
 * 1. 자산등록 배치(registrationBatch)
 * 2. 감가상각 배치(depreciationBatch)
 * 3. 전표 배치
 * - (운용리스) 제품출고
 * - (운용리스) 설치완료-렌탈자산 인식
 * - (운용리스) 설치완료-재고가액 확정
 * - (운용리스) 청구
 * - (운용리스) 청구 취소
 * - [운용리스:상각] 렌탈자산 감가상각
 * - (운용리스) 수납
 * - (운용리스) 입금
 * - [운용리스:서비스매출] 필터배송
 */

@RestController
@RequestMapping("/accounting/v1/rentals/operating-lease/batch/posting")
class OleasePostingBatchController(
    private val leasePostingBatchService: LeasePostingBatchService,
    private val oleasePostingBatchService: OleasePostingBatchService,
    private val leasePostingBatchServiceV2: LeasePostingBatchServiceV2,
    private val oleasePostingBatchServiceV2: OleasePostingBatchServiceV2,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun logging(templateCode: DocumentTemplateCode,
                startDateTime: LocalDateTime,
                endDateTime: LocalDateTime,
                companyCode: CompanyCode) {

        logger.info { "O.LEASE-POSTING[${templateCode.koreanText}] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
    }
    //------------------------------------------------------------------------------------------------------------------
    //                  Daily Batch API
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/daily")
    fun postingDaily(@RequestParam("startYearMonth") startYearMonth: YearMonth,
                     @RequestParam("endYearMonth") endYearMonth: YearMonth,
                     @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                     @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200) {

        YearMonthVisitor(startYearMonth,endYearMonth).visit{startLocalDateTime, endLocalDateTime ->
            postingCOLO010(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCORA010(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            //postingCOOR004(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCOCP030(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCOCP040(startLocalDateTime,endLocalDateTime,timezone,companyCode)
        }
    }
    /**
     * 운용리스 > 전표 배치 > (운용리스) 제품출고
     */
    @PostMapping("/COLO010")
    fun postingCOLO010(
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

        logging(DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingProductShipped(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingProductShipped(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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

    /**
     * 운용리스 > 전표 배치 > (운용리스) 설치완료-렌탈자산 인식
     */
    @PostMapping("/CORA010")
    fun postingCORA010(
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
        logging(DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingInstallation(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingInstallation(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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

    /**
     * 운용리스 > 전표 배치 > (운용리스) 설치완료-재고가액 확정
     */
    @PostMapping("/COOR060")
    fun postingCOOR060(
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
        logging(DocumentTemplateCode.OLEASE_PRICE_DIFFERENCE,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            oleasePostingBatchService.postingCOOR060(
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
            oleasePostingBatchServiceV2.postingCOOR060(
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
    /**
     * 운용리스 > 전표 배치 > (운용리스) 수납
     */
    @PostMapping("/COCP030")
    fun postingCOCP030(
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
        logging(DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingPayment(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingPayment(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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

    /**
     * 운용리스 > 전표 배치 > (운용리스) 입금
     */
    @PostMapping("/COCP040")
    fun postingCOCP040(
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
        logging(DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingDeposit(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingDeposit(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
    @PostMapping("/monthly")
    fun postingMonthly(@RequestParam("startYearMonth") startYearMonth: YearMonth,
                       @RequestParam("endYearMonth") endYearMonth: YearMonth,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200) {

        YearMonthVisitor(startYearMonth,endYearMonth).visit{ startLocalDateTime, endLocalDateTime ->
            postingCOCP010(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCOCP020(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCORA020(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCOSS001(startLocalDateTime,endLocalDateTime,timezone,companyCode)
        }
    }

    /**
     * 운용리스 > 전표 배치 > (운용리스) 청구
     */
    @PostMapping("/COCP010")
    fun postingCOCP010(
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
        logging(DocumentTemplateCode.OLEASE_PAYMENT_BILLING,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingBilling(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_BILLING,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingBilling(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_PAYMENT_BILLING,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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

    /**
     * 운용리스 > 전표 배치 > (운용리스) 청구 취소
     */
    @PostMapping("/COCP020")
    fun postingCOCP020(
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
    ) {
        logging(DocumentTemplateCode.OLEASE_PAYMENT_BILLING_CANCELLED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        leasePostingBatchServiceV2.postingBilling(
            tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
            companyCode,
            DocumentTemplateCode.OLEASE_PAYMENT_BILLING_CANCELLED,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE,
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
            accountingEvents,
            true
        )
    }

    /**
     * 운용리스 > 전표 배치 > [운용리스:상각] 렌탈자산 감가상각
     */
    @PostMapping("/CORA020")
    fun postingCORA020(
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
        logging(DocumentTemplateCode.OLEASE_RENTAL_ASSET_DEPRECIATION,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            oleasePostingBatchService.postingCORA020(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
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
            oleasePostingBatchServiceV2.postingCORA020(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
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

    /**
     * 운용리스 > 전표 배치 > [운용리스:서비스매출] 필터배송
     */
    @PostMapping("/COSS001")
    fun postingCOSS001(
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
        logging(DocumentTemplateCode.OLEASE_FILTER_SHIPPED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingFilterShipped(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_FILTER_SHIPPED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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
            leasePostingBatchServiceV2.postingFilterShipped(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.OLEASE_FILTER_SHIPPED,
                LeaseType.OPERATING_LEASE,
                SalesType.OPERATING_LEASE,
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