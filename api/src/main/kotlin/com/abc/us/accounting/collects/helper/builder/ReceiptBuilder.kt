package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.ReceiptMethodEnum
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.generated.models.OmsBillingCharge
import com.abc.us.generated.models.OmsBillingPayment
import com.abc.us.generated.models.PaymentMethod
import com.abc.us.oms.domain.billing.entity.ChargePayment
import java.math.BigDecimal
import java.time.ZoneOffset

class ReceiptBuilder {
    companion object {
        private fun convertReceiptMethod(method: PaymentMethod): ReceiptMethodEnum {
            return when (method) {
                PaymentMethod.CREDIT_CARD -> ReceiptMethodEnum.CREDIT_CARD
                PaymentMethod.SHOP_PAY -> ReceiptMethodEnum.SHOP_PAY
                PaymentMethod.PAYPAL -> ReceiptMethodEnum.PAYPAL
                else -> ReceiptMethodEnum.NONE
            }
        }
        private fun convertReceiptMethod(method: String): ReceiptMethodEnum {
            return when (method) {
                "CREDIT_CARD" -> ReceiptMethodEnum.CREDIT_CARD
                "SHOP_PAY" -> ReceiptMethodEnum.SHOP_PAY
                "PAYPAL" -> ReceiptMethodEnum.PAYPAL
                else -> ReceiptMethodEnum.NONE
            }
        }
        fun build(payment : ChargePayment) : CollectReceipt {
            val price = EmbeddablePrice(totalPrice = BigDecimal(payment.totalPrice),
                currency = "USD")
                .apply {
                    tax = payment.tax?.let { BigDecimal(it)}
                }
            val relation = EmbeddableRelation().apply {
                entity = CollectCharge::class.simpleName
                field = "charge_id"
                value = payment.chargeId
            }

            val name = EmbeddableName().apply {
                firstName = payment.firstName
                lastName = payment.lastName
                primaryEmail = payment.email
                primaryPhone = payment.phone
                //mobile = payment.phone

            }

            val location = EmbeddableLocation()
                .apply {
                    address1 = payment.address1
                    address2 = payment.address2
                    state = payment.state
                    city = payment.city
                    zipCode = payment.zipcode
                }
            return CollectReceipt(price = price,
                relation = relation,
                name = name,
                location = location).apply {
                invoiceId = payment.invoiceId
                receiptId = payment.payoutId
                transactionId = payment.transactionId
                receiptMethod = payment.paymentMethod.let { convertReceiptMethod(it) }
                cardNumber = payment.cardNumber
                cardType = payment.cardType
                installmentMonths = payment.installmentMonths
                receiptTime = payment.paymentTime.toOffset()
                //taxLines = TaxLineBuilder.build(payment)
            }
        }
        fun build(payment: OmsBillingPayment) : CollectReceipt {
            val price = EmbeddablePrice(totalPrice = BigDecimal(payment.totalPrice),
                                        currency = "USD")
                .apply {
                    tax = payment.tax?.let { BigDecimal(it)}
                }
            val relation = EmbeddableRelation().apply {
                entity = CollectCharge::class.simpleName
                field = "charge_id"
                value = payment.chargeId
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
                invoiceId = payment.invoiceId
                receiptId = payment.payoutId
                transactionId = payment.transactionId
                receiptMethod = payment.paymentMethod.let { convertReceiptMethod(it) }
                cardNumber = payment.cardNumber
                cardType = payment.cardType
                installmentMonths = payment.installmentMonths
                receiptTime = payment.paymentTime
                taxLines = TaxLineBuilder.build(payment)
            }
        }

        fun builds(omsCharges : List<OmsBillingCharge>) : MutableList<CollectReceipt> {
            val receipts = mutableListOf<CollectReceipt>()
            omsCharges.forEach { omsCharge ->
                omsCharge.payment?.let { receipts.add(build(it)) }
            }
            return receipts
        }
    }
}