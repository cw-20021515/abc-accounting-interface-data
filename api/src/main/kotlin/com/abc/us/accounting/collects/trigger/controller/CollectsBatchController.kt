package com.abc.us.accounting.collects.trigger.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/accounting/v1/collects/batch")
class CollectsBatchController (
    private val eventPublisher : ApplicationEventPublisher
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun publishEvent(fromDateTime: LocalDateTime,
                     toDateTime: LocalDateTime,
                     timezone: String,
                     test: Boolean = false, listenPath : String) {
        val trailer = AsyncEventTrailer.Builder()
            .listener(listenPath)
            .addQuery("fromDateTime",fromDateTime)
            .addQuery("toDateTime", toDateTime)
            .addQuery("timezone", TimeZoneCode.fromCode(timezone))
            .test(test)
            .build(this)
        logger.info { "COLLECT-EVENT(${listenPath}) FROM(${fromDateTime})-TO(${toDateTime}) : DO-TEST(${test})" }
        eventPublisher.publishEvent(trailer)
    }

//    fun publishEvent(startTime: Long,endTime: Long,test: Boolean = false, listenPath : String) {
//
//        val from = EpochToISO8856.convert(startTime)
//        val to = EpochToISO8856.convert(endTime)
//        val trailer = AsyncEventTrailer.Builder()
//            .listener(listenPath)
//            .addQuery("fromCreateTime",from)
//            .addQuery("toCreateTime", to)
//            .test(test)
//            .build(this)
//
//        val fromTime = ISO8856ToLocalDate.convert(from)
//        val toTime = ISO8856ToLocalDate.convert(to)
//        logger.info { "COLLECT-EVENT(${listenPath}) FROM(${fromTime})-TO(${toTime}) : DO-TEST(${test})" }
//        eventPublisher.publishEvent(trailer)
//    }
    @PostMapping("/charge")
    @Throws(Throwable::class)
    fun collectCharges(
    @RequestParam("startDateTime") startDateTime: LocalDateTime,
    @RequestParam("endDateTime") endDateTime: LocalDateTime,
    @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
    @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/charge")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/customer")
    @Throws(Throwable::class)
    fun collectCustomer(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/customer")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/material")
    @Throws(Throwable::class)
    fun collectMaterials(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/material")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/order")
    @Throws(Throwable::class)
    fun collectOrders(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {

        publishEvent(startDateTime,endDateTime,timezone,test,"collects/order")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/order-item")
    @Throws(Throwable::class)
    fun collectOrderItems(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/order-item")
        return ResponseEntity.ok("SUCCESS")
    }


    @PostMapping("/service-flow")
    @Throws(Throwable::class)
    fun collectServiceFlow(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/service-flow")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/vendor")
    @Throws(Throwable::class)
    fun collectsVendor(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/vendor")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/inventory-valuation")
    @Throws(Throwable::class)
    fun collectsInventoryValuation(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam(required = false) test: Boolean = false,
    ): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,test,"collects/inventory-valuation")
        return ResponseEntity.ok("SUCCESS")
    }
}