package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.entity.Money
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.abc.us.accounting.supports.utils.DateConverter.toDate
import com.abc.us.accounting.supports.utils.DateConverter.toLocalDate
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.IdGenerator
import com.github.javafaker.Faker
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

// TODO: Implement the DocumentFixture class
object DocumentFixture {
    private val faker = Faker()
    val START_DATE:LocalDate = LocalDate.of(2024,10,1)
    val END_DATE:LocalDate = LocalDate.of(2030,12,31)

    private var companyService: CompanyServiceable

    init {
        SpringContext.getBean(CompanyServiceable::class.java).let {
            companyService = it
        }
    }


    fun createDocuments(
        size:Int,
        docType:DocumentType?=null,
        documentDate: LocalDate?=null,
        postingDate: LocalDate?=null,
        entryDate: LocalDate?=null,

        docStatus: DocumentStatus?=null,
        workflowStatus: WorkflowStatus?=null,

        companyCode: CompanyCode? = null,

        amount:BigDecimal? = null,
        text:String? = null,
        customerId:String? = null,
        vendorId: String? = null,
        isOrigin: Boolean? = null,

        relationType: RelationType? = null,
        refDocType: DocumentType? = null,
        refDocId: String? = null,
        refReasonCode: String? = null,
        docTemplateCode: DocumentTemplateCode? = null,
        bizSystem: BizSystemType? = null,
        bizTxId: String? = null,
        bizProcess: BizProcessType? = null,
        bizEvent: BizEventType? = null,
        accountingEvent: String? = null,

        createTime: OffsetDateTime? = null,
        createdBy: String? = null,
        updateTime: OffsetDateTime? = null,
        updatedBy: String? = null,
    ): List<Document> {
        return (1..size).map {
            createDocument(
                docType= docType ?: randomDocumentType(),
                documentDate = documentDate ?: randomDate(),
                postingDate = postingDate ?: randomDate(),
                entryDate = entryDate ?: randomDate(),
                docStatus = docStatus ?: randomDocumentStatus(),
                workflowStatus = workflowStatus ?: randomApprovalStatus(),

                companyCode = companyCode ?: CompanyCode.entries.random(),
                totalAmount = amount ?: BigDecimal(faker.commerce().price(10.0, 60.0)),
                text = text ?: faker.lorem().sentence(),
                customerId = customerId ?: randomCustomerId(docType),
                vendorId = vendorId ?: randomVendorId(docType),

                isOrigin = isOrigin ?: faker.number().numberBetween(0, 1) == 1,
                relationType = relationType ?: randomRelationType(isOrigin),
                refDocType = refDocType ?: randomDocumentType(),
                refDocId = refDocId ?: randomDocumentId(refDocType),
                refReasonCode = refReasonCode,

                docTemplateCode= docTemplateCode ?: randomDocTemplateCode(),
                bizSystem = bizSystem ?: randomBizSystemType(),
                bizTxId = bizTxId ?: randomBizTxId(bizSystem),
                bizProcess = bizProcess ?: randomBizProcess(bizSystem),
                bizEvent = bizEvent ?: randomBizEvent(bizSystem, bizProcess),
                accountingEvent = accountingEvent,

                createTime = createTime ?: OffsetDateTime.now(),
                createdBy = createdBy ?: faker.name().username(),
                updateTime = updateTime ?: OffsetDateTime.now(),
                updatedBy = updatedBy ?: faker.name().username()
            )
        }
    }

