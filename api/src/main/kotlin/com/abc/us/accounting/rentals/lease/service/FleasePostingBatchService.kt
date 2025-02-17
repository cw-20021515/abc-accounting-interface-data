package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.lease.utils.RentalUtil.AccountName
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.PostingUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class FleasePostingBatchService(
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val companyService: CompanyService,
    private val documentService: DocumentService,
    private val documentMasterService: DocumentMasterService,
    private val fleaseFindService: FleaseFindService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 설치완료 (매출인식 - 재화매출) : 금융리스
     */
    fun postingInstallGoods(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        companyCode: CompanyCode,
        documentTemplateCode: DocumentTemplateCode,
        leaseType: LeaseType,
        salesType: SalesType,
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
        // [1.3.1] 주문상태: 설치완료, 서비스플로우: SERVICE_COMPLETED
        //      조건
        //      - ServiceFlowStatus.SERVICE_COMPLETED
        //      - ServiceFlowType.INSTALL
        //      in
        //      - 장부가액
        // 전기일: 출고일
        val context =

            try {// context 설정
                RentalUtil.getContext(
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
            } catch (e: Exception) {
                TODO("Not yet implemented")
            }

        // 전표 탬플릿 정보 조회
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 조회
        val list = fleaseFindService.findInstallGoodsTarget(
            fromTime,
            toTime,
            listOf(leaseType)
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

            //장부금
            val depreciationBookValue = it.history.initialBookValue ?: BigDecimal.ZERO
            //현할차
            val depreciationCurrentDifference = it.history.initialCurrentDifference ?: BigDecimal.ZERO
            //장부금 + 현할차
            val amount = NumberUtil.plus(depreciationBookValue, depreciationCurrentDifference)

            val docItemRequests = docTemplateItems.map { docTemplateItem ->
                RentalUtil.toDocumentItemRequest(
                    companyService,
                    documentMasterService,
                    context,
                    companyCode,
                    docTemplateItem,
                    RentalUtil.getAttributeMap(
                        docTemplateItem = docTemplateItem,
                        salesType = salesType,
                        leaseType = leaseType,
                        companyCode = companyCode,
                        contract = it.contract,
                        material = it.material,
                        channel = it.channel,
                        rentalCodeMaster = it.rentalCodeMaster,
                        installation = it.installation
                    ),
                    when (docTemplateItem.accountCode) {
                        /**
                         * 금융리스
                         */
                        // 금융리스채권-할부발생
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_INSTALLMENT
                        ) -> amount
                        // 상품매출-금융리스
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.SALES_GOODS_F_LEASE
                        ) -> depreciationBookValue
                        // 현할차금-금융리스
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.UNEARNEDINTERESTINCOME_F_LEASE
                        ) -> depreciationCurrentDifference
                        else -> BigDecimal(0)
                    },
                    listOf(
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.REFERRAL_CODE,
                        DocumentAttributeType.CHARGE_ID,
                        DocumentAttributeType.INVOICE_ID
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
                reference = it.history.id,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.history.id!!),
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
     * 2. 금융리스 월마감작업(월 1회)
     */
    fun postingMonthEnd(
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
        val docTemplateCode = DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_INTEREST_INCOME
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 월마감작업 대상 조회
        val baseDate = RentalUtil.getLastDate(baseYearMonth)
        val list = fleaseFindService.findMonthEndTarget(
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
                it.contract.contractId,
                it.schedule.depreciationYearMonth
            )
            val documentDate = LocalDate.parse("${it.schedule.depreciationBillYearMonth}-01")

            // 이자수익
            val amount = it.schedule.depreciationInterestIncome ?: BigDecimal.ZERO

            val docItemRequests = docTemplateItems.map { docTemplateItem ->
                RentalUtil.toDocumentItemRequest(
                    companyService,
                    documentMasterService,
                    context,
                    companyCode,
                    docTemplateItem,
                    RentalUtil.getAttributeMap(
                        docTemplateItem = docTemplateItem,
                        salesType = SalesType.FINANCIAL_LEASE,
                        leaseType = LeaseType.FINANCIAL_LEASE,
                        companyCode = companyCode,
                        contract = it.contract,
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    when (docTemplateItem.accountCode) {
                        /**
                         * 금융리스
                         */
                        // 현할차금-금융리스
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.UNEARNEDINTERESTINCOME_F_LEASE
                        ) -> amount
                        // 이자수익-금융리스
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.INTEREST_INCOME_F_LEASE
                        ) -> amount
                        else -> BigDecimal(0)
                    },
                    listOf(
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.REFERRAL_CODE,
                        DocumentAttributeType.CHARGE_ID,
                        DocumentAttributeType.INVOICE_ID
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
                reference = it.schedule.id,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.schedule.id!!),
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
}