package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.collects.domain.type.MaterialType
import com.abc.us.accounting.collects.domain.type.SalesTaxType
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.OnetimeConfig
import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentServiceable
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderItemWithExtraInfo
import com.abc.us.accounting.rentals.onetime.domain.type.OnetimeCriteriaResult
import com.abc.us.accounting.rentals.onetime.utils.OnetimeAccountCode
import com.abc.us.accounting.supports.utils.BigDecimals.equalsWithScale
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.Hashs
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class OnetimeDocumentService(
    private val onetimeConfig: OnetimeConfig,
    private val documentServiceable: DocumentServiceable,
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val orderItemRepository: CollectOrderItemRepository,
    private val serviceFlowRepository: CollectServiceFlowRepository,
    private val installationRepository: CollectInstallationRepository,
    private val inventoryValuationRepository: CollectInventoryValuationRepository,
    private val materialRepository: CollectMaterialRepository,
    private val taxLineRepository: CollectTaxLineRepository,
    private val channelRepository: CollectChannelRepository,
    private val documentMasterService: DocumentMasterService,
    private val companyService: CompanyService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        const val DEFAULT_CUSTOMER_ID = "default_customer_id"
        // 결제 수수료: 2.5% 가정
        const val DEFAULT_MAX_RESULT = Int.MAX_VALUE
        val defaultTransactionFeeRate = BigDecimal(0.025).toScale()

        data class OnetimeProcessRequest (
            val docTemplate: DocumentTemplate,
            val orderItem: CollectOrderItem,
            val material: CollectMaterial,
            val serviceFlowItem:CollectServiceFlow? = null,
            val installItem: CollectInstallation? = null,
            val inventoryValuation: CollectInventoryValuation? = null,
            val taxlines:List<CollectTaxLine> = listOf(),
            val customerId: String? = null,
            val channel: CollectChannel? = null,
            val referralCode:String? = null,
        )


        // 구현 변경 필요
        enum class SalesTaxMock(val rate: BigDecimal) {
            STATE( BigDecimal(0.0625).toScale()),
            COUNTY(BigDecimal.ZERO.toScale()),
            CITY(BigDecimal(0.005).toScale()),
            SPECIAL( BigDecimal(0.015).toScale())
            ;

            fun salesTaxAmount(amount: BigDecimal): BigDecimal {
                return amount.multiply(rate)
            }

            companion object {
                fun fromSalesTaxType (taxType: SalesTaxType): SalesTaxMock {
                    return when(taxType) {
                        SalesTaxType.STATE -> STATE
                        SalesTaxType.CITY -> CITY
                        SalesTaxType.COUNTY -> COUNTY
                        SalesTaxType.SPECIAL -> SPECIAL
                        else -> throw IllegalArgumentException("Invalid DefaultSalesTax taxType: $taxType")
                    }
                }

                private fun getSalesTaxAmount(amount: BigDecimal): BigDecimal {
                    return listOf(STATE, COUNTY, CITY, SPECIAL).map { it.salesTaxAmount(amount) }.sumOf { it }
                }

                fun getTaxIncludedAmount(amount: BigDecimal): BigDecimal {
                    return amount + getSalesTaxAmount(amount)
                }
            }
        }
    }

    fun processOnetimeBatch(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult:Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        logger.info("processOnetimeBatch: companyCode:$companyCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        val templateCodes = DocumentTemplateCode.findAllBySalesType(SalesType.ONETIME).sortedBy { it.ordinal }

        return processOnetimeBatchWithTemplateCodes(context, companyCode, templateCodes, startTime, endTime, maxResult = maxResult)
    }


    fun processPaymentReceived(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED
        logger.info("processOrderReceipt: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }

    fun processPaymentDeposit(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT
        logger.info("processPaymentDeposit: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }

    fun processProductShipped(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED
        logger.info("processProductShipped: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }


    fun processSalesRecognition(context:DocumentServiceContext, companyCode: CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_SALES_RECOGNITION
        logger.info("processProductShipped: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }


    fun processCOGSRecognition(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_COGS_RECOGNITION
        logger.info("processCostRecognition: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }


    fun processAdvancedPaymentOffset(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult: Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        val templateCode = DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET
        logger.info("processAdvancedPaymentOffset: companyCode:$companyCode, templateCode:$templateCode, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(templateCode), startTime, endTime, maxResult)
    }



    fun processOnetimeBatchWithTemplateCodes(context:DocumentServiceContext, companyCode: CompanyCode, templateCodes: List<DocumentTemplateCode>, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult:Int = DEFAULT_MAX_RESULT):List<DocumentResult> {
        logger.info("processOnetimeBatchWithTemplateCodes: companyCode:$companyCode, templateCodes:${templateCodes}, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")

        val requests = prepareOnetimeBatchRequest(context, companyCode, templateCodes, startTime, endTime, maxResult)

        logger.info("prepareOnetimeBatchRequest: companyCode:$companyCode, startTime: $startTime, endTime: $endTime, requests.size: ${requests.size}")
        val postingRequests = requests.filterIsInstance<CreateDocumentRequest>()
        if ( postingRequests.isNotEmpty()) {
            return documentServiceable.posting(context, postingRequests)
        }
        logger.info("processOnetimeBatchWithTemplateCodes: posting ignored by posting request is empty, companyCode:$companyCode, templateCodes:${templateCodes}, startTime: $startTime, endTime: $endTime, maxResult: $maxResult")
        return emptyList()
    }

    /**
     * oderItems 에서 읽어서 여러건 처리 하는 경우
     *
     * TODO
     * 1. 주문이 정확한 상태를 읽어서 처리 되도록 고려 : OK
     * 2. 부품 판매에 대한 회계처리 고려: 추후 고려
     * 3. ServiceFlow 상태에 대한 정합성 고려: OK
     * 4. 입금(Deposit)에 대한 회계처리 고려(입금 정보를 통해서 확인해야 함)
     * 5. 판매세 연계: OK (정합성 문제 있음)
     * 6. 고객정보 연계: OK (정합성 문제 수정)
     * 7. 기타 주문이 아닌 경우에 대한 회계처리 고려 (orderItem의 시간범위에 안들어 오더라도 이전에 처리된 경우에 대한 고려)
     */
    fun prepareOnetimeBatchRequest(context:DocumentServiceContext, companyCode: CompanyCode, templateCodes: List<DocumentTemplateCode>,
                                   startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now(), maxResult:Int = DEFAULT_MAX_RESULT
    ): List<HashableDocumentRequest> {
        require(companyCode.isSalesCompany()) { "companyCode is not sales company: $companyCode" }

        val orderItemTypes = listOf(OrderItemType.ONETIME)
        val orderItemStatuses = listOf(OrderItemStatus.ORDER_RECEIVED, OrderItemStatus.BOOKING_CONFIRMED, OrderItemStatus.INSTALL_COMPLETED)
        val serviceTypes = listOf(ServiceFlowType.INSTALL)
        val serviceStatuses = listOf(ServiceFlowStatus.SERVICE_SCHEDULED, ServiceFlowStatus.SERVICE_COMPLETED)

        val docTemplates = documentTemplateServiceable.findDocTemplates(companyCode, templateCodes)
        require(docTemplates.isNotEmpty()) { "docTemplates is empty by templateCodes: $templateCodes" }

        val results:MutableList<HashableDocumentRequest> = mutableListOf()
        var pageNumber = 0
        var slice: Slice<CollectOrderItemWithExtraInfo>

        //TODO: 추후 페이징 처리 다시 고민 필요
        val pageable:Pageable = Pageable.unpaged()
        do {
            slice = orderItemRepository.findAllWithCustomerIdByCriteria(startTime, endTime, orderItemTypes, orderItemStatuses, pageable)
            if ( slice.isEmpty) {
                logger.warn ("ONETIME orderItems is empty by startTime: $startTime, endTime: $endTime")
            } else {
                logger.info ("ONETIME orderItems fetch:${slice.size} by startTime: $startTime, endTime: $endTime")
            }

            val channelIds = slice.mapNotNull { it.channelId }.distinct()

            val orderItemIds = slice.map { it.collectOrderItem.orderItemId }.distinct()
            val materialIds = slice.map { it.collectOrderItem.materialId!! }.distinct()
            val serviceFlows = serviceFlowRepository.findAllBy(orderItemIds, serviceTypes, serviceStatuses, startTime, endTime).groupBy { it.orderItemId!! }
            val installations = installationRepository.findValidByOrderItemIdIn(orderItemIds).associateBy { it.orderItemId!! }
            val materials = materialRepository.findAllByMaterialIdIn(materialIds).associateBy { it.materialId }
            val inventoryValues = inventoryValuationRepository.findAllBy(materialIds, issueTime =  endTime).associateBy { it.materialId }
            val taxLines = taxLineRepository.findByOrderItemIdIn(orderItemIds).groupBy { it.relation?.value!! }
            val channels = channelRepository.findAllByActiveChannelIdIn(channelIds).associateBy { it.channelId!! }

            if(materials.size != materialIds.size) {
                val missedMaterialIds = materialIds.filter { !materials.containsKey(it) }
                logger.warn("materials size is not matched by materials.size:${materials.size}, materialIds: ${materialIds.size}, missedMaterialIds: $missedMaterialIds by materialIds: $materialIds")
            }
            if (inventoryValues.size != materialIds.size) {
                val missedMaterialIds = materialIds.filter { !inventoryValues.containsKey(it) }
                logger.warn("inventoryValues is not matched inventoryValues:${inventoryValues.size}, materialIds:${materials.size} by missedMaterialIds:${missedMaterialIds}, materialIds: $materialIds")
            }

            val nonProductOrderItemIds = slice.asSequence().filter {
                val material = materials[it.collectOrderItem.materialId]
                val filter = ( material == null || material.materialType != MaterialType.PRODUCT)
                filter
            }.map { it.collectOrderItem.orderItemId }.toList()
            if ( nonProductOrderItemIds.isNotEmpty() ) {
                logger.warn("Found order items with non-product material types:size:${nonProductOrderItemIds.size}, orderItemIds: $nonProductOrderItemIds")
            }

            val orderItemIdsNotFoundByServiceFlows = orderItemIds.filter { orderItemId -> !serviceFlows.keys.contains(orderItemId)}
            if ( orderItemIdsNotFoundByServiceFlows.isNotEmpty() ) {
                logger.warn("serviceFlowItems is null or empty by orderItemIds: $orderItemIdsNotFoundByServiceFlows")
            }

            val processData = slice.asSequence()
                .filter { it -> checkFilteringRule(context, orderItem = it.collectOrderItem, customerId = it.customerId) }
                .map { orderItemWithExtraInfo ->
                    val orderItem = orderItemWithExtraInfo.collectOrderItem
                    val serviceFlowItems = serviceFlows[orderItem.orderItemId]
                    val channel = channels[orderItemWithExtraInfo.channelId]
                    val processData = if (serviceFlowItems.isNullOrEmpty()) {
                        logger.warn { "serviceFlowItems is null or empty by orderItemId: ${orderItem.orderItemId}"}
                        prepareOnetimeBatchRequest(context, docTemplates, materials, inventoryValues, installations, taxLines, orderItemWithExtraInfo, null, channel = channel)
                    }else {
                        logger.info{"serviceFlowItems: ${serviceFlowItems.size} by orderItemId: ${orderItem.orderItemId}, serviceStatus:${serviceFlowItems.map{it.serviceStatus}}"}
                        serviceFlowItems.map { serviceFlowItem ->
                            prepareOnetimeBatchRequest(context, docTemplates, materials, inventoryValues, installations, taxLines, orderItemWithExtraInfo, serviceFlowItem, channel)
                        }.flatten()
                    }
                    processData
                }.flatten()
                .distinctBy { it.docHash }

            results.addAll(processData)
            if ( results.size >= maxResult) {
                break
            }
            pageNumber ++
        }while(slice.hasNext())

        return results.distinctBy { it.docHash }.take(maxResult)
    }

    fun prepareOnetimeBatchRequest(context:DocumentServiceContext,
                                   docTemplates:List<DocumentTemplate>,
                                   materials: Map<String, CollectMaterial>,
                                   inventoryValues: Map<String, CollectInventoryValuation>,
                                   installations: Map<String, CollectInstallation>,
                                   taxLines: Map<String, List<CollectTaxLine>>,
                                   orderItemWithExtraInfo: CollectOrderItemWithExtraInfo,
                                   serviceFlowItem: CollectServiceFlow? = null,
                                   channel:CollectChannel?= null): List<HashableDocumentRequest> {
        val orderItem = orderItemWithExtraInfo.collectOrderItem
        val customerId = orderItemWithExtraInfo.customerId
        val referralCode = orderItemWithExtraInfo.referralCode


        val material = materials[orderItem.materialId!!]
        val inventoryValuation = inventoryValues[orderItem.materialId!!]
        val installItem = installations[orderItem.orderItemId]
        val filteredTaxLines = taxLines[orderItem.orderItemId] ?: emptyList()
        if (material == null) {
            logger.warn("material is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}")
        }
        logger.info{"prepareOnetimeBatchRequest, orderItemId:${orderItem.orderItemId}, orderItemStatus:${orderItem.orderItemStatus}, " +
                "serviceType:${serviceFlowItem?.serviceType}, serviceStatus:${serviceFlowItem?.serviceStatus}, installTime:${installItem?.installationTime}, docTemplateCodes: ${docTemplates.map { it.docTemplateKey.docTemplateCode }}" }

        val processData = docTemplates
            .filter { docTemplate -> checkFilteringRule(context, docTemplate, orderItem, material, serviceFlowItem, installItem, inventoryValuation, customerId = customerId)}
            .filter { docTemplate -> checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installItem, inventoryValuation) }
            .mapNotNull { docTemplate ->
                val onetimeProcessRequest = OnetimeProcessRequest(docTemplate, orderItem, material!!, serviceFlowItem, installItem, inventoryValuation, filteredTaxLines, customerId, channel, referralCode )

                when (docTemplate.docTemplateKey.docTemplateCode) {
                    DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED -> preparePaymentReceipt(context, onetimeProcessRequest)
                    DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> preparePaymentDeposit(context, onetimeProcessRequest)
                    DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED -> prepareProductShipped(context, onetimeProcessRequest)
                    DocumentTemplateCode.ONETIME_SALES_RECOGNITION -> prepareSalesRecognition(context, onetimeProcessRequest)
                    DocumentTemplateCode.ONETIME_COGS_RECOGNITION -> prepareCOGSRecognition(context, onetimeProcessRequest)
                    DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> prepareAdvancePaymentOffset(context, onetimeProcessRequest)
                    else -> null
                }
            }
        return processData
    }

    /**
     * filtering rule
     */
    fun checkFilteringRule(context:DocumentServiceContext,
                           docTemplate: DocumentTemplate? = null,
                           orderItem: CollectOrderItem,
                           material: CollectMaterial? = null,
                           serviceFlowItem:CollectServiceFlow? = null,
                           installItem: CollectInstallation? = null,
                           inventoryValuation: CollectInventoryValuation? = null,
                           customerId:String? = null):Boolean {
        val rule = context.filteringRule ?: run {
            logger.trace { "filterWithContext, No filtering rule" }
            return true
        }
        val companyCode = docTemplate?.docTemplateKey?.companyCode

        return with(rule) {
            when {
                docTemplateCodes.isNotEmpty() && ( docTemplate != null &&  !docTemplateCodes.contains(docTemplate.docTemplateKey.docTemplateCode) ) ->
                    logAndReturnFalse(rule, "docTemplate", docTemplateCodes, docTemplate.docTemplateKey.docTemplateCode)

                companyCodes.isNotEmpty() && !companyCodes.contains(companyCode) ->
                    logAndReturnFalse(rule, "company", companyCodes, companyCode)

                orderIds.isNotEmpty() && !orderIds.contains(orderItem.orderId) ->
                    logAndReturnFalse(rule, "orderId", orderIds, orderItem.orderId)

                orderItemIds.isNotEmpty() && !orderItemIds.contains(orderItem.orderItemId) ->
                    logAndReturnFalse(rule, "orderItem", orderItemIds, orderItem.orderItemId)

                customerIds.isNotEmpty() && ( customerId != null && !customerIds.contains(customerId)) ->
                    logAndReturnFalse(rule, "customerId", customerIds, customerId)

                materialIds.isNotEmpty() && ( material != null && !materialIds.contains(material.materialId)) ->
                    logAndReturnFalse(rule, "material", materialIds, material.materialId)

                serviceFlowIds.isNotEmpty() && ( serviceFlowItem != null && !serviceFlowIds.contains(serviceFlowItem.serviceFlowId) )  ->
                    logAndReturnFalse(rule, "serviceFlow", serviceFlowIds, serviceFlowItem.serviceFlowId)

                bisSystems.isNotEmpty() && ( docTemplate != null && !bisSystems.contains(docTemplate.bizSystem)) ->
                    logAndReturnFalse(rule, "bizSystem", bisSystems, docTemplate.bizSystem)

                bizProcesses.isNotEmpty() && (docTemplate != null && !bizProcesses.contains(docTemplate.bizProcess)) ->
                    logAndReturnFalse(rule, "bizProcess", bizProcesses, docTemplate.bizProcess)

                bizEvents.isNotEmpty() && (docTemplate != null && !bizEvents.contains(docTemplate.bizEvent)) ->
                    logAndReturnFalse(rule, "bizEvent", bizEvents, docTemplate.bizEvent)

                accountingEvents.isNotEmpty() && !accountingEvents.contains(docTemplate?.accountEvent?.name) ->
                    logAndReturnFalse(rule, "accountingEvent", accountingEvents, docTemplate?.accountEvent?.name)

                else -> {
                    logger.trace { "checkFilteringRule, Succeeded - All checks passed by context filtering rule: $rule" }
                    true
                }
            }
        }
    }

    private fun logAndReturnFalse(rule:FilteringRule, fieldName: String, expected: Any, actual: Any?): Boolean {
        logger.trace { "checkFilteringRule, Failed $fieldName check - expected: $expected, actual: $actual by filtering rule:$rule" }
        return false
    }

    /**
     * complexCriteria filtering rule
     */
    fun checkComplexCriteria(context:DocumentServiceContext,
                             docTemplate: DocumentTemplate,
                             orderItem: CollectOrderItem,
                             material: CollectMaterial? = null,
                             serviceFlowItem:CollectServiceFlow? = null,
                             installItem: CollectInstallation? = null,
                             inventoryValuation: CollectInventoryValuation? = null): Boolean {

        val defaultResult:OnetimeCriteriaResult = when { // 공통 조건
            orderItem.orderItemType != OrderItemType.ONETIME -> OnetimeCriteriaResult.ORDER_ITEM_TYPE_IS_NOT_ONETIME
            orderItem.materialId == null -> OnetimeCriteriaResult.ORDER_ITEM_MATERIAL_IS_NULL
            material == null -> OnetimeCriteriaResult.MATERIAL_IS_NULL
            material.materialType != MaterialType.PRODUCT -> OnetimeCriteriaResult.MATERIAL_TYPE_IS_NOT_PRODUCT          // TODO: PRODUCT 가 아닌 경우 추가 구현 필요
            else -> OnetimeCriteriaResult.SUCCEEDED
        }

        val detailResult:OnetimeCriteriaResult = when (docTemplate.docTemplateKey.docTemplateCode) {
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED -> {
                when {
                    orderItem.orderItemStatus != OrderItemStatus.ORDER_RECEIVED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_RECEIVED
                    else -> OnetimeCriteriaResult.SUCCEEDED
                }
            }
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> {
                when {
                    orderItem.orderItemStatus != OrderItemStatus.ORDER_RECEIVED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_RECEIVED
                    else -> OnetimeCriteriaResult.SUCCEEDED
                }
            }
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED -> {
                when {
                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
                    inventoryValuation == null -> OnetimeCriteriaResult.INVENTORY_VALUATION_IS_NULL
                    orderItem.orderItemStatus != OrderItemStatus.BOOKING_CONFIRMED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_BOOKING_CONFIRMED
                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_SCHEDULED  -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_SCHEDULED
                    else -> OnetimeCriteriaResult.SUCCEEDED
                }
            }
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION, DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> {
                when {
//                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
                    installItem == null -> OnetimeCriteriaResult.INSTALL_ITEM_IS_NULL                        // 설치정보가 없으면 처리 불가 (임시)
                    installItem.installationTime == null -> OnetimeCriteriaResult.INSTALL_TIME_IS_NULL
                    orderItem.orderItemStatus != OrderItemStatus.INSTALL_COMPLETED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_INSTALL_COMPLETED
//                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
//                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_COMPLETED -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_COMPLETED
                    else -> OnetimeCriteriaResult.SUCCEEDED
                }
            }
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION -> {
                when {
//                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
                    installItem == null -> OnetimeCriteriaResult.INSTALL_ITEM_IS_NULL
                    installItem.installationTime == null -> OnetimeCriteriaResult.INSTALL_TIME_IS_NULL
                    inventoryValuation == null -> OnetimeCriteriaResult.INVENTORY_VALUATION_IS_NULL                                                         // 재고가 없으면 처리 불가 (임시)
                    orderItem.orderItemStatus  != OrderItemStatus.INSTALL_COMPLETED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_INSTALL_COMPLETED
//                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
//                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_COMPLETED -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_COMPLETED
                    else -> OnetimeCriteriaResult.SUCCEEDED
                }
            }
            else -> OnetimeCriteriaResult.FAILED
        }
        val result = OnetimeCriteriaResult.failedResults(defaultResult, detailResult)
        if ( context.debug ) {
            val message=  "checkComplexCriteria - result:{} by docTemplateCode: {}, orderItemId: {}, orderItemStatus:{}, serviceFlowId: {}, installId: {}, installTime:{}, valuation: {}, serviceFlowType:{}, serviceFlowStatus:{}"
            if ( result.isNotEmpty() ) {
                logger.info(message,
                    result,
                    docTemplate.docTemplateKey.docTemplateCode,
                    orderItem.orderItemId,
                    orderItem.orderItemStatus,
                    serviceFlowItem?.serviceFlowId,
                    installItem?.installId,
                    installItem?.installationTime,
                    inventoryValuation?.materialId,
                    serviceFlowItem?.serviceType,
                    serviceFlowItem?.serviceStatus
                )
            } else {
                logger.trace(message,
                    result,
                    docTemplate.docTemplateKey.docTemplateCode,
                    orderItem.orderItemId,
                    orderItem.orderItemStatus,
                    serviceFlowItem?.serviceFlowId,
                    installItem?.installId,
                    installItem?.installationTime,
                    inventoryValuation?.materialId,
                    serviceFlowItem?.serviceType,
                    serviceFlowItem?.serviceStatus
                )

            }
        }
        return result.isEmpty()
    }

    fun validateTemplateCode(actual: DocumentTemplateCode, expected:DocumentTemplateCode) {
        require(actual == expected) {
            "docTemplate is not same!!, actual: ${actual}, expected: $expected"
        }
    }

    fun onetimeDocHash(docTemplate: DocumentTemplate, orderItem: CollectOrderItem):String {
        val companyCode = docTemplate.docTemplateKey.companyCode
        val docTemplateCode = docTemplate.docTemplateKey.docTemplateCode
//        val docHash = Hashs.hash(companyCode, docTemplateCode, orderItem.orderItemId)
        val hash = Hashs.hash(companyCode, docTemplateCode, orderItem.orderItemId)
        val docHash = "${hash}.${companyCode}.${docTemplateCode}.${orderItem.orderItemId}"
        return docHash
    }

    /**
     * condition:  orderItemStatus is PAYMENT_RECEIVED, orderType is ONETIME
     */
    fun preparePaymentReceipt(
        context: DocumentServiceContext,
        prepareRequest: OnetimeProcessRequest
    ): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem = prepareRequest.orderItem
        val material = prepareRequest.material
        val taxlines = prepareRequest.taxlines

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED)
        require(checkComplexCriteria(context, docTemplate, orderItem, material) ) {
            "meetsComplexCriteria not matched by template: $docTemplate, orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        logger.info { "prepareOrderReceipt for orderItemId: ${orderItem.orderItemId}, orderItemStatus: ${orderItem.orderItemStatus}" }

        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        require(orderItem.materialId != null) {
            "materialId is null by orderItemId: ${orderItem.orderItemId}"
        }
        val onetimeAmount = getOnetimeBaseAmount(orderItem)
        val stateTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.STATE, orderItem, taxlines)
        val countyTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.COUNTY, orderItem, taxlines)
        val cityTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.CITY, orderItem, taxlines)
        val calculationSpecialTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.SPECIAL, orderItem, taxlines)
        val onetimeTaxIncludedAmount = getOnetimeTaxIncludedAmount(orderItem, taxlines)

        val specialTaxAmount = onetimeTaxIncludedAmount - onetimeAmount - stateTaxAmount - countyTaxAmount - cityTaxAmount
        val diff = specialTaxAmount.compareTo(calculationSpecialTaxAmount)
        if ( diff != 0) {
            logger.warn("special tax difference:$diff by special taxline:$calculationSpecialTaxAmount, actual:$specialTaxAmount, orderItemId:${orderItem.orderItemId}")
        }
        val companyCode = docTemplate.docTemplateKey.companyCode


        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when (docTemplateItem.accountCode) {
                OnetimeAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, onetimeTaxIncludedAmount)
                OnetimeAccountCode.ADVANCED_PAYMENTS.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, onetimeAmount)
                OnetimeAccountCode.TAX_PAYABLE_STATE.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, stateTaxAmount)
                OnetimeAccountCode.TAX_PAYABLE_COUNTY.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, countyTaxAmount)
                OnetimeAccountCode.TAX_PAYABLE_CITY.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, cityTaxAmount)
                OnetimeAccountCode.TAX_PAYABLE_SPECIAL.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, specialTaxAmount)
                else -> null
            }
        }.toMutableList()


        if ( orderItem.updateTime == null ) {
            logger.warn { "updateTime is null by orderItemId:${orderItem.orderItemId}, please check source data, orderReceiptDate treat to today" }
        }
        val orderReceiptDate:LocalDate = orderItem.updateTime?.toLocalDate() ?: LocalDate.now()

        val currency  = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)

        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = orderReceiptDate,
            postingDate = orderReceiptDate,
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

    /**
     * 입금완료
     * condition:  orderItemStatus is ORDER_RECEIVED, orderItem is PURCHASE
     * 입금은 주문접수일 + 3일 이라고 가정
     * 반제전표임
     */
    fun preparePaymentDeposit (context:DocumentServiceContext, prepareRequest: OnetimeProcessRequest): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem = prepareRequest.orderItem
        val material = prepareRequest.material
        val taxlines = prepareRequest.taxlines

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT)
        require(checkComplexCriteria(context, docTemplate, orderItem, material) ) {
            "meetsComplexCriteria not matched by template: $docTemplate, orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }

        logger.info { "preparePaymentDeposit for orderItemId: ${orderItem.orderItemId}, orderItemStatus: ${orderItem.orderItemStatus}" }

        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        require(orderItem.materialId != null) {
            "materialId is null by orderItemId: ${orderItem.orderItemId}"
        }

        //TODO: 임시 구현 수정 필요(입금정보 기반), 주문 접수 후 + 3일 이라고 가정
        if ( orderItem.updateTime == null ) {
            logger.warn { "updateTime is null by orderItemId:${orderItem.orderItemId}, please check source data, depositDate treat to today + 3 day" }
        }
        val baseDate:LocalDate = orderItem.updateTime?.toLocalDate() ?: LocalDate.now()
        val depositDate:LocalDate = baseDate.plusDays(3)

        val total = getOnetimeTaxIncludedAmount(orderItem, taxlines)
        val fee = total.multiply(defaultTransactionFeeRate).toScale(Constants.ACCOUNTING_SCALE)
        val net = total.subtract(fee).toScale(Constants.ACCOUNTING_SCALE)
        val companyCode = docTemplate.docTemplateKey.companyCode

        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when (docTemplateItem.accountCode) {
                OnetimeAccountCode.DEPOSITS_ON_DEMAND_1.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, net)
                OnetimeAccountCode.BANK_SERVICE_CHARGE.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, fee)
                OnetimeAccountCode.CREDIT_CARD_RECEIVABLES.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, total)
                else -> null
            }
        }.toMutableList()

        val currency = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)

        // 반제 전표임 => 바꿔야 함 (추후 고민)
        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = depositDate,
            postingDate = depositDate,
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


    /**
     * 일시불 - 제품출고: DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED
     * condition:  orderItemStatus is ORDER_RECEIVED, orderItem is PURCHASE
     * 출고일 주문 접수일 + 5일 이라고 가정
     * 반제전표임
     */
    fun prepareProductShipped (context:DocumentServiceContext, prepareRequest: OnetimeProcessRequest): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem = prepareRequest.orderItem
        val material = prepareRequest.material
        val serviceFlowItem = prepareRequest.serviceFlowItem!!
        val inventoryValuation = prepareRequest.inventoryValuation

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED)

        require(checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installItem= null, inventoryValuation = inventoryValuation) ) {
            "meetsComplexCriteria not matched by docTemplate: $docTemplate, orderItem:${orderItem.orderItemId}, serviceFlowItem:${serviceFlowItem.serviceFlowId}, inventoryValuation:${inventoryValuation?.materialId}"
        }

        logger.info { "prepareProductShipped for orderItemId: ${orderItem.orderItemId}, orderItemStatus: ${orderItem.orderItemStatus}, serviceType: ${serviceFlowItem.serviceType}, serviceStatus:${serviceFlowItem.serviceStatus}" }

        require(inventoryValuation != null) {
            "inventoryValuation is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        require(orderItem.materialId  == material.materialId) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in material:${material.materialId}"
        }

        require(inventoryValuation.materialId == orderItem.materialId!!) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in inventoryValuation:${inventoryValuation.materialId}"
        }

        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        // 제품 출고일시 => ServiceFlow Status가 SERVICE_SCHEDULED인 시간을로 해야 함
        if ( serviceFlowItem.updateTime == null ) {
            logger.warn { "serviceFlow updateTime is null by orderItemId:${orderItem.orderItemId}, serviceFlowId:${serviceFlowItem.serviceFlowId}, please check source data, productShippedDate treat to today" }
        }
        val productShippedDate:LocalDate = serviceFlowItem.updateTime?.toLocalDate() ?: LocalDate.now()
        val inventoryValue = inventoryValuation.stockAvgUnitPrice.toScale(Constants.ACCOUNTING_SCALE)

        val companyCode = docTemplate.docTemplateKey.companyCode

        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when (docTemplateItem.accountCode) {
                OnetimeAccountCode.INVENTORY_IN_TRANSIT.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, inventoryValue)
                OnetimeAccountCode.INVENTORIES.getAccountCode(companyCode) -> toDocumentItemRequest(context,prepareRequest, docTemplateItem, inventoryValue)
                else -> null
            }
        }.toMutableList()

        val currency = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)
        // 반제전표임 => 바꿔야 함 (추후 고민)
        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = productShippedDate,
            postingDate = productShippedDate,
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

    /**
     *  일시불 - 매출인식(설치완료시): DocumentTemplateCode.ONETIME_INSTALLATION_COMPLETED
     * condition:  orderItemStatus is INSTALL_COMPLETE, orderItem is PURCHASE
     * 출고일 주문 접수일 + 5일 이라고 가정
     * 반제전표임
     */
    fun prepareSalesRecognition (context:DocumentServiceContext, prepareRequest: OnetimeProcessRequest): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem  = prepareRequest.orderItem
        val material = prepareRequest.material
        val serviceFlowItem = prepareRequest.serviceFlowItem
        val installation = prepareRequest.installItem!!
        val taxlines = prepareRequest.taxlines

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_SALES_RECOGNITION)

        require(checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installation) ) {
            "meetsComplexCriteria not matched by docTemplate: $docTemplate, orderItem:${orderItem.orderItemId}" +
                    ", serviceFlowItem:${serviceFlowItem?.serviceFlowId}, installItem:${installation.installId}"
        }

        logger.info{"prepareSalesRecognition, orderItemId:${orderItem.orderItemId}, orderItemStatus:${orderItem.orderItemStatus}, serviceType:${serviceFlowItem?.serviceType}, serviceStatus:${serviceFlowItem?.serviceStatus}, installationTime:${installation.installationTime}"}

        require(orderItem.materialId  == material.materialId) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in material:${material.materialId}"
        }


        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val onetimeAmount = getOnetimeBaseAmount(orderItem)
        val stateTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.STATE, orderItem, taxlines)
        val countyTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.COUNTY, orderItem, taxlines)
        val cityTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.CITY, orderItem, taxlines)
        val calculationSpecialTaxAmount = getOnetimeSalesTaxAmount(SalesTaxType.SPECIAL, orderItem, taxlines)
        val onetimeTaxIncludedAmount = getOnetimeTaxIncludedAmount(orderItem, taxlines)

        val specialTaxAmount = onetimeTaxIncludedAmount - onetimeAmount - stateTaxAmount - countyTaxAmount - cityTaxAmount
        val diff = specialTaxAmount.compareTo(calculationSpecialTaxAmount)
        if ( diff != 0) {
            logger.warn("special tax difference:$diff by special taxline:$calculationSpecialTaxAmount, actual:$specialTaxAmount, orderItemId:${orderItem.orderItemId}")
        }

        if ( installation.installationTime == null ) {
            logger.warn { "installation installationTime is null by orderItemId:${orderItem.orderItemId}, installationId:${installation.installId}, please check source data, installationDate treat to today:${LocalDate.now()}" }
        }
        val installationDate = installation.installationTime?.toLocalDate() ?: LocalDate.now()

        val companyCode = docTemplate.docTemplateKey.companyCode
        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when (docTemplateItem.accountCode) {
                OnetimeAccountCode.ACCOUNT_RECEIVABLE_SALES.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, onetimeAmount)
                OnetimeAccountCode.SALES_WHOLESALES.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, onetimeAmount)
