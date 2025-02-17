package com.abc.us.accounting.qbo.controller

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.qbo.controller.QboBatchController.Companion
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/accounting/v1/qbo/syncup")
class QboSyncController(private val eventPublisher : ApplicationEventPublisher) {

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

    @PostMapping("/account")
    @Throws(Throwable::class)
    fun syncAccount(@RequestParam startDateTime: LocalDateTime,
                       @RequestParam endDateTime: LocalDateTime,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/account")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/branch")
    @Throws(Throwable::class)
    fun syncBranch(@RequestParam startDateTime: LocalDateTime,
                       @RequestParam endDateTime: LocalDateTime,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/branch")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/customer")
    @Throws(Throwable::class)
    fun syncCustomer(@RequestParam startDateTime: LocalDateTime,
                   @RequestParam endDateTime: LocalDateTime,
                   @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                   @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/customer")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/item")
    @Throws(Throwable::class)
    fun syncItem(@RequestParam startDateTime: LocalDateTime,
                     @RequestParam endDateTime: LocalDateTime,
                     @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                     @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/item")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/employee")
    @Throws(Throwable::class)
    fun syncEmployee(@RequestParam startDateTime: LocalDateTime,
                   @RequestParam endDateTime: LocalDateTime,
                   @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                   @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/employee")
        return ResponseEntity.ok("SUCCESS")
    }
    @PostMapping("/vendor")
    @Throws(Throwable::class)
    fun syncVendor(@RequestParam startDateTime: LocalDateTime,
                       @RequestParam endDateTime: LocalDateTime,
                       @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                       @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/vendor")
        return ResponseEntity.ok("SUCCESS")
    }

    @PostMapping("/cost-center")
    @Throws(Throwable::class)
    fun syncCostCenter(@RequestParam startDateTime: LocalDateTime,
                     @RequestParam endDateTime: LocalDateTime,
                     @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
                     @RequestParam(required = false) reversing: Boolean = false): ResponseEntity<String> {
        publishEvent(startDateTime,endDateTime,timezone,reversing,"qbo/syncup/cost-center")
        return ResponseEntity.ok("SUCCESS")
    }
}