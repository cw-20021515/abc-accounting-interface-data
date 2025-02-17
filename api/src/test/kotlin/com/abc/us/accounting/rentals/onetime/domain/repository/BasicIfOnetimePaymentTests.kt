package com.abc.us.accounting.rentals.onetime.domain.repository

import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.iface.domain.repository.oms.*
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicIfOnetimePaymentTests (
    private val materialRepository: IfMaterialRepository,
    private val orderItemRepository: IfOrderItemRepository,
    private val onetimePaymentRepository: IfOnetimePaymentRepository,
    private val serviceFlowRepository: IfServiceFlowRepository,
    private val channelRepository: IfChannelRepository,
//    private val inventoryCosting: InventoryCostingRepository,
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        val dallas = ZoneId.of("America/Chicago")
        val dallasOffset = dallas.rules.getOffset(Instant.now())
        val companyCode = CompanyCode.T200
        val startTime = OffsetDateTime.of(2025, 2, 1, 0, 0, 0, 0, dallasOffset)
        val endTime = startTime.plusMonths(2)
    }

    @Test
    fun `basic test`() {
        val channels = channelRepository.findAll()
        logger.info("channels = {}", channels)

        val serviceFlows = serviceFlowRepository.findAll()
        logger.info("serviceFlows = {}", serviceFlows)

        val onetimePayments = onetimePaymentRepository.findAll()
        logger.info("onetimePayments = {}", onetimePayments)

        val materials = materialRepository.findAll()
        logger.info("materials = {}", materials)

        val orderItems = orderItemRepository.findAll()
        logger.info("orderItems = {}", orderItems)
    }

    @Test
    fun `onetimePayments test`() {
        // 최초 결제
        run {
            val slice = onetimePaymentRepository.findOnetimePayments(startTime, isRefund = false)
            logger.info("onetimePaymentReceipts, size={}", slice.size)
            slice.content.forEach { item ->
                logger.info("onetimePaymentItem = {}",item)
            }

            slice.size shouldBe 13
        }

        // 취소, 환불
        run {
            val slice = onetimePaymentRepository.findOnetimePayments(startTime, endTime, isRefund = true)
            logger.info("onetimePaymentRefunds, size={}", slice.size)
            slice.content.forEach { item ->
                logger.info("onetimePaymentItem = {}",item)
            }
            slice.size shouldBe 5

            val orderItemIds = slice.filter{item -> item.refunds != null}
                .map { item -> item.refunds!!.map { it.orderItemId } }
                .flatten()

            logger.info("orderItemIds = {}", orderItemIds)
            val orderItems = orderItemRepository.findByOrderItemIdsIn(orderItemIds)
            orderItems.forEach{ orderItem ->
                logger.info("orderItem:${orderItem}")
            }
        }
    }
}