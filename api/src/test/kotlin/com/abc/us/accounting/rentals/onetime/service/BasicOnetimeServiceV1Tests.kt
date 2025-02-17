package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.collects.domain.type.MaterialType
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.DocumentOriginRequest
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.FilteringRule
import com.abc.us.accounting.documents.service.DocumentAutoClearingService
import com.abc.us.accounting.documents.service.DocumentPersistenceService
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import com.abc.us.accounting.rentals.onetime.domain.repository.CustomOnetimeSupportRepository
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.OffsetDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicOnetimeServiceV1Tests(
    private val onetimeDocumentService: OnetimeDocumentService,
    private val persistenceService: DocumentPersistenceService,
    private val documentAutoClearingService: DocumentAutoClearingService,
    private val customOnetimeSupportRepository: CustomOnetimeSupportRepository,
    private val timeLogger: TimeLogger = TimeLogger()

): FunSpec({
    val texasTimeZone = TimeZoneCode.TEXAS
    val dallasOffset = texasTimeZone.getZoneOffset()

    val companyCode = CompanyCode.T200
    val startTime = OffsetDateTime.of(2024, 11, 1, 0, 0, 0, 0, dallasOffset)
    val endTime = startTime.plusMonths(2).minusNanos(1)

    val isFullTest = false
    val eachMaxResult = if (isFullTest) 5000 else 1

    test ("[일시불] 전체 전표처리") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val maxResult = if (isFullTest) 5000 else 10
        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime, maxResult = maxResult)
        logger.debug("results: {} by companyCode: {}, startTime: {}, endTime: {}", results.size, companyCode, startTime, endTime)

        // 반제대상 시간은 현재까지로 해야 함
        val clearing = documentAutoClearingService.processAutoClearing(context, companyCode, startTime, endTime = texasTimeZone.now())
        logger.debug("clearing: {} by companyCode: {}, startTime: {}, endTime: {}", clearing.size, companyCode, startTime, endTime)

        persistenceService.cleanup(context, results.map { it.docId })
        results.size shouldBeLessThanOrEqual maxResult
        clearing.size shouldBeGreaterThan 0
    }

    test ("[일시불] 중복처리 테스트") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val maxResult = if (isFullTest) 5000 else 10

        val results1 = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime, maxResult = maxResult)
        logger.debug("results1: ${results1.size}, results1 docIds: ${results1.map { it.docId }}, results1 docHashes: ${results1.map { it.docHash }}")

        val results2 = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime, maxResult = maxResult)
        logger.debug("results2: ${results2.size}, results2 docIds: ${results2.map { it.docId }}, results2 docHashes: ${results2.map { it.docHash }}")

        results1.size shouldBe results2.size

        results1.zip(results2).forEach { (r1, r2) ->
            r1.docId shouldBe r2.docId
            r1.docHash shouldBe r2.docHash
        }

        val results = results1 + results2

        persistenceService.cleanup(context, results.map { it.docId })
        results.size shouldBeLessThanOrEqual maxResult*2
    }

    /**
     * TODO: 자동반제 성능 최적화 필요
     */
    test ("[일시불] 매출/원가 전표처리") {
        val context = DocumentServiceContext.SAVE_DEBUG
        val templateCodes = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        )

        val maxResult = if (isFullTest) 5000 else 10
        var results = emptyList<DocumentResult>()
        timeLogger.measureAndLog ("[일시불] 매출/원가 전표처리 - 포스팅 전표 생성") {
            results = onetimeDocumentService.processOnetimeBatchWithTemplateCodes(
                context,
                companyCode,
                templateCodes,
                startTime,
                maxResult = maxResult
            )
        }

        if ( isFullTest ) {
            val expected = expectedCount(templateCodes, startTime = startTime, endTime = endTime, customOnetimeSupportRepository)
            logger.info("results.size:${results.size}, expected size:${expected}")
            results.size shouldBe expected
        }

        timeLogger.measureAndLog("[일시불] 매출/원가 전표처리 - 자동 반제전표 생성") {
//            results.chunked(Constants.DOCUMENT_BATCH_SIZE).forEach { chunk ->
//                documentAutoClearingService.postClearing(context, chunk)
//            }
            val cleared = documentAutoClearingService.postClearing(context, results)
            logger.info("cleared results: ${cleared.size}, results: ${results.size}")
        }
        logger.info("results: ${results.size}")

        timeLogger.measureAndLog("[일시불] 매출/원가 전표처리 - cleanup") {
            persistenceService.cleanup(context, results.map { it.docId })
        }
