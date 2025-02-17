package com.abc.us.accounting.collects.domain.service

//import com.abc.us.accounting.collects.domain.entity.collect.CollectContract
//import com.abc.us.accounting.collects.domain.type.ARChargeItemType
//import com.abc.us.accounting.collects.domain.type.ARChargeStatus
//import com.abc.us.accounting.collects.model.*
//import com.abc.us.accounting.collects.service.ARService
//import com.abc.us.accounting.supports.entity.toEntityHash
//import junit.framework.TestCase.*
//import org.junit.jupiter.api.BeforeEach
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import java.math.BigDecimal
//import java.time.OffsetDateTime
//import kotlin.test.Test
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class ARServiceTest @Autowired constructor(private val service: ARService) {
//    private lateinit var jsonData: String
//    private lateinit var mockAr: AccountsReceivable
//
//    @BeforeEach
//    fun setup() {
//        jsonData = """
//            {
//              "code": "SUCCESS",
//              "message": "요청이 성공했습니다",
//              "data": {
//                "page": {
//                  "current": 1,
//                  "total": 10,
//                  "size": 20,
//                  "totalItems": 200
//                },
//                "items": [
//                  {
//                    "chargeId": "chargeId-001",
//                    "billingCycle": 1,
//                    "targetMonth": "2024-09",
//                    "totalPrice": 0,
//                    "chargeStatus": "CREATED",
//                    "chargeItems": [
//                      {
//                        "chargeItemType": "SERVICE_FEE",
//                        "serviceFlowId": "serviceFlowId-001",
//                        "quantity": 0,
//                        "totalPrice": 0,
//                        "priceDetail": {
//                          "discountPrice": 0,
//                          "itemPrice": 0,
//                          "prepaidAmount": 0,
//                          "tax": 0,
//                          "taxLines": [
//                            {
//                              "title": "State Tax",
//                              "rate": 0.0625,
//                              "price": 16
//                            }
//                          ],
//                          "currency": "USD"
//                        },
//                        "isTaxExempt": true,
//                        "createTime": "2019-08-24T14:15:22Z",
//                        "chargeItemId": "chargeItemId-001"
//                      }
//                    ],
//                    "createTime": "2019-08-24T14:15:22Z",
//                    "updateTime": "2019-08-24T14:15:22Z",
//                    "contractId": "contractId-001",
//                    "payment": {
//                      "invoiceId": "invoiceId-001",
//                      "chargeId": "chargeId-001",
//                      "totalPrice": 0,
//                      "chargeItems": [
//                        {
//                          "chargeItemType": "SERVICE_FEE",
//                          "serviceFlowId": "serviceFlowId-001",
//                          "quantity": 0,
//                          "totalPrice": 0,
//                          "priceDetail": {
//                            "discountPrice": 0,
//                            "itemPrice": 0,
//                            "prepaidAmount": 0,
//                            "tax": 0,
//                            "taxLines": [
//                              {
//                                "title": "State Tax",
//                                "rate": 0.0625,
//                                "price": 16
//                              }
//                            ],
//                            "currency": "USD"
//                          },
//                          "isTaxExempt": true,
//                          "createTime": "2019-08-24T14:15:22Z",
//                          "chargeItemId": "chargeItemId-"
//                        }
//                      ],
//                      "paymentMethod": "CREDIT_CARD",
//                      "transactionId": "transactionId-001",
//                      "cardNumber": "3333-33**-****",
//                      "cardType": "VISA",
//                      "installmentMonths": 0,
//                      "paymentTime": "2019-08-24T14:15:22Z",
//                      "billingAddress": {
//                        "firstName": "John",
//                        "lastName": "Doe",
//                        "email": "john.doe@email.com",
//                        "phone": 16135551212,
//                        "mobile": 16135551212,
//                        "state": "TX",
//                        "city": "Austin",
//                        "address1": "Congress Ave",
//                        "address2": "701",
//                        "zipcode": "78701",
//                        "remark": "string"
//                      }
//                    }
//                  }
//                ]
//              }
//            }
//        """.trimIndent()
//
//        mockAr = AccountsReceivable(
//            arId = "AR-001",
//            billingCycle = 1,
//            targetMonth = "2024-11",
//            totalPrice = BigDecimal("1000.00"),
//            arStatus = ARChargeStatus.CREATED,
//            createTime = OffsetDateTime.now(),
//            updateTime = OffsetDateTime.now(),
//            //contractId = "contractId-001",
//            contract = CollectContract().apply {
//                contractId = "contractId-001"
//            }.apply { hashCode = toEntityHash() },
//            arItems = mutableListOf(
//                AccountsReceivableItem(
//                    arItemId = "Item-001",
//                    arItemType = ARChargeItemType.SERVICE_FEE,
//                    serviceFlowId = "Flow-001",
//                    quantity = 1,
//                    totalPrice = BigDecimal("1000.00"),
//                    isTaxExempt = false,
//                    createTime = OffsetDateTime.now(),
//                    priceDetail = PriceDetail(
//                        discountPrice = BigDecimal("100.00"),
//                        itemPrice = BigDecimal("900.00"),
//                        prepaidAmount = BigDecimal("0.00"),
//                        tax = BigDecimal("90.00"),
//                        currency = "USD",
//                        taxLines = listOf(
//                            TaxLine(title = "State Tax", rate = BigDecimal("0.1"), price = BigDecimal("90.00"))
//                        ),
//                        promotions = emptyList()
//                    )
//                )
//            ),
//            payment = PaymentDTO(
//                paymentId = "Payment-001",
//                arId = "AR-001",
//                totalPrice = BigDecimal("1000.00"),
//                paymentMethod = "CREDIT_CARD",
//                transactionId = "Txn-001",
//                cardNumber = "4111-1111-1111-1111",
//                cardType = "VISA",
//                installmentMonths = 0,
//                paymentTime = OffsetDateTime.now(),
//                arItems = mutableListOf(
//                    AccountsReceivableItem(
//                        arItemId = "Item-001",
//                        arItemType = ARChargeItemType.SERVICE_FEE,
//                        serviceFlowId = "Flow-001",
//                        quantity = 1,
//                        totalPrice = BigDecimal("1000.00"),
//                        isTaxExempt = false,
//                        createTime = OffsetDateTime.now(),
//                        priceDetail = PriceDetail(
//                            discountPrice = BigDecimal("100.00"),
//                            itemPrice = BigDecimal("900.00"),
//                            prepaidAmount = BigDecimal("0.00"),
//                            tax = BigDecimal("90.00"),
//                            currency = "USD",
//                            taxLines = listOf(
//                                TaxLine(title = "State Tax", rate = BigDecimal("0.1"), price = BigDecimal("90.00"))
//                            ),
//                            promotions = emptyList()
//                        )
//                    )
//                ),
//                billingAddress = LocationDTO(
//                    firstName = "John",
//                    lastName = "Doe",
//                    email = "john.doe@example.com",
//                    phone = "1234567890",
//                    mobile = "1234567890",
//                    state = "TX",
//                    city = "Austin",
//                    address1 = "123 Main St",
//                    address2 = null,
//                    zipcode = "78701",
//                    remark = "Test Address"
//                )
//            )
//        )
//    }
//    @Test
//    fun `saveFromCharges and findById 테스트`() {
//        // 서비스 호출
//        service.saveFromCharges(jsonData)
//
//        // Assertions
//        val savedEntity = service.findById("chargeId-001")
//        assertNotNull(savedEntity)
//
//        assertEquals("chargeId-001", savedEntity.arId)
//        //assertEquals(0.00.toBigDecimal(), savedEntity.totalPrice)
//        //assertEquals("contractId-001", savedEntity.contractId)
//        assertEquals(ARChargeStatus.CREATED, savedEntity.arStatus)
//    }
//    @Test
//    fun `findAll 테스트`() {
//        service.saveFromCharges(jsonData)
//        val results = service.findAll()
//        // Assertions
//        assertNotNull(results)
//        assertEquals(1, results.size)
//        assertEquals("chargeId-001", results[0].arId)
//        assertEquals(1, results[0].billingCycle)
//        assertEquals("2024-09", results[0].targetMonth)
//        //assertEquals("contractId-001", results[0].contractId)
//        assertTrue(results[0].arItems.isEmpty())
//    }
//    @Test
//    fun `saveFromAR should correctly save AccountsReceivable and related entities`() {
//        service.saveFromAR(mockAr)
//        val savedARs = service.findAll()
//
//        // Assert AccountsReceivable
//        assert(savedARs.size == 1) { "AccountsReceivable should have been saved once." }
//        assert(savedARs[0].arId == mockAr.arId) { "AccountsReceivable arId mismatch." }
//        assert(savedARs[0].billingCycle == mockAr.billingCycle) { "AccountsReceivable billingCycle mismatch." }
//        assert(savedARs[0].totalPrice == mockAr.totalPrice) { "AccountsReceivable totalPrice mismatch." }
//        // Assert AccountsReceivable
//        assert(savedARs.size == 1) { "AccountsReceivable should have been saved once." }
//        assert(savedARs[0].arId == mockAr.arId) { "AccountsReceivable arId mismatch." }
//        assert(savedARs[0].billingCycle == mockAr.billingCycle) { "AccountsReceivable billingCycle mismatch." }
//        assert(savedARs[0].totalPrice == mockAr.totalPrice) { "AccountsReceivable totalPrice mismatch." }
//
//        // Assert AccountsReceivableItem
//        assert(savedARs[0].arItems.size == mockAr.arItems.size) { "All ARItems should be saved." }
//        val savedItem = savedARs[0].arItems[0]
//        val expectedItem = mockAr.arItems[0]
//        assert(savedItem.arItemId == expectedItem.arItemId) { "ARItem arItemId mismatch." }
//        assert(savedItem.priceDetail?.taxLines?.size == expectedItem.priceDetail?.taxLines?.size) { "ARItem taxLines mismatch." }
//
//
//        // Assert Payment
////        assert(savedPayments.size == 1) { "Payment should have been saved once." }
////        val savedPayment = savedPayments[0]
////        val expectedPayment = mockAr.payment!!
////        assert(savedPayment.paymentId == expectedPayment.paymentId) { "Payment paymentId mismatch." }
////        assert(savedPayment.billingAddress?.state == expectedPayment.billingAddress?.state) { "Payment billingAddress mismatch." }
////
////        // Assert Location
////        assert(savedLocations.size == 1) { "Location should have been saved once." }
////        val savedLocation = savedLocations[0]
////        val expectedLocation = mockAr.payment?.billingAddress!!
////        assert(savedLocation.firstName == expectedLocation.firstName) { "Location firstName mismatch." }
////        assert(savedLocation.city == expectedLocation.city) { "Location city mismatch." }
//    }
//}