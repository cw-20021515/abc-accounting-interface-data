package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.fixtures.TestAccountCode
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.DocumentItemResult
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentAutoClearingServiceTests  (
    private val persistenceService: DocumentPersistenceService,
    private val documentService: DocumentService,
    private val documentAutoClearingService: DocumentAutoClearingService,
    private val companyServiceable: CompanyServiceable
): FunSpec({
    val logger = LoggerFactory.getLogger(this::class.java)
    val companyCode = CompanyCode.T200


    test("PostClearing 테스트") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val clearingAccountCode1 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode)    // 카드미수금
        val clearingAccountCode2 = TestAccountCode.ADVANCED_FROM_CUSTOMERS.getAccountCode(companyCode)    // 선수금
        val clearingAccountCode3 = TestAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode)    // 외상매출금-일시불

        val customerId = "AC0001"
        val txCurrency = companyServiceable.getCompanyCurrency(companyCode)
        val orderItemId = "order_item_id_1"
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

        val result1 = BasicDocumentClearingTests.posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
            docHash = null,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            template1, documentOrigin1,
            totalAmount = totalAmount,
            customerId = customerId,
            companyCode = companyCode,
            txCurrency = txCurrency.name,
            orderItemId = orderItemId)

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

        val result2  = BasicDocumentClearingTests.posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
            docHash = null, docTemplate = template2, originRequest = documentOrigin2,
            totalAmount = totalAmount,
            customerId = customerId,
            companyCode = companyCode,
            txCurrency = txCurrency.name,
            orderItemId = orderItemId)

        result1.docItems.forEach { item ->
            item.attributes.first { it.type== DocumentAttributeType.ORDER_ITEM_ID }.value shouldBe orderItemId
        }
        result2.docItems.forEach { item ->
            item.attributes.first { it.type== DocumentAttributeType.ORDER_ITEM_ID }.value shouldBe orderItemId
        }

        val results = documentAutoClearingService.postClearing(context, listOf(result1, result2))
        logger.debug("results size: ${results.size}")
        results.forEach {
            logger.debug("result: {}", it)
        }
        results.size shouldBe 1

        val actualResult = results.first()

        val expectedResult1 = documentService.findByDocId(context, result1.docId)

        val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
        checkableDocItems.addAll(result1.docItems)
        checkableDocItems.addAll(result2.docItems)

        actualResult.docItems.size shouldBe 3
        val clearingItem = actualResult.docItems.first{ it.accountCode == clearingAccountCode1 }
        clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
        expectedResult1.docItems.first{ it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

        persistenceService.cleanup(context, listOf(result1.docId, result2.docId))
    }

    test("자동반제 테스트") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val clearingAccountCode1 = TestAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode)    // 카드미수금
        val clearingAccountCode2 = TestAccountCode.ADVANCED_FROM_CUSTOMERS.getAccountCode(companyCode)    // 선수금
        val clearingAccountCode3 = TestAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode)    // 외상매출금-일시불

        val customerId = "AC0001"
        val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
        val orderItemId = "order_item_id_1"
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

        val result1 = BasicDocumentClearingTests.posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
            docHash = null,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            template1, documentOrigin1,
            totalAmount = totalAmount,
            customerId = customerId,
            companyCode = companyCode,
            txCurrency = txCurrency,
            orderItemId = orderItemId)

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

        val result2  = BasicDocumentClearingTests.posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
            docHash = null, docTemplate = template2, originRequest = documentOrigin2,
            totalAmount = totalAmount,
            customerId = customerId,
            companyCode = companyCode,
            txCurrency = txCurrency,
            orderItemId = orderItemId)

        result1.docItems.forEach { item ->
            item.attributes.first { it.type== DocumentAttributeType.ORDER_ITEM_ID }.value shouldBe orderItemId
        }
        result2.docItems.forEach { item ->
            item.attributes.first { it.type== DocumentAttributeType.ORDER_ITEM_ID }.value shouldBe orderItemId
        }

        val startTime = result1.createTime.minusDays(1)
        val results = documentAutoClearingService.processAutoClearing(context, companyCode, startTime)
        logger.debug("results size: ${results.size}")
        results.forEach {
            logger.debug("result: $it")
        }

        results.size shouldBe 1

        val actualResult = results.first()

        val expectedResult1 = documentService.findByDocId(context, result1.docId)

        val checkableDocItems:MutableList<DocumentItemResult> = mutableListOf()
        checkableDocItems.addAll(result1.docItems)
        checkableDocItems.addAll(result2.docItems)

        actualResult.docItems.size shouldBe 3
        val clearingItem = actualResult.docItems.first{ it.accountCode == clearingAccountCode1 }
        clearingItem.docItemStatus shouldBe DocumentItemStatus.CLEARING
        expectedResult1.docItems.first{ it.accountCode == clearingAccountCode1 }.docItemStatus shouldBe DocumentItemStatus.CLEARED

        persistenceService.cleanup(context, listOf(result1.docId, result2.docId))
    }

})
