package com.abc.us.accounting.collects.fixtures

import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
import com.abc.us.accounting.collects.domain.entity.collect.CollectDeposit
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.BillingTypeEnum
import com.abc.us.accounting.collects.domain.type.ReceiptMethodEnum
import com.abc.us.generated.models.OmsBillingCharge
import com.github.javafaker.Faker
import java.math.BigDecimal
import java.util.*

object CollectReceiptFixture {
    private val faker = Faker()

    fun randomReceiptMethod(): ReceiptMethodEnum {
        return ReceiptMethodEnum.entries.random()
    }
    private fun makeDeposit(omsCharge: OmsBillingCharge) : CollectDeposit? {
        val mockupPrice = BigDecimal(omsCharge.totalPrice)
        return CollectDeposit().apply {
//            hashCode: String? = null
//            createTime: OffsetDateTime? = null
//            updateTime: OffsetDateTime? = null
//            isActive: Boolean = true
            //omsCharge.
            //relation : EmbeddableRelation? = null
            transactionId = omsCharge.chargeId
            depositId = UUID.randomUUID().toString()
            currency = "USD"
            depositDate = omsCharge.startDate!!.plusDays(3)
            amount = mockupPrice.toString()
            //adjustmentsFeeAmount = mockupPrice.multiply(0.1).toString()
//            adjustmentsGrossAmount: String? = null
//            chargesFeeAmount: String? = null
//            chargesGrossAmount: String? = null
//            refundsFeeAmount: String? = null
//            refundsGrossAmount: String? = null
//            reservedFundsFeeAmount: String? = null
//            reservedFundsGrossAmount: String? = null
//            retriedDepositsFeeAmount: String? = null
//            retriedDepositsGrossAamount: String? = null
            salesFeeAmount = mockupPrice.multiply(BigDecimal(0.02)).toString()
//            salesGrossAmount: String? = null
            fees = mockupPrice.multiply(BigDecimal(0.02)).toString()
//            gross: String? = null
//            net: String? = null
        }
    }
    private fun makeReceipt(omsCharge: OmsBillingCharge) : CollectReceipt? {
        val mockupPrice = BigDecimal(faker.commerce().price(10.0, 1000.0))
        val price = EmbeddablePrice(totalPrice = mockupPrice,
                                    currency = "USD"
        )
            .apply {
                itemPrice = mockupPrice.multiply(BigDecimal(0.8))
                discountPrice = mockupPrice.multiply(BigDecimal(0.1))
                registrationPrice = mockupPrice.multiply(BigDecimal(0.035))
                tax = mockupPrice.multiply(BigDecimal(0.065))
            }
        val relation = EmbeddableRelation().apply {
            entity = CollectCharge::class.simpleName
            field = "charge_id"
            value = omsCharge.chargeId
        }
        val name = EmbeddableName().apply {
            firstName = "firstName"
            lastName = "lastName"
            primaryEmail = "primaryEmail"
            primaryPhone = "primaryPhone"
            mobile = "mobile"
        }

        val location = EmbeddableLocation()
            .apply {
                address1 = "address1"
                address2 = "address2"
                state = "state"
                city = "city"
                zipCode = "zipCode"
            }
        return CollectReceipt(price = price,
                              name =  name,
                              location = location,
                              relation =  relation).apply {
            chargeId = omsCharge.chargeId
            receiptId = UUID.randomUUID().toString()
            //depositId = UUID.randomUUID().toString()
            transactionId = omsCharge.chargeId
            receiptMethod = randomReceiptMethod()
            cardNumber = faker.number().numberBetween(1, 10).toString()
            cardType = faker.code().isbn10()
            installmentMonths = omsCharge.createTime!!.monthValue
            receiptTime = omsCharge.createTime.plusDays(3)
            billingType = BillingTypeEnum.INVOICE_BILLING
        }
    }
    fun makeMockupDeposits(omsCharges : List<OmsBillingCharge>) : MutableList<CollectDeposit> {
        val deposits = mutableListOf<CollectDeposit>()
        omsCharges.forEach { omsCharge ->

            makeDeposit(omsCharge)?.let { deposits.add(it) }
        }
        return deposits
    }
    fun makeMockupReceipts(omsCharges : List<OmsBillingCharge>) : MutableList<CollectReceipt> {
        val receipts = mutableListOf<CollectReceipt>()
        omsCharges.forEach { omsCharge ->

            makeReceipt(omsCharge)?.let { receipts.add(it) }
        }
        return receipts
    }
}