//        results.size shouldBe 383
        results.size shouldBeLessThanOrEqual maxResult
    }


    test("[일시불] 주문접수") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val results = onetimeDocumentService.processPaymentReceived(context, companyCode, startTime, endTime, maxResult = eachMaxResult)
        logger.info("results: ${results.size}")

        if ( isFullTest ) {
            val orderItemTypes = listOf(OrderItemType.ONETIME).map { it.name }
            val materialTypes = listOf(MaterialType.PRODUCT).map { it.name }
            val orderItemStatuses = listOf(OrderItemStatus.ORDER_RECEIVED).map { it.name }
            val count = customOnetimeSupportRepository.countByCriteria(startTime, endTime, orderItemTypes, orderItemStatuses, materialTypes)
            logger.info("count: $count, result: ${results.size}")
            results.size shouldBeLessThanOrEqual eachMaxResult
            results.size shouldBe count
        }
        persistenceService.cleanup(context, results.map { it.docId })
    }

    test("[일시불] 입금완료") {
        val context = DocumentServiceContext.SAVE_DEBUG
        val results = onetimeDocumentService.processPaymentDeposit(context, companyCode, startTime, endTime, maxResult = eachMaxResult)
        logger.info("results: ${results.size}")

        if ( isFullTest ) {

            val orderItemTypes = listOf(OrderItemType.ONETIME).map { it.name }
            val materialTypes = listOf(MaterialType.PRODUCT).map { it.name }
            val orderItemStatuses = listOf(OrderItemStatus.ORDER_RECEIVED).map { it.name }
            val count = customOnetimeSupportRepository.countByCriteria(
                startTime,
                endTime,
                orderItemTypes,
                orderItemStatuses,
                materialTypes
            )
            logger.info("count: $count, result: ${results.size}")
            results.size shouldBeLessThanOrEqual eachMaxResult
            results.size shouldBe count
        }
        persistenceService.cleanup(context, results.map { it.docId })
    }


    test("[일시불] 제품출고") {
        val context = DocumentServiceContext.SAVE_DEBUG

        val results = onetimeDocumentService.processProductShipped(context, companyCode, startTime, endTime, maxResult = eachMaxResult)
        logger.info("results: ${results.size}")

        if ( isFullTest ) {
            val expected = expectedCount(listOf(DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED),
                startTime = startTime, endTime = endTime, customOnetimeSupportRepository)
            logger.info("expected: $expected, result: ${results.size}")
            results.size shouldBe expected
        }
        results.size shouldBeLessThanOrEqual eachMaxResult
        persistenceService.cleanup(context, results.map { it.docId })
    }

    test("[일시불] 매출인식 (설치완료)") {
        val context = DocumentServiceContext.SAVE_DEBUG
        val results = onetimeDocumentService.processSalesRecognition(context, companyCode, startTime, endTime, maxResult = eachMaxResult)

        if ( isFullTest ) {
            val orderItemIds = results.map { it.docItems.map { it.attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value } }.flatten() }.flatten()
            logger.info("results: ${results.size}, orderItemIds: $orderItemIds")

            val orderItemTypes = listOf(OrderItemType.ONETIME)
            val materialTypes = listOf(MaterialType.PRODUCT)
            val orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)

            val expected = customOnetimeSupportRepository.findByCriteria3(
                startTime,
                endTime,
                orderItemTypes,
                orderItemStatuses,
                materialTypes
            ).distinctBy{ it.collectOrderItem.orderItemId }

            val expectedOrderItemIds = expected
                .map { it.collectOrderItem.orderItemId }
            logger.info("expected: ${expected.size}, result: ${results.size}, expectedOrderItemIds: $expectedOrderItemIds")

            val missedOrderItemIds = expectedOrderItemIds.filter { it !in orderItemIds }
            logger.info { "missedOrderItemIds: $missedOrderItemIds" }
            results.size shouldBe expected.size
        }

        persistenceService.cleanup(context, results.map { it.docId })
        results.size shouldBeLessThanOrEqual eachMaxResult
    }

    test ("[일시불] 매출원가인식 (설치완료)"){
        val context = DocumentServiceContext.SAVE_DEBUG
        val results = onetimeDocumentService.processCOGSRecognition(context, companyCode, startTime, endTime, maxResult = eachMaxResult)

        if ( isFullTest ) {
            val orderItemIds = results.map { it.docItems.map { it.attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value } }.flatten() }.flatten()
            logger.info("results: ${results.size}, orderItemIds: $orderItemIds")

            val orderItemTypes = listOf(OrderItemType.ONETIME)
            val materialTypes = listOf(MaterialType.PRODUCT)
            val orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)

            val expected = customOnetimeSupportRepository.findByCriteria3(
                startTime,
                endTime,
                orderItemTypes,
                orderItemStatuses,
                materialTypes
            ).distinctBy{ it.collectOrderItem.orderItemId }

            val expectedOrderItemIds = expected
                .map { it.collectOrderItem.orderItemId }
            logger.info("expected: ${expected.size}, result: ${results.size}, expectedOrderItemIds: $expectedOrderItemIds")

            val missedOrderItemIds = expectedOrderItemIds.filter { it !in orderItemIds }
            logger.info { "missedOrderItemIds: $missedOrderItemIds" }
            results.size shouldBe expected.size
        }
        persistenceService.cleanup(context, results.map { it.docId })

        results.size shouldBeLessThanOrEqual eachMaxResult
    }

    test ("[일시불] 선수금 대체(설치완료)"){
        val context = DocumentServiceContext.SAVE_DEBUG
        val results = onetimeDocumentService.processAdvancedPaymentOffset(context, companyCode, startTime, endTime, maxResult = eachMaxResult)

        if ( isFullTest ) {
            val orderItemIds = results.map { it.docItems.map { it.attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value } }.flatten() }.flatten()
            logger.info("results: ${results.size}, orderItemIds: $orderItemIds")

            val orderItemTypes = listOf(OrderItemType.ONETIME)
            val materialTypes = listOf(MaterialType.PRODUCT)
            val orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)

            val expected = customOnetimeSupportRepository.findByCriteria3(
                startTime,
                endTime,
                orderItemTypes,
                orderItemStatuses,
                materialTypes
            ).distinctBy{ it.collectOrderItem.orderItemId }

            val expectedOrderItemIds = expected
                .map { it.collectOrderItem.orderItemId }
            logger.info("expected: ${expected.size}, result: ${results.size}, expectedOrderItemIds: $expectedOrderItemIds")

            val missedOrderItemIds = expectedOrderItemIds.filter { it !in orderItemIds }
            logger.info { "missedOrderItemIds: $missedOrderItemIds" }
            results.size shouldBe expected.size
        }

        persistenceService.cleanup(context, results.map { it.docId })
