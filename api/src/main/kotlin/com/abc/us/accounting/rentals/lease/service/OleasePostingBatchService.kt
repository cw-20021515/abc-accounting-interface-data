package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.*
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.supports.utils.PostingUtil
import com.abc.us.accounting.supports.utils.Hashs
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class OleasePostingBatchService(
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val companyService: CompanyService,
    private val documentService: DocumentService,
    private val documentMasterService: DocumentMasterService,
    private val oleaseBatchService: OleaseBatchService,
    private val leasePostingBatchService: LeasePostingBatchService,
    private val leaseFindService: LeaseFindService,
    private val oleaseFindService: OleaseFindService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 1. 주문/설치 > (운용리스) 설치완료-재고가액 확정
     */
    fun postingCOOR060(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        companyCode: CompanyCode,
        companyCodes: List<CompanyCode> = listOf(),
        docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        orderIds: List<String> = listOf(),
        orderItemIds: List<String> = listOf(),
        customerIds: List<String> = listOf(),
        materialIds: List<String> = listOf(),
        serviceFlowIds: List<String> = listOf(),
        contractIds: List<String> = listOf(),
        bisSystems: List<BizSystemType> = listOf(),
        bizTxIds: List<String> = listOf(),
        bizProcesses: List<BizProcessType> = listOf(),
        bizEvents: List<BizEventType> = listOf(),
        accountingEvents: List<String> = listOf()
    ) {
        // [1.3.2] 주문상태: 설치완료 후 상품 재고가액 확정시 (월말결산 후) => 미신사는 월이동평균(미국법인 기준)으로 가격차이 기표 없음
        //      조건
        //      - OrderItemStatus.INSTALL_COMPLETED
        //      in
        //      - ?
        // 전기일: 설치일

        // context 설정
        val context = RentalUtil.getContext(
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

        // 전표 탬플릿 정보 조회
        val docTemplateCode = DocumentTemplateCode.OLEASE_PRICE_DIFFERENCE
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 자산등록 정보 조회
        val list = leaseFindService.findInstallationInfo(
            fromTime,
            toTime,
            LeaseType.OPERATING_LEASE
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                contract = it.contract
            )
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val docHash = Hashs.hash(
                docTemplateCode,
                it.installation.serialNumber
            )
            val documentDate = it.installation.installationTime?.toLocalDate()!!
            val amount = BigDecimal(0)

            val docItemRequests = docTemplateItems.map { docTemplateItem ->
                RentalUtil.toDocumentItemRequest(
                    companyService,
                    documentMasterService,
                    context,
                    companyCode,
                    docTemplateItem,
                    RentalUtil.getAttributeMap(
                        docTemplateItem = docTemplateItem,
                        salesType = SalesType.OPERATING_LEASE,
                        leaseType = LeaseType.OPERATING_LEASE,
                        companyCode = companyCode,
                        contract = it.contract,
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    amount
                )
            }.toMutableList()
            val currency = companyService.getCompany(companyCode).currency
            val request = CreateDocumentRequest(
                docType = docTemplate.documentType,
                docHash = docHash,
                documentDate = documentDate,
                postingDate = documentDate,
                companyCode = companyCode,
                txCurrency = currency.name,
                reference = it.contract.contractId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.installation.installId!!),
                docItems = docItemRequests
            )
            requests.add(request)
        }
        val res = PostingUtil.posting(
            context,
            requests,
            documentService
        )
        logger.debug("{}", res)
    }

    /**
     * 2. 청구/수납 > [운용리스:상각] 렌탈자산 감가상각
     */
    fun postingCORA020(
        baseYearMonth: LocalDate,
        companyCode: CompanyCode,
        companyCodes: List<CompanyCode> = listOf(),
        docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        orderIds: List<String> = listOf(),
        orderItemIds: List<String> = listOf(),
        customerIds: List<String> = listOf(),
        materialIds: List<String> = listOf(),
        serviceFlowIds: List<String> = listOf(),
        contractIds: List<String> = listOf(),
        bisSystems: List<BizSystemType> = listOf(),
        bizTxIds: List<String> = listOf(),
        bizProcesses: List<BizProcessType> = listOf(),
        bizEvents: List<BizEventType> = listOf(),
        accountingEvents: List<String> = listOf()
    ) {
        // [2.1.1] 렌탈자산의 상각
        //      조건
        //      - 매월 말일
        //      in
        //      - 감가상각비: rental_asset_depreciation_schedule.depreciation_expense

        // context 설정
        val context = RentalUtil.getContext(
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

        // 전표 탬플릿 정보 조회
        val docTemplateCode = DocumentTemplateCode.OLEASE_RENTAL_ASSET_DEPRECIATION
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 감가상각 대상 조회
        val baseDate = RentalUtil.getLastDate(baseYearMonth)
        val list = oleaseFindService.findDepreciationTarget(
            baseDate
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                contract = it.contract
            )
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val docHash = Hashs.hash(
                docTemplateCode,
                it.installation.serialNumber,
                baseYearMonth
            )
            val documentDate = baseDate
            val amount = it.schedule.depreciationExpense

            val docItemRequests = docTemplateItems.map { docTemplateItem ->
                RentalUtil.toDocumentItemRequest(
                    companyService,
                    documentMasterService,
                    context,
                    companyCode,
                    docTemplateItem,
                    RentalUtil.getAttributeMap(
                        docTemplateItem = docTemplateItem,
                        salesType = SalesType.OPERATING_LEASE,
                        leaseType = LeaseType.OPERATING_LEASE,
                        companyCode = companyCode,
                        contract = it.contract,
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    amount,
                    listOf(
                        DocumentAttributeType.INVOICE_ID,
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.CHARGE_ID,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.REFERRAL_CODE
                    )
                )
            }.toMutableList()
            val currency = companyService.getCompany(companyCode).currency
            val request = CreateDocumentRequest(
                docType = docTemplate.documentType,
                docHash = docHash,
                documentDate = documentDate,
                postingDate = documentDate,
                companyCode = companyCode,
                txCurrency = currency.name,
                reference = it.schedule.id.toString(),
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.schedule.id.toString()),
                docItems = docItemRequests
            )
            requests.add(request)
        }
        val res = PostingUtil.posting(
            context,
            requests,
            documentService
        )
        logger.debug("{}", res)
    }

    /**
     * 입력한 시간 범위로 전표 test
     */
    fun test(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        companyCode: CompanyCode,
        companyCodes: List<CompanyCode> = listOf(),
        docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        orderIds: List<String> = listOf(),
        orderItemIds: List<String> = listOf(),
        customerIds: List<String> = listOf(),
        materialIds: List<String> = listOf(),
        serviceFlowIds: List<String> = listOf(),
        contractIds: List<String> = listOf(),
        bisSystems: List<BizSystemType> = listOf(),
        bizTxIds: List<String> = listOf(),
        bizProcesses: List<BizProcessType> = listOf(),
        bizEvents: List<BizEventType> = listOf(),
        accountingEvents: List<String> = listOf()
    ) {
        val baseYearMonth = fromTime.toLocalDate()

        oleaseBatchService.registrationBatch(
            fromTime,
            toTime,
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
        oleaseBatchService.depreciationBatch(
            baseYearMonth,
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
        leasePostingBatchService.postingProductShipped(
            fromTime,
            toTime,
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
        leasePostingBatchService.postingInstallation(
            fromTime,
            toTime,
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
        leasePostingBatchService.postingBilling(
            baseYearMonth,
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
        postingCORA020(
            baseYearMonth,
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
        leasePostingBatchService.postingPayment(
            fromTime,
            toTime,
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
        leasePostingBatchService.postingDeposit(
            fromTime,
            toTime,
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
        leasePostingBatchService.postingFilterShipped(
            baseYearMonth,
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