//                OnetimeAccountCode.TAX_PAYABLE_STATE.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, stateTaxAmount)
//                OnetimeAccountCode.TAX_PAYABLE_COUNTY.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, countyTaxAmount)
//                OnetimeAccountCode.TAX_PAYABLE_CITY.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, cityTaxAmount)
//                OnetimeAccountCode.TAX_PAYABLE_SPECIAL.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, specialTaxAmount)
                else -> null
            }
        }.toMutableList()

        val currency = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)

        // 반제전표임 => 반제전표는 별도 배치로 처리 예정
        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = installationDate,
            postingDate = installationDate,
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



    /**
     * 일시불 - 매출원가인식(설치완료시): DocumentTemplateCode.ONETIME_COGS_RECOGNITION
     * condition:  orderItemStatus is INSTALL_COMPLETE, orderItem is ONETIME
     * 출고일 주문 접수일 + 5일 이라고 가정
     * 반제전표임
     */
    fun prepareCOGSRecognition (context:DocumentServiceContext, prepareRequest: OnetimeProcessRequest): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem = prepareRequest.orderItem
        val material = prepareRequest.material
        val serviceFlowItem = prepareRequest.serviceFlowItem
        val installation = prepareRequest.installItem!!
        val inventoryValuation = prepareRequest.inventoryValuation

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_COGS_RECOGNITION)
        require (checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installation, inventoryValuation) ) {
            "meetsComplexCriteria not matched by docTemplate: ${docTemplate.docTemplateKey}, orderItem:${orderItem.orderItemId}" +
                    ", materialId:${material.materialId}, serviceFlowItem:${serviceFlowItem?.serviceFlowId}, " +
                    "installItem:${installation.installId}, installTime:${installation.installationTime}, inventoryValuation:${inventoryValuation?.materialId}"
        }

        require(inventoryValuation != null) {
            "inventoryValuation is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        require(orderItem.materialId  == material.materialId) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in material:${material.materialId}"
        }

        require(inventoryValuation.materialId == orderItem.materialId!!) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in inventoryValuation:${inventoryValuation.materialId}"
        }

        require(checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installation, inventoryValuation) ) {
            "meetsComplexCriteria not matched by docTemplate: $docTemplate, orderItem:${orderItem.orderItemId}" +
                    ", serviceFlowItem:${serviceFlowItem?.serviceFlowId}, installItem:${installation.installId}, inventoryValuation:${inventoryValuation.materialId}"
        }

        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        if ( installation.installationTime == null ) {
            logger.warn { "installation installationTime is null by orderItemId:${orderItem.orderItemId}, installationId:${installation.installId}, please check source data, installationDate treat to today:${LocalDate.now()}" }
        }
        val installationDate = installation.installationTime?.toLocalDate() ?: LocalDate.now()
        val inventoryValue = inventoryValuation.stockAvgUnitPrice.toScale(Constants.ACCOUNTING_SCALE)

        val companyCode = docTemplate.docTemplateKey.companyCode

        val docItemRequests = docTemplateItems.mapNotNull { docTemplateItem ->
            when (docTemplateItem.accountCode) {
                OnetimeAccountCode.COGS_PRODUCT.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, inventoryValue)
                OnetimeAccountCode.INVENTORY_IN_TRANSIT.getAccountCode(companyCode) -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, inventoryValue)
                else -> null
            }
        }.toMutableList()

        val currency = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)

        // 반제전표임 => 바꿔야 함 (추후 고민)
        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = installationDate,
            postingDate = installationDate,
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


    /**
     * 일시불 - 매출인식(설치완료시): DocumentTemplateCode.ONETIME_INSTALLATION_COMPLETED
     * condition:  orderItemStatus is INSTALL_COMPLETE, orderItem is PURCHASE
     * 출고일 주문 접수일 + 5일 이라고 가정
     * 반제전표임
     */
    fun prepareAdvancePaymentOffset (context:DocumentServiceContext, prepareRequest: OnetimeProcessRequest): HashableDocumentRequest {
        val docTemplate = prepareRequest.docTemplate
        val orderItem = prepareRequest.orderItem
        val material = prepareRequest.material
        val serviceFlowItem = prepareRequest.serviceFlowItem
        val installation = prepareRequest.installItem!!
        val taxlines = prepareRequest.taxlines

        validateTemplateCode(docTemplate.docTemplateKey.docTemplateCode, DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET)

        require(checkComplexCriteria(context, docTemplate, orderItem, material, serviceFlowItem, installation) ) {
            "meetsComplexCriteria not matched by docTemplate: $docTemplate, orderItem:${orderItem.orderItemId}" +
                    ", serviceFlowItem:${serviceFlowItem?.serviceFlowId}, installItem:${installation.installId}"
        }
        require(orderItem.materialId  == material.materialId) {
            "materialId must be same by orderItemId: ${orderItem.orderItemId}, materialId in order:${orderItem.materialId}, materialId in material:${material.materialId}"
        }


        val docTemplateItems = documentTemplateServiceable.findDocTemplateItems(docTemplate.docTemplateKey)
        require(docTemplateItems.size >= 2) {
            "docTemplateItems is not matched by docTemplateKey: ${docTemplate.docTemplateKey}, must be greater than 2, but size: ${docTemplateItems.size}"
        }

        val onetimeTaxIncludedAmount = getOnetimeTaxIncludedAmount(orderItem, taxlines)
        val onetimeAmount = getOnetimeBaseAmount(orderItem)

        if ( installation.installationTime == null ) {
            logger.warn { "installation installationTime is null by orderItemId:${orderItem.orderItemId}, installationId:${installation.installId}, please check source data, installationDate treat to today:${LocalDate.now()}" }
        }
        val installationDate = installation.installationTime?.toLocalDate() ?: LocalDate.now()

        val docItemRequests = docTemplateItems
            .mapNotNull { docTemplateItem -> toDocumentItemRequest(context, prepareRequest, docTemplateItem, onetimeAmount) }
            .toMutableList()

        val companyCode = docTemplate.docTemplateKey.companyCode
        val currency = companyService.getCompanyCurrency(companyCode)
        val docHash = onetimeDocHash(docTemplate, orderItem)

        // 반제전표임 => 바꿔야 함 (추후 고민)
        val request = CreateDocumentRequest(
            docType = docTemplate.documentType,
            docHash = docHash,
            documentDate = installationDate,
            postingDate = installationDate,
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

    fun getOnetimeSalesTaxAmount(salesTaxType: SalesTaxType, orderItem:CollectOrderItem, taxlines:List<CollectTaxLine>):BigDecimal {
        require(orderItem.orderItemType == OrderItemType.ONETIME) {
            "orderItemType must be ONETIME by orderItemId: ${orderItem.orderItemId}"
        }
        require(orderItem.price != null) {
            "ordreItem price is null by orderItemId:${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        require(orderItem.price?.itemPrice != null) {
            "onetimePrice is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }

        val basePrice = getOnetimeBaseAmount(orderItem)

        if (onetimeConfig.enableTaxline) {
            if ( taxlines.isEmpty() ) {
                logger.warn("Since taxlines is empty, using mock data instead by salesTaxType:${salesTaxType}, taxlines:${taxlines}, orderItemId:${orderItem.orderItemId}")
                return SalesTaxMock.fromSalesTaxType(salesTaxType).salesTaxAmount(basePrice)
            }

            val filtered = taxlines.filter { it.salesTaxType == salesTaxType }

            if ( filtered.isEmpty()) {
                logger.info ("taxline not found by taxType: $salesTaxType, taxlines:${taxlines.size}, orderItemId:${orderItem.orderItemId}")
                return BigDecimal.ZERO
            } else if ( filtered.size > 1 ) {
                logger.warn ("Multiple taxlines detected - expected single taxline  by taxType: $salesTaxType, taxlines:${taxlines.size}, orderItemId:${orderItem.orderItemId}")
            }

            return when (salesTaxType) {
                SalesTaxType.STATE, SalesTaxType.CITY, SalesTaxType.COUNTY -> {
                    // 가장 최근것을 위로 오도록 정렬
                    val taxline = filtered.sortedByDescending { it.updateTime  }.first()
                    taxline.price.toScale()
                }
                SalesTaxType.SPECIAL -> {       // Special Tax는 여러건임
                    filtered.map { it.price }.sumOf { it }
                }
                SalesTaxType.NONE -> throw IllegalArgumentException("SalesTaxType must be NONE  by taxlines:${taxlines}, orderItemId:${orderItem.orderItemId}")
            }
        } else {
            return SalesTaxMock.fromSalesTaxType(salesTaxType).salesTaxAmount(basePrice)
        }
    }

    fun getOnetimeBaseAmount (orderItem:CollectOrderItem):BigDecimal{

        require(orderItem.orderItemType == OrderItemType.ONETIME) {
            "orderItemType must be ONETIME by orderItemId: ${orderItem.orderItemId}"
        }
        require(orderItem.price != null) {
            "ordreItem price is null by orderItemId:${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        require(orderItem.price?.itemPrice != null) {
            "onetimePrice is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        val basePrice = orderItem.price?.itemPrice ?: BigDecimal.ZERO

        return basePrice.toScale()
    }

    /**
     * materialId 에 해당하는 onetimeAmount를 구함 (세금포함여부에 따라 다르게 나옴)
     */
    fun getOnetimeTaxIncludedAmount (orderItem:CollectOrderItem, taxlines: List<CollectTaxLine>): BigDecimal {
        require(orderItem.orderItemType == OrderItemType.ONETIME) {
            "orderItemType must be ONETIME by orderItemId: ${orderItem.orderItemId}"
        }
        require(orderItem.price != null) {
            "ordreItem price is null by orderItemId:${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }
        require(orderItem.price?.itemPrice != null) {
            "onetimePrice is null by orderItemId: ${orderItem.orderItemId}, materialId: ${orderItem.materialId}"
        }

        val basePrice = getOnetimeBaseAmount(orderItem)

        val taxIncludedAmount = if ( onetimeConfig.enableTaxline ) {
            if ( taxlines.isEmpty() ) {
                logger.warn("taxIncludedAmount, Since taxlines is empty, using mock data instead by orderItemId:${orderItem.orderItemId}")
                return SalesTaxMock.getTaxIncludedAmount(basePrice)
            }

            val distincted = taxlines.distinctBy { it.salesTaxType }
            if ( distincted.size < 3 ) {
                logger.warn("taxlines must be greater than 2, but ${distincted.size}, taxlines:${distincted.map { it.salesTaxType }}, orderItemId:${orderItem.orderItemId}")
            }

            val salesTaxAmount = distincted.sumOf { it.price }.toScale()
            basePrice + salesTaxAmount
        } else {
            SalesTaxMock.getTaxIncludedAmount(basePrice)
        }
        return taxIncludedAmount
    }


    fun toDocumentItemRequest(context:DocumentServiceContext,
                              request: OnetimeProcessRequest,
                              docTemplateItem:DocumentTemplateItem,
                              amount:BigDecimal): DocumentItemRequest? {
        val docTemplate = request.docTemplate
        val orderItem = request.orderItem
        val material = request.material
        val installation = request.installItem
        val customerId = request.customerId
        val channel = request.channel
        val referralCode = request.referralCode

        val accountCode = docTemplateItem.accountCode
        val accountSide = docTemplateItem.accountSide
        val companyCode = docTemplate.docTemplateKey.companyCode
        val currency = companyService.getCompanyCurrency(companyCode)

        if ( BigDecimal.ZERO.equalsWithScale(amount) ) {
            logger.info("DocumentItem is ignored by amount is ${BigDecimal.ZERO}, orderItemId:${orderItem.orderItemId}, templateCode:${docTemplate.docTemplateKey.docTemplateCode}, accountCode:${accountCode}")
            return null
        }

        val docItemAttributeRequests = toDocumentItemAttributeRequests(context, docTemplateItem, orderItem, material, installation, customerId, channel, referralCode).toMutableList()
        // do something
        return DocumentItemRequest(
            companyCode = companyCode,
            accountCode = accountCode,
            accountSide = accountSide,
            txCurrency = currency.name,
            txAmount = amount,
            text = docTemplateItem.korText!!,
            costCenter = docTemplateItem.costCenter,
            profitCenter = docTemplateItem.profitCenter,
            segment = docTemplateItem.segment,
            project = docTemplateItem.project,
            customerId = customerId ?: DEFAULT_CUSTOMER_ID,
            vendorId = null,
            attributes = docItemAttributeRequests
        )
    }


    fun toDocumentItemAttributeRequests(
        context: DocumentServiceContext,
        docTemplateItem: DocumentTemplateItem, orderItem: CollectOrderItem,
        material: CollectMaterial,
        installation: CollectInstallation? = null,
        customerId: String? = null,
        channel: CollectChannel? = null,
        referralCode: String? = null
    ): List<DocumentItemAttributeRequest> {
        val accountCode = docTemplateItem.accountCode
        val companyCode = docTemplateItem.docTemplateKey.companyCode

        val attributeTypeMasters = documentMasterService.getAllByAccountCodeIn(companyCode, listOf(accountCode))
        val attributeTypeValueMap = constructAttributeTypeValueMap(context, docTemplateItem, orderItem, material, installation, customerId, channel, referralCode)

        return attributeTypeMasters
            .filter { it.fieldRequirement.isAcceptable() }
            .filter { attributeTypeValueMap.containsKey(it.attributeType) }
            .filter { attributeTypeValueMap[it.attributeType] != null }
            .map { attributeTypeMaster ->
                DocumentItemAttributeRequest(
                    attributeType = attributeTypeMaster.attributeType,
                    attributeValue = attributeTypeValueMap[attributeTypeMaster.attributeType]!!
                )
            }
    }



    fun constructAttributeTypeValueMap(
        context: DocumentServiceContext,
        docTemplateItem: DocumentTemplateItem, orderItem: CollectOrderItem,
        material: CollectMaterial,
        installation: CollectInstallation? = null,
        customerId: String? = null,
        channel: CollectChannel? = null,
        referralCode: String? = null
    ): MutableMap<DocumentAttributeType, String?> {
        val attributeTypeValueMap = mutableMapOf<DocumentAttributeType, String?>()
        require(orderItem.materialId != null && orderItem.materialId!! == material.materialId) {
            "materialId is not matched by orderItem:${orderItem.orderItemId}, material:${material.materialId}"
        }

        // customer type
        attributeTypeValueMap[DocumentAttributeType.COST_CENTER] = docTemplateItem.costCenter
        attributeTypeValueMap[DocumentAttributeType.PROFIT_CENTER] = docTemplateItem.profitCenter
        attributeTypeValueMap[DocumentAttributeType.SEGMENT] = docTemplateItem.segment
        attributeTypeValueMap[DocumentAttributeType.PROJECT] = docTemplateItem.project

        // customerId, vendorId는 orderItemId에서 들어오는 내용으로 채워야 함
        attributeTypeValueMap[DocumentAttributeType.CUSTOMER_ID] = customerId ?: DEFAULT_CUSTOMER_ID
        attributeTypeValueMap[DocumentAttributeType.SALES_TYPE] = orderItem.orderItemType.name
        attributeTypeValueMap[DocumentAttributeType.SALES_ITEM] = SalesItem.PRODUCT.code        // 전체

        // vendorId는 고객 프로세스에서는 없음
        attributeTypeValueMap[DocumentAttributeType.VENDOR_ID] = null

        attributeTypeValueMap[DocumentAttributeType.ORDER_ID] = orderItem.orderId
        attributeTypeValueMap[DocumentAttributeType.ORDER_ITEM_ID] = orderItem.orderItemId
        attributeTypeValueMap[DocumentAttributeType.CONTRACT_ID] = orderItem.contractId
        attributeTypeValueMap[DocumentAttributeType.SERIAL_NUMBER] = installation?.serialNumber
        attributeTypeValueMap[DocumentAttributeType.INSTALL_ID] = installation?.installId
        attributeTypeValueMap[DocumentAttributeType.BRANCH_ID] = null
        attributeTypeValueMap[DocumentAttributeType.WAREHOUSE_ID] = null
        attributeTypeValueMap[DocumentAttributeType.TECHNICIAN_ID] = installation?.technicianId

        // 일시불은 없음
        attributeTypeValueMap[DocumentAttributeType.RENTAL_CODE] = null
        attributeTypeValueMap[DocumentAttributeType.LEASE_TYPE] = null
        attributeTypeValueMap[DocumentAttributeType.CONTRACT_DURATION] = null
        attributeTypeValueMap[DocumentAttributeType.COMMITMENT_DURATION] = null
        attributeTypeValueMap[DocumentAttributeType.CURRENT_TERM] = null
        attributeTypeValueMap[DocumentAttributeType.CHARGE_ID] = null
        attributeTypeValueMap[DocumentAttributeType.INVOICE_ID] = null

        // 판매조직과 추천정보가 있을때 사용, orderItemId를 통해서 얻어야 함
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_ID] = channel?.channelId
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_TYPE] = channel?.channelType?.name
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_NAME] = channel?.channelName
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_DETAIL] = channel?.channelDetail
        attributeTypeValueMap[DocumentAttributeType.REFERRAL_CODE] = referralCode


        // material type
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_ID] = material.materialId
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_TYPE] = material.materialType.name
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_CATEGORY_CODE] = material.materialCategoryCode.name
        attributeTypeValueMap[DocumentAttributeType.PRODUCT_CATEGORY] = material.productType.name
        attributeTypeValueMap[DocumentAttributeType.FILTER_TYPE] = material.filterType?.name
        attributeTypeValueMap[DocumentAttributeType.FEATURE_TYPE] = material.featureCode?.name


        return attributeTypeValueMap
    }

}