    fun createDocument(
        docType: DocumentType = randomDocumentType(),
        documentId: String = randomDocumentId(docType)!!,
        documentDate: LocalDate = LocalDate.now(),
        postingDate: LocalDate = LocalDate.now(),
        entryDate: LocalDate = LocalDate.now(),
        docStatus: DocumentStatus = randomDocumentStatus(),
        workflowStatus: WorkflowStatus = randomApprovalStatus(),
        companyCode: CompanyCode = CompanyCode.entries.random(),
        fiscalYearMonth: FiscalYearMonth? = null,
        currency: String? = null,
        totalAmount:BigDecimal = BigDecimal(faker.commerce().price(10.0, 60.0)),
        text:String = faker.lorem().sentence(),

        customerId:String? = randomCustomerId(docType),
        vendorId:String? = randomVendorId(docType),

        isOrigin: Boolean = faker.number().numberBetween(0, 1) == 1,

        relationType:RelationType? = randomRelationType(isOrigin),
        refDocType:DocumentType? = if (isOrigin) null else randomDocumentType(),
        refDocId:String? = if (isOrigin) null else randomDocumentId(refDocType!!),
        refReasonCode:String? = null,

        bizSystem: BizSystemType? = randomBizSystemType(),
        bizTxId: String? = randomBizTxId(bizSystem),
        bizProcess: BizProcessType? = randomBizProcess(bizSystem),
        bizEvent: BizEventType? = randomBizEvent(bizSystem, bizProcess),
        accountingEvent: String? = null,
        docTemplateCode: DocumentTemplateCode = randomDocTemplateCode(),

        createTime: OffsetDateTime = OffsetDateTime.now(),
        createdBy: String = faker.name().username(),
        updateTime: OffsetDateTime = OffsetDateTime.now(),
        updatedBy: String = faker.name().username()
    ): Document {
        val docHash = Hashs.hash("testFixture",
            customerId,
            vendorId,
            documentDate,
            postingDate,
            currency,
            text,
            isOrigin,
            docTemplateCode,
            bizSystem?.code,
            bizTxId,
            bizEvent?.name,
            accountingEvent)

        val adjustedCurrency = currency ?: companyService.getCompanyCurrency(companyCode).name
        val adjustedFiscalYearMonth = fiscalYearMonth ?: FiscalYearMonth.from(postingDate, fiscalRule = companyService.getCompanyFiscalRule(companyCode))

        val money = Money.of(totalAmount, adjustedCurrency)

        return Document(
            _id = documentId,
            docType = docType,
            docHash = docHash,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = entryDate,
            fiscalYearMonth = adjustedFiscalYearMonth,
            docStatus = docStatus,
            workflowStatus = workflowStatus,
            companyCode = companyCode,
            txMoney = Money.of(totalAmount, adjustedCurrency),
            money = money,

            reference = null,
            text = text,

            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        )
    }

    fun randomDocumentType(): DocumentType {
        return DocumentType.entries.random()
    }

    fun randomDocumentId(documentType: DocumentType? = null, date:LocalDate= LocalDate.now()): String? {
        if (documentType == null) {
            return null
        }

        return IdGenerator.generateId(documentType.code, date, faker.number().numberBetween(1, 10000).toLong())
    }

    fun randomDate (startDate:LocalDate =START_DATE, endDate:LocalDate = END_DATE): LocalDate {
        val start:Date = startDate.toDate()
        val end:Date = endDate.toDate()
        val date:Date= faker.date().between(start, end)
        return date.toLocalDate()
    }

    fun randomDocumentStatus(): DocumentStatus {
        return DocumentStatus.entries.random()
    }

    fun randomApprovalStatus(): WorkflowStatus {
        return WorkflowStatus.entries.random()
    }


    fun randomDocTemplateCode(): DocumentTemplateCode {
        val size = TestDocumentTemplateMapping.entries.size
        return TestDocumentTemplateMapping.entries.random().templateCode
//        return faker.number().numberBetween(0, size).let { docTemplateIdList[it] }
    }

    fun randomCustomerId(docType: DocumentType?):String? {
        if (docType == null) {
            return null
        }
        val lookup = listOf(AccountType.CUSTOMER, AccountType.SALES)
        if ( docType.allowAccountTypes.any{ lookup.contains(it) } ) {
            return UUID.randomUUID().toString()
        }
        return null
    }

    fun randomVendorId(docType: DocumentType?):String? {
        if (docType == null) {
            return null
        }
        val lookup = listOf(AccountType.VENDOR)
        if ( docType.allowAccountTypes.any{ lookup.contains(it) } ) {
            return faker.number().digits(10).toString()
        }
        return null
    }

    fun randomRelationType(isOrigin: Boolean?): RelationType? {
        if (isOrigin == null || isOrigin) {
            return null
        }
        return RelationType.entries.random()
    }

    fun randomBizSystemType(): BizSystemType {
        return BizSystemType.entries.random()
    }

    fun randomBizTxId(bizSystem: BizSystemType?): String? {
        if (bizSystem == null || bizSystem == BizSystemType.ABC_ACCOUNTING) {
            return null
        }
        return IdGenerator.generateId(bizSystem.code, LocalDate.now(), faker.number().numberBetween(1, 10000).toLong())
    }

    fun randomBizProcess(bizSystem: BizSystemType?): BizProcessType? {
        if (bizSystem == null || bizSystem == BizSystemType.ABC_ACCOUNTING) {
            return null
        }
        return BizProcessType.entries.random()
    }

    fun randomBizEvent(bizSystem: BizSystemType?, bizProcessType: BizProcessType?): BizEventType? {
        if (bizProcessType == null || bizSystem == BizSystemType.ABC_ACCOUNTING) {
            return null
        }

        return BizEventType.findByProcessType(bizProcessType).random()
    }
}
