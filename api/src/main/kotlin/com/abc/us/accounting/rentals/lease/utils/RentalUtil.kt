package com.abc.us.accounting.rentals.lease.utils

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.repository.CollectChannelRepository
import com.abc.us.accounting.collects.domain.repository.CollectOrderRepository
import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentItemAttributeRequest
import com.abc.us.accounting.documents.model.DocumentItemRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.FilteringRule
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.iface.domain.model.IfOmsBillingChargeItem
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster
import com.abc.us.accounting.rentals.master.domain.entity.RentalDistributionRule
import com.abc.us.accounting.rentals.master.domain.entity.RentalPricingMaster
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.master.domain.type.MaterialCareType
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.iface.domain.repository.oms.IfChannelRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.iface.domain.type.oms.IfChargeItemType
import com.abc.us.accounting.rentals.lease.model.v2.RentalChargeDetail
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class RentalUtil {
    enum class AccountName {
        /**
         * 운용리스
         */
        ACCOUNTS_RECEIVABLE_RENTAL, // 렌탈미수금-렌탈료
        SALES_GOODS_O_LEASE, // 상품매출-운용리스
        ADVANCES_FROM_CUSTOMERS, // 선수금
        DEPOSITS_SALES_TAX_STATE, // 예수금-판매세-State
        DEPOSITS_SALES_TAX_COUNTY, // 예수금-판매세-County
        DEPOSITS_SALES_TAX_CITY, // 예수금-판매세-City
        DEPOSITS_SALES_TAX_SPECIAL, // 예수금-판매세-Special
        CASH_REGULAR_DEPOSITS, // 현금-보통예금
        SERVICE_FEES_CREDIT_CARD_FEES, // 지급수수료-카드수수료
        OTHER_RECEIVABLES_CREDIT_CARD, // 카드미수금
        // v2
        SALES_RENTAL, // 렌탈료매출-재화
        UNEARNED_SERVICE_INCOME_O_LEASE, // 선수금-운용(비리스) -> 정상
        SALES_RENTAL_SERVICE, // 렌탈료매출-서비스 -> 해지시
        ACCOUNTS_RECEIVABLE_SERVICE, // 외상매출금-용역수입
        SERVICE_REVENUE_AS, // 용역수입(A/S)
        OTHER_SALES, // 기타매출
        SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING, // 용역수입(이전설치및해체)
        ACCOUNTS_RECEIVABLE_RENTAL_PENALTY, // 렌탈미수금-위약금
        PENALTY_INCOME_ON_RENTAL_CANCELLATION_O_LEASE, // 계약해지이익(위약금)-렌탈
        OTHER_RECEIVABLES_LATE_FEE_O_LEASE, // 미수금-연체이자(렌탈)
        MISCELLANEOUS_INCOME_LATE_FEE, // 잡이익-연체이자
        OTHER_RECEIVABLES_LOST_FEE, // 미수금-분실료
        MISCELLANEOUS_INCOME_LOST_FEE, // 잡이익-분실료

        /**
         * 금융리스
         */
        ACCOUNTS_RECEIVABLE_F_LEASE_MONTHLY, // 금융리스채권-월렌탈료
        ACCOUNTS_RECEIVABLE_F_LEASE_INSTALLMENT, // 금융리스채권-할부발생
        SALES_GOODS_F_LEASE, // 상품매출-금융리스
        UNEARNEDINTERESTINCOME_F_LEASE, // 현할차금-금융리스
        INTEREST_INCOME_F_LEASE, // 이자수익-금융리스
        // v2
        FINANCE_LEASE_RECEIVABLE_BILLING, // 금융리스채권-월 렌탈료
        FINANCE_LEASE_RECEIVABLE_EQUIPMENT_CURRENT, // 금융리스채권-할부발생(리스렌탈료 일부상계)
        UNEARNED_SERVICE_INCOME_F_LEASE, // 선수금-금융(비리스) -> 정상
        SALES_F_LEASE_SERVICE, // 용역수입(금융리스) -> 해지시
        FINANCE_LEASE_RECEIVABLE_PENALTY, // 금융리스채권-위약금
        PENALTY_INCOME_ON_RENTAL_CANCELLATION_F_LEASE, // 계약해지이익(위약금)-금융리스
        OTHER_RECEIVABLES_LATE_FEE_F_LEASE, // 미수금-연체이자(금융리스)
    }

    companion object {
        private val accountCodeDefaultMap: Map<AccountName, String> = mapOf(
            // 운용리스
            AccountName.ACCOUNTS_RECEIVABLE_RENTAL to "1117030",
            AccountName.SALES_GOODS_O_LEASE to "4103020",
            AccountName.ADVANCES_FROM_CUSTOMERS to "2111010",
            AccountName.DEPOSITS_SALES_TAX_STATE to "2115020",
            AccountName.DEPOSITS_SALES_TAX_COUNTY to "2115030",
            AccountName.DEPOSITS_SALES_TAX_CITY to "2115040",
            AccountName.DEPOSITS_SALES_TAX_SPECIAL to "2115050",
            AccountName.CASH_REGULAR_DEPOSITS to "1101010",
            AccountName.SERVICE_FEES_CREDIT_CARD_FEES to "5423020",
            AccountName.OTHER_RECEIVABLES_CREDIT_CARD to "1136010",
            // v2
            AccountName.SALES_RENTAL to "4113010",
            AccountName.UNEARNED_SERVICE_INCOME_O_LEASE to "2111040",
            AccountName.SALES_RENTAL_SERVICE to "4113011",
            AccountName.ACCOUNTS_RECEIVABLE_SERVICE to "1117100",
            AccountName.SERVICE_REVENUE_AS to "4116020",
            AccountName.OTHER_SALES to "4199010",
            AccountName.SERVICE_REVENUE_INSTALLATION_AND_DISMANTLING to "4116021",
            AccountName.ACCOUNTS_RECEIVABLE_RENTAL_PENALTY to "1117011",
            AccountName.PENALTY_INCOME_ON_RENTAL_CANCELLATION_O_LEASE to "5471070",
            AccountName.OTHER_RECEIVABLES_LATE_FEE_O_LEASE to "1126040",
            AccountName.MISCELLANEOUS_INCOME_LATE_FEE to "4299030",
            AccountName.OTHER_RECEIVABLES_LOST_FEE to "1126030",
            AccountName.MISCELLANEOUS_INCOME_LOST_FEE to "4299020",

            // 금융리스
            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_MONTHLY to "1117040",
            AccountName.ACCOUNTS_RECEIVABLE_F_LEASE_INSTALLMENT to "1117060",
            AccountName.SALES_GOODS_F_LEASE to "4103030",
            AccountName.UNEARNEDINTERESTINCOME_F_LEASE to "2215010",
            AccountName.INTEREST_INCOME_F_LEASE to "4201010",
            // v2
            AccountName.FINANCE_LEASE_RECEIVABLE_BILLING to "1117041",
            AccountName.FINANCE_LEASE_RECEIVABLE_EQUIPMENT_CURRENT to "1117043",
            AccountName.UNEARNED_SERVICE_INCOME_F_LEASE to "2111050",
            AccountName.SALES_F_LEASE_SERVICE to "4116070",
            AccountName.FINANCE_LEASE_RECEIVABLE_PENALTY to "1117042",
            AccountName.PENALTY_INCOME_ON_RENTAL_CANCELLATION_F_LEASE to "4237040",
            AccountName.OTHER_RECEIVABLES_LATE_FEE_F_LEASE to "1126070",
        )

        private val accountCodeMap: Map<CompanyCode, Map<AccountName, String>> = mapOf(
//            CompanyCode.T300 to mapOf(
//                AccountName.ACCOUNTS_RECEIVABLE_RENTAL to "test"
//            )
        )

        /**
         * context 세팅
         */
        fun getContext(
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
        ): DocumentServiceContext {
            return DocumentServiceContext.withFilteringRule(
                FilteringRule(
                    companyCodes = companyCodes,
                    docTemplateCodes = docTemplateCodes,
                    orderIds = orderIds,
                    orderItemIds = orderItemIds,
                    customerIds = customerIds,
                    materialIds = materialIds,
                    serviceFlowIds = serviceFlowIds,
                    contractIds = contractIds,
                    bisSystems = bisSystems,
                    bizTxIds = bizTxIds,
                    bizProcesses = bizProcesses,
                    bizEvents = bizEvents,
                    accountingEvents = accountingEvents
                )
            )
        }

        /**
         * 배치 대상 필터링
         */
        fun checkFilteringRule(
            context: DocumentServiceContext,
            docTemplate: DocumentTemplate? = null,
            contract: CollectContract,
            serviceFlow: CollectServiceFlow? = null
        ): Boolean {
            val rule = context.filteringRule ?: run {
                return true
            }
            val companyCode = docTemplate?.docTemplateKey?.companyCode
            return with(rule) {
                when {
                    docTemplateCodes.isNotEmpty() && (docTemplate != null && !docTemplateCodes.contains(docTemplate.docTemplateKey.docTemplateCode)) -> false
                    companyCodes.isNotEmpty() && !companyCodes.contains(companyCode) -> false
                    orderIds.isNotEmpty() && !orderIds.contains(contract.orderId) -> false
                    orderItemIds.isNotEmpty() && !orderItemIds.contains(contract.orderItemId) -> false
                    customerIds.isNotEmpty() && !customerIds.contains(contract.customerId) -> false
                    materialIds.isNotEmpty() && !materialIds.contains(contract.materialId) -> false
                    serviceFlowIds.isNotEmpty() && (serviceFlow != null && !serviceFlowIds.contains(serviceFlow.serviceFlowId)) -> false
                    bisSystems.isNotEmpty() && (docTemplate != null && !bisSystems.contains(docTemplate.bizSystem)) -> false
                    bizProcesses.isNotEmpty() && (docTemplate != null && !bizProcesses.contains(docTemplate.bizProcess)) -> false
                    bizEvents.isNotEmpty() && (docTemplate != null && !bizEvents.contains(docTemplate.bizEvent)) -> false
                    accountingEvents.isNotEmpty() && (docTemplate != null && !accountingEvents.contains(docTemplate.accountEvent?.name)) -> false
                    contractIds.isNotEmpty() && !contractIds.contains(contract.contractId) -> false
                    else -> true
                }
            }
        }

        /**
         * 배치 대상 필터링
         */
        fun checkFilteringRule(
            context: DocumentServiceContext,
            docTemplate: DocumentTemplate? = null,
            orderItem: IfOrderItem,
            serviceFlow: IfServiceFlow? = null,
        ): Boolean {
            val rule = context.filteringRule ?: run {
                return true
            }
            val companyCode = docTemplate?.docTemplateKey?.companyCode
            return with(rule) {
                when {
                    docTemplateCodes.isNotEmpty() && (docTemplate != null && !docTemplateCodes.contains(docTemplate.docTemplateKey.docTemplateCode)) -> false
                    companyCodes.isNotEmpty() && !companyCodes.contains(companyCode) -> false
                    orderIds.isNotEmpty() && !orderIds.contains(orderItem.orderId) -> false
                    orderItemIds.isNotEmpty() && !orderItemIds.contains(orderItem.orderItemId) -> false
                    customerIds.isNotEmpty() && !customerIds.contains(orderItem.customerId) -> false
                    materialIds.isNotEmpty() && !materialIds.contains(orderItem.materialId) -> false
                    serviceFlowIds.isNotEmpty() && (serviceFlow != null && !serviceFlowIds.contains(serviceFlow.serviceFlowId)) -> false
                    bisSystems.isNotEmpty() && (docTemplate != null && !bisSystems.contains(docTemplate.bizSystem)) -> false
                    bizProcesses.isNotEmpty() && (docTemplate != null && !bizProcesses.contains(docTemplate.bizProcess)) -> false
                    bizEvents.isNotEmpty() && (docTemplate != null && !bizEvents.contains(docTemplate.bizEvent)) -> false
                    accountingEvents.isNotEmpty() && (docTemplate != null && !accountingEvents.contains(docTemplate.accountEvent?.name)) -> false
                    contractIds.isNotEmpty() && !contractIds.contains(orderItem.contractId) -> false
                    else -> true
                }
            }
        }

        /**
         * 날짜를 연월로 변환
         */
        fun getYearMonth(
            date: LocalDate?
        ): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
            return date?.format(formatter) ?: ""
        }

        /**
         * 전표 속성 정보 조회
         */
        fun getAttributeMap(
            docTemplateItem: DocumentTemplateItem,
            salesType: SalesType,
            leaseType: LeaseType,
            companyCode: CompanyCode,
            contract: CollectContract? = null,
            installation: CollectInstallation? = null,
            material: CollectMaterial? = null,
            channel: CollectChannel? = null,
            charge: CollectCharge? = null,
            rentalCodeMaster: RentalCodeMaster? = null
        ): MutableMap<DocumentAttributeType, String?> {
            val salesItem = getSalesItem(
                docTemplateItem.accountCode,
                companyCode
            )
            val res: MutableMap<DocumentAttributeType, String?> = mutableMapOf()
            DocumentAttributeType.entries.forEach {
                res[it] = when (it) {
                    // 기본 설정값
                    DocumentAttributeType.SALES_TYPE ->
                        salesType.name
                    DocumentAttributeType.SALES_ITEM ->
                        salesItem.code
                    DocumentAttributeType.LEASE_TYPE ->
                        leaseType.name
                    DocumentAttributeType.COST_CENTER ->
                        docTemplateItem.costCenter
                    DocumentAttributeType.PROFIT_CENTER ->
                        docTemplateItem.profitCenter
                    DocumentAttributeType.SEGMENT ->
                        docTemplateItem.segment
                    DocumentAttributeType.PROJECT ->
                        docTemplateItem.project

                    // 별도 설정값
                    DocumentAttributeType.CUSTOMER_ID ->
                        contract?.customerId ?:
                        ""
                    DocumentAttributeType.ORDER_ID ->
                        contract?.orderId ?:
                        ""
                    DocumentAttributeType.ORDER_ITEM_ID ->
                        contract?.orderItemId ?:
                        installation?.orderItemId ?:
                        ""
                    DocumentAttributeType.CONTRACT_ID ->
                        contract?.contractId ?:
                        ""
                    DocumentAttributeType.SERIAL_NUMBER ->
                        installation?.serialNumber ?:
                        ""
                    DocumentAttributeType.RENTAL_CODE ->
                        contract?.rentalCode ?:
                        ""
                    DocumentAttributeType.CONTRACT_DURATION ->
                        (rentalCodeMaster?.contractDuration ?: "").toString()
                    DocumentAttributeType.CURRENT_TERM ->
                        (rentalCodeMaster?.currentTerm ?: "").toString()
                    DocumentAttributeType.CHANNEL_TYPE ->
                        channel?.channelType?.name ?:
                        ""
                    DocumentAttributeType.CHANNEL_NAME ->
                        channel?.channelName ?:
                        ""
                    DocumentAttributeType.CHANNEL_DETAIL ->
                        channel?.channelDetail ?:
                        ""
//                DocumentAttributeType.REFERRAL_CODE -> ""
//                DocumentAttributeType.VENDOR_ID -> ""
                    DocumentAttributeType.COMMITMENT_DURATION ->
                        (contract?.durationInMonths ?: "").toString()
                    DocumentAttributeType.MATERIAL_ID ->
                        contract?.materialId ?:
                        material?.materialId ?:
                        ""
                    DocumentAttributeType.MATERIAL_TYPE ->
                        material?.materialType?.name ?:
                        ""
                    DocumentAttributeType.MATERIAL_CATEGORY_CODE ->
                        material?.materialCategoryCode?.name ?:
                        ""
                    DocumentAttributeType.PRODUCT_CATEGORY ->
                        material?.productType?.name ?:
                        ""
                    DocumentAttributeType.FILTER_TYPE ->
                        material?.filterType?.name ?:
                        ""
                    DocumentAttributeType.FEATURE_TYPE ->
                        material?.featureCode?.name ?:
                        ""
                    DocumentAttributeType.MATERIAL_SERIES_CODE ->
                        material?.materialSeriesCode ?:
                        ""
                    DocumentAttributeType.INSTALLATION_TYPE ->
                        material?.installationType?.symbol ?:
                        ""
                    DocumentAttributeType.CHANNEL_ID ->
                        channel?.channelId ?:
                        ""
                    DocumentAttributeType.CHARGE_ID ->
                        charge?.chargeId ?:
                        ""
                    DocumentAttributeType.INSTALL_ID ->
                        installation?.installId ?:
                        ""
                    // todo
                    DocumentAttributeType.BRANCH_ID ->
                        ""
                    // todo
                    DocumentAttributeType.WAREHOUSE_ID ->
                        ""
                    DocumentAttributeType.TECHNICIAN_ID ->
                        installation?.technicianId ?:
                        ""
                    DocumentAttributeType.INVOICE_ID ->
                        charge?.invoiceId ?:
                        ""
                    else -> ""
                }
            }
            return res
        }

        /**
         * 전표 속성 정보 조회
         */
        fun getAttributeMap(
            docTemplateItem: DocumentTemplateItem,
            salesType: SalesType,
            leaseType: LeaseType,
            companyCode: CompanyCode,
            contract: IfContract? = null,
            orderItem: IfOrderItem? = null,
            serviceFlow: IfServiceFlow? = null,
            material: IfMaterial? = null,
            channel: IfChannel? = null,
            charge: IfCharge? = null,
            invoice: IfInvoice? = null,
            rentalCodeMaster: RentalCodeMaster? = null
        ): MutableMap<DocumentAttributeType, String?> {
            val salesItem = getSalesItem(
                docTemplateItem.accountCode,
                companyCode
            )
            val res: MutableMap<DocumentAttributeType, String?> = mutableMapOf()
            DocumentAttributeType.entries.forEach {
                res[it] = when (it) {
                    // 기본 설정값
                    DocumentAttributeType.SALES_TYPE ->
                        salesType.name
                    DocumentAttributeType.SALES_ITEM ->
                        salesItem.code
                    DocumentAttributeType.LEASE_TYPE ->
                        leaseType.name
                    DocumentAttributeType.COST_CENTER ->
                        docTemplateItem.costCenter
                    DocumentAttributeType.PROFIT_CENTER ->
                        docTemplateItem.profitCenter
                    DocumentAttributeType.SEGMENT ->
                        docTemplateItem.segment
                    DocumentAttributeType.PROJECT ->
                        docTemplateItem.project

                    // 별도 설정값
                    DocumentAttributeType.CUSTOMER_ID ->
                        orderItem?.customerId ?:
                        ""
                    DocumentAttributeType.ORDER_ID ->
                        orderItem?.orderId ?:
                        ""
                    DocumentAttributeType.ORDER_ITEM_ID ->
                        orderItem?.orderItemId ?:
                        ""
                    DocumentAttributeType.CONTRACT_ID ->
                        orderItem?.contractId ?:
                        ""
                    DocumentAttributeType.SERIAL_NUMBER ->
                        serviceFlow?.serialNumber ?:
                        ""
                    DocumentAttributeType.RENTAL_CODE ->
                        contract?.rentalCode ?:
                        ""
                    DocumentAttributeType.CONTRACT_DURATION ->
                        (rentalCodeMaster?.contractDuration ?: "").toString()
                    DocumentAttributeType.CURRENT_TERM ->
                        (rentalCodeMaster?.currentTerm ?: "").toString()
                    DocumentAttributeType.CHANNEL_TYPE ->
                        channel?.channelType?.name ?:
                        ""
                    DocumentAttributeType.CHANNEL_NAME ->
                        channel?.channelName ?:
                        ""
                    DocumentAttributeType.CHANNEL_DETAIL ->
                        channel?.channelDetail ?:
                        ""
//                DocumentAttributeType.REFERRAL_CODE -> ""
//                DocumentAttributeType.VENDOR_ID -> ""
                    DocumentAttributeType.COMMITMENT_DURATION ->
                        (contract?.durationInMonths ?: "").toString()
                    DocumentAttributeType.MATERIAL_ID ->
                        orderItem?.materialId ?:
                        material?.materialId ?:
                        ""
                    DocumentAttributeType.MATERIAL_TYPE ->
                        material?.materialType?.name ?:
                        ""
                    DocumentAttributeType.MATERIAL_CATEGORY_CODE ->
                        material?.materialCategoryCode?.name ?:
                        ""
                    DocumentAttributeType.PRODUCT_CATEGORY ->
                        material?.productType?.name ?:
                        ""
                    DocumentAttributeType.FILTER_TYPE ->
                        material?.filterType ?:
                        ""
                    DocumentAttributeType.FEATURE_TYPE ->
                        material?.featureCode?:
                        ""
                    DocumentAttributeType.MATERIAL_SERIES_CODE ->
                        material?.materialSeriesCode ?:
                        ""
                    DocumentAttributeType.INSTALLATION_TYPE ->
                        material?.installationType ?:
                        ""
                    DocumentAttributeType.CHANNEL_ID ->
                        channel?.channelId ?:
                        ""
                    DocumentAttributeType.CHARGE_ID ->
                        charge?.chargeId ?:
                        ""
                    DocumentAttributeType.INSTALL_ID ->
                        serviceFlow?.installId ?:
                        ""
                    DocumentAttributeType.BRANCH_ID ->
                        serviceFlow?.branchId ?:
                        ""
                    DocumentAttributeType.WAREHOUSE_ID ->
                        serviceFlow?.warehouseId ?:
                        ""
                    DocumentAttributeType.TECHNICIAN_ID ->
                        serviceFlow?.technicianId ?:
                        ""
                    DocumentAttributeType.INVOICE_ID ->
                        invoice?.invoiceId ?:
                        ""
                    else -> ""
                }
            }
            return res
        }

        /**
         * 월의 마지막 일 계산
         */
        fun getLastDate(
            yearMonth: LocalDate
        ): LocalDate {
            val lastDate = yearMonth.withDayOfMonth(yearMonth.lengthOfMonth())
            return lastDate
        }

        /**
         * 월의 마지막 일 계산
         */
        fun getLastDate(
            yearMonth: org.joda.time.YearMonth
        ): LocalDate {
            val date = LocalDate.of(yearMonth.year, yearMonth.monthOfYear, 1)
            return getLastDate(date)
        }

        /**
         * 다음달의 첫 일 계산
         */
        fun getNextFirstDate(
            yearMonth: LocalDate
        ): LocalDate {
            val firstDate = yearMonth.withDayOfMonth(1)
            val nextFirstDate = firstDate.plusMonths(1)
            return nextFirstDate
        }

        /**
         * 다음달의 첫 일 계산
         */
        fun getNextFirstDate(
            yearMonth: org.joda.time.YearMonth
        ): LocalDate {
            val date = LocalDate.of(yearMonth.year, yearMonth.monthOfYear, 1)
            return getNextFirstDate(date)
        }

        /**
         * 기준월의 마지막일 반환
         */
        fun getLastDate(
            targetMonth: String
        ): LocalDate {
            val firstDate = LocalDate.parse("$targetMonth-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val lastDate = firstDate.withDayOfMonth(firstDate.lengthOfMonth())
            return lastDate
        }

        /**
         * 기준월의 다음달 첫 일 계산
         */
        fun getNextFirstDate(
            targetMonth: String
        ): LocalDate {
            val firstDate = LocalDate.parse("$targetMonth-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val nextFirstDate = firstDate.plusMonths(1)
            return nextFirstDate
        }

        /**
         * 해당 월의 마지막 일 계산
         */
        fun getLastDate(
            dateTime: OffsetDateTime
        ): OffsetDateTime {
            val lastDateTime = dateTime.withDayOfMonth(YearMonth.from(dateTime).lengthOfMonth())
            return lastDateTime
        }

        /**
         * 같은 연월인지 확인
         */
        fun checkSameYearMonth(
            d1: LocalDate,
            d2: LocalDate
        ): Boolean {
            return (
                d1.year == d2.year &&
                d1.month == d2.month
            )
        }

        /**
         * 전표 계정코드 조회
         */
        fun getAccountCode(
            companyCode: CompanyCode,
            accountName: AccountName
        ): String {
            return accountCodeMap[companyCode]?.get(accountName)
                ?: accountCodeDefaultMap[accountName]!!
        }

        /**
         * 계정과목에 따라 salesItem 설정
         */
        private fun getSalesItem(
            accountCode: String,
            companyCode: CompanyCode
        ): SalesItem {
            return when (accountCode) {
                getAccountCode(
                    companyCode,
                    AccountName.ADVANCES_FROM_CUSTOMERS // 선수금
                ),
                getAccountCode(
                    companyCode,
                    AccountName.UNEARNED_SERVICE_INCOME_O_LEASE // 선수금-운용(비리스)
                ),
                getAccountCode(
                    companyCode,
                    AccountName.UNEARNED_SERVICE_INCOME_F_LEASE // 선수금-금융(비리스)
                ),
                getAccountCode(
                    companyCode,
                    AccountName.SALES_RENTAL_SERVICE // 렌탈료매출-서비스
                ),
                getAccountCode(
                    companyCode,
                    AccountName.SALES_F_LEASE_SERVICE // 용역수입(금융리스)
                )
                -> SalesItem.SERVICE
                getAccountCode(
                    companyCode,
                    AccountName.INTEREST_INCOME_F_LEASE // 이자수익
                ) -> SalesItem.INTEREST_INCOME
                else -> SalesItem.PRODUCT
            }
        }

        /**
         * 계약 시작/종료월 일할 계산
         */
        fun getDailyRatio(
            baseYearMonth: LocalDate,
            startDate: LocalDate,
            endDate: LocalDate?
        ): BigDecimal {
            val startMonthDailyRatio = NumberUtil.getDailyRatio(startDate)
            return if (
                checkSameYearMonth(
                    startDate,
                    baseYearMonth
                )
            ) {
                startMonthDailyRatio
            } else if (
                endDate != null &&
                checkSameYearMonth(
                    endDate,
                    baseYearMonth
                )
            ) {
                NumberUtil.minus(
                    BigDecimal.ONE,
                    startMonthDailyRatio
                )
            } else {
                BigDecimal(1)
            }
        }

        /**
         * 주문접수일 기준 rentalDistributionRule 가져오기
         */
        fun getRentalDistributionRule(
            rentalDistributionRules: List<RentalDistributionRule>,
            contract: CollectContract,
            order: CollectOrder
        ): RentalDistributionRule? {
            val orderDate = order.orderCreateTime!!.toLocalDate()
            val list = rentalDistributionRules.filter {
                it.materialId == contract.materialId &&
                it.rentalCode == contract.rentalCode &&
                (
                    it.startDate.isBefore(orderDate) ||
                    it.startDate.isEqual(orderDate)
                ) &&
                it.materialCareType == MaterialCareType.SELF_CARE
            }
            return if (list.isEmpty()) {
                null
            } else {
                list.maxBy {
                    it.startDate
                }
            }
        }

        /**
         * 주문접수일 기준 rentalDistributionRule 가져오기
         */
        fun getRentalDistributionRule(
            rentalDistributionRules: List<RentalDistributionRule>,
            contract: IfContract,
            orderItem: IfOrderItem
        ): RentalDistributionRule? {
            val orderDate = orderItem.createTime.toLocalDate()
            val list = rentalDistributionRules.filter {
                it.materialId == orderItem.materialId &&
                it.rentalCode == contract.rentalCode &&
                (
                    it.startDate.isBefore(orderDate) ||
                    it.startDate.isEqual(orderDate)
                ) &&
                it.materialCareType == MaterialCareType.SELF_CARE
            }
            return if (list.isEmpty()) {
                null
            } else {
                list.maxBy {
                    it.startDate
                }
            }
        }

        /**
         * 주문접수일 기준 rentalPricingMaster 가져오기
         */
        fun getRentalPricingMaster(
            rentalPricingMasters: List<RentalPricingMaster>,
            contract: CollectContract,
            material: CollectMaterial,
            order: CollectOrder
        ): RentalPricingMaster? {
            val orderDate = order.orderCreateTime!!.toLocalDate()
            val list = rentalPricingMasters.filter {
                it.rentalCode == contract.rentalCode &&
                it.materialModelNamePrefix == material.materialSeriesCode &&
                it.materialCareType == MaterialCareType.SELF_CARE && (
                    it.startDate!!.isBefore(orderDate) ||
                    it.startDate.isEqual(orderDate)
                )
            }
            return if (list.isEmpty()) {
                null
            } else {
                list.maxBy {
                    it.startDate!!
                }
            }
        }

        /**
         * 주문접수일 기준 rentalPricingMaster 가져오기
         */
        fun getRentalPricingMaster(
            rentalPricingMasters: List<RentalPricingMaster>,
            contract: IfContract,
            material: IfMaterial,
            orderItem: IfOrderItem
        ): RentalPricingMaster? {
            val orderDate = orderItem.createTime.toLocalDate()
            val list = rentalPricingMasters.filter {
                it.rentalCode == contract.rentalCode &&
                it.materialModelNamePrefix == material.materialSeriesCode &&
                it.materialCareType == MaterialCareType.SELF_CARE && (
                    it.startDate!!.isBefore(orderDate) ||
                    it.startDate.isEqual(orderDate)
                )
            }
            return if (list.isEmpty()) {
                null
            } else {
                list.maxBy {
                    it.startDate!!
                }
            }
        }

        /**
         * orderId로 채널 정보 조회
         */
        fun findChannelsByOrder(
            collectOrderRepository: CollectOrderRepository,
            collectChannelRepository: CollectChannelRepository,
            orderIds: List<String>
        ): MutableMap<String, CollectOrder> {
            val orderMap = mutableMapOf<String, CollectOrder>()
            val channelIds = mutableSetOf<String>()
            collectOrderRepository.findActiveByOrderIdsIn(orderIds)?.let { orders ->
                orders.forEach { order ->
                    orderMap[order.orderId] = order
                    order.channelId?.let {
                        channelIds.add(it)
                    }
                }
            }
            collectChannelRepository.findActiveByChannelIdIs(channelIds.toList())?.let { channels ->
                channels.forEach { channel->
                    orderMap.forEach { (orderId, order) ->
                        if (order.channelId == channel.channelId) {
                            order.channel = channel
                        }
                    }
                }
            }
            return orderMap
        }

        /**
         * orderItemId로 채널 정보 조회
         */
        fun findChannelsByOrderItem(
            ifOrderItemRepository: IfOrderItemRepository,
            ifChannelRepository: IfChannelRepository,
            orderItemIds: List<String>
        ): MutableMap<String, IfOrderItem> {
            val orderItemMap = mutableMapOf<String, IfOrderItem>()
            val channelIds = mutableSetOf<String>()
            ifOrderItemRepository.findByOrderItemIdsIn(orderItemIds).let { orderItems ->
                orderItems.forEach { orderItem ->
                    orderItemMap[orderItem.orderItemId] = orderItem
                    orderItem.channelId.let {
                        channelIds.add(it)
                    }
                }
            }
            ifChannelRepository.findByChannelIdIn(channelIds.toList()).let { channels ->
                channels.forEach { channel->
                    orderItemMap.forEach { (orderItemId, orderItem) ->
                        if (orderItem.channelId == channel.channelId) {
                            orderItem.channel = channel
                        }
                    }
                }
            }
            return orderItemMap
        }

        /**
         * DocumentItemRequest 생성
         */
        fun toDocumentItemRequest(
            companyService: CompanyService,
            documentMasterService: DocumentMasterService,
            context: DocumentServiceContext,
            companyCode: CompanyCode,
            docTemplateItem: DocumentTemplateItem,
            attributeTypeValueMap: MutableMap<DocumentAttributeType, String?>,
            amount: BigDecimal,
            exceptAttrList: List<DocumentAttributeType> = listOf()
        ): DocumentItemRequest {
            val accountCode = docTemplateItem.accountCode
            val accountSide = docTemplateItem.accountSide

            val docItemAttributeRequests = toDocumentItemAttributeRequests(
                documentMasterService,
                context,
                docTemplateItem,
                attributeTypeValueMap,
                exceptAttrList
            ).toMutableList()
            // do something
            val currency = companyService.getCompany(companyCode).currency
            return DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accountCode,
                accountSide = accountSide,
                txCurrency = currency.name,
                txAmount = amount,
                text = docTemplateItem.korText!!,
                costCenter = attributeTypeValueMap[DocumentAttributeType.COST_CENTER]!!,
                profitCenter = attributeTypeValueMap[DocumentAttributeType.PROFIT_CENTER],
                segment = attributeTypeValueMap[DocumentAttributeType.SEGMENT],
                project = attributeTypeValueMap[DocumentAttributeType.PROJECT],
                customerId = attributeTypeValueMap[DocumentAttributeType.CUSTOMER_ID]!!,
                vendorId = attributeTypeValueMap[DocumentAttributeType.VENDOR_ID],
                attributes = docItemAttributeRequests
            )
        }

        /**
         * 전표 attributes 생성
         */
        fun toDocumentItemAttributeRequests(
            documentMasterService: DocumentMasterService,
            context: DocumentServiceContext,
            docTemplateItem: DocumentTemplateItem,
            attributeTypeValueMap: MutableMap<DocumentAttributeType, String?>,
            exceptAttrList: List<DocumentAttributeType> = listOf()
        ): List<DocumentItemAttributeRequest> {
            val accountCode = docTemplateItem.accountCode
            val companyCode = docTemplateItem.docTemplateKey.companyCode
            val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(accountCode))
            return attributeTypeMasters
                .asSequence()
                .filter { !exceptAttrList.contains(it.attributeType) }
                .filter { it.fieldRequirement.isAcceptable() }
                .filter { attributeTypeValueMap.containsKey(it.attributeType) }
                .filter { attributeTypeValueMap[it.attributeType] != null }
                .map { attributeTypeMaster ->
                    DocumentItemAttributeRequest(
                        attributeType = attributeTypeMaster.attributeType,
                        attributeValue = attributeTypeValueMap[attributeTypeMaster.attributeType]!!
                    )
                }
                .toList()
        }

        /**
         * companyCode, accountName 으로 docTemplateItem 필터링
         */
        fun filterDocTemplateItem(
            docTemplateItems: List<DocumentTemplateItem>,
            companyCode: CompanyCode,
            accountNames: List<AccountName>
        ): List<DocumentTemplateItem> {
            val accountCodes = accountNames.map {
                getAccountCode(
                    companyCode,
                    it
                )
            }
            return docTemplateItems.filter {
                accountCodes.contains(it.accountCode)
            }
        }

        /**
         * 청구/수납 금액 정보 생성
         */
        fun makeChargeDetail(
            chargeId: String,
            targetMonth: String,
            chargeItems: List<IfOmsBillingChargeItem>,
            rentalDistributionRule: RentalDistributionRule
        ): RentalChargeDetail {
            // 렌탈료(재화 + 서비스)
            val rentalFee = chargeItems.find {
                it.chargeItemType == IfChargeItemType.RENTAL_FEE
            }?.totalPrice ?: BigDecimal.ZERO
            // 렌탈료 재화 부분
            val rentalFeeForProduct = NumberUtil.multiply(
                rentalFee,
                rentalDistributionRule.distributionRatio.m01
            )
            // 렌탈료 서비스 부분
            val rentalFeeForService = NumberUtil.minus(
                rentalFee,
                rentalFeeForProduct
            )
            // 유상(단순방문)
            val asFees = chargeItems.filter {
                it.chargeItemType == IfChargeItemType.SERVICE_FEE
            }.map {
                it.totalPrice
            }
            // 유상(부품교체)
            val partsFees = chargeItems.filter {
                it.chargeItemType == IfChargeItemType.PART_COST
            }.map {
                it.totalPrice
            }
            // 유상(이사, 이전설치, 해체)
            val laborFees = chargeItems.filter {
                listOf(
                    IfChargeItemType.INSTALLATION_FEE,
                    IfChargeItemType.DISMANTILING_FEE,
                    IfChargeItemType.REINSTALLATION_FEE,
                    IfChargeItemType.RELOCATION_FEE
                ).contains(it.chargeItemType)
            }.map {
                it.totalPrice
            }
            // 분실료
            val lostFee = chargeItems.find {
                it.chargeItemType == IfChargeItemType.LOSS_FEE
            }?.totalPrice ?: BigDecimal.ZERO
            // 위약금
            val penaltyFee = chargeItems.find {
                it.chargeItemType == IfChargeItemType.TERMINATION_PENALTY
            }?.totalPrice ?: BigDecimal.ZERO
            // 연체료
            val lateFee = chargeItems.filter {
                it.chargeItemType == IfChargeItemType.LATE_FEE
            }.fold(BigDecimal.ZERO) { acc, chargeItem ->
                acc.plus(chargeItem.totalPrice)
            }
            // 외상매출금-용역수입(공임: asFees, 부품비: partsFees, 설치비, 해체비, 이전 설치비: laborFees)
            val arService = NumberUtil.setScale(
                asFees.fold(BigDecimal.ZERO) { acc, totalPrice ->
                    acc.plus(totalPrice)
                }.plus(
                    partsFees.fold(BigDecimal.ZERO) { acc, totalPrice ->
                        acc.plus(totalPrice)
                    }
                ).plus(
                    laborFees.fold(BigDecimal.ZERO) { acc, totalPrice ->
                        acc.plus(totalPrice)
                    }
                )
            )
            return RentalChargeDetail(
                chargeId,
                targetMonth,
                rentalFee,
                rentalFeeForProduct,
                rentalFeeForService,
                arService,
                asFees,
                partsFees,
                laborFees,
                lostFee,
                penaltyFee,
                lateFee
            )
        }

        /**
         * if_invoice.charges 파싱
         */
        fun makeChargeDetails(
            invoice: IfInvoice,
            rentalDistributionRule: RentalDistributionRule
        ): List<RentalChargeDetail> {
            return invoice.charges.map { charge ->
                makeChargeDetail(
                    charge.chargeId,
                    charge.targetMonth,
                    charge.chargeItems,
                    rentalDistributionRule
                )
            }
        }
    }
}