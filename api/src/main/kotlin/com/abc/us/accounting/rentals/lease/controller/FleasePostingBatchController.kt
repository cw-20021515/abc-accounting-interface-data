package com.abc.us.accounting.rentals.lease.controller


import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.service.FleasePostingBatchService
import com.abc.us.accounting.rentals.lease.service.LeasePostingBatchService
import com.abc.us.accounting.rentals.lease.service.v2.FleasePostingBatchServiceV2
import com.abc.us.accounting.rentals.lease.service.v2.LeasePostingBatchServiceV2
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.YearMonth
import com.abc.us.accounting.supports.visitor.YearMonthVisitor

/**
 * 1. 제품 출고      : bookingConfirmedBatch
 * 2. 설치 완료(매출-재화, 매출-원가, 매출-재고가액 확정) : installBatch(GOODS, COGS, INVENTORY)
 * 3. 상각        : depreciationBatch
 * 4. 배치(월말)-청구, 수납, 입금 : postingBatch(BILLING, PAYMENT, DEPOSIT)
 * 5. 필터 교체    : filterShippedBatch

 */
@RestController
@Tag(name = "렌탈 금융상각 스케줄(배치) API")
@RequestMapping("/accounting/v1/rentals/financial-lease/batch/posting")
class FleasePostingBatchController(
    private val leasePostingBatchService: LeasePostingBatchService,
    private val fleasePostingBatchService: FleasePostingBatchService,
    private val leasePostingBatchServiceV2: LeasePostingBatchServiceV2,
    private val fleasePostingBatchServiceV2: FleasePostingBatchServiceV2
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun logging(templateCode: DocumentTemplateCode,
                startDateTime: LocalDateTime,
                endDateTime: LocalDateTime,
                companyCode: CompanyCode) {

        logger.info { "F.LEASE-POSTING[${templateCode.koreanText}] FROM(${startDateTime})-TO(${endDateTime}) : COMPANY(${companyCode})" }
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
            postingCFLO010(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCFOR020(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCFOR030(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCFOR040(startLocalDateTime,endLocalDateTime,timezone,companyCode)
        }
    }

    @Operation(summary = "렌탈 금융 리스 > 1-2. [금융 리스] 제품 출고", description = "서비스플로우: SERVICE_SCHEDULED - CT: 제품수령시(제품 출고시)")
    @PostMapping("/CFLO010")
    fun postingCFLO010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            leasePostingBatchService.postingProductShipped(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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

    @Operation(summary = "렌탈 금융 리스 > 1-3. 제품 출고(매출 인식-재화 매출) 배치")
    @PostMapping("/CFOR020")
    fun postingCFOR020(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_SALES_RECOGNITION,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            fleasePostingBatchService.postingInstallGoods(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_SALES_RECOGNITION,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
            fleasePostingBatchServiceV2.postingInstallGoods(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_SALES_RECOGNITION,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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

    @Operation(summary = "렌탈 금융 리스 > 1-3. 제품 출고(매출 원가 인식) 배치")
    @PostMapping("/CFOR030")
    fun postingCFOR030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_COGS_RECOGNITION,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            leasePostingBatchService.postingInstallation(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_COGS_RECOGNITION,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_COGS_RECOGNITION,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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

    @Operation(summary = "렌탈 금융 리스 > 1-3. 제품 출고(재고가액 확정) 배치")
    @PostMapping("/CFOR040")
    fun postingCFOR040(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_PRICE_DIFFERENCE,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
    }
    //------------------------------------------------------------------------------------------------------------------
    //                  Monthly Batch API
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/monthly")
    fun postingMonthly(@RequestParam("startYearMonth") startYearMonth: YearMonth,
                       @RequestParam("endYearMonth") endYearMonth: YearMonth,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200) {

        YearMonthVisitor(startYearMonth,endYearMonth).visit{startLocalDateTime, endLocalDateTime ->
            postingCFCP020(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCFCP030(startLocalDateTime,endLocalDateTime,timezone,companyCode)
            postingCFFA010(startLocalDateTime,endLocalDateTime,timezone,companyCode)
        }
    }
    @Operation(summary = "렌탈 금융 리스 > 2. 상각 -> 전표 배치(월말)-청구")
    @PostMapping("/CFCP010")
    fun postingCFCP010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_PAYMENT_BILLING, startDateTime, endDateTime, companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            leasePostingBatchService.postingBilling(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_PAYMENT_BILLING,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_PAYMENT_BILLING,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
    @Operation(summary = "렌탈 금융 리스 > 2. 상각 -> 전표 배치(월말)-수납")
    @PostMapping("/CFCP020")
    fun postingCFCP020(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        @RequestParam(required = false) test: Boolean = false,
    ) {
        logging(DocumentTemplateCode.FLEASE_PAYMENT_RECEIVED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            leasePostingBatchService.postingPayment(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_PAYMENT_RECEIVED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_PAYMENT_RECEIVED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
    @Operation(summary = "렌탈 금융 리스 > 2. 상각 -> 전표 배치(월말)-입금")
    @PostMapping("/CFCP030")
    fun postingCFCP030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        @RequestParam(required = false) test: Boolean = false,
    ) {
        logging(DocumentTemplateCode.FLEASE_PAYMENT_DEPOSIT,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if(version == "v1") {
            leasePostingBatchService.postingDeposit(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_PAYMENT_DEPOSIT,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_PAYMENT_DEPOSIT,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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

    @Operation(summary = "렌탈 금융 리스 > 2. 상각 -> 전표 배치(월마감작업(월 1회))")
    @PostMapping("/CFFA010")
    fun postingCFFA010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        @RequestParam(required = false) test: Boolean = false,
    ) {
        logging(DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_INTEREST_INCOME,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            fleasePostingBatchService.postingMonthEnd(
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
            fleasePostingBatchServiceV2.postingMonthEnd(
                tzCode.convertTime(startDateTime, TimeZoneCode.UTC),
                tzCode.convertTime(endDateTime, TimeZoneCode.UTC),
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
    @Operation(summary = "렌탈 금융 리스 > 필터 교체 배치")
    @PostMapping("/CFSS010")
    fun postingCFSS010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode = CompanyCode.N200,
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
        logging(DocumentTemplateCode.FLEASE_FILTER_SHIPPED,startDateTime,endDateTime,companyCode)
        val tzCode = TimeZoneCode.fromCode(timezone)
        if (version == "v1") {
            leasePostingBatchService.postingFilterShipped(
                tzCode.toLocalDate(startDateTime, TimeZoneCode.UTC),
                companyCode,
                DocumentTemplateCode.FLEASE_FILTER_SHIPPED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
                DocumentTemplateCode.FLEASE_FILTER_SHIPPED,
                LeaseType.FINANCIAL_LEASE,
                SalesType.FINANCIAL_LEASE,
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
