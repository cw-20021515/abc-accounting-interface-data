package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectLocation
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.collect.CollectServiceFlow
import com.abc.us.accounting.collects.domain.entity.collect.CollectTaxLine
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.BillingTypeEnum
import com.abc.us.accounting.collects.domain.type.ReceiptMethodEnum
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.oms.domain.serviceflow.entity.ServiceFlow
import com.abc.us.oms.domain.serviceflow.entity.ServiceLocation
import com.abc.us.oms.domain.serviceflow.entity.ServicePayment
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.ZoneOffset

class ServiceFlowBuilder {
    companion object {
        private val logger = KotlinLogging.logger {}
        fun convertServiceType(type : String) : ServiceFlowType {
            return when(type) {
                "INSTALL"               -> ServiceFlowType.INSTALL
                "REPLACEMENT"           -> ServiceFlowType.REPLACEMENT
                "RETURN"                -> ServiceFlowType.RETURN
                "REFUND"                -> ServiceFlowType.REFUND
                "REPAIR"                -> ServiceFlowType.REPAIR
                "COURIER"               -> ServiceFlowType.COURIER
                "RELOCATION_UNINSTALL"  -> ServiceFlowType.RELOCATION_UNINSTALL
                "RELOCATION_INSTALL"    -> ServiceFlowType.RELOCATION_INSTALL
                "REINSTALL"             -> ServiceFlowType.REINSTALL
                else -> TODO()
            }
        }

        fun convertServiceStatus(status : String) : ServiceFlowStatus {
            return when(status) {
                "SERVICE_CREATED"   -> ServiceFlowStatus.SERVICE_CREATED
                "SHIPPING_SCHEDULED"-> ServiceFlowStatus.SERVICE_CREATED
                "BOOKING_SCHEDULED" -> ServiceFlowStatus.BOOKING_SCHEDULED
                "BOOKING_CONFIRMED" -> ServiceFlowStatus.BOOKING_CONFIRMED
                "SERVICE_SCHEDULED" -> ServiceFlowStatus.SERVICE_SCHEDULED
                "SERVICE_STARTED"   -> ServiceFlowStatus.SERVICE_STARTED
                "SERVICE_COMPLETED" -> ServiceFlowStatus.SERVICE_COMPLETED
                "SERVICE_CANCELED"  -> ServiceFlowStatus.SERVICE_CANCELED
                "SHIPPING_CANCELED" -> ServiceFlowStatus.SHIPPING_CANCELED
                "BOOKING_CANCELED"  -> ServiceFlowStatus.BOOKING_CANCELED
                "SERVICE_ON_HOLD"   -> ServiceFlowStatus.SERVICE_ON_HOLD
                else -> TODO()
            }
        }
        fun convertBillingType(type : String) : BillingTypeEnum {
            return when(type) {
                "INVOICE_BILLING" -> BillingTypeEnum.INVOICE_BILLING
                "ON_SITE_PAYMENT" -> BillingTypeEnum.ON_SITE_PAYMENT
                else -> BillingTypeEnum.NONE
            }
        }
        fun buildLocation(loc : ServiceLocation?) : CollectLocation? {
            return loc?.let {
                CollectLocation().apply {
                    relation = EmbeddableRelation().apply {
                        entity = CollectServiceFlow::class.simpleName
                        field = "service_flow_id"
                        value = loc.serviceFlowId
                    }
                    locationId = loc.id

                    location = EmbeddableLocation().apply {
                        branchId = it.branchId
                        warehouseId = it.warehouseId
                        address1 = it.address1
                        address2 = it.address2
                        zipCode = it.zipcode
                        city = it.city
                        state = it.state
                        latitude = it.latitude
                        longitude = it.longitude
                    }
                    name = EmbeddableName().apply {
                        lastName = it.lastName
                        firstName = it.firstName
                        primaryPhone = it.phone
                        primaryEmail = it.email

                    }
                }
            }
        }
        fun buildTaxLine(payment : ServicePayment) : MutableList<CollectTaxLine> {
            val collects = mutableListOf<CollectTaxLine>()
            payment.taxLines.forEach { line ->
                val collect = TaxLineBuilder.buildFromLine(r =EmbeddableRelation().apply {
                    entity = CollectReceipt::class.simpleName
                    field = "receipt_id"
                    value = payment.id
                },line )
                collects.add(collect)
            }
            return collects
        }

        fun convertReceiptMethod(type : String) : ReceiptMethodEnum {
            return when(type) {
                "CREDIT_CARD" -> ReceiptMethodEnum.CREDIT_CARD
                "SHOP_PAY" -> ReceiptMethodEnum.SHOP_PAY
                "PAYPAL" -> ReceiptMethodEnum.PAYPAL
                else -> ReceiptMethodEnum.NONE
            }
        }

        fun buildReceipt( payment : ServicePayment) : CollectReceipt {
            val price = EmbeddablePrice(totalPrice = BigDecimal(payment.totalPrice),
                                        currency = payment.currency)
                .apply {
                    discountPrice = BigDecimal(payment.discountPrice)
                    itemPrice = BigDecimal(payment.itemPrice)
                    prepaidAmount = BigDecimal(payment.prepaidAmount)
                    tax = BigDecimal(payment.tax)
                    registrationPrice = BigDecimal(payment.registrationPrice)
                }
            val relation = EmbeddableRelation().apply {
                entity = CollectServiceFlow::class.simpleName
                field = "service_flow_id"
                value = payment.serviceFlowId
            }
            val name = EmbeddableName().apply {
                lastName = payment.lastName
                firstName = payment.firstName
                primaryPhone = payment.phone
                primaryEmail = payment.email
            }
            val location = EmbeddableLocation().apply {
                address1 = payment.address1
                address2 = payment.address2
                zipCode = payment.zipcode
                city = payment.city
                state=payment.state

            }

            return CollectReceipt(price = price,
                                  relation = relation,
                                  name = name,
                                  location = location).apply {
                receiptId = payment.id
                receiptMethod = payment.paymentMethod?.let {  convertReceiptMethod(it)}
                transactionId = payment.transactionId
                receiptTime = payment.paymentTime?.toOffset()
                remark = payment.remark
                cardNumber = payment.cardNumber
                cardType = payment.cardType
                installmentMonths = payment.installmentMonths
                billingType = convertBillingType(payment.serviceBillingType)
                taxLines = buildTaxLine(payment)
                createTime = payment.createTime?.toOffset()
                updateTime = payment.updateTime?.toOffset()
            }
        }


        fun buildReceipt(payments :  MutableList<ServicePayment>) : MutableList<CollectReceipt> {
            val collects = mutableListOf<CollectReceipt>()
            payments.forEach { collects.add(buildReceipt(it)) }
            return collects
        }
        fun buildServiceFlow(flow : ServiceFlow) : CollectServiceFlow {
            return CollectServiceFlow(
                serviceFlowId = flow.id,
                serviceType = convertServiceType(flow.serviceType),
                orderId = flow.orderId,
                orderItemId = flow.orderItemId,
                customerServiceId = flow.customerServiceId,
                customerServiceTicketId = flow.customerServiceTicketId,
                billingId = flow.billingId,
                workId = flow.workId,
                serviceStatus = convertServiceStatus(flow.serviceStatusCode),
                location = buildLocation(flow.serviceLocation),
                createTime = flow.serviceCreateTime?.toOffset(),
                updateTime = flow.serviceUpdateTime?.toOffset(),
                cancelTime = flow.serviceCancelTime?.toOffset(),
                receipts = buildReceipt(flow.servicePayments)
                )
        }
        fun build(serviceFlows: MutableList<ServiceFlow>): MutableList<CollectServiceFlow> {

            val collects = mutableListOf<CollectServiceFlow>()
            serviceFlows.forEach { serviceFlow ->
                val collect = buildServiceFlow(serviceFlow)
                collects.add(collect)
            }
            return collects
        }
    }
}