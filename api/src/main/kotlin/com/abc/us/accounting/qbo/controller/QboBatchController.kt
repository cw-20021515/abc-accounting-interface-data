package com.abc.us.accounting.qbo.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.interact.QBOCertifier
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/accounting/v1/qbo/batch")
class QboBatchController(
    private var certifier : QBOCertifier,
    private val eventPublisher : ApplicationEventPublisher
){
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun publishEvent(startDateTime: LocalDateTime,
                     endDateTime: LocalDateTime,
                     timezone: String,
                     reversing: Boolean = false, listenPath : String) {
        val trailer = AsyncEventTrailer.Builder()
            .listener(listenPath)
            .addQuery("startDateTime",startDateTime)
            .addQuery("endDateTime", endDateTime)
            .addQuery("timezone", TimeZoneCode.fromCode(timezone))
            .reversing(reversing)
            .build(this)
        logger.info { "QBO-BATCH-EVENT(${listenPath}) FROM(${startDateTime})-TO(${endDateTime}) : REVERSING(${reversing})" }
        eventPublisher.publishEvent(trailer)
    }
//    @PostMapping("/account")
//    @Throws(Throwable::class)
//    fun submitAccount(@RequestParam startDateTime: LocalDateTime,
//                      @RequestParam endDateTime: LocalDateTime,
//                      @RequestParam("timeZoneCode", required = false, defaultValue = "America/Chicago") timeZoneCode: TimeZoneCode,
//                      @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
//        publishEvent(startDateTime,endDateTime,timeZoneCode,reversing,"qbo/account")
//        return ResponseEntity.ok("SUCCESS")
//    }

//    @PostMapping("/customers")
//    @Throws(Throwable::class)
//    fun submitCustomers(@RequestParam startDateTime: LocalDateTime,
//                        @RequestParam endDateTime: LocalDateTime,
//                        @RequestParam("timeZoneCode", required = false, defaultValue = "America/Chicago") timeZoneCode: TimeZoneCode,
//                        @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
//        publishEvent(startDateTime,endDateTime,timeZoneCode,reversing,"qbo/customers")
//        return ResponseEntity.ok("SUCCESS")
//    }

    @PostMapping("/materials")
    @Throws(Throwable::class)
    fun submitMaterial(@RequestParam startDateTime: LocalDateTime,
                       @RequestParam endDateTime: LocalDateTime,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/materials")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/journal-entries")
    @Throws(Throwable::class)
    fun submitJournalEntries(@RequestParam startDateTime: LocalDateTime,
                             @RequestParam endDateTime: LocalDateTime,
                             @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                             @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/journal-entries")
        return ResponseEntity.ok("SUCCESS")
    }

    @RequestMapping("/refresh-credential")
    fun refreshCredential(): ResponseEntity<String> {
        return ResponseEntity.ok(certifier.configure())
    }
}