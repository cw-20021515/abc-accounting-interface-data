package com.abc.us.accounting.rentals.onetime.utils

import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.FilteringRule
import com.abc.us.accounting.iface.domain.entity.oms.IfMaterial
import com.abc.us.accounting.iface.domain.entity.oms.IfOnetimePayment
import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.iface.domain.entity.oms.IfServiceFlow
import com.abc.us.accounting.logistics.domain.entity.InventoryCosting
import mu.KotlinLogging

object FilteringRules {
    private val logger = KotlinLogging.logger {}

    private fun logAndReturnFalse(rule:FilteringRule, fieldName: String, expected: Any, actual: Any?): Boolean {
        logger.trace { "checkFilteringRule, Failed $fieldName check - expected: $expected, actual: $actual by filtering rule:$rule" }
        return false
    }

    fun checkFilteringRule(context: DocumentServiceContext,
                           docTemplate:DocumentTemplate? = null,
                           onetimePayment:IfOnetimePayment? = null,
                           orderItem:IfOrderItem? = null,
                           material:IfMaterial? = null,
                           serviceFlow: IfServiceFlow? = null,
                           inventoryCosting: InventoryCosting? = null,
                           customerId: String? = null,
    ): Boolean {
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

                orderIds.isNotEmpty() && ( orderItem != null && !orderIds.contains(orderItem.orderId)) ->
                    logAndReturnFalse(rule, "orderId", orderIds, orderItem.orderId)

                orderItemIds.isNotEmpty() && ( orderItem != null && !orderItemIds.contains(orderItem.orderItemId)) ->
                    logAndReturnFalse(rule, "orderItem", orderItemIds, orderItem.orderItemId)

                customerIds.isNotEmpty() && ( customerId != null && !customerIds.contains(customerId)) ->
                    logAndReturnFalse(rule, "customerId", customerIds, customerId)

                materialIds.isNotEmpty() && ( material != null && !materialIds.contains(material.materialId)) ->
                    logAndReturnFalse(rule, "material", materialIds, material.materialId)

                serviceFlowIds.isNotEmpty() && ( serviceFlow != null && !serviceFlowIds.contains(serviceFlow.serviceFlowId) )  ->
                    logAndReturnFalse(rule, "serviceFlow", serviceFlowIds, serviceFlow.serviceFlowId)

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


//    /**
//     * complexCriteria filtering rule
//     */
//    fun checkComplexCriteria(context:DocumentServiceContext,
//                             docTemplate: DocumentTemplate,
//                             orderItem: IfOrderItem,
//                             onetimePayment: IfOnetimePayment? = null,
//                             material: IfMaterial? = null,
//                             serviceFlowItem: IfServiceFlow? = null,
//                             inventoryCosting: InventoryCosting? = null): Boolean {
//
//        val defaultResult: OnetimeCriteriaResult = when { // 공통 조건
//            orderItem.orderItemType != IfOrderItemType.PURCHASE -> OnetimeCriteriaResult.ORDER_ITEM_TYPE_IS_NOT_ONETIME
//            orderItem.materialId == null -> OnetimeCriteriaResult.ORDER_ITEM_MATERIAL_IS_NULL
//            material == null -> OnetimeCriteriaResult.MATERIAL_IS_NULL
//            material.materialType != IfMaterialType.PRODUCT -> OnetimeCriteriaResult.MATERIAL_TYPE_IS_NOT_PRODUCT          // TODO: PRODUCT 가 아닌 경우 추가 구현 필요
//            else -> OnetimeCriteriaResult.SUCCEEDED
//        }
//
//        val detailResult: OnetimeCriteriaResult = when (docTemplate.docTemplateKey.docTemplateCode) {
//            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED -> {
//                when {
//                    onetimePayment!= null && onetimePayment?.refunds == null -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_RECEIVED
//                    else -> OnetimeCriteriaResult.SUCCEEDED
//                }
//            }
//            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> {
//                when {
//                    orderItem.orderItemStatus != OrderItemStatus.ORDER_RECEIVED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_RECEIVED
//                    else -> OnetimeCriteriaResult.SUCCEEDED
//                }
//            }
//            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED -> {
//                when {
//                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
//                    inventoryValuation == null -> OnetimeCriteriaResult.INVENTORY_VALUATION_IS_NULL
//                    orderItem.orderItemStatus != OrderItemStatus.BOOKING_CONFIRMED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_BOOKING_CONFIRMED
//                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
//                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_SCHEDULED  -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_SCHEDULED
//                    else -> OnetimeCriteriaResult.SUCCEEDED
//                }
//            }
//            DocumentTemplateCode.ONETIME_SALES_RECOGNITION, DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> {
//                when {
////                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
//                    installItem == null -> OnetimeCriteriaResult.INSTALL_ITEM_IS_NULL                        // 설치정보가 없으면 처리 불가 (임시)
//                    installItem.installationTime == null -> OnetimeCriteriaResult.INSTALL_TIME_IS_NULL
//                    orderItem.orderItemStatus != OrderItemStatus.INSTALL_COMPLETED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_INSTALL_COMPLETED
////                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
////                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_COMPLETED -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_COMPLETED
//                    else -> OnetimeCriteriaResult.SUCCEEDED
//                }
//            }
//            DocumentTemplateCode.ONETIME_COGS_RECOGNITION -> {
//                when {
////                    serviceFlowItem == null -> OnetimeCriteriaResult.SERVICE_FLOW_IS_NULL
//                    installItem == null -> OnetimeCriteriaResult.INSTALL_ITEM_IS_NULL
//                    installItem.installationTime == null -> OnetimeCriteriaResult.INSTALL_TIME_IS_NULL
//                    inventoryValuation == null -> OnetimeCriteriaResult.INVENTORY_VALUATION_IS_NULL                                                         // 재고가 없으면 처리 불가 (임시)
//                    orderItem.orderItemStatus  != OrderItemStatus.INSTALL_COMPLETED -> OnetimeCriteriaResult.ORDER_STATUS_IS_NOT_INSTALL_COMPLETED
////                    serviceFlowItem.serviceType != ServiceFlowType.INSTALL -> OnetimeCriteriaResult.SERVICE_TYPE_IS_NOT_INSTALL
////                    serviceFlowItem.serviceStatus != ServiceFlowStatus.SERVICE_COMPLETED -> OnetimeCriteriaResult.SERVICE_FLOW_STATUS_IS_NOT_SERVICE_COMPLETED
//                    else -> OnetimeCriteriaResult.SUCCEEDED
//                }
//            }
//            else -> OnetimeCriteriaResult.FAILED
//        }
//        val result = OnetimeCriteriaResult.failedResults(defaultResult, detailResult)
//        if ( context.debug ) {
//            val message=  "checkComplexCriteria - result:{} by docTemplateCode: {}, orderItemId: {}, orderItemStatus:{}, serviceFlowId: {}, installId: {}, installTime:{}, valuation: {}, serviceFlowType:{}, serviceFlowStatus:{}"
//            if ( result.isNotEmpty() ) {
//                OnetimeDocumentService.logger.info(message,
//                    result,
//                    docTemplate.docTemplateKey.docTemplateCode,
//                    orderItem.orderItemId,
//                    orderItem.orderItemStatus,
//                    serviceFlowItem?.serviceFlowId,
//                    installItem?.installId,
//                    installItem?.installationTime,
//                    inventoryValuation?.materialId,
//                    serviceFlowItem?.serviceType,
//                    serviceFlowItem?.serviceStatus
//                )
//            } else {
//                OnetimeDocumentService.logger.trace(message,
//                    result,
//                    docTemplate.docTemplateKey.docTemplateCode,
//                    orderItem.orderItemId,
//                    orderItem.orderItemStatus,
//                    serviceFlowItem?.serviceFlowId,
//                    installItem?.installId,
//                    installItem?.installationTime,
//                    inventoryValuation?.materialId,
//                    serviceFlowItem?.serviceType,
//                    serviceFlowItem?.serviceStatus
//                )
//
//            }
//        }
//        return result.isEmpty()
//    }



}