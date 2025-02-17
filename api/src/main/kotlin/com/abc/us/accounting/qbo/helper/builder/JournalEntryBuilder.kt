package com.abc.us.accounting.qbo.helper.builder

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.MathConfig
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.entity.DocumentItemAttribute
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.qbo.domain.entity.QboJournalEntry
import com.abc.us.accounting.qbo.domain.entity.key.QboJournalEntryKey
import com.abc.us.accounting.qbo.helper.adjustment.RoundingAdjustment
import com.abc.us.accounting.qbo.service.QboAccountService
import com.abc.us.accounting.qbo.service.QboCustomerService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.utils.DateConverter.toDate
import com.intuit.ipp.data.*
import mu.KotlinLogging
import org.springframework.util.StringUtils

class JournalEntryBuilder(
    private val matchConfig : MathConfig,
    private val customerService: QboCustomerService,
    private val qboAccountService: QboAccountService
) {
    companion object {
        private val converter = JsonConverter()
        private val logger = KotlinLogging.logger {}

        fun buildSubmit(companyCode: String,
                        results: List<JournalEntry>?,
                        docMap: MutableMap<String, Document>): MutableList<QboJournalEntry> {
            val entries = mutableListOf<QboJournalEntry>()

            results?.let {
                it.forEach { result ->
                    val submitJson = converter.toJson(result)
                    val document = docMap[result.docNumber]!!
                    val submitted =
                        QboJournalEntry(
                            key = QboJournalEntryKey(
                                qboId = result.id,
                                docId = result.docNumber,
                                companyCode = companyCode
                            ),
                            docHash = document.docHash,
                            postingDate = document.postingDate,
                            submitResult = submitJson?.let { it } ?: ""
                        )
                            .apply {
                                roundingDifference = document.roundingDifference
                                syncToken = result.syncToken
                                createTime = document.createTime
                                updateTime = document.updateTime
                            }
                    entries.add(submitted)
                    logger.info("Add JournalEntry-[${result.id}.${result.docNumber}]")
                }
            }
            return entries
        }
    }

    fun buildEntityRef(entityType : EntityTypeEnum, item : DocumentItem) : ReferenceType {
        return ReferenceType().apply {
            when(entityType) {
                EntityTypeEnum.CUSTOMER-> {
                    if(item.customerId == null) {
                        throw NoSuchFieldException("Validation ERROR : NULL Customer-Document(${item.docId}) ")
                    }
                    else {
                        val customer = customerService.findByCustomer(item.companyCode.code,item.customerId)
                        if (customer == null) {
                            throw NoSuchFieldException("Validation ERROR : Not found Customer(${item.customerId})-Document(${item.docId}) ")
                        }
                        value = customer.key.qboId
                        if(StringUtils.hasText(customer.name.displayName)) {
                            name = customer.name.displayName
                        }
                        else {
                            name = customer.name.lastName + " " + customer.name.firstName
                        }
                    }
                }
                EntityTypeEnum.VENDOR -> {
//                    submittedVendorRepository.findByCustomerId(item.vendorId!!)?.let { vendor ->
//                        name = vendor.name.displayName
//                        value = vendor.id.submitId
//                    }
                }
                EntityTypeEnum.EMPLOYEE -> {
//                    submittedEmployeeRepository.findByEmployeeId(item.employeeId!!)?.let { employee ->
//                        name = employee.name.displayName
//                        value = employee.id.submitId
//                    }
                }
                EntityTypeEnum.JOB -> {
                    // TODO : hschoi --> 무엇으로 채워야 할지 모름
                    TODO()
                }
                EntityTypeEnum.OTHER -> {
                    // TODO : hschoi --> 무엇으로 채워야 할지 모름
                    TODO()
                }
            }
        }
    }
    fun buildEntityType(item : DocumentItem) : EntityTypeEnum {
        if(StringUtils.hasText(item.customerId))
            return EntityTypeEnum.CUSTOMER

        if(StringUtils.hasText(item.vendorId))
            return EntityTypeEnum.VENDOR

        return EntityTypeEnum.OTHER
    }
    fun buildAccountRef(companyCode : String,item : DocumentItem) : ReferenceType {
        return ReferenceType().apply {
            val account = qboAccountService.findQboAccount(companyCode, item.accountCode)
            if(account != null) {
                if(account.key.qboId != null) {
                    value = account.key.qboId
                    name = account.key.accountName
                }
                else {
                    throw NoSuchFieldException("Validation ERROR : Not found Account-QBO-ID(${item.accountCode})-Document(${item.docId}) ")
                }
            }
            else {
                throw NoSuchFieldException("Validation ERROR : Not Found Account[${companyCode}-${item.accountCode}]")
            }
        }
    }
    fun buildJournalEntryLineAttribute(attribute : DocumentItemAttribute, lineDetail : JournalEntryLineDetail) {

        when(attribute.attributeId.attributeType) {
            DocumentAttributeType.COST_CENTER -> {
//                lineDetail.classRef = ReferenceType().apply {
//                    //사용 목적 : 부서, 프로젝트 등으로 라인을 세분화.
//                    value = attribute.value
//                    name = attribute.attributeId.attributeType.name
//                }
                TODO()
            }
            DocumentAttributeType.PROFIT_CENTER -> TODO()
            DocumentAttributeType.BUSINESS_AREA -> TODO()
            DocumentAttributeType.SEGMENT -> TODO()
            DocumentAttributeType.PROJECT -> TODO()
            DocumentAttributeType.CUSTOMER_ID -> TODO()
            DocumentAttributeType.ORDER_ID -> TODO()
            DocumentAttributeType.ORDER_ITEM_ID -> TODO()
            DocumentAttributeType.CONTRACT_ID -> TODO()
            DocumentAttributeType.SERIAL_NUMBER -> TODO()
            DocumentAttributeType.SALES_TYPE -> TODO()
            DocumentAttributeType.SALES_ITEM -> TODO()
            DocumentAttributeType.RENTAL_CODE -> TODO()
            DocumentAttributeType.LEASE_TYPE -> TODO()
            DocumentAttributeType.CONTRACT_DURATION -> TODO()
            DocumentAttributeType.COMMITMENT_DURATION -> TODO()
            DocumentAttributeType.CURRENT_TERM -> TODO()
            DocumentAttributeType.CHANNEL_TYPE -> TODO()
            DocumentAttributeType.CHANNEL_NAME -> TODO()
            DocumentAttributeType.CHANNEL_DETAIL -> TODO()
            DocumentAttributeType.REFERRAL_CODE -> TODO()
            DocumentAttributeType.CHARGE_ID -> TODO()
            DocumentAttributeType.VENDOR_ID -> TODO()
            DocumentAttributeType.PAYOUT_ID -> TODO()
            DocumentAttributeType.VENDOR_INVOICE_ID -> TODO()
            DocumentAttributeType.PURCHASE_ORDER -> TODO()
            DocumentAttributeType.MATERIAL_ID -> TODO()
            DocumentAttributeType.MATERIAL_TYPE -> TODO()
            DocumentAttributeType.MATERIAL_CATEGORY_CODE -> TODO()
            DocumentAttributeType.PRODUCT_CATEGORY -> TODO()
            DocumentAttributeType.MATERIAL_SERIES_CODE -> TODO()
            DocumentAttributeType.INSTALLATION_TYPE -> TODO()
            DocumentAttributeType.FILTER_TYPE -> TODO()
            DocumentAttributeType.FEATURE_TYPE -> TODO()
            DocumentAttributeType.CHANNEL_ID -> TODO()
            else -> TODO()
        }
    }
    fun buildLines(companyCode: String,docItems : MutableList<DocumentItem>) : MutableList<Line> {
        //        val credential = qboService.getCredential()
//        val biz = Business()
//        val division = Division()
//        val property = Property()
//        val store = Store()
//        val department = Department()
//        val territory = Territory()
        val lines = mutableListOf<Line>()
        docItems.forEach { item ->
            val line = Line().apply {
                detailType = LineDetailTypeEnum.JOURNAL_ENTRY_LINE_DETAIL
                amount = item.money.amount
                lineNum = item.lineNumber.toBigInteger()
                description = item.text
                taxLineDetail = TaxLineDetail().apply {
                    taxRateRef = ReferenceType().apply { }
                    //serviceDate = ""
                }
                journalEntryLineDetail = JournalEntryLineDetail().apply {

                    postingType = when (item.accountSide) {
                        AccountSide.DEBIT -> PostingTypeEnum.DEBIT
                        AccountSide.CREDIT -> PostingTypeEnum.CREDIT
                    }

                    // TODO hschoi -> attribute 용도 파악 완료 후 적용 예정

//                item.attributes.forEach { attr ->
//                    buildJournalEntryLineAttribute(attr,this)
//                }

                    entity = EntityTypeRef().apply {
                        type = buildEntityType(item)
                        entityRef = buildEntityRef(type,item)
                        accountRef = buildAccountRef(companyCode,item)
                    }
                }
            }
            lines.add(line)
        }
        return lines
    }
    fun buildJournalEntry(doc : Document) : JournalEntry {
        return JournalEntry().apply {
            txnDate = doc.postingDate.toDate()
            docNumber = doc.id
            privateNote = doc.text

        }
    }
    fun build(companyCode : String,
              docMap : MutableMap<String,Document>,
              block : (Document,JournalEntry,RoundingAdjustment)->Unit)  {
        docMap.forEach{docId,doc->
            try {
                val je = buildJournalEntry(doc)
                val lines = buildLines(companyCode,doc.items)
                je.apply { line.addAll(lines) }
                val adjustment = RoundingAdjustment(Constants.QBO_SCALE,matchConfig.getRoundingMode())
                if(!adjustment.execute(je)) {
                    logger.error {
                        "FAILED-ROUNDING-ADJUSTMENT[${companyCode}.${doc.id}]-" +
                                "DEBIT(${adjustment.debits}) " +
                                "does not equal " +
                                "CREDIT(${adjustment.credits})" }
                    return@forEach
                }
                block(doc,je,adjustment)
            }
            catch (e : Exception){
                logger.error ( "BUILD(JournalEntry)-ERROR[${e.message}]",e )
            }
        }
    }
}