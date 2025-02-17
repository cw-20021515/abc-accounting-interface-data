package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.fixtures.ClearingDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestAccountCode
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate


/**
 * 매뉴얼 반제 케이스
 */
@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentClearingTests  (
    private val persistenceService: DocumentPersistenceService,
    private val documentService: DocumentService,
    private val documentSupportService: DocumentSupportService,
    private val documentRelationService: DocumentRelationService,
    private val documentTemplateService: DocumentTemplateService,
    private val companyServiceable: CompanyServiceable,
    private val timeLogger: TimeLogger = TimeLogger()
): FunSpec({
    val companyCode =CompanyCode.T200

    fun clearing(context: DocumentServiceContext = DocumentServiceContext.ONLY_DEBUG,
                 docType:DocumentType,
                 docHash: String? = null,
                 candidates: List<DocumentResult>,
                 clearingAccountCodes:List<String>,
                 clearingAmountRate: BigDecimal = BigDecimal.ONE,
                 docTemplate: TestDocumentTemplateMapping,
                 originRequest: DocumentOriginRequest,
                 customerId:String?=null,
                 vendorId:String?=null,
                 companyCode: CompanyCode?=null,
                 txCurrency:String?=null
    ): DocumentResult {

        val clearingRequest = ClearingDocumentRequestFixture.generateByTemplate (
            docType, docHash, candidates,
            clearingAccountCodes, clearingAmountRate, docTemplate, originRequest,
            customerId = customerId, vendorId = vendorId)

        val accounts = docTemplate.getAccountInfos(clearingRequest.companyCode)

        clearingRequest.docItems.size shouldBeLessThanOrEqual accounts.size
        // clearing 호출
        val results = documentService.clearing(context, listOf(clearingRequest))

        results.size shouldBe 1
        val result = results[0]
        result.docType shouldBe docType
        result.docItems.size shouldBe clearingRequest.docItems.size

        for (i in result.docItems.indices) {
            accounts.map { it.code }.contains(result.docItems[i].accountCode) shouldBe  true
        }


        val clearingDocId = result.docId
        val clearingDocItems = result.docItems.filter { it.accountCode in clearingAccountCodes }
        val clearingDocItemIds = clearingDocItems.map { it.docItemId }
        val refDocItems = candidates.map {it.docItems}.flatten().filter { it.accountCode in clearingAccountCodes }
        val refDocItemIds = refDocItems.map { it.docItemId }


        val pc_relations = persistenceService.findDocumentItemRelations(listOf(), refDocItemIds, listOf(RelationType.PARTIAL_CLEARING))
        val pcDocItemIds = pc_relations.map { it.docItemId }.filter { !clearingDocItemIds.contains(it) }
        val pcDocItems = persistenceService.findDocumentItems(pcDocItemIds)
        val pcDocItemResults = documentSupportService.createDocumentItemResult(context, pcDocItems, emptyList())

        for ( clearingDocItem in clearingDocItems ) {
            val expectedClearedDocItem = refDocItems.first { it.accountCode == clearingDocItem.accountCode }

            val refDocItem = refDocItems.first { it.accountCode == clearingDocItem.accountCode }
            val refDocItemId = refDocItem.docItemId

            val expectedRelationType:RelationType = documentRelationService.getClearingRelationType(context, listOf(refDocItem), listOf(clearingDocItem), pcDocItemResults)
            logger.debug("Expected RelationType: $expectedRelationType, by refDocItem: $refDocItem, clearingDocItem: $clearingDocItem")

            val relations = persistenceService.findDocumentRelations(listOf(clearingDocId), listOf(), listOf(expectedRelationType))
            relations.size shouldBeGreaterThanOrEqual 1
            val docItemRelations = persistenceService.findDocumentItemRelations(listOf(), listOf(expectedClearedDocItem.docItemId), listOf(expectedRelationType))
            docItemRelations.size shouldBeGreaterThanOrEqual 1

            expectedClearedDocItem.docItemId shouldBeIn docItemRelations.map { it.refDocItemId }
            expectedClearedDocItem.amount shouldBeIn docItemRelations.map { it.refAmount }

            clearingDocItem.docItemId shouldBeIn docItemRelations.map { it.docItemId }
            clearingDocItem.amount shouldBeIn docItemRelations.map { it.amount }

            val docItemRelation = docItemRelations[0]
            val expectedAmounts = docItemRelations.map{ it.refAmount.multiply(clearingAmountRate).toScale(Constants.ACCOUNTING_SCALE) }
            docItemRelation.amount shouldBeIn expectedAmounts
        }
        return result
    }

    test("[일시불 반제] 주문접수, 입금 케이스(카드미수금), 매출인식, 선수금 대체"){
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG

            val clearingAccountCode1 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode)    // 카드미수금
            val clearingAccountCode2 = TestAccountCode.ADVANCED_FROM_CUSTOMERS.getAccountCode(companyCode)    // 선수금
            val clearingAccountCode3 = TestAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode)    // 외상매출금-일시불

            val customerId = "CC0001"
            val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
            val totalAmount = BigDecimal("1082.5").setScale(Constants.ACCOUNTING_SCALE)

            // 일시불 주문접수(결제완료) / 입금완료 케이스  (카드미수금)
            // 1) 일시불 주문접수 - templateId: CTOI001
            val template1 = TestDocumentTemplateMapping.ONETIME_PAYMENT_RECEIVED
            val documentOrigin1 = DocumentOriginRequest(
                docTemplateCode = template1.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_order_received",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.ORDER_RECEIVED,
                accountingEvent = "One Time Sales Order Received",
            )

            val result1 = posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template1,
                documentOrigin1,
                totalAmount = totalAmount,
                customerId = customerId,
                companyCode = companyCode,
                txCurrency = txCurrency)

            // 2) 입금완료 - templateId: CTOR002
            val template2 = TestDocumentTemplateMapping.ONETIME_PAYMENT_DEPOSIT
            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_payment_deposit",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "One Time Sales Deposit Received",
            )

            val result2  = clearing(context, DocumentType.ACCOUNTING_DOCUMENT, docHash = null,
                        listOf(result1), listOf(clearingAccountCode1),
                        docTemplate = template2,
                        originRequest = documentOrigin2,
                        customerId = customerId,
                        companyCode = companyCode,
                        txCurrency = txCurrency)

            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
            checkableDocItems.addAll(result1.docItems)
            checkableDocItems.addAll(result2.docItems)

            persistenceService.findDocumentItems(checkableDocItems.map { it -> it.docItemId }).forEach {
                logger.debug("DocumentItem: $it")
            }
            result2.docItems.size shouldBe 3
            val clearingItem = result2.docItems.first{ it.accountCode == clearingAccountCode1 }
            clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult1.docItems.first{ it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

//            CTOR004 - 매출인식
            val template3 = TestDocumentTemplateMapping.ONETIME_SALES_RECOGNITION
            val documentOrigin3 = DocumentOriginRequest(
                docTemplateCode = template3.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_order_received",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.INSTALLATION_COMPLETED,
                accountingEvent = "One Time Sales Order Received",
            )

            val result3 = posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT, docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template3, documentOrigin3,
                totalAmount = totalAmount,
                customerId = customerId, companyCode = companyCode, txCurrency = txCurrency)


            // CTOR006 - 선수금 대체, 선수금, 외상매출금 반제
            // 2111010 - clearingAccountCode2
            // 1117010 - clearingAccountCode3
            val template4 = TestDocumentTemplateMapping.ONETIME_ADVANCE_PAYMENT_OFFSET
            val documentOrigin4 = DocumentOriginRequest(
                docTemplateCode = template4.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_payment_deposit",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "One Time Sales Deposit Received",
            )

            val accountCodes4 = listOf(clearingAccountCode2, clearingAccountCode3)
            val result4  = clearing(context, DocumentType.ACCOUNTING_DOCUMENT,docHash = null,
                listOf(result1, result3),
                accountCodes4,
                docTemplate = template4,
                originRequest = documentOrigin4,
                customerId = customerId)

            val expectedResult41 = documentService.findByDocId(context, result1.docId)
            val expectedResult43 = documentService.findByDocId(context, result3.docId)

            result4.docItems.first { it.accountCode == clearingAccountCode2 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            result4.docItems.first { it.accountCode == clearingAccountCode3 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult41.docItems.first { it.accountCode in accountCodes4 }.docItemStatus shouldBe DocumentItemStatus.CLEARED
            expectedResult43.docItems.first { it.accountCode in accountCodes4 }.docItemStatus shouldBe DocumentItemStatus.CLEARED


            persistenceService.findDocumentItemRelations(listOf(),
                result1.docItems.filter{it.accountCode == clearingAccountCode2}.map { it.docItemId },
                listOf(RelationType.CLEARING)).size shouldBe 1
            persistenceService.findDocumentItemRelations(listOf(),
                result3.docItems.filter { it.accountCode == clearingAccountCode3 }.map { it.docItemId },
                listOf(RelationType.CLEARING)).size shouldBe 1
            persistenceService.findDocumentItemRelations(
                result4.docItems.map { it.docItemId },
                listOf(), listOf(RelationType.CLEARING)).size shouldBe 2

            persistenceService.cleanup(context, listOf(result1.docId, result2.docId, result3.docId, result4.docId))
        }
    }


    test("[운용리스 반제] 청구, 수납, 입금 케이스"){
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG
            val customerId = "CC0002"

            val clearingAccountCode1 = TestAccountCode.ACCOUNT_RECEIVABLE_RENTAL.getAccountCode(companyCode) // 렌탈미수금-렌탈료
            val clearingAccountCode2 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) // 카드미수금.

            // 1) 운용리스 청구 - templateId: COBP001
            val template1 = TestDocumentTemplateMapping.OPERATING_LEASE_CUSTOMER_BILLING
            val docType1 = DocumentType.CUSTOMER_INVOICE

            val documentOrigin1 = DocumentOriginRequest(
                docTemplateCode = template1.templateCode,
                bizSystem = BizSystemType.OPERATING_LEASE,
                bizTxId = "operating_lease_customer_invoice_issued",
                bizProcess = BizProcessType.PAYMENT,
                bizEvent = BizEventType.PAYMENT_BILLING,
                accountingEvent = "Operating Lease Customer Invoice Issued",
            )

            val result1 = posting( context,
                documentService,
                docType1,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template1,
                documentOrigin1,
                customerId = customerId,
                companyCode = companyCode)

            // 2) 수납완료 - templateId: COBP002
            val template2 = TestDocumentTemplateMapping.OPERATING_LEASE_PAYMENT_RECEIVED
            val docType2 = DocumentType.CUSTOMER_PAYMENT

            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = BizSystemType.OPERATING_LEASE,
                bizTxId = "operating_lease_online_payment_received",
                bizProcess = BizProcessType.PAYMENT,
                bizEvent = BizEventType.PAYMENT_RECEIVED,
                accountingEvent = "Operating Lease Online Payment Received",
            )
            val result2 = clearing(context,
                docType2,
                docHash = null,
                listOf(result1),
                listOf(clearingAccountCode1),
                docTemplate = template2,
                originRequest = documentOrigin2,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            result2.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult1.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED



            // 3) 입금완료 - templateId: COBP003
            val template3 = TestDocumentTemplateMapping.OPERATING_LEASE_DEPOSIT_RECEIVED
            val docType3 = DocumentType.CUSTOMER_DOCUMENT
            val documentOrigin3 = DocumentOriginRequest(
                docTemplateCode = template3.templateCode,
                bizSystem = BizSystemType.OPERATING_LEASE,
                bizTxId = "operating_lease_deposit",
                bizProcess = BizProcessType.PAYMENT,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "Operating Lease Deposit",
            )
            val result3 = clearing(
                context,
                docType3,
                docHash = null,
                listOf(result2),
                listOf(clearingAccountCode2),
                docTemplate = template3,
                originRequest = documentOrigin3,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult2 = documentService.findByDocId(context, result2.docId)


            result3.docItems.first { it.accountCode == clearingAccountCode2 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult2.docItems.first { it.accountCode == clearingAccountCode2 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

            persistenceService.cleanup(context, listOf(result1.docId, result2.docId, result3.docId))
        }
    }

    test("[금융리스 반제] 청구, 수납, 입금 케이스"){
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG
            val customerId = "CC0003"

            val clearingAccountCode1 = TestAccountCode.FINANCE_LEASE_RECEIVABLE_BILLING.getAccountCode(companyCode) // 금융리스채권-월렌탈료
            val clearingAccountCode2 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) // 카드미수금

            val bizSystem = BizSystemType.FINANCIAL_LEASE
            val bizProcess = BizProcessType.PAYMENT

            // 1) 금융리스 청구 - templateId: CFCP001
            val template1 = TestDocumentTemplateMapping.FINANCIAL_LEASE_CUSTOMER_BILLING
            val docType1 = DocumentType.CUSTOMER_INVOICE
            val documentOrigin1 = DocumentOriginRequest(
                docTemplateCode = template1.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_invoice_issued",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_BILLING,
                accountingEvent = "Operating Lease Customer Invoice Issued",
            )
            val result1 = posting( context,
                documentService,
                docType1,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template1,
                documentOrigin1,
                customerId = customerId,
                companyCode = companyCode)

            // 2) 수납완료 - templateId: CFCP002
            val template2 = TestDocumentTemplateMapping.FINANCIAL_LEASE_PAYMENT_RECEIVED
            val docType2 = DocumentType.CUSTOMER_PAYMENT

            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_payment_received",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_RECEIVED,
                accountingEvent = "Operating Lease Payment Received",
            )
            val result2 = clearing(context,
                docType2,
                docHash = null,
                listOf(result1),
                listOf(clearingAccountCode1),
                docTemplate = template2,
                originRequest = documentOrigin2,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            result2.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult1.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED


            // 3) 입금완료 - templateId: CFCP003
            val template3 = TestDocumentTemplateMapping.FINANCIAL_LEASE_DEPOSIT_RECEIVED
            val docType3 = DocumentType.CUSTOMER_DOCUMENT
            val documentOrigin3 = DocumentOriginRequest(
                docTemplateCode = template3.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_deposit",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "Operating Lease Deposit",
            )
            val result3 = clearing(context,
                docType2,
                docHash = null,
                listOf(result2),
                listOf(clearingAccountCode2),
                docTemplate = template3,
                originRequest = documentOrigin3,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult2 = documentService.findByDocId(context, result2.docId)

            result3.docItems.first { it.accountCode == clearingAccountCode2 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult2.docItems.first { it.accountCode == clearingAccountCode2 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

            persistenceService.cleanup(context, listOf(result1.docId, result2.docId, result3.docId))
        }
    }


    test("[부분반제] 케이스"){
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG
            val customerId = "CC0004"
            val clearingAccountCode1 = TestAccountCode.FINANCE_LEASE_RECEIVABLE_BILLING.getAccountCode(companyCode) // 금융리스채권-월렌탈료


            val bizSystem = BizSystemType.OPERATING_LEASE
            val bizProcess = BizProcessType.PAYMENT

            // 일시불 주문접수(결제완료) / 입금완료 케이스  (카드미수금)
            // 1) 운용리스 청구 - templateId: CFCP001
            val template1 = TestDocumentTemplateMapping.FINANCIAL_LEASE_CUSTOMER_BILLING
            val docType1 = DocumentType.CUSTOMER_INVOICE
            val documentOrigin1 = DocumentOriginRequest(
                docTemplateCode = template1.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_invoice_issued",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_BILLING,
                accountingEvent = "Operating Lease Customer Invoice Issued",
            )
            val result1 = posting( context,
                documentService,
                docType1,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template1,
                documentOrigin1,
                customerId = customerId,
                companyCode = companyCode)

            // 2) 수납완료 - templateId: CFCP002
            val template2 = TestDocumentTemplateMapping.FINANCIAL_LEASE_PAYMENT_RECEIVED
            val docType2 = DocumentType.CUSTOMER_PAYMENT
            val clearingAmountRate2 = BigDecimal("0.6")
            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_payment_received",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_RECEIVED,
                accountingEvent = "Operating Lease Payment Received",
            )
            val result2 = clearing(context,
                docType2,
                docHash = null,
                listOf(result1),
                listOf(clearingAccountCode1),
                clearingAmountRate2,
                docTemplate = template2,
                originRequest = documentOrigin2,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            persistenceService.findDocumentRelations(listOf(result1.docId), listOf(), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 0
            persistenceService.findDocumentRelations(listOf(), listOf(result1.docId), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1
            persistenceService.findDocumentRelations(listOf(result2.docId), listOf(), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1
            persistenceService.findDocumentRelations(listOf(), listOf(result2.docId), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 0
            val refDocItemIds = result1.docItems.filter { it.accountCode == clearingAccountCode1 }.map { it.docItemId }
            val pcItemIds = result2.docItems.filter { it.accountCode == clearingAccountCode1 }.map { it.docItemId }
            persistenceService.findDocumentItemRelations(listOf(), refDocItemIds, listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1
            persistenceService.findDocumentItemRelations(pcItemIds, listOf(), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1

            result2.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.PARTIAL
            expectedResult1.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.NORMAL


            val template3 = TestDocumentTemplateMapping.FINANCIAL_LEASE_PAYMENT_RECEIVED
            val docType3 = DocumentType.CUSTOMER_PAYMENT
            val clearingAmountRate3 = BigDecimal("0.4")
            val documentOrigin3 = DocumentOriginRequest(
                docTemplateCode = template3.templateCode,
                bizSystem = bizSystem,
                bizTxId = "financial_lease_payment_received",
                bizProcess = bizProcess,
                bizEvent = BizEventType.PAYMENT_RECEIVED,
                accountingEvent = "Operating Lease Payment Received",
            )
            val result3 = clearing(context,
                docType3,
                docHash = null,
                listOf(result1),
                listOf(clearingAccountCode1),
                clearingAmountRate3,
                docTemplate = template3,
                originRequest = documentOrigin3,
                customerId = customerId,
                companyCode = companyCode)
            val expectedResult2 = documentService.findByDocId(context, result2.docId)
            val expectedResult11 = documentService.findByDocId(context, result1.docId)

            persistenceService.findDocumentRelations(listOf(result1.docId), listOf(), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 0
            persistenceService.findDocumentRelations(listOf(), listOf(result1.docId), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1
            persistenceService.findDocumentRelations(listOf(result3.docId), listOf(), listOf(RelationType.CLEARING)).size shouldBe 1
            persistenceService.findDocumentRelations(listOf(), listOf(result3.docId), listOf(RelationType.CLEARING)).size shouldBe 0
            val clearingCompleteItemIds = result3.docItems.filter { it.accountCode == clearingAccountCode1 }.map { it.docItemId }
            persistenceService.findDocumentItemRelations(listOf(), refDocItemIds, listOf(RelationType.PARTIAL_CLEARING, RelationType.CLEARING)).size shouldBe 2
            persistenceService.findDocumentItemRelations(pcItemIds, listOf(), listOf(RelationType.PARTIAL_CLEARING)).size shouldBe 1
            persistenceService.findDocumentItemRelations(clearingCompleteItemIds, listOf(), listOf(RelationType.CLEARING)).size shouldBe 1

            result3.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult2.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.PARTIAL
            expectedResult11.docItems.first { it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED


            persistenceService.cleanup(context, listOf(result1.docId, result2.docId, result3.docId))
        }
    }

    test("[반제] 반제항목 조회 테스트"){
        timeLogger.measureAndLog {
            val context = DocumentServiceContext.SAVE_DEBUG
            val clearingAccountCode11 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode)    // 카드미수금
            val clearingAccountCode12 = TestAccountCode.ADVANCED_FROM_CUSTOMERS.getAccountCode(companyCode)    // 선수금
            val clearingAccountCode13 = TestAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode)    // 외상매출금-일시불

            val customerId = "CC0005"
            val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
            val totalAmount = BigDecimal("1082.5").setScale(Constants.ACCOUNTING_SCALE)

            val template11 = TestDocumentTemplateMapping.ONETIME_PAYMENT_RECEIVED
            val docType11 = DocumentType.ACCOUNTING_DOCUMENT
            // 일시불 주문접수(결제완료) / 입금완료 케이스  (카드미수금)
            // 1) 일시불 주문접수 - templateId: CTOI001
            val documentOrigin11 = DocumentOriginRequest(
                docTemplateCode = template11.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_order_received",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.ORDER_RECEIVED,
                accountingEvent = "One Time Sales Order Received",
            )

            val result11 = posting(context,
                documentService,
                docType11,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template11, documentOrigin11,
                totalAmount = totalAmount,
                customerId = customerId,
                companyCode = companyCode,
                txCurrency = txCurrency)


            // 1) 운용리스 청구 - templateId: COBP001
            val template21 = TestDocumentTemplateMapping.OPERATING_LEASE_CUSTOMER_BILLING
            val docType21 = DocumentType.CUSTOMER_INVOICE

            val documentOrigin21 = DocumentOriginRequest(
                docTemplateCode = template21.templateCode,
                bizSystem = BizSystemType.OPERATING_LEASE,
                bizTxId = "operating_lease_customer_invoice_issued",
                bizProcess = BizProcessType.PAYMENT,
                bizEvent = BizEventType.PAYMENT_BILLING,
                accountingEvent = "Operating Lease Customer Invoice Issued",
            )
            val result21 = posting( context,
                documentService,
                docType21,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template21,
                documentOrigin21,
                customerId = customerId,
                companyCode = companyCode)


            // 1) 금융리스 청구 - templateId: CFCP001
            val template31 = TestDocumentTemplateMapping.FINANCIAL_LEASE_CUSTOMER_BILLING
            val docType31 = DocumentType.CUSTOMER_INVOICE
            val documentOrigin31 = DocumentOriginRequest(
                docTemplateCode = template31.templateCode,
                bizSystem = BizSystemType.FINANCIAL_LEASE,
                bizTxId = "financial_lease_invoice_issued",
                bizProcess = BizProcessType.PAYMENT,
                bizEvent = BizEventType.PAYMENT_BILLING,
                accountingEvent = "Operating Lease Customer Invoice Issued",
            )
            val result31 = posting( context,
                documentService,
                docType31,
                docHash = null,
                documentDate = LocalDate.now(),
                postingDate = LocalDate.now(),
                template31,
                documentOrigin31,
                customerId = customerId,
                companyCode = companyCode)


            // 반제 항목 조회 및 계산
            run {
                // 반제 관련 accountSide 조회
                val accounts = template11.getAccountInfos(companyCode)
                val accountSide11 = accounts.firstOrNull{ it.code == clearingAccountCode11 }?.accountSide
                accountSide11 shouldBe AccountSide.DEBIT

                val orderItemId = result11.docItems.first { it.accountCode == clearingAccountCode11 }
                    .attributes.firstOrNull{ it.type == DocumentAttributeType.ORDER_ITEM_ID }?.value

                val request11 = LookupRefDocItemRequest(
                    docType = docType11,
                    companyCode = companyCode,
                    docTemplateCode = template11.templateCode,
                    accountCode = clearingAccountCode11,
                    accountSide = accountSide11!!,
                    customerId =  customerId,
                    vendorId = null,
                    orderItemId = orderItemId
                )

                val refDocItemIds11 = documentService.lookupRefDocItems(context, listOf(request11))
                refDocItemIds11.size shouldBe 1
                refDocItemIds11.map{it.docItemId}.contains(result11.docItems.first { it.accountCode == clearingAccountCode11 }.docItemId) shouldBe true

                val template12 = TestDocumentTemplateMapping.ONETIME_PAYMENT_DEPOSIT

                val docTemplate = documentTemplateService.findDocTemplate(companyCode, template12.templateCode)

                val docTemplateItems = documentTemplateService.findDocTemplateItems(companyCode, template12.templateCode)
                docTemplateItems.filter { it.accountCode == clearingAccountCode11 }.size shouldBe 1
                docTemplateItems.filter { it.refDocTemplateCode == template11.templateCode }.size shouldBe 1
                val docTemplateMapping = TestDocumentTemplateMapping.findByTemplateKey(docTemplate.docTemplateKey)

                val documentOrigin12 = DocumentOriginRequest(
                    docTemplateCode = template12.templateCode,
                    bizSystem = BizSystemType.ONETIME,
                    bizTxId = "one_time_sales_payment_deposit",
                    bizProcess = BizProcessType.ORDER,
                    bizEvent = BizEventType.PAYMENT_DEPOSIT,
                    accountingEvent = "One Time Sales Deposit Received",
                )


                val originClearingRequest = ClearingDocumentRequestFixture.generateByTemplate (
                    docTemplate.documentType, docHash = null, listOf(result11),
                    listOf(clearingAccountCode11),
                    BigDecimal.ONE,
                    TestDocumentTemplateMapping.findByTemplateKey(docTemplate.docTemplateKey),
                    documentOrigin12,
                    customerId = customerId,
                    vendorId = null)

                originClearingRequest.refDocItemIds shouldBe refDocItemIds11.map { it.docItemId }
                val modifiedClearingRequest = originClearingRequest.copy(refDocItemIds11.map { it.docItemId })
                val findAccounts = docTemplateMapping.getAccountInfos(companyCode)
                modifiedClearingRequest.docItems.size shouldBe findAccounts.size
                // clearing 호출
                val results = documentService.clearing(context, listOf(modifiedClearingRequest))
                val result12 = results.first()

                val expectedResult1 = documentService.findByDocId(context, result11.docId)

                val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
                checkableDocItems.addAll(result11.docItems)
                checkableDocItems.addAll(result12.docItems)

                persistenceService.findDocumentItems(checkableDocItems.map { it -> it.docItemId }).forEach {
                    logger.debug("DocumentItem: {}", it)
                }
                result12.docItems.size shouldBe 3
                val clearingItem = result12.docItems.first{ it.accountCode == clearingAccountCode11 }
                clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
                expectedResult1.docItems.first{ it.accountCode == clearingAccountCode11 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

                persistenceService.cleanup(context, listOf(result12.docId))
            }

            run {
                val clearingAccountCode21 = TestAccountCode.ACCOUNT_RECEIVABLE_RENTAL.getAccountCode(companyCode) // 렌탈미수금-렌탈료
                val accounts21 = template21.getAccountInfos(companyCode)
                val accountSide21 = accounts21.firstOrNull{ it.code == clearingAccountCode21 }?.accountSide
                accountSide21 shouldBe AccountSide.DEBIT

                val orderItemId = result21.docItems.first { it.accountCode == clearingAccountCode21 }
                    .attributes.firstOrNull{ it.type == DocumentAttributeType.ORDER_ITEM_ID }?.value

                val request21 = LookupRefDocItemRequest(
                    docType = docType21,
                    companyCode = companyCode,
                    docTemplateCode = template21.templateCode,
                    accountCode = clearingAccountCode21,
                    accountSide = accountSide21!!,
                    customerId =  customerId,
                    vendorId = null,
                    orderItemId = orderItemId
                )

                val refDocItemIds21 = documentService.lookupRefDocItems(context, listOf(request21))
                refDocItemIds21.size shouldBe 1
                refDocItemIds21.map{it.docItemId}.contains(result21.docItems.first { it.accountCode == clearingAccountCode21 }.docItemId) shouldBe true
            }

            run {
                val clearingAccountCode31 = TestAccountCode.FINANCE_LEASE_RECEIVABLE_BILLING.getAccountCode(companyCode) // 금융리스채권-월렌탈료
                val accounts31 = template31.getAccountInfos(companyCode)
                val accountSide31 = accounts31.firstOrNull{ it.code == clearingAccountCode31 }?.accountSide
                accountSide31 shouldBe AccountSide.DEBIT

                val orderItemId = result31.docItems.first { it.accountCode == clearingAccountCode31 }
                    .attributes.firstOrNull{ it.type == DocumentAttributeType.ORDER_ITEM_ID }?.value

                val request31 = LookupRefDocItemRequest(
                    docType = docType31,
                    companyCode = companyCode,
                    docTemplateCode = template31.templateCode,
                    accountCode = clearingAccountCode31,
                    accountSide = accountSide31!!,
                    customerId =  customerId,
                    vendorId = null,
                    orderItemId = orderItemId
                )
                val refDocItemIds31 = documentService.lookupRefDocItems(context, listOf(request31))
                refDocItemIds31.size shouldBe 1
                val refDocItemId = result31.docItems.first { it.accountCode == clearingAccountCode31 }.docItemId
                refDocItemIds31.map { it.docItemId }.contains(refDocItemId) shouldBe true
            }

            run {   // 고객을 다르게 조회
                val clearingAccountCode31 = TestAccountCode.FINANCE_LEASE_RECEIVABLE_BILLING.getAccountCode(companyCode) // 금융리스채권-월렌탈료
                val accounts31 = template31.getAccountInfos(companyCode)
                val accountSide31 = accounts31.firstOrNull{ it.code == clearingAccountCode31 }?.accountSide
                accountSide31 shouldBe AccountSide.DEBIT

                val request31 = LookupRefDocItemRequest(
                    docType = docType31,
                    companyCode = companyCode,
                    docTemplateCode = template31.templateCode,
                    accountCode = clearingAccountCode31,
                    accountSide = accountSide31!!,
                    customerId =  "123",
                    vendorId = null
                )
                val refDocItemIds31 = documentService.lookupRefDocItems(context, listOf(request31))
                refDocItemIds31.size shouldBe 0
            }
            persistenceService.cleanup(context, listOf(result11.docId, result21.docId, result31.docId))
        }
    }

    test("[반제] 반제 중복 호출 테스트"){
        val context = DocumentServiceContext.SAVE_DEBUG

        val clearingAccountCode11 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode)    // 카드미수금
        val clearingAccountCode12 = TestAccountCode.ADVANCED_FROM_CUSTOMERS.getAccountCode(companyCode)    // 선수금
        val clearingAccountCode13 = TestAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode)    // 외상매출금-일시불

        val customerId = "CC0002"
        val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
        val totalAmount = BigDecimal("1082.5").setScale(Constants.ACCOUNTING_SCALE)

        // 일시불 주문접수(결제완료) / 입금완료 케이스  (카드미수금)
        // 1) 일시불 주문접수 - templateId: CTOI001
        val template1 = TestDocumentTemplateMapping.ONETIME_PAYMENT_RECEIVED
        val documentOrigin1 = DocumentOriginRequest(
            docTemplateCode = template1.templateCode,
            bizSystem = BizSystemType.ONETIME,
            bizTxId = "one_time_sales_order_received",
            bizProcess = BizProcessType.ORDER,
            bizEvent = BizEventType.ORDER_RECEIVED,
            accountingEvent = "One Time Sales Order Received",
        )

        val result1 = posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT, docHash = null,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            template1, documentOrigin1,
            totalAmount = totalAmount,
            customerId = customerId, companyCode = companyCode, txCurrency = txCurrency)

        // 2) 입금완료 - templateId: CTOR002
        var docHash:String? = null
        val result22:DocumentResult
        run {
            val template2 = TestDocumentTemplateMapping.ONETIME_PAYMENT_DEPOSIT
            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_payment_deposit",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "One Time Sales Deposit Received"
            )

            val result2  = clearing(context, DocumentType.ACCOUNTING_DOCUMENT,
                docHash = null, listOf(result1), listOf(clearingAccountCode11),
                docTemplate = template2,
                originRequest = documentOrigin2,
                customerId = customerId,
                companyCode = companyCode,
                txCurrency = txCurrency)

            docHash = result2.docHash
            result22 = result2

            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
            checkableDocItems.addAll(result1.docItems)
            checkableDocItems.addAll(result2.docItems)

            persistenceService.findDocumentItems(checkableDocItems.map { it -> it.docItemId }).forEach {
                logger.debug("DocumentItem: {}", it)
            }
            result2.docItems.size shouldBe 3
            val clearingItem = result2.docItems.first{ it.accountCode == clearingAccountCode11 }
            clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult1.docItems.first{ it.accountCode == clearingAccountCode11 }.docItemStatus shouldBe DocumentItemStatus.CLEARED
        }

        // 2회 호출
        run {
            val template2 = TestDocumentTemplateMapping.ONETIME_PAYMENT_DEPOSIT
            val documentOrigin2 = DocumentOriginRequest(
                docTemplateCode = template2.templateCode,
                bizSystem = BizSystemType.ONETIME,
                bizTxId = "one_time_sales_payment_deposit",
                bizProcess = BizProcessType.ORDER,
                bizEvent = BizEventType.PAYMENT_DEPOSIT,
                accountingEvent = "One Time Sales Deposit Received"
            )

            val result2  = clearing(context, DocumentType.ACCOUNTING_DOCUMENT, docHash = docHash,
                listOf(result1), listOf(clearingAccountCode11),
                docTemplate = template2,
                originRequest = documentOrigin2,
                customerId = customerId,
                companyCode = companyCode,
                txCurrency = txCurrency)

            val expectedResult1 = documentService.findByDocId(context, result1.docId)

            val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
            checkableDocItems.addAll(result1.docItems)
            checkableDocItems.addAll(result2.docItems)

            persistenceService.findDocumentItems(checkableDocItems.map { it -> it.docItemId }).forEach {
                logger.debug("DocumentItem: {}", it)
            }
            result2.docItems.size shouldBe 3
            val clearingItem = result2.docItems.first{ it.accountCode == clearingAccountCode11 }
            clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
            expectedResult1.docItems.first{ it.accountCode == clearingAccountCode11 }.docItemStatus shouldBe DocumentItemStatus.CLEARED


            val expectedLineNumber = 1
            val expectedDocItem = result22.docItems.first{ it.lineNumber == expectedLineNumber}
            val actualDocItem = result2.docItems.first{ it.lineNumber == expectedLineNumber}

            actualDocItem.docItemId shouldBe expectedDocItem.docItemId
            actualDocItem.createTime.isEqual(expectedDocItem.createTime) shouldBe true
            actualDocItem.updateTime.isAfter(expectedDocItem.updateTime) shouldBe true

            persistenceService.cleanup(context, listOf(result1.docId, result22.docId, result2.docId))
        }
    }

}){
    companion object{
        val logger = LoggerFactory.getLogger(this::class.java)

        fun posting(context: DocumentServiceContext = DocumentServiceContext.ONLY_DEBUG,
                    documentService: DocumentService,
                    docType:DocumentType,
                    docHash: String?=null,
                    documentDate: LocalDate?=null,
                    postingDate:LocalDate?=null,
                    docTemplate:TestDocumentTemplateMapping,
                    originRequest: DocumentOriginRequest,
                    totalAmount:BigDecimal? = null,
                    customerId: String?=null,
                    vendorId: String?=null,
                    companyCode: CompanyCode?=null,
                    txCurrency:String?=null,
                    orderItemId:String?=null): DocumentResult {

            val request = CreateDocumentRequestFixture.generateByTemplate(
                docType,
                docTemplate,
                originRequest,
                documentDate = documentDate,
                postingDate = postingDate,
                totalAmount = totalAmount,
                docHash = docHash,
                customerId = customerId,
                vendorId = vendorId,
                companyCode = companyCode,
                txCurrency = txCurrency,
                orderItemId = orderItemId)
            val results = documentService.posting(context, listOf( request))
            logger.debug("Posting results: {}", results)
            results.size shouldBe 1

            val accounts = docTemplate.getAccountInfos(request.companyCode)
            results.forEach { result ->
                result.docType shouldBe docType
                result.docItems.size shouldBe accounts.size
                for (i in accounts.indices) {
                    result.docItems[i].accountCode shouldBe accounts[i].code
                }
            }
            return results.first()
        }

    }
}

