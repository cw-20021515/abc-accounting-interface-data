package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.DocumentPersistenceService
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import com.abc.us.accounting.iface.domain.entity.oms.IfOnetimePayment
import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.iface.domain.model.Refund
import com.abc.us.accounting.iface.domain.model.RefundKind
import com.abc.us.accounting.iface.domain.repository.oms.IfOnetimePaymentRepository
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemStatus
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemType
import com.abc.us.accounting.iface.domain.type.oms.IfTransactionType
import com.abc.us.accounting.rentals.onetime.model.OnetimePaymentProcessItem
import com.abc.us.accounting.rentals.onetime.service.v2.OnetimePaymentDepositProcessRule
import com.abc.us.accounting.rentals.onetime.service.v2.OnetimePaymentReceiptProcessRule
import com.abc.us.accounting.rentals.onetime.service.v2.OnetimePaymentRefundProcessRule
import com.abc.us.accounting.rentals.onetime.service.v2.OnetimeProcessService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.OffsetDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicOnetimePaymentProcessRuleTests (
    private val receiptProcessRule: OnetimePaymentReceiptProcessRule,
    private val depositProcessRule: OnetimePaymentDepositProcessRule,
    private val refundProcessRule: OnetimePaymentRefundProcessRule,
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    ): FunSpec({
    val texasTimeZone = TimeZoneCode.TEXAS
    val dallasOffset = texasTimeZone.getZoneOffset()

    val companyCode = CompanyCode.T200
    val startTime = OffsetDateTime.of(2025, 2, 1, 0, 0, 0, 0, dallasOffset)
    val endTime = startTime.plusMonths(2)

    val isFullTest = true
    val eachMaxResult = if (isFullTest) 5000 else 1

    val voidJson = """
{"kind": "VOID", "price": {"currency": "USD", "totalPrice": 1304.41}, "orderItemId": "0105503700034-0102", "paymentTime": "2025-02-06T04:17:39Z", "transactionId": "7159484776664", "acquirerReferenceNumber": "958635141141712"}
""".trimIndent()
    val refundJson = """
{
  "kind": "REFUND",
  "price": {
    "currency": "USD",
    "totalPrice": 1461.37
  },
  "orderItemId": "0105503700034-0502",
  "paymentTime": "2025-02-06T04:17:01Z",
  "transactionId": "7159484121304",
  "acquirerReferenceNumber": "5410692566074431"
}        
    """.trimIndent()

    val orderItemJson = """
{
  "id": "1al4liL3cnx",
  "orderItemId": "0105503700034-0102",
  "orderItemStatus": "ORDER_COMPLETED",
  "lastOrderItemStatus": "ORDER_RECEIVED",
  "orderProductType": "INSTALL",
  "orderItemType": "PURCHASE",
  "orderId": "0105503700034",
  "channelId": "aa9ef31a90e57a6a172ea2ef7014cd16",
  "customerId": "25a0fb7e3312377f1ff4cbf4ec73466d",
  "referrerCode": null,
  "contractId": null,
  "materialId": "WP_999011",
  "quantity": 1,
  "address": {
    "orderId": "0105503700034",
    "lastName": "Min",
    "firstName": "Steve",
    "address1": "12822 Crow Valley Ln",
    "address2": "1234",
    "zipcode": "77099",
    "city": "Houston",
    "state": "TX",
    "phone": "+821032079213",
    "email": "hg.min@coway.com",
    "id": "1al4lgt4mY9"
  },
  "tax": 99.41,
  "taxLines": [
    {
      "title": "Texas State Tax",
      "rate": 0.0625,
      "price": 75.31
    },
    {
      "title": "Houston City Tax",
      "rate": 0.01,
      "price": 12.05
    },
    {
      "title": "Houston Mta Transit",
      "rate": 0.01,
      "price": 12.05
    }
  ],
  "subtotalPrice": 1350,
  "itemPrice": 1350,
  "discountPrice": 145,
  "registrationPrice": 0,
  "createTime": "2025-02-06 04:16:00.287",
  "updateTime": "2025-02-06 04:16:00.591942",
  "channel": null
}        
""".trimIndent()

    val onetimePaymentJson = """
{
  "id": "1al4tiH7rHk",
  "paymentId": "1al4lgt4mY5",
  "transactionType": "CHARGE",
  "orderId": "0105503700034",
  "transactionId": "7159482351832",
  "paymentMethod": "CREDIT_CARD",
  "paymentTime": "2025-02-06T04:15:14Z",
  "currency": "USD",
  "totalPrice": 6835.99,
  "tax": 520.99,
  "subtotalPrice": 6315,
  "itemPrice": 6750,
  "discountPrice": 435,
  "prepaidAmount": 0,
  "registrationPrice": 0,
  "promotions": {},
  "taxLines": [
    {
      "title": "Texas State Tax",
      "rate": 0.0625,
      "price": 394.69,
      "paymentId": "1al4lgt4mY5",
      "id": "1al4tiBRsgK"
    },
    {
      "title": "Houston City Tax",
      "rate": 0.01,
      "price": 63.15,
      "paymentId": "1al4lgt4mY5",
      "id": "1al4tiBRsgL"
    },
    {
      "title": "Houston Mta Transit",
      "rate": 0.01,
      "price": 63.15,
      "paymentId": "1al4lgt4mY5",
      "id": "1al4tiBRsgM"
    }
  ],
  "address": {
    "firstName": "Steve",
    "lastName": "Min",
    "email": "hg.min@coway.com",
    "phone": "+821032079213",
    "state": "TX",
    "city": "Houston",
    "address1": "12822 Crow Valley Ln",
    "address2": "1234",
    "zipcode": "77099"
  },
  "refunds": [
    {
      "kind": "REFUND",
      "price": {
        "currency": "USD",
        "totalPrice": 1461.37
      },
      "orderItemId": "0105503700034-0502",
      "paymentTime": "2025-02-06T04:17:01Z",
      "transactionId": "7159484121304",
      "acquirerReferenceNumber": "5410692566074431"
    },
    {
      "kind": "REFUND",
      "price": {
        "currency": "USD",
        "totalPrice": 1304.41
      },
      "orderItemId": "0105503700034-0302",
      "paymentTime": "2025-02-06T04:17:19Z",
      "transactionId": "7159484285144",
      "acquirerReferenceNumber": "4144054995849728"
    },
    {
      "kind": "REFUND",
      "price": {
        "currency": "USD",
        "totalPrice": 1461.37
      },
      "orderItemId": "0105503700034-0402",
      "paymentTime": "2025-02-06T04:17:29Z",
      "transactionId": "7159484514520",
      "acquirerReferenceNumber": "5533146269153804"
    },
    {
      "kind": "VOID",
      "price": {
        "currency": "USD",
        "totalPrice": 1304.41
      },
      "orderItemId": "0105503700034-0102",
      "paymentTime": "2025-02-06T04:17:39Z",
      "transactionId": "7159484776664",
      "acquirerReferenceNumber": "958635141141712"
    }
  ],
  "updateTime": "2025-02-06T04:17:48.916472Z"
}        
""".trimIndent()

    test ("refund json parsing") {
        logger.info("json:$voidJson")
        val refund = Refund.parse(voidJson)

        refund.kind shouldBe RefundKind.VOID
        refund.price.totalPrice shouldBe BigDecimal("1304.41")
        refund.orderItemId shouldBe "0105503700034-0102"
        refund.transactionId shouldBe "7159484776664"
        refund.acquirerReferenceNumber shouldBe "958635141141712"
    }

    test ("orderItem json parsing") {
        val orderItem = IfOrderItem.parse(orderItemJson)
        orderItem.orderItemType shouldBe IfOrderItemType.PURCHASE
        orderItem.orderId shouldBe "0105503700034"
        orderItem.orderItemId shouldBe "0105503700034-0102"

    }

    test("onetimePayment json parsing") {
        val onetimePayment = IfOnetimePayment.parse(onetimePaymentJson)
        onetimePayment.paymentId shouldBe "1al4lgt4mY5"
        onetimePayment.transactionType shouldBe IfTransactionType.CHARGE
    }

    test ("[OnetimePaymentRefundProcessRule] processPaymentVoid test") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)

        val void = Refund.parse(voidJson)
        val orderItem = IfOrderItem.parse(orderItemJson)
        val onetimePayment = IfOnetimePayment.parse(onetimePaymentJson)
        val docTemplate = documentTemplateServiceable.findDocTemplates(companyCode, listOf(DocumentTemplateCode.ONETIME_PAYMENT_VOID))[0]

        val voidItems = onetimePayment.refunds?.filter { it.kind == RefundKind.VOID  } ?: emptyList()
        voidItems.size shouldBe 1

        val voidItem = voidItems.first()
        val processItem = OnetimePaymentProcessItem(
            companyCode,
            docTemplate,
            customerId = orderItem.customerId,
            onetimePayment = onetimePayment,
            orderItem = orderItem,
            refund = voidItem
        )

        voidItem shouldBe void

        val result = refundProcessRule.processPaymentVoid(context, processItem = processItem)
        logger.info("result: $result")

        result shouldNotBe null
        result!!.reference shouldBe onetimePayment.paymentId
        val orderItemIds = result.docItems[0].attributes.filter { it.attributeType == DocumentAttributeType.ORDER_ITEM_ID }.map { it.attributeValue }
        orderItemIds shouldContain orderItem.orderItemId
    }


    test ("[OnetimePaymentRefundProcessRule] processPaymentRefund test") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)

        val refund = Refund.parse(refundJson)
        val orderItem = IfOrderItem.parse(orderItemJson)
        val onetimePayment = IfOnetimePayment.parse(onetimePaymentJson)
        val docTemplate = documentTemplateServiceable.findDocTemplates(companyCode, listOf(DocumentTemplateCode.ONETIME_PAYMENT_REFUND))[0]

        val refundItems = onetimePayment.refunds?.filter { it.kind == RefundKind.REFUND  } ?: emptyList()
        refundItems.size shouldBe 3

        val refundItem = refundItems.first()
        val processItem = OnetimePaymentProcessItem(
            companyCode,
            docTemplate,
            customerId = orderItem.customerId,
            onetimePayment = onetimePayment,
            orderItem = orderItem,
            refund = refundItem
        )

        refundItem shouldBe refund

        val result = refundProcessRule.processPaymentRefund(context, processItem = processItem)
        logger.info("result: $result")

        result shouldNotBe null
        result!!.reference shouldBe onetimePayment.paymentId
        val orderItemIds = result.docItems[0].attributes.filter { it.attributeType == DocumentAttributeType.ORDER_ITEM_ID }.map { it.attributeValue }
        orderItemIds shouldContain orderItem.orderItemId
    }

}) {
    companion object {
        val logger = KotlinLogging.logger { }
    }
}