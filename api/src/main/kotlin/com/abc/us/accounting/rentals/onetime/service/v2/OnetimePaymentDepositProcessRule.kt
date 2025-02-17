package com.abc.us.accounting.rentals.onetime.service.v2

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.HashableDocumentRequest
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.iface.domain.entity.oms.IfOnetimePayment
import com.abc.us.accounting.iface.domain.repository.oms.IfOnetimePaymentRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.rentals.onetime.model.OnetimePaymentProcessItem
import com.abc.us.accounting.rentals.onetime.service.OnetimeDocumentService.Companion.defaultTransactionFeeRate
import com.abc.us.accounting.rentals.onetime.utils.FilteringRules
import com.abc.us.accounting.rentals.onetime.utils.OnetimeAccountCode
import com.abc.us.accounting.rentals.onetime.utils.OnetimeUtils
import com.abc.us.accounting.rentals.onetime.utils.OnetimeUtils.validateTemplateCode
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import mu.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime

@Order(3)
@Component
class OnetimePaymentDepositProcessRule(
    private val onetimePaymentRepository: IfOnetimePaymentRepository,
    private val orderItemRepository: IfOrderItemRepository,
    private val docTemplateServiceable: DocumentTemplateServiceable,
    private val documentMasterService: DocumentMasterService,
    private val companyService: CompanyService
): OnetimeProcessRule
{

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val name: String
        get() = this::class.java.simpleName

    override val supportedTemplateCodes: List<DocumentTemplateCode>
        get() = listOf(DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT)

    override fun toString():String  = name

    override fun process(
        context: DocumentServiceContext,
        companyCode: CompanyCode,
        docTemplates: List<DocumentTemplate>,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): List<HashableDocumentRequest> {
        logger.info("$name, prepare, companyCode:$companyCode, startTime:$startTime, endTime:$endTime, docTemplateCodes:${docTemplates.map { it.docTemplateKey.docTemplateCode }}")

        require(companyCode.isSalesCompany()) { "companyCode is not sales company: $companyCode" }
        require(docTemplates.isNotEmpty()) { "docTemplates is empty" }

        val filteredDocTemplates = docTemplates.filter { supportedTemplateCodes.contains(it.docTemplateKey.docTemplateCode) }

        val results:MutableList<HashableDocumentRequest> = mutableListOf()
        var pageNumber = 0
        var slice: Slice<IfOnetimePayment>

        val pageable: Pageable = Pageable.unpaged()

        do {
            slice = onetimePaymentRepository.findOnetimePayments(startTime, endTime, false, pageable)
            log(slice, startTime, endTime, pageable)

            val orderIds = slice.map { it.orderId }.distinct()
            val orderItems = orderItemRepository.findDistinctByOrderIdsIn(orderIds).associateBy { it.orderId }

            val processedData = slice.asSequence()
                .map { onetimePayment ->
                    val orderItem = orderItems[onetimePayment.orderId]!!
                    val customerId = orderItem.customerId

                    val processedByTemplates = filteredDocTemplates
                        .filter { docTemplate -> FilteringRules.checkFilteringRule(context, docTemplate, onetimePayment, customerId = customerId) }
                        .mapNotNull { docTemplate ->
                            val processItem = OnetimePaymentProcessItem(companyCode, docTemplate, customerId, onetimePayment)

                            val data = when(docTemplate.docTemplateKey.docTemplateCode) {
                                DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> processPaymentDeposit(context, processItem = processItem)
                                else -> null
                            }
                            data
                        }
                    processedByTemplates
                }.flatten()
                .distinctBy { it.docHash }
            results.addAll(processedData)
            pageNumber ++
        }while(slice.hasNext())

        return results.distinctBy { it.docHash }.take(context.maxResult)
    }

    private fun log(slice: Slice<IfOnetimePayment>, startTime: OffsetDateTime, endTime: OffsetDateTime, pageable: Pageable) {
        if (slice.isEmpty) {
            logger.warn ("ONETIME onetimePayments is empty by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        } else {
            logger.info("ONETIME onetimePayments fetch:${slice} by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        }
    }

    /**
     * condition:  orderItemStatus is PAYMENT_RECEIVED, orderType is ONETIME
     */
    fun processPaymentDeposit(
        context: DocumentServiceContext,
        processItem: OnetimePaymentProcessItem
    ): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode

        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT)
        require(processItem.onetimePayment != null) { "onetimePayment must not be null" }
        val onetimePayment = processItem.onetimePayment

        logger.info { "${docTemplateCode} for onetimePayment - orderId: ${onetimePayment.orderId}, payment_id: ${onetimePayment.paymentId}" }

        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val currency = companyService.getCompanyCurrency(companyCode)

        val total = onetimePayment.totalPrice
        val fee = total.multiply(defaultTransactionFeeRate).toScale(Constants.ACCOUNTING_SCALE)
        val net = total.subtract(fee).toScale(Constants.ACCOUNTING_SCALE)


        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when(docTemplateItem.accountCode) {
                OnetimeAccountCode.DEPOSITS_ON_DEMAND_1.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, net, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.BANK_SERVICE_CHARGE.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, fee, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, total, attributeTypeMasters, currency)
                }
                else -> null
            }

        }.toMutableList()

        val paymentDepositDate: LocalDate = OnetimeUtils.toLocalDate(onetimePayment.paymentTime).plusDays(3)
        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, onetimePayment.paymentId)

        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = paymentDepositDate,
            postingDate = paymentDepositDate,
            companyCode = companyCode,
            txCurrency = currency.name,
            reference = onetimePayment.paymentId,
            text = docTemplate.korText,
            createTime = OffsetDateTime.now(),
            createdBy = docTemplate.bizSystem.toString(),
            docOrigin = docTemplate.toDocumentOriginRequest(onetimePayment.paymentId),
            docItems = docItemRequests
        )

        return request
    }
}