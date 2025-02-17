package com.abc.us.accounting.rentals.onetime.utils

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.config.SalesTaxConfig
import com.abc.us.accounting.documents.domain.entity.DocumentItemAttributeMaster
import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentItemAttributeRequest
import com.abc.us.accounting.documents.model.DocumentItemRequest
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.iface.domain.model.TaxLine
import com.abc.us.accounting.rentals.onetime.model.OnetimePaymentProcessItem
import com.abc.us.accounting.rentals.onetime.model.SalesTax
import com.abc.us.accounting.supports.utils.BigDecimals.equalsWithScale
import com.abc.us.accounting.supports.utils.Hashs
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object OnetimeUtils {
    private val logger = KotlinLogging.logger {}


    fun validateTemplateCode(actual: DocumentTemplateCode, expected: DocumentTemplateCode) {
        require(actual == expected) {
            "docTemplate is not same!!, actual: ${actual}, expected: $expected"
        }
    }

    fun validateSalesTax(salesTaxConfig: SalesTaxConfig,
                         name:String,
                         docTemplateCode: DocumentTemplateCode,
                         orderId:String,
                         paymentId: String,
                         orderItemId:String? = null,
                         salesTax: SalesTax,
                         tax: BigDecimal=BigDecimal.ZERO):Boolean {
        if ( !salesTax.total.equalsWithScale(tax) ) {
            val message = "$name, docTemplateCode:${docTemplateCode} tax is mismatched by orderId:${orderId}, paymentId:${paymentId}, orderItemId:${orderItemId}, tax:${tax} must be equal to sum:${salesTax.total} of taxlines:${salesTax.taxLines}"
            logger.error (message)
            if ( salesTaxConfig.ignoreToleranceException ) {
                return false
            }
            throw IllegalArgumentException(message)
        }
        return true
    }


    fun toLocalDate(offsetDateTime: OffsetDateTime): LocalDate {
        return TimeZoneCode.convertTime(offsetDateTime, TimeZoneCode.system()).toLocalDate()
    }

    /**
     * ONETIME_PAYMENT_RECEIPT: orderId인 경우에는 docHash를 orderId기반으로 작성
     */
    fun onetimeDocHash(companyCode: CompanyCode, docTemplateCode: DocumentTemplateCode, vararg items: Any?):String {

        val hash = Hashs.hash(companyCode, docTemplateCode, items)

        val docHash = "${hash}.${companyCode}.${docTemplateCode}.${items.mapNotNull { it?.toString() }.joinToString(separator = "|")}"
        return docHash
    }


    fun toDocumentItemRequest(context: DocumentServiceContext,
                              processItem: OnetimePaymentProcessItem,
                              docTemplateItem: DocumentTemplateItem,
                              amount: BigDecimal,
                              attributeTypeMasters: List<DocumentItemAttributeMaster>,
                              currency: CurrencyCode,
                              ): DocumentItemRequest? {
        val orderItem = processItem.orderItem
        val material = processItem.material
        val serviceFlow = processItem.serviceFlow
        val channel = processItem.channel

        val accountCode = docTemplateItem.accountCode
        val accountSide = docTemplateItem.accountSide
        val companyCode = processItem.docTemplate.docTemplateKey.companyCode
        val docTemplateCode =  processItem.docTemplate.docTemplateKey.docTemplateCode
        val customerId = processItem.customerId
        val onetimePayment = processItem.onetimePayment
        val orderId = onetimePayment?.orderId

        if ( BigDecimal.ZERO.equalsWithScale(amount) ) {
            logger.info("DocumentItem is ignored by amount is ${BigDecimal.ZERO}, orderId:${orderId}, templateCode:${docTemplateCode}, accountCode:${accountCode}")
            return null
        }

        val docItemAttributeRequests = toDocumentItemAttributeRequests(context, docTemplateItem, customerId,
                    onetimePayment, orderItem, material, serviceFlow, channel, attributeTypeMasters).toMutableList()
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
            customerId = customerId,
            vendorId = null,
            attributes = docItemAttributeRequests
        )
    }


    private fun toDocumentItemAttributeRequests(
        context: DocumentServiceContext,
        docTemplateItem: DocumentTemplateItem,
        customerId: String,
        onetimePayment: IfOnetimePayment? = null,
        orderItem: IfOrderItem? = null,
        material: IfMaterial? = null,
        serviceFlow: IfServiceFlow? = null,
        channel: IfChannel? = null,
        attributeTypeMasters: List<DocumentItemAttributeMaster>
    ): List<DocumentItemAttributeRequest> {
        val attributeTypeValueMap = constructAttributeTypeValueMap(context, docTemplateItem, customerId, onetimePayment, orderItem, material, serviceFlow, channel)

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



    private fun constructAttributeTypeValueMap(
        context: DocumentServiceContext,
        docTemplateItem: DocumentTemplateItem,
        customerId: String,
        onetimePayment: IfOnetimePayment? = null,
        orderItem: IfOrderItem? = null,
        material: IfMaterial? = null,
        serviceFlow: IfServiceFlow? = null,
        channel: IfChannel? = null,
        referralCode: String? = null
    ): MutableMap<DocumentAttributeType, String?> {

        val attributeTypeValueMap = mutableMapOf<DocumentAttributeType, String?>()
        require(orderItem == null || material == null || orderItem?.materialId == material?.materialId) {
            "materialId is not matched by orderItem:${orderItem?.orderItemId}, material:${material?.materialId}"
        }

        // customer type
        attributeTypeValueMap[DocumentAttributeType.COST_CENTER] = docTemplateItem.costCenter
        attributeTypeValueMap[DocumentAttributeType.PROFIT_CENTER] = docTemplateItem.profitCenter
        attributeTypeValueMap[DocumentAttributeType.SEGMENT] = docTemplateItem.segment
        attributeTypeValueMap[DocumentAttributeType.PROJECT] = docTemplateItem.project

        // customerId, vendorId는 orderItemId에서 들어오는 내용으로 채워야 함
        attributeTypeValueMap[DocumentAttributeType.CUSTOMER_ID] = customerId
        attributeTypeValueMap[DocumentAttributeType.SALES_TYPE] = SalesType.ONETIME.name
        attributeTypeValueMap[DocumentAttributeType.SALES_ITEM] = SalesItem.TOTAL.code

        // vendorId는 고객 프로세스에서는 없음
        attributeTypeValueMap[DocumentAttributeType.VENDOR_ID] = null


        attributeTypeValueMap[DocumentAttributeType.ORDER_ID] = onetimePayment?.orderId ?: orderItem?.orderId
        attributeTypeValueMap[DocumentAttributeType.ORDER_ITEM_ID] = orderItem?.orderItemId
        attributeTypeValueMap[DocumentAttributeType.CONTRACT_ID] = orderItem?.contractId
        attributeTypeValueMap[DocumentAttributeType.SERIAL_NUMBER] = serviceFlow?.serialNumber
        attributeTypeValueMap[DocumentAttributeType.INSTALL_ID] = serviceFlow?.installId
        attributeTypeValueMap[DocumentAttributeType.BRANCH_ID] = serviceFlow?.installId
        attributeTypeValueMap[DocumentAttributeType.WAREHOUSE_ID] = serviceFlow?.warehouseId
        attributeTypeValueMap[DocumentAttributeType.TECHNICIAN_ID] = serviceFlow?.technicianId

        // 일시불은 없음
        attributeTypeValueMap[DocumentAttributeType.RENTAL_CODE] = null
        attributeTypeValueMap[DocumentAttributeType.LEASE_TYPE] = null
        attributeTypeValueMap[DocumentAttributeType.CONTRACT_DURATION] = null
        attributeTypeValueMap[DocumentAttributeType.COMMITMENT_DURATION] = null
        attributeTypeValueMap[DocumentAttributeType.CURRENT_TERM] = null

        attributeTypeValueMap[DocumentAttributeType.PAMENT_ID] = onetimePayment?.paymentId
        attributeTypeValueMap[DocumentAttributeType.CHARGE_ID] = null
        attributeTypeValueMap[DocumentAttributeType.INVOICE_ID] = null

        // 판매조직과 추천정보가 있을때 사용, orderItemId를 통해서 얻어야 함
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_ID] = channel?.channelId
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_TYPE] = channel?.channelType?.name
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_NAME] = channel?.channelName
        attributeTypeValueMap[DocumentAttributeType.CHANNEL_DETAIL] = channel?.channelDetail
        attributeTypeValueMap[DocumentAttributeType.REFERRAL_CODE] = referralCode


        // material type
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_ID] = material?.materialId
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_TYPE] = material?.materialType?.name
        attributeTypeValueMap[DocumentAttributeType.MATERIAL_CATEGORY_CODE] = material?.materialCategoryCode?.name
        attributeTypeValueMap[DocumentAttributeType.PRODUCT_CATEGORY] = material?.productType?.name
        attributeTypeValueMap[DocumentAttributeType.FILTER_TYPE] = material?.filterType
        attributeTypeValueMap[DocumentAttributeType.FEATURE_TYPE] = material?.featureCode


        return attributeTypeValueMap
    }
}