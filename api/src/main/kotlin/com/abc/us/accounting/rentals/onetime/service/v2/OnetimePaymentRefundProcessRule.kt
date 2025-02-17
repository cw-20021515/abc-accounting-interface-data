package com.abc.us.accounting.rentals.onetime.service.v2

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.SalesTaxConfig
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
import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.iface.domain.model.Refund
import com.abc.us.accounting.iface.domain.model.RefundKind
import com.abc.us.accounting.iface.domain.repository.oms.IfOnetimePaymentRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.rentals.onetime.model.OnetimePaymentProcessItem
import com.abc.us.accounting.rentals.onetime.model.SalesTax
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

@Order(2)
@Component
class OnetimePaymentRefundProcessRule(
    private val salesTaxConfig: SalesTaxConfig,
    private val onetimePaymentRepository: IfOnetimePaymentRepository,
    private val orderItemRepository: IfOrderItemRepository,
    private val docTemplateServiceable: DocumentTemplateServiceable,
    private val documentMasterService: DocumentMasterService,
    private val companyService: CompanyService
): OnetimeProcessRule {

    companion object {
        private val logger = KotlinLogging.logger {}
    }
    override val name: String
        get() = this::class.java.simpleName

    override val supportedTemplateCodes: List<DocumentTemplateCode>
        get() = listOf(DocumentTemplateCode.ONETIME_PAYMENT_VOID, DocumentTemplateCode.ONETIME_PAYMENT_REFUND)

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
            slice = onetimePaymentRepository.findOnetimePayments(startTime, endTime, true, pageable)
            log(slice, startTime, endTime, pageable)

            val orderIds = slice.map { it.orderId }.distinct()
            val orderItems = orderItemRepository.findByOrderIdsIn(orderIds)
            if ( orderItems.isEmpty() ) {
                logger.warn {"orderItems must not be empty by orderIds: $orderIds"}
            }

            val processedData = slice.asSequence()
                .mapNotNull { onetimePayment ->
                    processRefundItems(context, companyCode, filteredDocTemplates, orderItems, onetimePayment)
                }.flatten()
                .distinctBy { it.docHash }
            results.addAll(processedData)
            pageNumber ++
        }while(slice.hasNext())

        return results.distinctBy { it.docHash }.take(context.maxResult)
    }

    private fun processRefundItems(
        context: DocumentServiceContext,
        companyCode: CompanyCode,
        filteredDocTemplates: List<DocumentTemplate>,
        orderItems: List<IfOrderItem>,
        onetimePayment: IfOnetimePayment,
    ): List<HashableDocumentRequest>? {
        val curOrderItems = orderItems.filter { it.orderId == onetimePayment.orderId }
        val refunds = onetimePayment.refunds ?: return null

        return refunds.mapNotNull { refund ->
            val orderItem = curOrderItems.filter { it.orderItemId == refund.orderItemId }.maxByOrNull { it.updateTime }
                ?: return@mapNotNull null

            processRefundItem(context, companyCode, filteredDocTemplates, onetimePayment, orderItem, refund)
        }.flatten()
    }

    private fun processRefundItem(
        context: DocumentServiceContext,
        companyCode: CompanyCode,
        filteredDocTemplates: List<DocumentTemplate>,
        onetimePayment: IfOnetimePayment,
        orderItem:IfOrderItem,
        refund: Refund,
    ): List<HashableDocumentRequest> {
        return filteredDocTemplates
            .filter { docTemplate ->
                FilteringRules.checkFilteringRule(
                    context,
                    docTemplate,
                    customerId = orderItem.customerId,
                    onetimePayment = onetimePayment,
                    orderItem = orderItem
                )
            }
            .map { docTemplate -> OnetimePaymentProcessItem(
                companyCode,
                docTemplate,
                customerId = orderItem.customerId,
                onetimePayment = onetimePayment,
                orderItem = orderItem,
                refund = refund
            )}
            .filter { processItem ->  checkCondition(context, processItem)}
            .mapNotNull { processItem ->
                when(processItem.docTemplate.docTemplateKey.docTemplateCode) {
                    DocumentTemplateCode.ONETIME_PAYMENT_VOID -> processPaymentVoid(context, processItem)
                    DocumentTemplateCode.ONETIME_PAYMENT_REFUND -> processPaymentRefund(context, processItem)
                    else -> null
                }
            }
    }

    private fun checkCondition (context:DocumentServiceContext, processItem: OnetimePaymentProcessItem): Boolean {
        val docTemplateCode = processItem.docTemplate.docTemplateKey.docTemplateCode
        val refund = processItem.refund

        val result = when (docTemplateCode) {
            DocumentTemplateCode.ONETIME_PAYMENT_VOID -> {
                when {
                    refund == null -> false
                    refund.kind != RefundKind.VOID -> false
                    else -> true
                }
            }
            DocumentTemplateCode.ONETIME_PAYMENT_REFUND -> {
                when {
                    refund == null -> false
                    refund.kind != RefundKind.REFUND -> false
                    else -> true
                }
            }
            else -> false
        }

        return result
    }



    private fun log(slice: Slice<IfOnetimePayment>, startTime: OffsetDateTime, endTime: OffsetDateTime, pageable: Pageable) {
        if (slice.isEmpty) {
            logger.warn ("ONETIME onetimePayments is empty by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        } else {
            logger.info("ONETIME onetimePayments fetch:${slice.size} by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        }
    }

    fun processPaymentVoid(context: DocumentServiceContext, processItem: OnetimePaymentProcessItem): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
        val orderItem = processItem.orderItem
        val refund = processItem.refund
        val currency  = companyService.getCompanyCurrency(companyCode)

        require(processItem.onetimePayment != null) { "onetimePayment must not be null" }
        val onetimePayment = processItem.onetimePayment

        if ( orderItem == null ) {
            logger.info { "${docTemplateCode} for onetimePayment - orderItemId is null by orderId: ${onetimePayment.orderId}, paymentId: ${onetimePayment.paymentId}" }
            return null
        }

        if ( refund == null ) {
            logger.info { "${docTemplateCode} for onetimePayment - refund is null by orderId: ${onetimePayment.orderId}, paymentId: ${onetimePayment.paymentId}" }
            return null
        }

        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_VOID)
        logger.info { "${docTemplateCode} for onetimePayment - orderId: ${onetimePayment.orderId}, paymentId: ${onetimePayment.paymentId}, orderItemId:${orderItem.orderItemId}" }


        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val subtotal = orderItem.subtotalPrice
        val tax = orderItem.tax
        val salesTax = SalesTax.of(salesTaxConfig, tax, orderItem.taxLines)
        val total = subtotal.plus(tax)

        if (!OnetimeUtils.validateSalesTax(salesTaxConfig, name, docTemplateCode,
                onetimePayment.orderId, onetimePayment.paymentId, orderItemId = orderItem.orderItemId, salesTax = salesTax, tax=tax)) {
            return null
        }

        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when(docTemplateItem.accountCode) {
                OnetimeAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, total, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.ADVANCED_PAYMENTS.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_STATE.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.state, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_COUNTY.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.county, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_CITY.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.city, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_SPECIAL.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.special, attributeTypeMasters, currency)
                }
                else -> null
            }

        }.toMutableList()

        val voidDate: LocalDate = OnetimeUtils.toLocalDate(onetimePayment.updateTime)
        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, onetimePayment.paymentId, orderItem.orderItemId)

        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = voidDate,
            postingDate = voidDate,
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

    fun processPaymentRefund(context: DocumentServiceContext, processItem: OnetimePaymentProcessItem): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
        val orderItem = processItem.orderItem
        val refund = processItem.refund
        val currency  = companyService.getCompanyCurrency(companyCode)

        require(processItem.onetimePayment != null) { "onetimePayment must not be null" }
        val onetimePayment = processItem.onetimePayment

        if ( orderItem == null ) {
            logger.info { "${docTemplateCode} for onetimePayment - orderItemId is null by orderId: ${onetimePayment.orderId}, paymentId: ${onetimePayment.paymentId}" }
            return null
        }

        if ( refund == null ) {
            logger.info { "${docTemplateCode} for onetimePayment - refund is null by orderId: ${onetimePayment.orderId}, paymentId: ${onetimePayment.paymentId}" }
            return null
        }

        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_REFUND)
        logger.info { "${docTemplateCode} for onetimePayment - orderId: ${onetimePayment.orderId}, payment_id: ${onetimePayment.paymentId}" }

        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val subtotal = orderItem.subtotalPrice
        val tax = orderItem.tax
        val salesTax = SalesTax.of(salesTaxConfig, tax, orderItem.taxLines)

        val total = subtotal.plus(tax)
        val fee = total.multiply(defaultTransactionFeeRate).toScale(Constants.ACCOUNTING_SCALE)
        val net = total.subtract(fee).toScale(Constants.ACCOUNTING_SCALE)

        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when(docTemplateItem.accountCode) {
                OnetimeAccountCode.ADVANCED_PAYMENTS.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.OTHER_ACCOUNTS_PAYABLE.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, net, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.BANK_SERVICE_CHARGE.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, fee, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_STATE.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.state, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_COUNTY.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.county, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_CITY.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.city, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.TAX_PAYABLE_SPECIAL.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.special, attributeTypeMasters, currency)
                }
                else -> null
            }

        }.toMutableList()

        val refundDate: LocalDate = OnetimeUtils.toLocalDate(onetimePayment.updateTime)
        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, onetimePayment.paymentId, orderItem.orderItemId)

        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = refundDate,
            postingDate = refundDate,
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