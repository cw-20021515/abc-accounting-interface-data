package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.config.SalesTaxConfig
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.iface.domain.type.oms.IfChargeItemType
import com.abc.us.accounting.rentals.lease.model.v2.RentalBillingTargetV2
import com.abc.us.accounting.rentals.lease.model.v2.RentalDocumentItem
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.lease.utils.RentalUtil.AccountName
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.rentals.onetime.model.SalesTax
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.PostingUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class LeasePostingBatchServiceV2(
    private val salesTaxConfig: SalesTaxConfig,
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val companyService: CompanyService,
    private val documentService: DocumentService,
    private val documentMasterService: DocumentMasterService,
    private val leaseFindServiceV2: LeaseFindServiceV2
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 1. 주문/설치 > 제품출고
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
        val list = leaseFindServiceV2.findProductShippedTarget(
            fromTime,
            toTime,
            listOf(leaseType)
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
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
            val documentDate = it.serviceFlow.updateTime.toLocalDate()
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
                docOrigin = docTemplate.toDocumentOriginRequest(it.serviceFlow.serviceFlowId),
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
     * 1. 주문/설치 > 설치완료-렌탈자산 인식
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
        val list = leaseFindServiceV2.findInstallationInfo(
            fromTime,
            toTime,
            leaseType
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
            )
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val docHash = Hashs.hash(
                docTemplateCode,
                it.serviceFlow.serialNumber
            )
            val documentDate = it.serviceFlow.createTime.toLocalDate()
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
                        orderItem = it.orderItem,
                        serviceFlow = it.serviceFlow,
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
                docOrigin = docTemplate.toDocumentOriginRequest(it.serviceFlow.installId!!),
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
     * 청구/청구 취소
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
        accountingEvents: List<String> = listOf(),
        isCancel: Boolean = false // 청구 취소 여부
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
        val billingList = leaseFindServiceV2.findRentalBillingTarget(
            baseYearMonth,
            listOf(leaseType)
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
            )
        }
        // isCancel 값에 따라 청구/청구 취소 대상 구분
        val list: List<RentalBillingTargetV2>
        if (isCancel) {
            // 연체료(lateFee)가 있는 청구일 경우 지난 청구 취소 후 재청구
            val prevYearMonth = baseYearMonth.minusMonths(1)
            val prevTargetMonth = RentalUtil.getYearMonth(prevYearMonth)
            val cancelChargeIds = billingList.map {
                it.invoice.charges.filter { charge ->
                    val lateFeeItem = charge.chargeItems.find { chargeItem ->
                        chargeItem.chargeItemType == IfChargeItemType.LATE_FEE
                    }
                    // 연체료가 있으면서, 지난달 청구
                    lateFeeItem != null && charge.targetMonth == prevTargetMonth
                }
            }.flatten().map {
                it.chargeId
            }
            // 청구 취소 대상이 없다면 종료
            if (cancelChargeIds.isEmpty()) {
                return
            }
            // 청구 취소 대상 조회(지난달)
            list = leaseFindServiceV2.findRentalBillingTarget(
                prevYearMonth,
                listOf(leaseType),
                cancelChargeIds
            ).filter {
                RentalUtil.checkFilteringRule(
                    context = context,
                    docTemplate = docTemplate,
                    orderItem = it.orderItem
                )
            }
        } else {
            list = billingList
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val chargeDetails = RentalUtil.makeChargeDetails(
                it.invoice,
                it.rentalDistributionRule
            )
            chargeDetails.forEach { chargeDetail ->
                val docHash = Hashs.hash(
                    docTemplateCode,
                    it.invoice.invoiceId,
                    chargeDetail.chargeId
                )
                // 운용리스: 사용월의 마지막일 / 금융리스: 사용월의 다음달 첫일
                val documentDate = if (leaseType == LeaseType.OPERATING_LEASE) {
                    RentalUtil.getLastDate(
                        chargeDetail.targetMonth
                    )
                } else {
                    RentalUtil.getNextFirstDate(
                        chargeDetail.targetMonth
                    )
                }
                /**
                 * todo: AS, 역방향 청구 처리 필요
                 *
                 * AS
                 * - 유상(단순방문): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_AS
                 * - 유상(부품교체): ACCOUNTS_RECEIVABLE_SERVICE, OTHER_SALES
                 * - 유상(이사): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                 * - 유상(이전설치): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                 * - 유상(해체): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                 *
                 * 역방향
                 * - 중도해지
                 *  - 무약정(운용리스)
                 *      - 해체비(회사귀책 x): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                 *      - 분실료: OTHER_RECEIVABLES_LOST_FEE, MISCELLANEOUS_INCOME_LOST_FEE
                 *  - 약정(금융리스)
                 *      - 해체비(회사귀책 x): ACCOUNTS_RECEIVABLE_SERVICE, SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                 *      - 위약금(회사귀책 x): FINANCE_LEASE_RECEIVABLE_PENALTY, PENALTY_INCOME_ON_RENTAL_CANCELLATION_F_LEASE
                 *      - 분실료: OTHER_RECEIVABLES_LOST_FEE, MISCELLANEOUS_INCOME_LOST_FEE
                 * 채권
                 *  - 가해약
                 *      - 연체료: OTHER_RECEIVABLES_LATE_FEE_O_LEASE, MISCELLANEOUS_INCOME_LATE_FEE
                 *      OTHER_RECEIVABLES_LATE_FEE_F_LEASE, MISCELLANEOUS_INCOME_LATE_FEE
                 */
                val total = NumberUtil.setScale(
                    chargeDetail.rentalFee
                        .plus(chargeDetail.asFees.fold(BigDecimal.ZERO) { a, b -> a.plus(b) })
                        .plus(chargeDetail.partsFees.fold(BigDecimal.ZERO) { a, b -> a.plus(b) })
                        .plus(chargeDetail.laborFees.fold(BigDecimal.ZERO) { a, b -> a.plus(b) })
                        .plus(chargeDetail.lostFee)
                        .plus(chargeDetail.penaltyFee)
                )
                if (total.stripTrailingZeros() == BigDecimal.ZERO) {
                    return@forEach
                }
                val billingDocumentItems = listOf(
                    RentalDocumentItem(
                        chargeDetail.rentalFee,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            listOf(
                                if (leaseType == LeaseType.OPERATING_LEASE) {
                                    AccountName.ACCOUNTS_RECEIVABLE_RENTAL
                                } else {
                                    AccountName.FINANCE_LEASE_RECEIVABLE_BILLING
                                }
                            )
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.rentalFeeForProduct,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            listOf(
                                if (leaseType == LeaseType.OPERATING_LEASE) {
                                    AccountName.SALES_RENTAL
                                } else {
                                    AccountName.FINANCE_LEASE_RECEIVABLE_EQUIPMENT_CURRENT
                                }
                            )
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.rentalFeeForService,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            listOf(
                                // todo: 계약종료 확인필요
                                if (leaseType == LeaseType.OPERATING_LEASE) {
                                    AccountName.UNEARNED_SERVICE_INCOME_O_LEASE // or SALES_RENTAL_SERVICE
                                } else {
                                    AccountName.UNEARNED_SERVICE_INCOME_F_LEASE // or SALES_F_LEASE_SERVICE
                                }
                            )
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.arService,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            listOf(
                                AccountName.ACCOUNTS_RECEIVABLE_SERVICE
                            )
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.penaltyFee,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            if (leaseType == LeaseType.OPERATING_LEASE) {
                                listOf(
                                    AccountName.ACCOUNTS_RECEIVABLE_RENTAL_PENALTY,
                                    AccountName.PENALTY_INCOME_ON_RENTAL_CANCELLATION_O_LEASE
                                )
                            } else {
                                listOf(
                                    AccountName.FINANCE_LEASE_RECEIVABLE_PENALTY,
                                    AccountName.PENALTY_INCOME_ON_RENTAL_CANCELLATION_F_LEASE
                                )
                            }
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.lateFee,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            if (leaseType == LeaseType.OPERATING_LEASE) {
                                listOf(
                                    AccountName.OTHER_RECEIVABLES_LATE_FEE_O_LEASE,
                                    AccountName.MISCELLANEOUS_INCOME_LATE_FEE
                                )
                            } else {
                                listOf(
                                    AccountName.OTHER_RECEIVABLES_LATE_FEE_F_LEASE,
                                    AccountName.MISCELLANEOUS_INCOME_LATE_FEE
                                )
                            }
                        )
                    ),
                    RentalDocumentItem(
                        chargeDetail.lostFee,
                        RentalUtil.filterDocTemplateItem(
                            docTemplateItems,
                            companyCode,
                            listOf(
                                AccountName.OTHER_RECEIVABLES_LOST_FEE,
                                AccountName.MISCELLANEOUS_INCOME_LOST_FEE
                            )
                        )
                    )
                ).filter { billingDocumentItem ->
                    billingDocumentItem.amount.stripTrailingZeros() != BigDecimal.ZERO
                }.toMutableList()
                billingDocumentItems.addAll(
                    chargeDetail.asFees.map { fee ->
                        RentalDocumentItem(
                            fee,
                            RentalUtil.filterDocTemplateItem(
                                docTemplateItems,
                                companyCode,
                                listOf(
                                    AccountName.SERVICE_REVENUE_AS
                                )
                            )
                        )
                    }
                )
                billingDocumentItems.addAll(
                    chargeDetail.partsFees.map { fee ->
                        RentalDocumentItem(
                            fee,
                            RentalUtil.filterDocTemplateItem(
                                docTemplateItems,
                                companyCode,
                                listOf(
                                    AccountName.OTHER_SALES
                                )
                            )
                        )
                    }
                )
                billingDocumentItems.addAll(
                    chargeDetail.laborFees.map { fee ->
                        RentalDocumentItem(
                            fee,
                            RentalUtil.filterDocTemplateItem(
                                docTemplateItems,
                                companyCode,
                                listOf(
                                    AccountName.SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING
                                )
                            )
                        )
                    }
                )
                val docItemRequests = billingDocumentItems.map { billingDocumentItem ->
                    // 각 케이스에 맞는 전표항목 처리
                    billingDocumentItem.docTemplateItems.map { docTemplateItem ->
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
                                orderItem = it.orderItem,
                                serviceFlow = it.serviceFlows.first(),
                                material = it.material,
                                channel = it.channel,
                                charge = it.charge,
                                invoice = it.invoice,
                                rentalCodeMaster = it.rentalCodeMaster
                            ),
                            billingDocumentItem.amount,
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
                    }
                }.flatten().toMutableList()
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
        }
        val res = PostingUtil.posting(
            context,
            requests,
            documentService
        )
        logger.debug("{}", res)
    }

    /**
     * 수납
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
        val list = leaseFindServiceV2.findPaymentTarget(
            fromTime,
            toTime,
            listOf(leaseType),
            test = true
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
            )
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val docHash = Hashs.hash(
                docTemplateCode,
                it.chargePayment.paymentId
            )
            val documentDate = it.chargePayment.paymentTime!!.toLocalDate()
            val chargeDetail = RentalUtil.makeChargeDetail(
                it.charge.chargeId,
                it.charge.targetMonth!!,
                it.chargePayment.chargeItems,
                it.rentalDistributionRule
            )
            val total = it.chargePayment.totalPrice
            val taxLines = it.chargePayment.taxLines
            val salesTax = SalesTax.of(salesTaxConfig, it.chargePayment.tax, taxLines)
            if (total.stripTrailingZeros() == BigDecimal.ZERO) {
                return@forEach
            }
            val paymentDocumentItems = listOf(
                RentalDocumentItem(
                    total,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.OTHER_RECEIVABLES_CREDIT_CARD)
                    )
                ),
                RentalDocumentItem(
                    chargeDetail.rentalFee,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(
                            if (leaseType == LeaseType.OPERATING_LEASE) {
                                AccountName.ACCOUNTS_RECEIVABLE_RENTAL
                            } else {
                                AccountName.FINANCE_LEASE_RECEIVABLE_BILLING
                            }
                        )
                    )
                ),
                RentalDocumentItem(
                    chargeDetail.arService,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(
                            AccountName.ACCOUNTS_RECEIVABLE_SERVICE
                        )
                    )
                ),
                RentalDocumentItem(
                    chargeDetail.penaltyFee,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.ACCOUNTS_RECEIVABLE_RENTAL_PENALTY)
                    )
                ),
                RentalDocumentItem(
                    chargeDetail.lateFee,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        if (leaseType == LeaseType.OPERATING_LEASE) {
                            listOf(
                                AccountName.OTHER_RECEIVABLES_LATE_FEE_O_LEASE,
                            )
                        } else {
                            listOf(
                                AccountName.OTHER_RECEIVABLES_LATE_FEE_F_LEASE,
                            )
                        }
                    )
                ),
                RentalDocumentItem(
                    chargeDetail.lostFee,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(
                            AccountName.OTHER_RECEIVABLES_LOST_FEE
                        )
                    )
                ),
                RentalDocumentItem(
                    salesTax.state,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.DEPOSITS_SALES_TAX_STATE)
                    )
                ),
                RentalDocumentItem(
                    salesTax.county,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.DEPOSITS_SALES_TAX_COUNTY)
                    )
                ),
                RentalDocumentItem(
                    salesTax.city,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.DEPOSITS_SALES_TAX_CITY)
                    )
                ),
                RentalDocumentItem(
                    salesTax.special,
                    RentalUtil.filterDocTemplateItem(
                        docTemplateItems,
                        companyCode,
                        listOf(AccountName.DEPOSITS_SALES_TAX_SPECIAL)
                    )
                ),
            ).filter { paymentDocumentItem ->
                paymentDocumentItem.amount.stripTrailingZeros() != BigDecimal.ZERO
            }.toMutableList()
            val docItemRequests = paymentDocumentItems.map { paymentDocumentItem ->
                // 각 케이스에 맞는 전표항목 처리
                paymentDocumentItem.docTemplateItems.map { docTemplateItem ->
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
                            orderItem = it.orderItem,
                            serviceFlow = it.serviceFlows.first(),
                            material = it.material,
                            channel = it.channel,
                            charge = it.charge,
                            invoice = it.invoice,
                            rentalCodeMaster = it.rentalCodeMaster
                        ),
                        paymentDocumentItem.amount,
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
                }
            }.flatten().toMutableList()
            val currency = companyService.getCompany(companyCode).currency
            val request = CreateDocumentRequest(
                docType = docTemplate.documentType,
                docHash = docHash,
                documentDate = documentDate,
                postingDate = documentDate,
                companyCode = companyCode,
                txCurrency = currency.name,
                reference = it.chargePayment.paymentId,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.chargePayment.paymentId),
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
     * 입금
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
        val list = leaseFindServiceV2.findDepositTarget(
            fromTime,
            toTime,
            listOf(leaseType),
            test = true
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
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
                        orderItem = it.orderItem,
                        serviceFlow = it.serviceFlows.first(),
                        material = it.material,
                        channel = it.channel,
                        charge = it.charge,
                        invoice = it.invoice,
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
     * 3. 서비스 매출 (필터 제공) > [서비스매출] 필터배송
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
        val list = leaseFindServiceV2.findFilterTarget(
            baseYearMonth,
            leaseType
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                docTemplate = docTemplate,
                orderItem = it.orderItem
            )
        }

        val requests: MutableList<CreateDocumentRequest> = mutableListOf()
        list.forEach {
            val docHash = Hashs.hash(
                docTemplateCode,
                it.serviceFlow.serialNumber,
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
                        orderItem = it.orderItem,
                        serviceFlow = it.serviceFlow,
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
                reference = it.serviceFlow.serialNumber,
                text = docTemplate.korText,
                createTime = OffsetDateTime.now(),
                createdBy = docTemplate.bizSystem.toString(),
                docOrigin = docTemplate.toDocumentOriginRequest(it.serviceFlow.serialNumber!!),
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