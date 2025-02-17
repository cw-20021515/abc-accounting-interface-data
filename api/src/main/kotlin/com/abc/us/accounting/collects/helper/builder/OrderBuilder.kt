package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.OrderProductTypeEnum
import com.abc.us.accounting.collects.domain.type.ReceiptMethodEnum
import com.abc.us.accounting.collects.helper.OmsApiOrder
import com.abc.us.accounting.collects.helper.OmsApiOrderMutableList
import com.abc.us.accounting.collects.helper.OmsOrderItemMutableList
import com.abc.us.generated.models.OrderType
import com.abc.us.generated.models.OrderView
import com.abc.us.generated.models.Payment
import com.abc.us.generated.models.PaymentMethod
import mu.KotlinLogging
import java.math.BigDecimal

class OrderBuilder {

    companion object {
        private val logger = KotlinLogging.logger {}

        fun convertReceiptMethod(method : PaymentMethod) : ReceiptMethodEnum {
            return when(method) {
                PaymentMethod.CREDIT_CARD -> ReceiptMethodEnum.CREDIT_CARD
                PaymentMethod.SHOP_PAY -> ReceiptMethodEnum.SHOP_PAY
                PaymentMethod.PAYPAL -> ReceiptMethodEnum.PAYPAL
                else -> TODO()
            }
        }
        fun buildReceipt(order : OrderView,payment: Payment) : CollectReceipt {
            logger.info { "BUILD-RECEIPT[${payment}]" }
            val price = EmbeddablePrice(totalPrice = BigDecimal(payment.price.totalPrice),
                                        currency = payment.price.currency
                                        )
                .apply {
                    discountPrice = BigDecimal(payment.price.discountPrice)
                    itemPrice = BigDecimal(payment.price.itemPrice)
                    prepaidAmount = payment.price.prepaidAmount?.let { BigDecimal(it) }
                    tax = BigDecimal(payment.price.tax)
                    registrationPrice = payment.price.registrationPrice?.let { BigDecimal(it) }
                }
            val relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.simpleName
                field = "order_id"
                value = order.orderId
            }
            val name = EmbeddableName().apply {
                payment.billingAddress?.let {properties ->
                    firstName = properties.firstName
                    lastName = properties.lastName
                    primaryEmail = properties.email
                    primaryPhone = properties.phone
                    mobile = properties.mobile
                }
            }
            val location = EmbeddableLocation()
                .apply {
                    payment.billingAddress?.let { properties ->
                        address1 = properties.address1
                        address2 = properties.address2
                        state = properties.state
                        city = properties.city
                        zipCode = properties.zipcode
                    }
                }
            return CollectReceipt(price = price,
                                  relation = relation,
                                  name = name,
                                  location = location).apply {
                receiptId = payment.payoutId
                transactionId = payment.transactionId
                receiptMethod = payment.paymentMethod?.let { convertReceiptMethod(it) }
                cardNumber = payment.cardNumber
                cardType = payment.cardType
                installmentMonths = payment.installmentMonths
                receiptTime = payment.paymentTime
            }
        }

        fun buildOrder(order : OmsApiOrder) : CollectOrder {
            logger.info { "BUILD-OMS_API_ORDER[${order}]" }
            return CollectOrder(orderId =order.orderId,
                                customerId = order.customerInformation.customerId).apply {
                channelOrderId = order.channelOrderId
                channelId = order.channel.channelId
                orderProductType = when(order.orderProductType) {
                    OrderType.INSTALL -> OrderProductTypeEnum.INSTALL
                    OrderType.COURIER -> OrderProductTypeEnum.COURIER
                }
                referrerCode = order.referrerCode
                createTime = order.createTime
                updateTime = order.updateTime
                orderCreateTime = order.orderCreateTime
                orderUpdateTime = order.orderUpdateTime
                customer = CustomerBuilder.build(order.customerInformation)
                receipt = order.payment?.let { buildReceipt(order,it) }

                receiptId = receipt?.let { it.receiptId }
                //orderItems = OrderItemBuilder.builds(OmsOrderItemMutableList(order.orderItems.toMutableList()))
            }
        }

        fun build(originOrders : OmsApiOrderMutableList) : MutableList<CollectOrder>{
            val orders = mutableListOf<CollectOrder>()
            originOrders.forEach{ order ->
                orders.add(buildOrder(order))
            }
            return orders
        }
    }
}