package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.abc.us.accounting.documents.service.DocumentMasterServiceable
import com.abc.us.accounting.supports.utils.Hashs
import com.github.javafaker.Faker
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object CreateDocumentRequestFixture {
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


    fun generates(
        size: Int,
        docType: DocumentType = DocumentFixture.randomDocumentType(),
        documentId: String? = null,
        documentDate: LocalDate = LocalDate.now(),
        postingDate: LocalDate = LocalDate.now(),
        entryDate: LocalDate = LocalDate.now(),
        docStatus: DocumentStatus = DocumentFixture.randomDocumentStatus(),
        workflowStatus: WorkflowStatus = DocumentFixture.randomApprovalStatus(),
        companyCode: CompanyCode = CompanyCode.entries.random(),
        currency: String? = null,
        text: String = faker.lorem().sentence(),
        customerId: String? = DocumentFixture.randomCustomerId(docType),
        vendorId: String? = DocumentFixture.randomVendorId(docType),
        originRequest: DocumentOriginRequest? = null,
        createTime: OffsetDateTime = OffsetDateTime.now(),
        createdBy: String = faker.name().username(),
    ): List<CreateDocumentRequest> {
        return (1..size).map {
            generate(
                docType = docType,
                documentId = documentId,
                documentDate = documentDate,
                postingDate = postingDate,
                entryDate = entryDate,
                docStatus = docStatus,
                workflowStatus = workflowStatus,
                companyCode = companyCode,
                txCurrency = currency,
                text = text,
                customerId = customerId,
                vendorId = vendorId,
                originRequest = originRequest,
                createTime = createTime,
                createdBy = createdBy
            )
        }
    }

    fun generate(
        docType: DocumentType = DocumentFixture.randomDocumentType(),
        documentId: String? = null,
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
    ): CreateDocumentRequest {
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

        val modifiedCompanyCode = companyCode ?: CompanyCode.randomSalesCompany()
        val modifiedTxCurrency = companyService.getCompanyCurrency(modifiedCompanyCode).name

        return CreateDocumentRequest(
//            docId = documentId,
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

    /**
     * 실제와 비슷한 전표 생성
     */
    fun generateByTemplateList(
        size: Int,
        docType: DocumentType?= null,
        companyCode: CompanyCode? = null,
        template: TestDocumentTemplateMapping ?= null,
        originRequest: DocumentOriginRequest?=null,
        docmentDate: LocalDate? = null,
        postingDate: LocalDate? = null,
    ): List<CreateDocumentRequest> {
        return (1..size).map {
            val docType = docType ?: DocumentType.entries.random()
            val companyCode = companyCode ?: CompanyCode.randomSalesCompany()
            val template = template ?: TestDocumentTemplateMapping.entries.random()
            val originRequest = originRequest ?: generateDocOriginRequest(companyCode = companyCode, docTemplateCode = template.templateCode)
            generateByTemplate(docType= docType, companyCode = companyCode, template = template, documentDate = docmentDate, postingDate = postingDate, docOriginRequest = originRequest)
        }
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
    fun generateByTemplate(docType: DocumentType,
                           template: TestDocumentTemplateMapping, docOriginRequest: DocumentOriginRequest?=null,
                           documentDate: LocalDate? = null,
                           postingDate: LocalDate? = null,
                           totalAmount: BigDecimal?=null,
                           documentId:String?=null,
                           docHash:String?=null,
                           customerId: String? = null,
                           vendorId: String? = null,
                           companyCode: CompanyCode? = null,
                           txCurrency: String? = null,
                           orderItemId:String? = null
                           ): CreateDocumentRequest {
        val adjustOriginRequest = docOriginRequest ?: generateDocOriginRequest(docTemplateCode = template.templateCode)

        val request = generate(documentId = documentId,
            docHash = docHash,
            docType = docType,
            documentDate = documentDate ?: LocalDate.now(),
            postingDate = postingDate ?: LocalDate.now(),
            originRequest = adjustOriginRequest,
            companyCode = companyCode,
            txCurrency = txCurrency)

        val accountInfos = TestDocumentTemplateMapping.findAccountInfos(template.templateCode, request.companyCode)
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
            val item = CreateDocumentItemRequestFixture.generate(companyCode=request.companyCode, accountCode = it.code, accountSide = it.accountSide, text = it.description, txAmount = amount, customerId = customerId, vendorId = vendorId)
            item
        }

        val creditItems = accountInfos.filter { it.accountSide == AccountSide.CREDIT }.map {
            val amount = creditAmounts.removeFirst()
            val item = CreateDocumentItemRequestFixture.generate(companyCode=request.companyCode, accountCode = it.code, accountSide = it.accountSide, text = it.description, txAmount = amount, customerId = customerId, vendorId = vendorId)
            item
        }


        val unsortedItems = mutableListOf(debitItems, creditItems).flatten()
        val sortedItems = accountInfos.map { account ->
            val item = unsortedItems.first{ it.accountCode == account.code && it.accountSide == account.accountSide }
            item
        }.filter {
            it.txAmount > BigDecimal.ZERO
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


    fun generatedUpdatedRequest(candidates:List<DocumentResult>):List<CreateDocumentRequest> {
        val requests:MutableList<CreateDocumentRequest> = mutableListOf()

        for ( candidate in candidates) {
            val docTemplateMapping = TestDocumentTemplateMapping.findByTemplateCode(candidate.docOrigin!!.docTemplateCode, companyCode = candidate.companyCode)
            val docOriginRequest = candidate.docOrigin!!.toRequest()
            val adjustedTotalAmount: BigDecimal = BigDecimal(faker.commerce().price(1000.0, 2000.0))
            val customerId = candidate.docItems.first().customerId
            val vendorId = candidate.docItems.first().vendorId

            val request = generateByTemplate(candidate.docType,
                docTemplateMapping,
                docOriginRequest,
                documentDate = candidate.documentDate,
                postingDate = candidate.postingDate,
                adjustedTotalAmount,
                candidate.docId,
                candidate.docHash,
                customerId, vendorId, candidate.companyCode, candidate.txCurrency
            )
            requests.add(request)
        }

        return requests
    }


}
