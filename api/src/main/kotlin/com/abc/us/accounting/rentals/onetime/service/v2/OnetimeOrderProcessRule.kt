package com.abc.us.accounting.rentals.onetime.service.v2

import com.abc.us.accounting.config.OnetimeConfig
import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.HashableDocumentRequest
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemStatus
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemType
import com.abc.us.accounting.logistics.domain.repository.InventoryCostingRepository
import com.abc.us.accounting.rentals.onetime.model.OnetimePaymentProcessItem
import com.abc.us.accounting.rentals.onetime.utils.FilteringRules
import com.abc.us.accounting.rentals.onetime.utils.OnetimeAccountCode
import com.abc.us.accounting.rentals.onetime.utils.OnetimeUtils
import com.abc.us.accounting.rentals.onetime.utils.OnetimeUtils.validateTemplateCode
import mu.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime


@Order(1)
@Component
class OnetimeOrderProcessRule(
    private val onetimeConfig: OnetimeConfig,
    private val orderItemRepository: IfOrderItemRepository,
    private val inventoryCostingRepository: InventoryCostingRepository,
    private val docTemplateServiceable: DocumentTemplateServiceable,
    private val documentMasterService: DocumentMasterService,
    private val companyService: CompanyService
):OnetimeProcessRule {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val name: String
        get() = this::class.java.simpleName

    override val supportedTemplateCodes: List<DocumentTemplateCode>
        get() = listOf(DocumentTemplateCode.ONETIME_SALES_RECOGNITION, DocumentTemplateCode.ONETIME_COGS_RECOGNITION, DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET)

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
        val onetimeOrderTypes = listOf(IfOrderItemType.PURCHASE, IfOrderItemType.ONETIME)
        val onetimeOrderStatuses = listOf(IfOrderItemStatus.INSTALL_COMPLETED)


        val results:MutableList<HashableDocumentRequest> = mutableListOf()
        var pageNumber = 0
        var slice: Slice<IfOrderItem>

        val pageable:Pageable = Pageable.unpaged()

        do {
            slice = orderItemRepository.findAllByTimeRange(startTime, endTime, onetimeOrderTypes, onetimeOrderStatuses, pageable)
            log(slice, startTime, endTime, pageable)

            val processedData = slice.asSequence()
                .map { orderItem ->
                    val customerId = orderItem.customerId

                    val processedByTemplates = filteredDocTemplates
                            .filter { docTemplate -> FilteringRules.checkFilteringRule(context, docTemplate, customerId = customerId, orderItem = orderItem) }
                            .mapNotNull { docTemplate ->
                                val processItem = OnetimePaymentProcessItem(companyCode, docTemplate, customerId, orderItem = orderItem)

                                val data = when(docTemplate.docTemplateKey.docTemplateCode) {
                                    DocumentTemplateCode.ONETIME_SALES_RECOGNITION -> processSalesRecognition(context, processItem = processItem)
                                    DocumentTemplateCode.ONETIME_COGS_RECOGNITION -> processCOGSRecognition(context, processItem = processItem)
                                    DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> processAdvancePamentOffset(context, processItem = processItem)
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

    private fun log(slice: Slice<IfOrderItem>, startTime: OffsetDateTime, endTime: OffsetDateTime, pageable: Pageable) {
        if (slice.isEmpty) {
            logger.warn ("ONETIME onetimePayments is empty by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        } else {
            logger.info("ONETIME onetimePayments fetch:${slice} by startTime:${startTime}, endTime:${endTime}, pageable:${pageable}")
        }
    }

    /**
     * condition:  orderItemStatus is PAYMENT_RECEIVED, orderType is ONETIME
     */
    fun processSalesRecognition(
        context: DocumentServiceContext,
        processItem: OnetimePaymentProcessItem
    ): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
        val currency  = companyService.getCompanyCurrency(companyCode)


        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_SALES_RECOGNITION)
        require(processItem.orderItem != null) { "${docTemplateCode} - orderItem must not be null" }
        val orderItem = processItem.orderItem
        logger.info { "${docTemplateCode} - orderItemId: ${orderItem.orderItemId}" }

        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val subtotal = orderItem.subtotalPrice


        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when(docTemplateItem.accountCode) {
                OnetimeAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
                }
                OnetimeAccountCode.SALES_WHOLESALES.getAccountCode(companyCode) -> {
                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
                }
                else -> null
            }

        }.toMutableList()

        val paymentReceiptDate: LocalDate = OnetimeUtils.toLocalDate(orderItem.updateTime)
        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, orderItem.orderItemId)

        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = paymentReceiptDate,
            postingDate = paymentReceiptDate,
            companyCode = companyCode,
            txCurrency = currency.name,
            reference = orderItem.orderItemId,
            text = docTemplate.korText,
            createTime = OffsetDateTime.now(),
            createdBy = docTemplate.bizSystem.toString(),
            docOrigin = docTemplate.toDocumentOriginRequest(orderItem.orderItemId),
            docItems = docItemRequests
        )

        return request
    }

    fun processCOGSRecognition(
        context: DocumentServiceContext,
        processItem: OnetimePaymentProcessItem
    ): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
        val currency  = companyService.getCompanyCurrency(companyCode)

        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED)
        require(processItem.orderItem != null) { "${docTemplateCode} - orderItem must not be null" }
        val orderItem = processItem.orderItem
        logger.info { "${docTemplateCode} - orderItemId: ${orderItem.orderItemId}" }

        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }
        return null

