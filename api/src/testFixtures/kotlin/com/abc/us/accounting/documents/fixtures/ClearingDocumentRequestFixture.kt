package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.ClearingDocumentRequest
import com.abc.us.accounting.documents.model.DocumentItemRequest
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.DocumentMasterServiceable
import com.github.javafaker.Faker
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object ClearingDocumentRequestFixture {
    private val faker = Faker()
    val START_DATE: LocalDate = LocalDate.of(2024, 10, 1)
    val END_DATE: LocalDate = LocalDate.of(2030, 12, 31)

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var accountService: AccountServiceable
    private var documentMasterService: DocumentMasterServiceable
    init {
        // AccountRepository 빈을 직접 주입
        SpringContext.getBean(AccountServiceable::class.java).let {
            accountService = it
        }
        // DocumentMasterService 빈을 직접 주입
        SpringContext.getBean(DocumentMasterServiceable::class.java).let {
            documentMasterService = it
        }
    }

    /**
     * 실제와 비슷한 전표 생성
     */
    fun generateByTemplate(docType: DocumentType,
                           docHash: String? = null,
                           candidates: List<DocumentResult>,
                           clearingAccountCodes:List<String>,
                           clearingAmountRate: BigDecimal = BigDecimal.ONE,
                           clearing: TestDocumentTemplateMapping,
                           originRequest: DocumentOriginRequest,
                           customerId:String? = null,
                           vendorId:String? = null): ClearingDocumentRequest {
        logger.info("generateByTemplate: docType:$docType, candidates:$candidates, clearingAccountCodes:$clearingAccountCodes, customerId:$customerId, vendorId:$vendorId, clearingAmountRate:$clearingAmountRate, clearingTemplate:$clearing, origin:$originRequest")

        require(candidates.isNotEmpty()) { "candidates must not be empty" }
        require(clearingAccountCodes.isNotEmpty()) { "clearingAccountCodes must not be empty" }
        candidates.map { it.docItems }.forEach { require(it.isNotEmpty()) { "docItems must not be empty" } }

        val candidateItems = candidates.map{ it.docItems }.flatten().filter { clearingAccountCodes.contains(it.accountCode) }

        require(candidateItems.size >= 1) { "candidateItems must be greater than 1" }
        candidates.map { it.companyCode }.distinct().let {
            require(it.size == 1) { "companyCode must be unique, companyCodes:$it" }
        }
        candidates.map { it.txCurrency }.distinct().let {
            require(it.size == 1) { "txCurrency must be unique, txCurrencies:$it" }
        }

        val companyCode = candidates.first().companyCode
        val txCurrency = CurrencyCode.fromCode(candidates.first().txCurrency)
        val refDocItemIds = candidateItems.map { it.docItemId }

        val candidateItemsMap = candidateItems.groupBy { it.accountCode }
        val allClearingItems = mutableListOf<DocumentItemRequest>()

        val candidateAccountCodes = candidateItemsMap.keys
        val candidateAccountAmountMap:MutableMap<String, BigDecimal> = mutableMapOf()

        candidateItemsMap.keys.forEach { clearingAccountCode ->
            logger.debug("clearingAccountCodes: $candidateAccountCodes")
            val curCandidateItems = candidateItemsMap[clearingAccountCode]!!
            val curCandidateItemIds = curCandidateItems.map { it.docItemId }

            val curCandiateTxAmounts = curCandidateItems.map { it.txAmount }
            val candidateAmount = curCandiateTxAmounts.sumOf { it }.multiply(clearingAmountRate)
            logger.debug("curCandidateItemIds: ${curCandidateItemIds}, candidateAmount: $candidateAmount" +
                    ", by candidate txAmounts:${curCandiateTxAmounts}, clearingAmountRate: $clearingAmountRate")

            candidateAccountAmountMap[clearingAccountCode] = candidateAmount
        }

        val clearingAccountInfos = TestDocumentTemplateMapping.findAccountInfos(clearing.templateCode, companyCode)
        require(clearingAccountInfos.any { candidateAccountCodes.contains(it.code) }) { "accountCodes:$candidateAccountCodes does not matched in clearingAccountInfos:${clearingAccountInfos}" }

        val clearingItems:List<DocumentItemRequest> = clearing.generateDocItemRequests(companyCode, txCurrency, candidateAccountAmountMap, customerId = customerId, vendorId = vendorId)
        logger.debug("clearingItems: $clearingItems")

        require(clearingItems.isNotEmpty()) { "clearingItems must not be empty" }
        require(clearingItems.size <= clearingAccountInfos.size) { "clearingItems must be less than equal, clearingItemSize:${clearingItems.size}, clearingAccountInfosSize:${clearingAccountInfos.size}" }

        for (item in clearingItems) {
            val account = accountService.getAccount(item.toAccountKey())
            require(account != null) { "account not found, accountCode:${item.accountCode}" }

            val needsAttributeCount = documentMasterService.getAllByAccountTypeIn(listOf(account.accountType)).filter { it.fieldRequirement.isAcceptable() }.size

            val attributes = CreateDocumentItemAttributeRequestFixture.generates(needsAttributeCount, account.accountType)
            for (attribute in attributes) {
                item.attributes.add(attribute)
            }
        }
        allClearingItems.addAll(clearingItems)

        val clearingRequest = ClearingDocumentRequest(
            docType = docType,
            docHash = docHash,

            postingDate = LocalDate.now(),
            documentDate = LocalDate.now(),
            companyCode = companyCode,
            txCurrency = txCurrency.name,

            refDocItemIds = refDocItemIds,
            reason = "clearing",

            reference = refDocItemIds.joinToString(","),
            text = "clearing test",

            createTime = OffsetDateTime.now(),
            createdBy = "test",

            docOrigin = originRequest,
            docItems = allClearingItems.toMutableList()
        )

        return clearingRequest
    }

}
