package com.abc.us.accounting.rentals.onetime.domain.repository

import com.abc.us.accounting.collects.domain.repository.CollectInventoryValuationRepository
import com.abc.us.accounting.collects.domain.repository.CollectMaterialRepository
import com.abc.us.accounting.collects.domain.repository.CollectOrderItemRepository
import com.abc.us.accounting.collects.domain.repository.CollectServiceFlowRepository
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicCollectRegistryTests (
    private val orderItemRepository: CollectOrderItemRepository,
    private val serviceFlowRepository: CollectServiceFlowRepository,
    private val materialRepository: CollectMaterialRepository,
    private val inventoryValuationRepository: CollectInventoryValuationRepository,
    ) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        val dallas = ZoneId.of("America/Chicago")
        val dallasOffset = dallas.rules.getOffset(Instant.now())

        val startTime = OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, dallasOffset)
        val endTime = startTime.plusMonths(1).minusNanos(1)
    }

    @Test
    fun `basic order item id tests`() {
        /**
         * select order_item_id, material_id, * from collect_order_item
         * where order_item_type = 'ONETIME'
         *   and create_time between '2024-12-01' and '2024-12-31'
         *   and is_active = 'Y'
         *   and order_item_status in ('ORDER_RECEIVED', 'BOOKING_CONFIRMED', 'INSTALL_COMPLETED');
         */

        val orderItemTypes = listOf(OrderItemType.ONETIME)
        val orderItemStatuses = listOf(OrderItemStatus.ORDER_RECEIVED, OrderItemStatus.BOOKING_CONFIRMED, OrderItemStatus.INSTALL_COMPLETED)
        val results = orderItemRepository.findAllByCriteria(startTime, endTime, orderItemTypes, orderItemStatuses)
        val size = results.size

        logger.info("results: ${results.size} by $startTime ~ $endTime")

        // inner join - 549, left join 566
        results.size shouldBe 382
    }


    @Test
    fun `basic lookup test`() {
        val orderItemIds = listOf("0105433800036-0402", "0105433800025-0101")
        val serviceTypes = listOf(ServiceFlowType.INSTALL)
//        val serviceStatuses = listOf(ServiceFlowStatus.SERVICE_SCHEDULED)
        val serviceStatuses = ServiceFlowStatus.entries
        val startTime = OffsetDateTime.parse("2024-11-01T00:00:00Z")
        val endTime = OffsetDateTime.parse("2024-12-31T23:59:59Z")

        var results = serviceFlowRepository.findAllBy(orderItemIds, serviceTypes, serviceStatuses, startTime, endTime)
        logger.info("results: $results")
        results.size shouldBe 14
        results.forEach{
            logger.info("result: $it")
            it.orderItemId shouldBeIn orderItemIds
        }
    }


    @Test
    fun `basic lookup by order_item_id`() {
        val orderItemIds = listOf("0105433800037-0202")
        val serviceTypes = listOf(ServiceFlowType.INSTALL)
//        val serviceStatuses = listOf(ServiceFlowStatus.SERVICE_SCHEDULED)
        val serviceStatuses = ServiceFlowStatus.entries
        val startTime = OffsetDateTime.parse("2024-11-01T00:00:00Z")
        val endTime = OffsetDateTime.parse("2024-12-31T23:59:59Z")

        var results = serviceFlowRepository.findAllBy(orderItemIds, serviceTypes, serviceStatuses, startTime, endTime)
        logger.info("results: ${results.size}")
        results.size shouldBe 7
        results.forEach {
            logger.info("result: $it")
            it.orderItemId shouldBeIn orderItemIds
        }
    }

    @Test
    fun `basic lookup by materialId`() {
        val materialIds = listOf(
            "WP_113627", "WP_113727", "WP_113786", "WP_113785", "WP_113725",
            "WP_113781", "WP_113637", "WP_113816", "WP_113819", "WP_113729",
            "WP_113820", "WP_113792", "WP_113713", "WP_113818", "WP_113780",
            "WP_113643", "WP_113782", "WP_113793", "WP_113784", "WP_113783")
        val materials = materialRepository.findAllByMaterialIdIn(materialIds)
        materials.size shouldBe materialIds.size

        val valuations = inventoryValuationRepository.findAllBy(materialIds, issueTime =  startTime).groupBy { it.materialId!! }
//        valuations.size shouldBe materialIds.size
        materialIds.forEach { materialId ->
            val valuation = valuations[materialId]
            if (valuation == null) {
                logger.warn("materialId: $materialId is not found")
            }
        }
    }


    @Test
    fun `basic lookup by single materialId`() {
        val materialIds = listOf(
            "WP_113637")
        val materials = materialRepository.findAllByMaterialIdIn(materialIds)
        materials.size shouldBe materialIds.size

        val valuations = inventoryValuationRepository.findAllBy(materialIds, issueTime =  startTime).groupBy { it.materialId!! }
        logger.debug("materialIds:${materialIds}, valuations: $valuations")

        valuations.size shouldBe materialIds.size
        materialIds.forEach { materialId ->
            val valuation = valuations[materialId]
            if (valuation == null) {
                logger.warn("materialId: $materialId is not found")
            }
        }
    }
}