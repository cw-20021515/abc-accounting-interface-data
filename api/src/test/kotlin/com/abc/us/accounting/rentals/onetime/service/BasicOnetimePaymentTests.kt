package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.DocumentPersistenceService
import com.abc.us.accounting.iface.domain.model.RefundKind
import com.abc.us.accounting.iface.domain.repository.oms.IfOnetimePaymentRepository
import com.abc.us.accounting.rentals.onetime.service.v2.OnetimeProcessService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.OffsetDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicOnetimePaymentTests (
    private val onetimeProcessService: OnetimeProcessService,
    private val onetimePaymentRepository: IfOnetimePaymentRepository,
    private val persistenceService: DocumentPersistenceService
): FunSpec({
    val texasTimeZone = TimeZoneCode.TEXAS
    val dallasOffset = texasTimeZone.getZoneOffset()

    val companyCode = CompanyCode.T200
    val startTime = OffsetDateTime.of(2025, 2, 1, 0, 0, 0, 0, dallasOffset)
    val endTime = startTime.plusMonths(2)

    val isFullTest = true
    val eachMaxResult = if (isFullTest) 5000 else 1

    test ("[일시불] 수납 확인") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)
        val results = onetimeProcessService.processOnetimeBatchWithTemplateCodes(context, companyCode,listOf(DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED), startTime, endTime)
        logger.info("results:${results.size}")

        val onetimePayments = onetimePaymentRepository.findOnetimePayments(startTime, endTime, false)
        val expected = onetimePayments.map { it.orderId }
        val actual = results.map { documentResult -> documentResult.docItems[0].attributes.filter { it.type == DocumentAttributeType.ORDER_ID }.map { it.value } }.flatten()
        val diff = expected - actual.toSet()

        persistenceService.cleanup(context, results.map { it.docId })
    }

    test ("[일시불] 입금 확인") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)
        val results = onetimeProcessService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT), startTime, endTime)

        logger.info("results:${results.size}")

        val onetimePayments = onetimePaymentRepository.findOnetimePayments(startTime, endTime, false)
        val expected = onetimePayments.map { it.orderId }
        val actual = results.map { documentResult -> documentResult.docItems[0].attributes.filter { it.type == DocumentAttributeType.ORDER_ID }.map { it.value } }.flatten()
        val diff = expected - actual.toSet()

        persistenceService.cleanup(context, results.map { it.docId })
    }

    test ("[일시불] 취소(승인취소)") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)
        val results = onetimeProcessService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(DocumentTemplateCode.ONETIME_PAYMENT_VOID), startTime, endTime)


        val onetimePayments = onetimePaymentRepository.findOnetimePayments(startTime, endTime, true)
        val expected = onetimePayments.mapNotNull { it.refunds }.flatten().filter { refund -> refund.kind == RefundKind.VOID }.map { refund -> refund.orderItemId }

        val actual = results.map { documentResult -> documentResult.docItems[0].attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value } }.flatten()
        val diff = expected - actual.toSet()

        logger.info{"actual:$actual"}
        logger.info{"expected:$expected"}
        logger.info{"diff:$diff"}

        actual.size shouldBe expected.size

        persistenceService.cleanup(context, results.map { it.docId })
    }

    test ("[일시불] 환불(매입취소)") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)
        val results = onetimeProcessService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(DocumentTemplateCode.ONETIME_PAYMENT_REFUND), startTime, endTime)
        logger.info("results:${results.size}")

        val onetimePayments = onetimePaymentRepository.findOnetimePayments(startTime, endTime, true)
        val expected = onetimePayments.mapNotNull { it.refunds }.flatten().filter { refund -> refund.kind == RefundKind.REFUND }.map { refund -> refund.orderItemId }

        val actual = results.map { documentResult -> documentResult.docItems[0].attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value } }.flatten()
        val diff = expected - actual.toSet()

        logger.info{"actual:$actual"}
        logger.info{"expected:$expected"}
        logger.info{"diff:$diff"}

        actual.size shouldBe expected.size

        persistenceService.cleanup(context, results.map { it.docId })
    }
}) {
    companion object {
        val logger = KotlinLogging.logger { }
    }
}