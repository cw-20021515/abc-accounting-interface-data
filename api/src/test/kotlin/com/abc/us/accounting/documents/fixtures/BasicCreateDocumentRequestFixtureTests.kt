package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.abc.us.accounting.documents.service.DocumentMasterServiceable
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.bigdecimal.shouldBeLessThan
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicCreateDocumentRequestFixtureTests(
    private val timeLogger: TimeLogger = TimeLogger(),
    private var accountService: AccountServiceable,
    private val companyServiceable: CompanyServiceable,
    private var documentMasterService: DocumentMasterServiceable,
) : AnnotationSpec(){

    val companyCode = Constants.TEST_COMPANY_CODE

    @Test
    fun `single document generation test`() {
        val context = DocumentServiceContext()

        val txCurrency = companyServiceable.getCompanyCurrency(companyCode)

        val request = CreateDocumentRequestFixture.generate(
            docType = DocumentType.ACCOUNTING_DOCUMENT,
            docStatus = DocumentStatus.REVERSAL,
            workflowStatus = WorkflowStatus.INITIAL,
            companyCode = companyCode)
        logger.info("CreateDocumentRequest:$request")

        val items1 = CreateDocumentItemRequestFixture.generates(1, companyCode = companyCode, accountSide = AccountSide.CREDIT, txAmount = BigDecimal(100.0))
        val items2 = CreateDocumentItemRequestFixture.generates(1, companyCode = companyCode, accountSide = AccountSide.DEBIT, txAmount = BigDecimal(100.0))
        val items = items1 + items2
        for (item in items) {
            logger.info("CreateDocumentItemRequest:$item")
            val accountType = accountService.getAccount(item.toAccountKey()).accountType
            val extras = CreateDocumentItemAttributeRequestFixture.generates(3, accountType)
            item.attributes.addAll(extras)
        }
        request.docItems.addAll(items)

        request.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
        request.documentDate shouldBe LocalDate.now()
        request.postingDate shouldBe LocalDate.now()
        request.companyCode shouldBe companyCode
        request.txCurrency shouldBe txCurrency.name

        request.docOrigin shouldBe null
        request.docItems.size shouldBeInRange 0..5

        for (documentItem in request.docItems) {
            logger.info("documentItem:$documentItem")
            documentItem.companyCode shouldBeIn CompanyCode.entries
            documentItem.accountCode shouldBeIn DocumentItemFixture.accountCodeList(companyCode)
            documentItem.txCurrency shouldBeIn DocumentItemFixture.currencies
            documentItem.txAmount shouldBeLessThan BigDecimal(1000.0)

            documentItem.attributes.size shouldBeInRange 0..5
        }
    }

    @Test
    fun `multiple create document request generation test`() {

        val size = 10
        timeLogger.measureAndLog {
            val requests = CreateDocumentRequestFixture.generates(
                size,
                docType = DocumentType.ACCOUNTING_DOCUMENT,
                documentDate = LocalDate.now().minusDays(10),
                docStatus = DocumentStatus.INITIAL,
                workflowStatus = WorkflowStatus.INITIAL,
                companyCode = companyCode
            )

            for (request in requests) {
                logger.info("request:$request")
                request.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                request.documentDate shouldBe LocalDate.now().minusDays(10)
                request.postingDate shouldBeAfter DocumentFixture.START_DATE
                request.postingDate shouldBeBefore DocumentFixture.END_DATE

                request.companyCode shouldBe companyCode
                request.txCurrency shouldBe CurrencyCode.USD.name
                request.docOrigin shouldBe null
            }
        }

        logger.info("next document generation=====================")
        timeLogger.measureAndLog {
            val requests = CreateDocumentRequestFixture.generates(10)
            for (request in requests) {
                logger.info("request:$request")
                request.documentDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                request.documentDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)
                request.postingDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                request.postingDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)

                request.docOrigin shouldBe null
            }
        }
    }

    @Test
    fun `multiple create document item request generation test`() {

        val items = CreateDocumentItemRequestFixture.generates(10,
            companyCode = companyCode,
            accountSide = AccountSide.CREDIT,
            txCurrency = CurrencyCode.USD.name,
        )
        for (item in items) {
            logger.info("item:$item")
            item.accountCode shouldBeIn DocumentItemFixture.accountCodeList(companyCode)
            item.accountSide shouldBe AccountSide.CREDIT
            item.txCurrency shouldBeIn DocumentItemFixture.currencies
            item.txAmount shouldBeLessThan BigDecimal(1000.0)
        }
    }

    @Test
    fun `document create document item extra request generation test`() {
        val attributes = CreateDocumentItemAttributeRequestFixture.generates(5, AccountType.entries.random())
        for (attribute in attributes) {
            logger.info("attribute:$attribute")
            attribute.attributeType shouldBeIn DocumentAttributeType.entries
        }
    }

    @Test
    fun`document create document request by template id`() {
        val template = TestDocumentTemplateMapping.ONETIME_RETURN_PAYMENT_RECEIVED
        var requests = CreateDocumentRequestFixture.generateByTemplateList(5, companyCode=companyCode, template = template)
        val accounts = template.getAccountInfos(companyCode)
        for (request in requests) {
            logger.info("request: templateId:${request.docOrigin?.docTemplateCode}, contents:$request")
            request.docItems.size shouldBe accounts.size
            for ( item in request.docItems) {
                logger.info("item:$item")
                item.accountCode shouldBeIn accounts.map { it.code }
            }
        }

        requests = CreateDocumentRequestFixture.generateByTemplateList(5, companyCode=companyCode)

        for ( request in requests ) {
            logger.info("request: templateId:${request.docOrigin?.docTemplateCode}, contents:$request")
            request.docOrigin!!.docTemplateCode shouldNotBe null

            val templateMapping = TestDocumentTemplateMapping.findByTemplateCode(request.docOrigin!!.docTemplateCode, companyCode)
            val curAccounts = templateMapping.getAccountInfos(companyCode)
            request.docItems.size shouldBe curAccounts.size
            for ( item in request.docItems) {
                logger.info("item:$item")
                item.accountCode shouldBeIn curAccounts.map { it.code }
            }
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
