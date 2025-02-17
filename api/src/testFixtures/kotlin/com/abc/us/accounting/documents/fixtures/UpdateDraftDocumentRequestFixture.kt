package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.model.UpdateDraftDocumentRequest
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.abc.us.accounting.documents.service.DocumentMasterServiceable
import com.abc.us.accounting.supports.utils.Hashs
import com.github.javafaker.Faker
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object UpdateDraftDocumentRequestFixture {
    private val faker = Faker()
    val START_DATE: LocalDate = LocalDate.of(2024, 10, 1)
    val END_DATE: LocalDate = LocalDate.of(2030, 12, 31)
    private val logger = LoggerFactory.getLogger(this::class.java)

    private var accountService: AccountServiceable
    private var documentMasterService: DocumentMasterServiceable
    private var companyService: CompanyServiceable
    init {
        // AccountRepository 빈을 직접 주입
        SpringContext.getBean(AccountServiceable::class.java).let {
            accountService = it
        }
        // DocumentMasterService 빈을 직접 주입
        SpringContext.getBean(DocumentMasterServiceable::class.java).let {
            documentMasterService = it
        }
        SpringContext.getBean(CompanyServiceable::class.java).let {
            companyService = it
        }
    }


    fun generate(
        documentId: String,
        docType: DocumentType = DocumentFixture.randomDocumentType(),
        docHash:String? = null,
        documentDate: LocalDate = LocalDate.now(),
        postingDate: LocalDate = LocalDate.now(),
        entryDate: LocalDate = LocalDate.now(),
        docStatus: DocumentStatus = DocumentFixture.randomDocumentStatus(),
        workflowStatus: WorkflowStatus = DocumentFixture.randomApprovalStatus(),
        companyCode: CompanyCode? = null,
        txCurrency: String? = null,
        text: String = faker.lorem().sentence(),
        customerId: String? = DocumentFixture.randomCustomerId(docType),
        vendorId: String? = DocumentFixture.randomVendorId(docType),
        originRequest: DocumentOriginRequest? = null,
        createTime: OffsetDateTime = OffsetDateTime.now(),
        createdBy: String = faker.name().username(),
    ): UpdateDraftDocumentRequest {
        val newDocHash = Hashs.hash(
            "testFixture",
            customerId,
            vendorId,
            documentDate,
            postingDate,
            txCurrency,
            text,
            originRequest?.docTemplateCode,
            originRequest?.docTemplateCode,
            originRequest?.bizSystem?.code,
            originRequest?.bizTxId,
            originRequest?.bizEvent?.name,
            originRequest?.accountingEvent
        )

        val modifiedCompanyCode = companyCode ?: CompanyCode.entries.random()
        val modifiedTxCurrency = companyService.getCompanyCurrency(modifiedCompanyCode).name

        return UpdateDraftDocumentRequest(
            docId = documentId,
            docType = docType,
            docHash = docHash ?: newDocHash,
            documentDate = documentDate,
            postingDate = postingDate,

            companyCode = modifiedCompanyCode,
            txCurrency = modifiedTxCurrency,
            text = text,

            docOrigin = originRequest,
            createTime = createTime,
        )
    }

    fun generateDocOriginRequest(
        companyCode: CompanyCode? = null,
        docTemplateCode: DocumentTemplateCode = DocumentFixture.randomDocTemplateCode(),
        bizSystem: BizSystemType = BizSystemType.entries.random(),
        bizTxId: String = faker.number().digits(10),
        bizEvent: BizEventType = BizEventType.entries.random(),
        bizProcess: BizProcessType = BizProcessType.entries.random(),
        accountingEvent: String = faker.lorem().sentence(),
    ): DocumentOriginRequest {
        return DocumentOriginRequest(
            docTemplateCode = docTemplateCode,
            bizSystem = bizSystem,
            bizTxId = bizTxId,
            bizEvent = bizEvent,
            bizProcess = bizProcess,
            accountingEvent = accountingEvent
        )
    }

    /**
     * 실제와 비슷한 전표 생성
     */
    fun generateByTemplate(documentId:String,
                           docType: DocumentType,
                           template: TestDocumentTemplateMapping,
                           docOriginRequest: DocumentOriginRequest?=null,
                           documentDate: LocalDate? = null,
                           postingDate: LocalDate? = null,
                           totalAmount: BigDecimal?=null,
                           docHash:String?=null,
                           customerId: String? = null,
                           vendorId: String? = null,
                           companyCode: CompanyCode? = null,
                           txCurrency: String? = null,
                           orderItemId:String? = null
       ): UpdateDraftDocumentRequest {
        val adjustOriginRequest = docOriginRequest ?: generateDocOriginRequest(docTemplateCode = template.templateCode)

        val request = generate(documentId = documentId,
            docHash = docHash,
            docType = docType,
            documentDate = documentDate ?: LocalDate.now(),
            postingDate = postingDate ?: LocalDate.now(),
            originRequest = adjustOriginRequest,
            companyCode = companyCode,
            txCurrency = txCurrency)

        val modifiedCompanyCode = companyCode ?: request.companyCode
        val accountInfos = TestDocumentTemplateMapping.findAccountInfos(template.templateCode, companyCode = modifiedCompanyCode)
        val modifiedTotalAmount:BigDecimal = totalAmount ?: BigDecimal(faker.commerce().price(1000.0, 2000.0))
        logger.info("generateByTemplate, docType:${docType}, template: ${template}, modifiedTotalAmount:${modifiedTotalAmount}")
        require(modifiedTotalAmount > BigDecimal.ZERO) { "totalAmount must be positive, but ${totalAmount}" }

        val debitItemCount = accountInfos.filter { it.accountSide == AccountSide.DEBIT }.size
        val creditItemCount = accountInfos.filter { it.accountSide == AccountSide.CREDIT }.size

        require(debitItemCount > 0) { "debitItemCount must be positive, but ${debitItemCount}, template:${template}" }
        require(creditItemCount > 0) { "creditItemCount must be positive, but ${creditItemCount}, template:${template}" }

        // credit
        val debitAmounts = MoneyDistributor.distribute(modifiedTotalAmount, debitItemCount).toMutableList()
        val creditAmounts = MoneyDistributor.distribute(modifiedTotalAmount, creditItemCount).toMutableList()

        require (debitAmounts.size == debitItemCount) { "debitAmounts.size must be ${debitItemCount}, but ${debitAmounts.size}" }
        require (creditAmounts.size == creditItemCount) { "creditAmounts.size must be ${creditItemCount}, but ${creditAmounts.size}" }

        val debitAmount = debitAmounts.sumOf { it }
        val creditAmount = creditAmounts.sumOf { it }
        require(debitAmount == creditAmount) { "debitAmount must be equal to creditAmount, but ${debitAmount} != ${creditAmount}" }


        val debitItems = accountInfos.filter { it.accountSide == AccountSide.DEBIT }.map {
            val amount = debitAmounts.removeFirst()
            val item = CreateDocumentItemRequestFixture.generate(companyCode= modifiedCompanyCode, accountCode = it.code, accountSide = it.accountSide, text = it.description, txAmount = amount, customerId = customerId, vendorId = vendorId)
            item
        }
        val creditItems = accountInfos.filter { it.accountSide == AccountSide.CREDIT }.map {
            val amount = creditAmounts.removeFirst()
            val item = CreateDocumentItemRequestFixture.generate(companyCode= modifiedCompanyCode, accountCode = it.code, accountSide = it.accountSide, text = it.description, txAmount = amount, customerId = customerId, vendorId = vendorId)
            item
        }


        val unsortedItems = mutableListOf(debitItems, creditItems).flatten()
        val sortedItems = accountInfos.map { account ->
            val item = unsortedItems.first{ it.accountCode == account.code && it.accountSide == account.accountSide }
            item
        }.toList()

        require(unsortedItems.size == sortedItems.size) { "unsortedItems.size must be equal to sortedItems.size, but ${unsortedItems.size} != ${sortedItems.size}" }

        // item attributes 추가
        for (item in sortedItems) {
            val account = accountService.getAccount(item.toAccountKey())
            require(account != null) { "account not found, accountCode:${item.accountCode}" }

            val needsAttributeCount = documentMasterService.getAllByAccountTypeIn(listOf(account.accountType)).filter { it.fieldRequirement.isAcceptable() }.size
            val attributes = CreateDocumentItemAttributeRequestFixture.generates(needsAttributeCount, account.accountType, attributeType = DocumentAttributeType.ORDER_ITEM_ID, attributeValue = orderItemId)
            for (attribute in attributes) {
                item.attributes.add(attribute)
            }
        }

        request.docItems.addAll(sortedItems)
        return request
    }


    fun generatedUpdateDocumentRequest(candidates:List<DocumentResult>):List<UpdateDraftDocumentRequest> {
        val requests:MutableList<UpdateDraftDocumentRequest> = mutableListOf()

        for ( candidate in candidates) {
            val docTemplateMapping = TestDocumentTemplateMapping.findByTemplateCode(candidate.docOrigin!!.docTemplateCode, candidate.companyCode)
            val docOriginRequest = candidate.docOrigin!!.toRequest()
            val adjustedTotalAmount: BigDecimal = BigDecimal(faker.commerce().price(1000.0, 2000.0))
            val customerId = candidate.docItems.first().customerId
            val vendorId = candidate.docItems.first().vendorId

            val request = generateByTemplate(
                candidate.docId,
                candidate.docType,
                docTemplateMapping,
                docOriginRequest,
                documentDate = candidate.documentDate,
                postingDate = candidate.postingDate,
                adjustedTotalAmount,
                candidate.docHash,
                customerId,
                vendorId,
                candidate.companyCode,
                candidate.txCurrency
            )
            requests.add(request)
        }

        return requests
    }


}
