package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.lease.utils.RentalUtil.AccountName
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.PostingUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class LeasePostingBatchService(
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val companyService: CompanyService,
    private val documentService: DocumentService,
    private val documentMasterService: DocumentMasterService,
    private val leaseFindService: LeaseFindService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 1. 주문/설치 > (운용리스) 제품출고
     */
    fun postingProductShipped(
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
        // [1.2] 주문상태: 확정배정, 서비스플로우: SERVICE_SCHEDULED  - 설치 제품 수령시(출고시, 1/10)
        //      조건
        //      - ServiceFlowStatus.SERVICE_SCHEDULED
        //      - ServiceFlowType.INSTALL
        //      in
        //      - 장부가액
        // 전기일: 출고일

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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 제품출고 조회
        val list = leaseFindService.findProductShippedTarget(
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
                it.serviceFlow.serviceFlowId,
                it.serviceFlow.serviceType,
                it.serviceFlow.orderItemId
            )
            val documentDate = it.serviceFlow.updateTime?.toLocalDate()!!
            val amount = NumberUtil.setScale(it.inventoryValue.stockAvgUnitPrice)

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
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    amount,
                    listOf(
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.SERIAL_NUMBER,
                        DocumentAttributeType.INSTALL_ID,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.TECHNICIAN_ID,
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
                reference = it.serviceFlow.serviceFlowId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.serviceFlow.serviceFlowId!!),
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
     * 1. 주문/설치 > (운용리스) 설치완료-렌탈자산 인식
     */
    fun postingInstallation(
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
        // [1.3] 주문상태: 설치완료  (설치완료일이 매출확정일)
        //      조건
        //      - OrderItemStatus.INSTALL_COMPLETED
        //      in
        //      - 장부가액
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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 자산등록 정보 조회
        val list = leaseFindService.findInstallationInfo(
            fromTime,
            toTime,
            leaseType
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
            val amount = it.inventoryValue.stockAvgUnitPrice

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
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    amount,
                    listOf(
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.REFERRAL_CODE,
                        DocumentAttributeType.CHARGE_ID,
                        DocumentAttributeType.INVOICE_ID,
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.WAREHOUSE_ID
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
                reference = it.contract.orderItemId,
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
     * 2. 청구/수납 > (운용리스) 청구
     */
    fun postingBilling(
        baseYearMonth: LocalDate,
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
        // [2.1] 청구
        //      조건
        //      - 매출확정월부터 매월 말일 기표
        //      in
        //      - 월 렌탈료(요금, 재화, 서비스): rental_distribution_rule.dist_price_m01, dist_price_s01
        //      - 일할계산 일수(설치일 or 전체)
        //      - 판매세 정보
        // 전기일: 월말

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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 청구 대상 조회
        val list = leaseFindService.findRentalBillingTarget(
            baseYearMonth,
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
                it.charge.chargeId
            )
            // 운용리스: 사용월의 마지막일 / 금융리스: 사용월의 다음달 첫일
            val documentDate = if (leaseType == LeaseType.OPERATING_LEASE) {
                RentalUtil.getLastDate(
                    it.charge.targetMonth!!
                )
            } else {
                RentalUtil.getNextFirstDate(
                    it.charge.targetMonth!!
                )
            }
            // todo: 청구 금액 수정 필요
            val startDate = it.contract.startDate!!
            val endDate = it.contract.endDate
            val dailyRatio = RentalUtil.getDailyRatio(
                baseYearMonth,
                startDate,
                endDate
            )
            val rentalFee = NumberUtil.multiply(
                it.rentalDistributionRule.distributionPrice.m01,
                dailyRatio
            )
            val serviceFee = NumberUtil.multiply(
                it.rentalDistributionRule.distributionPrice.s01!!,
                dailyRatio
            )
            val total = NumberUtil.plus(
                rentalFee,
                serviceFee
            )
            if (total.stripTrailingZeros() == BigDecimal.ZERO) {
                return@forEach
            }
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
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        charge = it.charge,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    when (docTemplateItem.accountCode) {
                        /**
                         * 운용리스
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_RENTAL
                        ) -> total
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.SALES_GOODS_O_LEASE
                        ) -> rentalFee
                        /**
                         * 금융리스
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_MONTHLY
                        ) -> total
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_INSTALLMENT
                        ) -> rentalFee
                        /**
                         * 공통
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ADVANCES_FROM_CUSTOMERS
                        ) -> serviceFee
                        else -> BigDecimal(0)
                    },
                    listOf(
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.REFERRAL_CODE,
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
                reference = it.charge.chargeId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.charge.chargeId),
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
     * 2. 청구/수납 > (운용리스) 수납
     */
    fun postingPayment(
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
        // [2.3] 결제상태: 결제완료, 수납(카드 수납시)
        //      조건
        //      - 결제상태=결제완료(수납)
        //      in
        //      - 청구금액

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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // todo: 청구 데이터로 임시 처리
        // 수납 대상 조회
        val list = leaseFindService.findPaymentTarget(
            fromTime,
            toTime,
            listOf(leaseType),
            test = true
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
                it.receipt.receiptId
            )
            val documentDate = it.receipt.receiptTime!!.toLocalDate()
            // todo: 수납 금액 수정 필요
            val baseYearMonth = fromTime.toLocalDate()
            val startDate = it.contract.startDate!!
            val endDate = it.contract.endDate
            val dailyRatio = RentalUtil.getDailyRatio(
                baseYearMonth,
                startDate,
                endDate
            )
            val stateRate = BigDecimal("0.0625")
            val countyRate = BigDecimal("0")
            val cityRate = BigDecimal("0.005")
            val specialRate = BigDecimal("0.015")
            val totalTaxRate = BigDecimal.ONE
                .plus(stateRate)
                .plus(countyRate)
                .plus(cityRate)
                .plus(specialRate)
            var rentalPrice = it.rentalPricingMaster.price!!
                .multiply(dailyRatio)
            val stateSalesTax = NumberUtil.multiply(rentalPrice, stateRate)
            val countySalesTax = NumberUtil.multiply(rentalPrice, countyRate)
            val citySalesTax = NumberUtil.multiply(rentalPrice, cityRate)
            var specialSalesTax = NumberUtil.multiply(rentalPrice, specialRate)
            val total = NumberUtil.multiply(
                rentalPrice,
                totalTaxRate
            )
            val sum = NumberUtil.setScale(
                rentalPrice
                    .plus(stateSalesTax)
                    .plus(countySalesTax)
                    .plus(citySalesTax)
                    .plus(specialSalesTax)
            )
            rentalPrice = NumberUtil.setScale(rentalPrice)
            if (total.stripTrailingZeros() == BigDecimal.ZERO) {
                return@forEach
            }
            // 단수차이 special에 적용
            if (total != sum) {
                specialSalesTax = specialSalesTax.plus(
                    total.minus(sum)
                )
            }
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
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        charge = it.charge,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    when (docTemplateItem.accountCode) {
                        /**
                         * 운용리스
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_RENTAL
                        ) -> rentalPrice
                        /**
                         * 금융리스
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_MONTHLY
                        ) -> rentalPrice
                        /**
                         * 공통
                         */
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.OTHER_RECEIVABLES_CREDIT_CARD
                        ) -> total
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.DEPOSITS_SALES_TAX_STATE
                        ) -> stateSalesTax
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.DEPOSITS_SALES_TAX_COUNTY
                        ) -> countySalesTax
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.DEPOSITS_SALES_TAX_CITY
                        ) -> citySalesTax
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.DEPOSITS_SALES_TAX_SPECIAL
                        ) -> specialSalesTax
                        else -> BigDecimal(0)
                    },
                    listOf(
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.PURCHASE_ORDER,
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.REFERRAL_CODE,
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
                reference = it.receipt.receiptId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.receipt.receiptId!!),
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
     * 2. 청구/수납 > (운용리스) 입금
     */
    fun postingDeposit(
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
        // [2.3.1] 결제상태: 입금완료, 입금(카드사 입금시)
        //      조건
        //      - 결제상태=입금완료(입금)
        //      in
        //      - 청구금액
        //      - 카드수수료 %

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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // todo: 청구 데이터로 임시 처리
        // 입금 대상 조회
        val list = leaseFindService.findDepositTarget(
            fromTime,
            toTime,
            listOf(leaseType),
            test = true
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
                it.deposit.depositId
            )
            val documentDate = it.deposit.depositDate!!
            // todo: 입금 금액 수정 필요
            val baseYearMonth = fromTime.toLocalDate()
            val startDate = it.contract.startDate!!
            val endDate = it.contract.endDate
            val dailyRatio = RentalUtil.getDailyRatio(
                baseYearMonth,
                startDate,
                endDate
            )
            val taxRate = BigDecimal("1.0825")
            val feeRate = BigDecimal("0.025")
            val tempTotal =
                it.rentalPricingMaster.price!!
                    .multiply(dailyRatio)
                    .multiply(taxRate)
            val fee = NumberUtil.multiply(
                tempTotal,
                feeRate
            )
            val total = NumberUtil.setScale(tempTotal)
            val cashAmount = NumberUtil.minus(
                total,
                fee
            )
            if (total.stripTrailingZeros() == BigDecimal.ZERO) {
                return@forEach
            }
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
                        installation = it.installation,
                        material = it.material,
                        channel = it.channel,
                        charge = it.charge,
                        rentalCodeMaster = it.rentalCodeMaster
                    ),
                    when (docTemplateItem.accountCode) {
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.CASH_REGULAR_DEPOSITS
                        ) -> cashAmount
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.SERVICE_FEES_CREDIT_CARD_FEES
                        ) -> fee
                        RentalUtil.getAccountCode(
                            companyCode,
                            AccountName.OTHER_RECEIVABLES_CREDIT_CARD
                        ) -> total
                        else -> BigDecimal(0)
                    },
                    listOf(
                        DocumentAttributeType.BRANCH_ID,
                        DocumentAttributeType.WAREHOUSE_ID,
                        DocumentAttributeType.CHANNEL_DETAIL,
                        DocumentAttributeType.REFERRAL_CODE,
                        DocumentAttributeType.INVOICE_ID,
                        DocumentAttributeType.PAYOUT_ID,
                        DocumentAttributeType.VENDOR_INVOICE_ID,
                        DocumentAttributeType.PURCHASE_ORDER
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
                reference = it.deposit.depositId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.deposit.depositId!!),
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
     * 3. 서비스 매출 (필터 제공) > [운용리스:서비스매출] 필터배송
     */
    fun postingFilterShipped(
        baseYearMonth: LocalDate,
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
        // [3.1] 필터 제공 (12개월 후 제공)
        //      조건
        //      - 12개월 후(처음 청구월에서 1년 뒤)
        //      in
        //      - 선수금(서비스) 합계(처음 값 + 1년(12개월))

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
        val docTemplateCode = documentTemplateCode
        val docTemplate = documentTemplateServiceable.findDocTemplate(companyCode, docTemplateCode)
        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)

        // 필터배송 대상 조회
        val list = leaseFindService.findFilterTarget(
            baseYearMonth,
            leaseType
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
            val documentDate = baseYearMonth
            // 금액 계산
            val contractStartDate = it.contract.startDate!!
            val contractEndDate = it.contract.endDate
            val priceData = it.price
            // 최초일 경우 (첫 서비스 금액) + (서비스 금액 * 11)
            // 중간일 경우 서비스 금액 * 12
            // 마지막은 11 + (마지막 서비스 금액)
            val amount = if (baseYearMonth.year == contractStartDate.year + 1) {
                NumberUtil.plus(
                    priceData.servicePrice.multiply(
                        NumberUtil.getDailyRatio(
                            contractStartDate
                        )
                    ), // 첫달 일할계산
                    priceData.servicePrice.multiply(
                        BigDecimal(11)
                    )
                )
            } else if (
                contractEndDate != null
                && baseYearMonth.year == contractEndDate.year
            ) {
                NumberUtil.plus(
                    priceData.servicePrice.multiply(
                        BigDecimal.ONE.minus(
                            NumberUtil.getDailyRatio(
                                contractStartDate
                            )
                        )
                    ), // 마지막달 일할계산
                    priceData.servicePrice.multiply(
                        BigDecimal(11)
                    )
                )
            } else {
                NumberUtil.multiply(
                    priceData.servicePrice,
                    BigDecimal(12)
                )
            }

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
                reference = it.installation.serialNumber,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.installation.serialNumber!!),
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