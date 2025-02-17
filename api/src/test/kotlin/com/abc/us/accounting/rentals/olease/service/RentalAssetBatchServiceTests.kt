package com.abc.us.accounting.rentals.olease.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.SalesType
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.model.RentalAssetHistoryRequest
import com.abc.us.accounting.rentals.lease.service.*
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class RentalAssetBatchServiceTests(
    private val oleaseService: OleaseService,
    private val oleaseBatchService: OleaseBatchService,
    private val oleasePostingBatchService: OleasePostingBatchService,
    private val leasePostingBatchService: LeasePostingBatchService,
    private val leaseFindService: LeaseFindService
): FunSpec({

    val fromTime = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    val toTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    val baseYearMonth = LocalDate.now()
    val baseDate = baseYearMonth.withDayOfMonth(baseYearMonth.lengthOfMonth())

    test("init") {
        oleaseService.deleteAllHistory()
    }

    test("registration") {
        oleaseBatchService.registrationBatch(
            fromTime,
            toTime
        )

        val installList = leaseFindService.findInstallationInfo(
            fromTime,
            toTime,
            LeaseType.OPERATING_LEASE
        )
        val rentalAssetList = oleaseService.findByReq(
            RentalAssetHistoryRequest()
        ).toList()
        installList.size shouldBe rentalAssetList.size

        if (rentalAssetList.isNotEmpty()) {
            val first = rentalAssetList[0]
            first.serialNumber.shouldNotBeNull()
            first.materialId.shouldNotBeNull()
            first.modelName.shouldNotBeNull()
            first.depreciationCount shouldBe null
            first.depreciationDate shouldBe null
            first.acquisitionCost.shouldNotBeNull()
            first.depreciationExpense shouldBe null
            first.accumulatedDepreciation shouldBe null
            first.bookValue.shouldNotBeNull()
            first.contractId.shouldNotBeNull()
            first.contractDate.shouldNotBeNull()
            first.contractStatus.shouldNotBeNull()
            first.orderId.shouldNotBeNull()
            first.orderItemId.shouldNotBeNull()
            first.customerId.shouldNotBeNull()
            first.eventType shouldBe RentalAssetEventType.REGISTRATION.name
        }
    }

    test("depreciation") {
        oleaseBatchService.depreciationBatch(baseYearMonth)
        oleaseBatchService.depreciationBatch(baseYearMonth)

        val depreciationList = leaseFindService.findValidRentalAssetHistory(baseDate)
        val rentalAssetList = oleaseService.findByReq(
            RentalAssetHistoryRequest()
        ).toList()
        depreciationList.size shouldBe rentalAssetList.size

        if (rentalAssetList.isNotEmpty()) {
            val first = rentalAssetList[0]
            first.serialNumber.shouldNotBeNull()
            first.materialId.shouldNotBeNull()
            first.modelName.shouldNotBeNull()
            first.depreciationCount.shouldNotBeNull()
            first.depreciationDate shouldBe baseDate
            first.acquisitionCost.shouldNotBeNull()
            first.depreciationExpense.shouldNotBeNull()
            first.accumulatedDepreciation.shouldNotBeNull()
            first.bookValue.shouldNotBeNull()
            first.contractId.shouldNotBeNull()
            first.contractDate.shouldNotBeNull()
            first.contractStatus.shouldNotBeNull()
            first.orderId.shouldNotBeNull()
            first.orderItemId.shouldNotBeNull()
            first.customerId.shouldNotBeNull()
            first.eventType shouldBe RentalAssetEventType.DEPRECIATION.name
        }
    }

    test("[운용리스] 제품출고") {
        leasePostingBatchService.postingProductShipped(
            fromTime,
            toTime,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE
        )
    }

    test("[운용리스] 설치완료-렌탈자산 인식") {
        leasePostingBatchService.postingInstallation(
            fromTime,
            toTime,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE
        )
    }

//    test("[운용리스] 설치완료-재고가액 확정") {
//        rentalAssetBatchService.postingCOOR004(
//            fromTime,
//            toTime,
//            Constants.TEST_COMPANY_CODE
//        )
//    }

    test("[운용리스] 청구") {
        leasePostingBatchService.postingBilling(
            baseYearMonth,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_PAYMENT_BILLING,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE
        )
    }

    test("[운용리스:상각] 렌탈자산 감가상각") {
        oleasePostingBatchService.postingCORA020(
            baseYearMonth,
            Constants.TEST_COMPANY_CODE
        )
    }

    test("[운용리스] 수납") {
        leasePostingBatchService.postingPayment(
            fromTime,
            toTime,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE
        )
    }

    test("[운용리스] 입금") {
        leasePostingBatchService.postingDeposit(
            fromTime,
            toTime,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE
        )
    }

    test("COSS001") {
        leasePostingBatchService.postingFilterShipped(
            baseYearMonth,
            Constants.TEST_COMPANY_CODE,
            DocumentTemplateCode.OLEASE_FILTER_SHIPPED,
            LeaseType.OPERATING_LEASE,
            SalesType.OPERATING_LEASE,
        )
    }

    test("getAccountCode") {
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.ACCOUNTS_RECEIVABLE_RENTAL
        ) shouldBe "1117030"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.SALES_GOODS_O_LEASE
        ) shouldBe "4103020"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.ADVANCES_FROM_CUSTOMERS
        ) shouldBe "2111010"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_STATE
        ) shouldBe "2115020"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_COUNTY
        ) shouldBe "2115030"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_CITY
        ) shouldBe "2115040"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_SPECIAL
        ) shouldBe "2115050"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.CASH_REGULAR_DEPOSITS
        ) shouldBe "1101010"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.SERVICE_FEES_CREDIT_CARD_FEES
        ) shouldBe "5423020"
        RentalUtil.getAccountCode(
            CompanyCode.N200,
            RentalUtil.AccountName.OTHER_RECEIVABLES_CREDIT_CARD
        ) shouldBe "1136010"

        RentalUtil.getAccountCode(
            CompanyCode.N100,
            RentalUtil.AccountName.ACCOUNTS_RECEIVABLE_RENTAL
        ) shouldBe "1117030"
        RentalUtil.getAccountCode(
            CompanyCode.N300,
            RentalUtil.AccountName.SALES_GOODS_O_LEASE
        ) shouldBe "4103020"
        RentalUtil.getAccountCode(
            CompanyCode.T100,
            RentalUtil.AccountName.ADVANCES_FROM_CUSTOMERS
        ) shouldBe "2111010"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_STATE
        ) shouldBe "2115020"
        RentalUtil.getAccountCode(
            CompanyCode.T300,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_COUNTY
        ) shouldBe "2115030"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_CITY
        ) shouldBe "2115040"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.DEPOSITS_SALES_TAX_SPECIAL
        ) shouldBe "2115050"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.CASH_REGULAR_DEPOSITS
        ) shouldBe "1101010"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.SERVICE_FEES_CREDIT_CARD_FEES
        ) shouldBe "5423020"
        RentalUtil.getAccountCode(
            CompanyCode.T200,
            RentalUtil.AccountName.OTHER_RECEIVABLES_CREDIT_CARD
        ) shouldBe "1136010"

//        RentalUtil.getAccountCode(
//            CompanyCode.T300,
//            RentalUtil.AccountName.ACCOUNTS_RECEIVABLE_RENTAL
//        ) shouldBe "test"
    }
})
