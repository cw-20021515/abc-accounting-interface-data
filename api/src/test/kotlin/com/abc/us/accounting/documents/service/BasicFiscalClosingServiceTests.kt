package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.FiscalKey
import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.repository.FiscalClosingRepository
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.BasicDocumentClearingTests.Companion.posting
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate


/**
 * 매뉴얼 반제 케이스
 */
@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicFiscalClosingServiceTests  (
    private val fiscalClosingService: FiscalClosingService,
    private val fiscalClosingRepository: FiscalClosingRepository,

    private val documentService: DocumentService,
    private val documentPersistenceService: DocumentPersistenceService,
    private val companyServiceable: CompanyServiceable,
    private val timeLogger: TimeLogger = TimeLogger()
): AnnotationSpec() {


    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun openFiscalYearTest(fiscalYear:Int) {
        for ( companyCode in CompanyCode.entries ) {
            for (month in 1..12) {
                openFiscalYearMonthTest(companyCode, FiscalYearMonth.of(fiscalYear, month))
            }
        }
    }

    fun openFiscalYearMonthTest(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        fiscalClosingService.openFiscalYearMonth(companyCode, fiscalYearMonth)
        val fiscalClosing = fiscalClosingService.getFiscalClosing(companyCode, fiscalYearMonth)
        fiscalClosing shouldNotBeNull {
            fiscalClosing!!.fiscalKey.companyCode shouldBe companyCode
            fiscalClosing.fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            fiscalClosing.fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            fiscalClosing.isOpen() shouldBe true
        }

        val fiscalKey = FiscalKey(companyCode, fiscalYearMonth)

        val find = fiscalClosingRepository.findById(fiscalKey)
        find shouldNotBeNull {
            find.get().fiscalKey.companyCode shouldBe companyCode
            find.get().fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            find.get().fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            find.get().isOpen() shouldBe true
        }
    }

    fun startClosingFiscalYearTest(fiscalYear:Int) {
        for ( companyCode in CompanyCode.entries ) {
            for (month in 1..12) {

                startClosingFiscalYearMonthTest(companyCode, FiscalYearMonth.of(fiscalYear, month))
            }
        }
    }

    fun startClosingFiscalYearMonthTest(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        fiscalClosingService.startClosingFiscalYearMonth(companyCode, fiscalYearMonth)

        val fiscalClosing = fiscalClosingService.getFiscalClosing(companyCode, fiscalYearMonth)
        fiscalClosing shouldNotBeNull {
            fiscalClosing!!.fiscalKey.companyCode shouldBe companyCode
            fiscalClosing.fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            fiscalClosing.fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            fiscalClosing.isOpen() shouldBe false
            fiscalClosing.isClosed() shouldBe false
            fiscalClosing.status shouldBe  ClosingStatus.CLOSING
        }

        val fiscalKey = FiscalKey(companyCode, fiscalYearMonth)
        val find = fiscalClosingRepository.findById(fiscalKey)
        find shouldNotBeNull {
            find.get().fiscalKey.companyCode shouldBe companyCode
            find.get().fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            find.get().fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            find.get().isOpen() shouldBe false
            find.get().isClosed() shouldBe false
            find.get().status shouldBe  ClosingStatus.CLOSING
        }
    }



    fun closeFiscalYearMonthTest(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        fiscalClosingService.closeFiscalYearMonth(companyCode, fiscalYearMonth)

        val fiscalClosing = fiscalClosingService.getFiscalClosing(companyCode, fiscalYearMonth)
        fiscalClosing shouldNotBeNull {
            fiscalClosing!!.fiscalKey.companyCode shouldBe companyCode
            fiscalClosing.fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            fiscalClosing.fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            fiscalClosing.isOpen() shouldBe false
            fiscalClosing.isClosed() shouldBe true
            fiscalClosing.status shouldBe  ClosingStatus.CLOSED
        }

        val fiscalKey = FiscalKey(companyCode, fiscalYearMonth)
        val find = fiscalClosingRepository.findById(fiscalKey)
        find shouldNotBeNull {
            find.get().fiscalKey.companyCode shouldBe companyCode
            find.get().fiscalKey.fiscalYearMonth.year shouldBe fiscalYearMonth.year
            find.get().fiscalKey.fiscalYearMonth.month shouldBe fiscalYearMonth.month
            find.get().isOpen() shouldBe false
            find.get().isClosed() shouldBe true
            find.get().status shouldBe  ClosingStatus.CLOSED
        }
    }

    fun closeFiscalYearTests(fiscalYear: Int){
        for ( companyCode in CompanyCode.entries ) {
            for (month in 1..12) {

                closeFiscalYearMonthTest (companyCode, FiscalYearMonth.of(fiscalYear, month))
            }
        }
    }

    @Test
    fun `회계연도 Open 테스트`() {
        val fiscalYear = 2024
        timeLogger.measureAndLog {
            openFiscalYearTest(fiscalYear)
            startClosingFiscalYearTest(fiscalYear)
        }
        fiscalClosingService.cleanup(fiscalYear)
        logger.info("회계연도 Open 테스트 완료")
    }

    @Test
    fun `회계마감 시작 테스트`() {
        val fiscalYear = 2024
        timeLogger.measureAndLog {
            openFiscalYearTest(fiscalYear)
            startClosingFiscalYearTest(fiscalYear)
        }
        fiscalClosingService.cleanup(fiscalYear)
        logger.info("회계마감 시작 테스트 완료")
    }


    @Test
    fun `회계마감 테스트`() {
        val fiscalYear = 2024
        timeLogger.measureAndLog {
            openFiscalYearTest(fiscalYear)
            startClosingFiscalYearTest(fiscalYear)
            closeFiscalYearTests(fiscalYear)
        }
        fiscalClosingService.cleanup(fiscalYear)
        logger.info("회계마감 테스트 완료")
    }

    @Test
    fun `회계마감 이후 전표 발생시 오류 발생하는지 테스트`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val companyCode = CompanyCode.T200
        val fiscalYearMonth = FiscalYearMonth.of(2024, 12)
        val localDate = LocalDate.of(2024, 12, 10)
        val customerId = "CC0001"
        val txCurrency = companyServiceable.getCompanyCurrency(companyCode).name
        val totalAmount = BigDecimal("1082.5").setScale(Constants.ACCOUNTING_SCALE)


        val template1 = TestDocumentTemplateMapping.ONETIME_PAYMENT_RECEIVED
        val documentOrigin1 = DocumentOriginRequest(
            docTemplateCode = template1.templateCode,
            bizSystem = BizSystemType.ONETIME,
            bizTxId = "one_time_sales_order_received",
            bizProcess = BizProcessType.ORDER,
            bizEvent = BizEventType.ORDER_RECEIVED,
            accountingEvent = "One Time Sales Order Received",
        )


        timeLogger.measureAndLog {
            // 정상: 마감전
            run {
                openFiscalYearMonthTest(companyCode, fiscalYearMonth)
                val result = posting(context, documentService,
                    DocumentType.ACCOUNTING_DOCUMENT,
                    docHash = null,
                    documentDate = localDate,
                    postingDate = localDate,
                    template1,
                    documentOrigin1,
                    totalAmount = totalAmount,
                    customerId = customerId,
                    companyCode = companyCode,
                    txCurrency = txCurrency)

                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT

                documentPersistenceService.cleanup(context, listOf(result.docId))
            }

            // 정상: 회계마감 시작
            run {
                startClosingFiscalYearMonthTest(companyCode, fiscalYearMonth)
                val result = posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
                    docHash = null,
                    documentDate = localDate,
                    postingDate = localDate,
                    template1, documentOrigin1,
                    totalAmount = totalAmount,
                    customerId = customerId,
                    companyCode = companyCode,
                    txCurrency = txCurrency)

                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT

                documentPersistenceService.cleanup(context, listOf(result.docId))
            }

            // 오류 발생
            val exception = shouldThrow<DocumentException.AlreadyFiscalClosedException> {
                closeFiscalYearMonthTest(companyCode, fiscalYearMonth)
                posting(context, documentService, DocumentType.ACCOUNTING_DOCUMENT,
                    docHash = null,
                    documentDate = localDate,
                    postingDate = localDate,
                    template1, documentOrigin1,
                    totalAmount = totalAmount,
                    customerId = customerId,
                    companyCode = companyCode,
                    txCurrency = txCurrency)
            }.also { exception ->
                with(exception){
                    message shouldContain "companyCode:$companyCode, baseDate"
                    message shouldContain "is already closed"
                }
            }
        }
        fiscalClosingService.cleanup(companyCode, fiscalYearMonth)
        logger.info("회계마감 테스트 완료")
    }
}