//package com.abc.us.accounting.rentals.flease.service
//
//import com.abc.us.accounting.commons.domain.type.TimeZoneCode
//import com.abc.us.accounting.documents.domain.type.CompanyCode
//import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
//import com.abc.us.accounting.rentals.flease.domain.type.RentalFinancialEventType
//import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
//import com.abc.us.accounting.supports.converter.EpochToISO8856
//import com.abc.us.accounting.supports.converter.EpochToOffsetDateTime
//import mu.KotlinLogging
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import java.time.OffsetDateTime
//import kotlin.test.Test
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class BasicRentalFinancialLeaseBatchTests @Autowired constructor(
//    val rentalFinancialBatchService: RentalFinancialBatchService,
//    val rentalFinancialBatchTemplateService: RentalFinancialBatchTemplateService
//) {
//
//    companion object{
//        val logger = KotlinLogging.logger {  }
//        val texasOffset = TimeZoneCode.TEXAS.getZoneOffset()
//        val startTime = OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, texasOffset)
////        val endTime = startTime.plusMonths(1).minusNanos(1)
//        val endTime = OffsetDateTime.now()
//        val companyCode: CompanyCode = CompanyCode.T200
//    }
//
//    @Test
//    fun `flease product shipped test`() {
//        val startTimeEpoch = startTime.toInstant().toEpochMilli()
//        val endTimeEpoch = endTime.toInstant().toEpochMilli()
//
//        val orderItemId = null
//        logger.info { "bookingConfirmedBatch[렌탈금융리스:제품 출고] FROM(${EpochToISO8856.convert(startTimeEpoch)})-TO(${EpochToISO8856.convert(endTimeEpoch)}) : COMPANY(${companyCode})" }
//
//        val from = EpochToOffsetDateTime.convert(startTimeEpoch)
//        val to = EpochToOffsetDateTime.convert(endTimeEpoch)
//        // 테스트[collect_order_item] : material_id = 'WP_113627' and order_item_id  like '01054317%' and order_id in ('0105431700002', '0105431700035', '0105431700060')
//        rentalFinancialBatchService.depreciationBatch(from, to,
//            RentalFinancialEventType.FLEASE_PRODUCT_SHIPPED,
//            ServiceFlowStatus.SERVICE_SCHEDULED,
//            orderItemId
//        )
//
//        rentalFinancialBatchTemplateService.postingCollection(from, to, companyCode,
//            RentalFinancialEventType.FLEASE_INSTALL_COMPLETED,
//            DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED)
//
//        logger.info("test complete")
//    }
//
//}