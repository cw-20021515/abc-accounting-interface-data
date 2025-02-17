package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.repository.AccountBalanceRecordRepository
import com.abc.us.accounting.documents.domain.repository.AccountBalanceRepository
import com.abc.us.accounting.documents.domain.repository.FiscalClosingBalanceSnapshotRepository
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.AccountModelsFixture
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicAccountBalanceServiceTests(
    private val persistenceService: DocumentPersistenceService,
    private val documentService: DocumentService,
    private val accountBalanceService: AccountBalanceService,
    private val accountBalanceRepository: AccountBalanceRepository,
    private val accountBalanceRecordRepository: AccountBalanceRecordRepository,
    private val fiscalClosingBalanceSnapshotRepository: FiscalClosingBalanceSnapshotRepository,
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
    val companyCode = Constants.TEST_COMPANY_CODE
    val processByExplicit = true

    fun processOpeningBalance(count:Int): List<AccountBalanceRecordResult> {
        val records = AccountModelsFixture.randomOpeningBalanceRequests(count)
        val results:MutableList<AccountBalanceRecordResult> = mutableListOf()

        AccountBalanceEvent.OpeningBalance(
            context = DocumentServiceContext.SAVE_DEBUG,
            requests = records
        ).let {
            results.addAll(accountBalanceService.processOpeningBalance(it))
        }

        return results
    }

    @Test
    fun `opening balance process test`() {
        val count = 5

        val results = processOpeningBalance(count)
        val ids = results.map { it.id }
        val founded = accountBalanceRecordRepository.findAllById(ids).let {
            it.forEach { record ->
                logger.info("record: $record")
            }
            it
        }
        founded.size shouldBe count

        founded.forEach {
            val record = it
            record.changeAmount shouldBe record.balanceAfterChange

            if ( record.accountNature == AccountSide.DEBIT ) {
                record.accumulatedDebitAfterChange shouldBe record.balanceAfterChange
                record.accumulatedCreditAfterChange shouldBe BigDecimal.ZERO.toScale()
            } else {
                record.accumulatedDebitAfterChange shouldBe BigDecimal.ZERO.toScale()
                record.accumulatedCreditAfterChange shouldBe record.balanceAfterChange
            }
        }


        accountBalanceRecordRepository.deleteAllById(ids)
    }

    @Test
    fun `basic post balance record tests`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val count = Int.MAX_VALUE
        val posingResults = posting(DocumentServiceContext.SAVE_DEBUG, 1, DocumentType.JOURNAL_ENTRY, companyCode, 10)

        val transactionBalanceResults:MutableList<AccountBalanceRecordResult> = mutableListOf()

        if ( processByExplicit ) {
            AccountBalanceEvent.DocumentCreated(
                context = context,
                requests = toDocumentBalanceRequests(posingResults)
            ).let {
                transactionBalanceResults.addAll(accountBalanceService.processDocumentCreated(it))
            }
        }

        val keys = transactionBalanceResults.map { it.toAccountKey() }.distinct()
        val balances = accountBalanceRepository.findAllById(keys)
        keys.size shouldBe balances.size

        persistenceService.cleanup(context, posingResults.map { it.docId })
        accountBalanceRecordRepository.deleteAllById(transactionBalanceResults.map { it.id })
    }

    @Test
    fun `basic post balance record tests - with opening balance`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val count = Int.MAX_VALUE
        val openingBalanceResults = processOpeningBalance(count)

        val posingResults = posting(DocumentServiceContext.SAVE_DEBUG, 1, DocumentType.JOURNAL_ENTRY, companyCode, 10)

        val transactionBalanceResults:MutableList<AccountBalanceRecordResult> = mutableListOf()

        if ( processByExplicit ) {
            AccountBalanceEvent.DocumentCreated(
                context = context,
                requests = toDocumentBalanceRequests(posingResults)
            ).let {
                transactionBalanceResults.addAll(accountBalanceService.processDocumentCreated(it))
            }
        }

        persistenceService.cleanup(context, posingResults.map { it.docId })
        accountBalanceRecordRepository.deleteAllById(openingBalanceResults.map { it.id })
        accountBalanceRecordRepository.deleteAllById(transactionBalanceResults.map { it.id })
    }


    @Test
    fun `basic modification test`() {
        val context = DocumentServiceContext.SAVE_DEBUG
        val count = Int.MAX_VALUE
        val openingBalanceResults = processOpeningBalance(count)

        val transactionBalanceResults:MutableList<AccountBalanceRecordResult> = mutableListOf()

        // 전표생성
        val postingResults = posting(DocumentServiceContext.SAVE_DEBUG, 1, DocumentType.JOURNAL_ENTRY, companyCode, 20)
        if ( processByExplicit ) {
            AccountBalanceEvent.DocumentCreated(
                context = context,
                requests = toDocumentBalanceRequests(postingResults)
            ).let {
                transactionBalanceResults.addAll(accountBalanceService.processDocumentCreated(it))
            }
            val expectedResult = postingResults.map { it.docItems.size }.sum()
            transactionBalanceResults.size shouldBe expectedResult
        }


        // 수정전표 발생
        val modificationBalanceResults:MutableList<AccountBalanceRecordResult> = mutableListOf()
        run {
            val candidates = postingResults.shuffled().take(10)
            val modifyRequests = CreateDocumentRequestFixture.generatedUpdatedRequest(candidates)
            val modifyResults = documentService.posting(context, modifyRequests)

            if ( processByExplicit ) {
                AccountBalanceEvent.DocumentModified(
                    context = context,
                    requests = toDocumentBalanceRequests(modifyResults, candidates)
                ).let {
                    modificationBalanceResults.addAll(accountBalanceService.processDocumentModified(it))
                }
                val curExpectedResult = modifyResults.map { it.docItems.size }.sum()
                modificationBalanceResults.size shouldBe curExpectedResult
            }
        }

        persistenceService.cleanup(context, postingResults.map { it.docId })
        accountBalanceRecordRepository.deleteAllById(openingBalanceResults.map { it.id })
        accountBalanceRecordRepository.deleteAllById(transactionBalanceResults.map { it.id })
        accountBalanceRecordRepository.deleteAllById(modificationBalanceResults.map { it.id })
    }


    fun toDocumentBalanceRequests (created:List<DocumentResult>, originals: List<DocumentResult> = listOf()):List<DocumentBalanceRequest> {
        val originalMap = originals.associateBy { it.docId }
        return created.map { it ->
            DocumentBalanceRequest(it, originalMap[it.docId])
        }
    }

    fun posting(context:DocumentServiceContext, iteration:Int, docType: DocumentType?=null, companyCode: CompanyCode? = null, count:Int=1):List<DocumentResult> {
        val requests:MutableList<CreateDocumentRequest> = mutableListOf()
        val modifiedDocType = docType ?: DocumentType.JOURNAL_ENTRY
        requests.addAll(CreateDocumentRequestFixture.generateByTemplateList(count, modifiedDocType, companyCode))
        for (request in requests) {
            val validateResults = CreateDocumentValidationRule.validateAll(context, listOf(request) )
            logger.debug("it:{}, Validation results: {}, by request: {}", iteration, validateResults, request)
            validateResults.size shouldBe 0
            val validateItemResults = DocumentItemValidationRule.validateAll(context, request.docItems)
            if (validateItemResults.isNotEmpty()) {
                logger.debug("it:{}, Item Validation results: {}", iteration, validateItemResults)
            }
            validateItemResults.size shouldBe 0
        }
        val results = documentService.posting(context, requests)
        return results
    }


}