//        results.size shouldBe 29
        results.size shouldBeLessThanOrEqual eachMaxResult
    }

    test ("[일시불] 전체 전표처리 - OrderId filtering") {
        val orderId = "0105436100013"
        val context = DocumentServiceContext.withOrderIds(listOf( orderId))

        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")

        for (result in results) {
            logger.debug { "result: $result" }
        }
        val orderItemIds = toOrderItemIds(results)
        logger.info("orderId:$orderId, orderItemIds: $orderItemIds")


        val expected = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        )

        val actual = results.mapNotNull{ it.docOrigin?.docTemplateCode }
        val diff = expected - actual.toSet()
        logger.info { "diff: $diff" }


        persistenceService.cleanup(context, results.map { it.docId })
        diff shouldBe expected.subList(2, expected.size)
        results.size shouldBe 2
    }


    test ("[일시불] 전체 전표처리 - OrderItemId filtering") {
//        val orderItemId = "0105434500037-0102"
//        val orderItemId = "0105435200006-0102"  // taxlines가 없는 케이스
        /**
         * State Tax가 2개임, Debit amount:1545.7550 and credit amount:1447.8750 are not equal, by templateCode:ONETIME_SALES_RECOGNITION, orderItemId:0105434700021-0102
         */
//        val orderItemId = "0105434700021-0102"
        val orderItemId = "0105436100016-0102" // --> customerId: 8dddc3c0df38f528d01edbb2e39ce0b1
        val context = DocumentServiceContext.withOrderItemIds(listOf( orderItemId))

        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")

        for (result in results) {
            logger.debug { "result: $result" }
        }
        val orderItemIds = toOrderItemIds(results)
        logger.info("orderItemIds: $orderItemIds")

        val expected = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        )

        val actual = results.mapNotNull{ it.docOrigin?.docTemplateCode }
        val diff = expected - actual.toSet()
        logger.info { "diff: $diff" }


        persistenceService.cleanup(context, results.map { it.docId })
        diff shouldBe emptyList()
        results.size shouldBe 6
    }


    test ("[일시불] 전체 전표처리 - CustomerId filtering") {
        val customerId = "8dddc3c0df38f528d01edbb2e39ce0b1";

        val context = DocumentServiceContext.withCustomerIds(listOf( customerId))

        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")

        for (result in results) {
            logger.debug { "result: $result" }
        }

        val orderItemIds = toOrderItemIds(results)

        logger.info("customerId:$customerId, orderItemIds: $orderItemIds")

        val expected = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        )

        val actual = results.mapNotNull{ it.docOrigin?.docTemplateCode }
        val diff = expected - actual.toSet()
        logger.info { "diff: $diff" }


        persistenceService.cleanup(context, results.map { it.docId })
        diff shouldBe emptyList()
        results.size shouldBeGreaterThanOrEqual 6
    }


    test ("[일시불] 전체 전표처리 - MaterialId filtering") {
        val materialId = "WP_113627"

        val context = DocumentServiceContext.withMaterialIds(listOf( materialId))

        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")

        for (result in results) {
            logger.debug { "result: $result" }
        }

        val orderItemIds = toOrderItemIds(results)
        logger.info("materialId:$materialId, orderItemIds: $orderItemIds")


        val expected = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        )

        val actual = results.mapNotNull{ it.docOrigin?.docTemplateCode }
        val diff = expected - actual.toSet()
        logger.info { "diff: $diff" }


        persistenceService.cleanup(context, results.map { it.docId })
        diff shouldBe emptyList()
        results.size shouldBe 162
    }


    test ("[일시불] 전체 전표처리 - Custom Filtering Rules") {
        val filteringRule = FilteringRule(
            orderItemIds = listOf("0105433700001-0102"),
            docTemplateCodes = listOf(DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT)
        )
        val context = DocumentServiceContext.withFilteringRule(filteringRule)
        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")
        for (result in results) {
            logger.debug { "result: $result" }
        }

        val orderItemIds = toOrderItemIds(results)
        logger.info("filteringRule:$filteringRule, orderItemIds: $orderItemIds")

        val expected = listOf(
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
        )

        val actual = results.mapNotNull{ it.docOrigin?.docTemplateCode }
        val diff = expected - actual.toSet()
        logger.info { "diff: $diff" }

        persistenceService.cleanup(context, results.map { it.docId })
        diff shouldBe emptyList()
        results.size shouldBe 1
    }

    test ("[일시불] 조회용 - Custom Filters") {
        val filteringRule = FilteringRule(
            orderItemIds = listOf("0105433000002-0102"),
            docTemplateCodes = listOf(DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED),
        )
        val context = DocumentServiceContext.withFilteringRule(filteringRule)
        val results = onetimeDocumentService.processOnetimeBatch(context, companyCode, startTime, endTime)
        logger.debug("results: ${results.size} by companyCode: $companyCode, startTime: $startTime, endTime: $endTime")
        for (result in results) {
            logger.debug { "result: $result" }
        }

        val orderItemIds = toOrderItemIds(results)
        logger.info("filteringRule:$filteringRule, orderItemIds: $orderItemIds")

        persistenceService.cleanup(context, results.map { it.docId })
    }


    test ("hashCode 테스트") {
        val localDate = startTime.toLocalDate()
        var hashCode = localDate.hashCode()
        var hash = Hashs.hash(localDate)
        logger.info("hashCode: $hashCode, hash: $hash, localDate: $localDate")

        hashCode shouldBe 4145857
        hash shouldBe "723a0ee071c009506628a0c674dfef55"

        // enum의 hashCode는 실행시마다 변경됨
        val templateCode = DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT
        hashCode = templateCode.hashCode()
        hash = Hashs.hash(templateCode)
        logger.info("hash: $hash, templateCode: $templateCode, templateCodeHash: ${hashCode}")
        hashCode shouldNotBe 1937138249
        hash shouldNotBe "e4a33a00ad0ac3284c35ab4dc014e14e"


        val origin = DocumentOriginRequest(
            docTemplateCode = DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            bizSystem = BizSystemType.ONETIME,
            bizTxId = "0105433700001-0102",
            bizProcess = BizProcessType.RECEIVABLES,
            bizEvent = BizEventType.ORDER_RECEIVED,
            accountingEvent = "event"
        )

        hash = Hashs.hash(origin)
        logger.info("hash: $hash, origin: $origin")
        hash shouldBe "7b05baccbff806f8911fa143f5fc90e7"

    }

}){

    companion object{
        val logger = KotlinLogging.logger {  }

        fun toOrderItemIds (documentResults:List<DocumentResult>): List<String> {
            return documentResults.map { document -> document.docItems.map {
                    docItem -> docItem.attributes.filter { attribute -> attribute.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value }
                }.flatten()
            }.flatten().distinct().sorted()
        }

        fun expectedCount (templateCodes:List<DocumentTemplateCode>, startTime:OffsetDateTime, endTime:OffsetDateTime,
                           customOnetimeSupportRepository: CustomOnetimeSupportRepository):Int {
            val suggested = listOf(DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
                DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
                DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
                DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
                DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
                DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET)

            templateCodes.forEach { templateCode ->
                templateCode shouldBeIn suggested
            }
            val orderItemTypes = listOf(OrderItemType.ONETIME)
            val materialTypes = listOf(MaterialType.PRODUCT)


            val counts = templateCodes.mapNotNull { templateCode ->
                when (templateCode) {
                    DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED, DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> {
                        val orderItemStatuses = listOf(OrderItemStatus.ORDER_RECEIVED)

                        val count = customOnetimeSupportRepository.countByCriteria(startTime, endTime,
                            orderItemTypes.map { it.name },
                            orderItemStatuses.map { it.name },
                            materialTypes.map { it.name })

                        logger.info("expectedCount count: $count by templateCode: $templateCode")
                        count
                    }
                    DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED -> {
                        val orderItemStatuses = listOf(OrderItemStatus.BOOKING_CONFIRMED)
                        val serviceFlowTypes = listOf(ServiceFlowType.INSTALL)
                        val serviceFlowStatuses = listOf(ServiceFlowStatus.SERVICE_SCHEDULED)

                        val count = customOnetimeSupportRepository.countByCriteriaWithServiceFlow(
                            startTime,
                            endTime,
                            orderItemTypes.map { it.name },
                            orderItemStatuses.map { it.name },
                            materialTypes.map { it.name },
                            serviceFlowTypes.map { it.name },
                            serviceFlowStatuses.map { it.name }
                        )
                        logger.info("expectedCount count: $count by templateCode: $templateCode")
                        count
                    }
                    DocumentTemplateCode.ONETIME_SALES_RECOGNITION, DocumentTemplateCode.ONETIME_COGS_RECOGNITION, DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> {
                        val orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)

                        val expected = customOnetimeSupportRepository.findByCriteria3(
                            startTime,
                            endTime,
                            orderItemTypes,
                            orderItemStatuses,
                            materialTypes
                        ).distinctBy{ it.collectOrderItem.orderItemId }

                        logger.info("expectedCount count: $expected.size by templateCode: $templateCode")
                        expected.size
                    }
                    else -> null
                }
            }
            return counts.sum()
        }
    }
}