//        val inventoryCosting =
//
//
//        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
//            when(docTemplateItem.accountCode) {
//                OnetimeAccountCode.COGS_PRODUCT.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, total, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.INVENTORY_IN_TRANSIT.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
//                }
//                else -> null
//            }
//
//        }.toMutableList()
//
//        val paymentReceiptDate: LocalDate = OnetimeUtils.toLocalDate(onetimePayment.paymentTime)
//        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, paymentId = onetimePayment.paymentId)
//
//        val request = CreateDocumentRequest(
//            docType = docTemplate.documentType,
//            docHash = docHash,
//            documentDate = paymentReceiptDate,
//            postingDate = paymentReceiptDate,
//            companyCode = companyCode,
//            txCurrency = currency.name,
//            reference = onetimePayment.paymentId,
//            text = docTemplate.korText,
//            createTime = OffsetDateTime.now(),
//            createdBy = docTemplate.bizSystem.toString(),
//            docOrigin = docTemplate.toDocumentOriginRequest(onetimePayment.paymentId),
//            docItems = docItemRequests
//        )
//
//        return request
    }



    fun processAdvancePamentOffset(
        context: DocumentServiceContext,
        processItem: OnetimePaymentProcessItem
    ): HashableDocumentRequest? {
        val docTemplate = processItem.docTemplate
        val onetimePayment = processItem.onetimePayment
        val companyCode = processItem.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
        val currency  = companyService.getCompanyCurrency(companyCode)

        validateTemplateCode(docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED)
        require(processItem.orderItem != null) { "${docTemplateCode} - orderItem must not be null" }
        val orderItem = processItem.orderItem

        logger.info { "${docTemplateCode} - orderItemId: ${orderItem.orderItemId}" }

        val docTemplateItems = docTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }
        return null

//        val total = onetimePayment.totalPrice
//        val subtotal = onetimePayment.subtotalPrice
//        val tax = onetimePayment.tax
//        val salesTax = SalesTax.of(tax, onetimePayment.taxLines)
//
//        if (!OnetimeUtils.validateSalesTax(context, name, docTemplateCode,
//                onetimePayment.orderId, onetimePayment.paymentId, orderItemId = null,salesTax=salesTax, tax=tax)){
//            return null
//        }
//
//
//        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
//            when(docTemplateItem.accountCode) {
//                OnetimeAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, total, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.ADVANCED_PAYMENTS.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, subtotal, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.TAX_PAYABLE_STATE.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.state, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.TAX_PAYABLE_COUNTY.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.county, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.TAX_PAYABLE_CITY.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.city, attributeTypeMasters, currency)
//                }
//                OnetimeAccountCode.TAX_PAYABLE_SPECIAL.getAccountCode(companyCode) -> {
//                    val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(docTemplateItem.accountCode))
//                    OnetimeUtils.toDocumentItemRequest(context, processItem, docTemplateItem, salesTax.special, attributeTypeMasters, currency)
//                }
//                else -> null
//            }
//
//        }.toMutableList()
//
//        val paymentReceiptDate: LocalDate = OnetimeUtils.toLocalDate(onetimePayment.paymentTime)
//        val docHash = OnetimeUtils.onetimeDocHash(companyCode, docTemplateCode, paymentId = onetimePayment.paymentId)
//
//        val request = CreateDocumentRequest(
//            docType = docTemplate.documentType,
//            docHash = docHash,
//            documentDate = paymentReceiptDate,
//            postingDate = paymentReceiptDate,
//            companyCode = companyCode,
//            txCurrency = currency.name,
//            reference = onetimePayment.paymentId,
//            text = docTemplate.korText,
//            createTime = OffsetDateTime.now(),
//            createdBy = docTemplate.bizSystem.toString(),
//            docOrigin = docTemplate.toDocumentOriginRequest(onetimePayment.paymentId),
//            docItems = docItemRequests
//        )
//
//        return request
    }

}