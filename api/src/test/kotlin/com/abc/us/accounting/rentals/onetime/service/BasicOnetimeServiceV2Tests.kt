package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.DocumentPersistenceService
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
class BasicOnetimeServiceV2Tests (
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

    test ("[일시불] 전체 전표처리") {
        val context = DocumentServiceContext.withSaveDebug(eachMaxResult)
        val rules = onetimeProcessService.getSupportedProcessRules()
        logger.info("rules: $rules")
        rules.size shouldBe 4

//        val results =   onetimeProcessService.processOnetimeBatch(context, companyCode, startTime, endTime)
//        logger.info("results:${results.size}")
//        persistenceService.cleanup(context, results.map { it.docId })
    }


}) {
    companion object {
        val logger = KotlinLogging.logger { }
    }
}