package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.bigdecimal.shouldBeLessThan
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentFixtureTests(
    private val companyServiceable: CompanyServiceable,
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec(){
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val companyCode = CompanyCode.T200

    @Test
    fun `single document generation test`() {
        val document = DocumentFixture.createDocument(
            docType = DocumentType.ACCOUNTING_DOCUMENT,
            docStatus = DocumentStatus.REVERSAL,
            workflowStatus = WorkflowStatus.INITIAL,
            companyCode = companyCode,
            isOrigin = true)
        logger.info("document:$document")

        val currencyCode = companyServiceable.getCompanyCurrency(companyCode)

        val documentItems = DocumentItemFixture.createDocumentItems(3, document = document, accountSide = AccountSide.CREDIT, companyCode = companyCode)

        document.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
        document.documentDate shouldBe LocalDate.now()
        document.postingDate shouldBe LocalDate.now()
        document.entryDate shouldBe LocalDate.now()
        document.fiscalYearMonth shouldBe companyServiceable.getCompanyFiscalYearMonth(companyCode, LocalDate.now())
        document.docStatus shouldBe DocumentStatus.REVERSAL
        document.workflowStatus shouldBe WorkflowStatus.INITIAL
        document.companyCode shouldBe companyCode
        document.txMoney.currencyCode() shouldBe currencyCode.name

        for (documentItem in documentItems) {
            logger.info("documentItem:$documentItem")
            documentItem.docId shouldBe document.id
            documentItem.lineNumber shouldBeGreaterThan 0
            documentItem.accountCode shouldBeIn DocumentItemFixture.accountCodeList(companyCode)
            documentItem.accountSide shouldBe AccountSide.CREDIT
            documentItem.txMoney.currency.currencyCode shouldBeIn DocumentItemFixture.currencies
            documentItem.txMoney.amount shouldBeLessThan BigDecimal(1000.0)
            documentItem.money.currency.currencyCode shouldBe currencyCode.name
            documentItem.money.amount shouldBeLessThan BigDecimal(1000.0)
            documentItem.createTime shouldBeBefore OffsetDateTime.now()
        }
    }

    @Test
    fun `multiple document generation test`() {

        val size = 10
        val currencyCode = companyServiceable.getCompanyCurrency(companyCode)

        timeLogger.measureAndLog {
            val documents = DocumentFixture.createDocuments(
                size,
                docType = DocumentType.ACCOUNTING_DOCUMENT,
                documentDate = LocalDate.now().minusDays(10),
                docStatus = DocumentStatus.INITIAL,
                workflowStatus = WorkflowStatus.INITIAL,
                companyCode = companyCode,
                isOrigin = true
            )

            for (document in documents) {
                logger.info("document:$document")
                document.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                document.documentDate shouldBe LocalDate.now().minusDays(10)
                document.postingDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                document.postingDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)
                document.entryDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                document.entryDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)

                document.docStatus shouldBe DocumentStatus.INITIAL
                document.workflowStatus shouldBe WorkflowStatus.INITIAL
                document.companyCode shouldBe companyCode
                document.txMoney.currencyCode() shouldBe currencyCode.name
            }
        }

        logger.info("next document generation=====================")
        timeLogger.measureAndLog {
            val documents = DocumentFixture.createDocuments(10)
            for (document in documents) {
                logger.info("document:$document")
                document.documentDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                document.documentDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)
                document.postingDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                document.postingDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)
                document.entryDate shouldBeAfter DocumentFixture.START_DATE.minusDays(1)
                document.entryDate shouldBeBefore DocumentFixture.END_DATE.plusDays(1)

//                document.fiscalYear shouldBe document.postingDate.year
//                document.fiscalMonth shouldBe document.postingDate.monthValue

//                document.docTemplateId!!.get(0) shouldBe 'C'
            }
        }
    }

    @Test
    fun `multiple document item generation test`() {

        val currencyCode = companyServiceable.getCompanyCurrency(companyCode)

        val document = DocumentFixture.createDocument(
            docType = DocumentType.ACCOUNTING_DOCUMENT,
            docStatus = DocumentStatus.REVERSAL,
            workflowStatus = WorkflowStatus.INITIAL,
            companyCode = companyCode,
            isOrigin = true
        )

        val documentItems = DocumentItemFixture.createDocumentItems(10,
            document = document,
            accountSide = AccountSide.CREDIT,
            txCurrency = document.txMoney.currencyCode(),
            companyCode = companyCode
        )
        for (documentItem in documentItems) {
            logger.info("documentItem:$documentItem")
            documentItem.docId shouldBe document.id
            documentItem.lineNumber shouldBeGreaterThan 0
            documentItem.accountCode shouldBeIn DocumentItemFixture.accountCodeList(companyCode)
            documentItem.accountSide shouldBe AccountSide.CREDIT
            documentItem.txMoney.currency.currencyCode shouldBeIn DocumentItemFixture.currencies
            documentItem.txMoney.amount shouldBeLessThan BigDecimal(1000.0)
            documentItem.money.currency.currencyCode shouldBe currencyCode.name
            documentItem.money.amount shouldBeLessThan BigDecimal(1000.0)
            documentItem.createTime shouldBeBefore OffsetDateTime.now()
        }
    }

    @Test
    fun `document item extra generation test`() {
        val documentItem = DocumentItemFixture.createDocumentItem()
        val documentItemAttributes = DocumentItemAttributeFixture.createDocumentItemAttributes(5, docItemId = documentItem.id)
        for (documentItemAttribute in documentItemAttributes) {
            logger.info("documentItemAttribute:$documentItemAttribute")
            documentItemAttribute.attributeId.docItemId shouldBe documentItem.id
            documentItemAttribute.attributeId.attributeType shouldBeIn DocumentAttributeType.entries
        }
    }

    @Test
    fun `template validation test`() {
        val mappings = TestDocumentTemplateMapping.entries
        for (mapping in mappings) {
            logger.info("mapping:$mapping")
            val accounts = TestDocumentTemplateMapping.findAccountInfos(mapping.templateCode, companyCode)
            val totalAmount: BigDecimal = BigDecimal(1000.0)
            require(totalAmount > BigDecimal.ZERO) { "totalAmount must be positive, but ${totalAmount}" }

            val debitItemCount = accounts.filter { it.accountSide == AccountSide.DEBIT }.size
            val creditItemCount = accounts.filter { it.accountSide == AccountSide.CREDIT }.size

            require(debitItemCount > 0) { "debitItemCount must be positive, but ${debitItemCount}, mapping:${mapping}" }
            require(creditItemCount > 0) { "creditItemCount must be positive, but ${creditItemCount}, mapping:${mapping}" }
        }
    }
}
