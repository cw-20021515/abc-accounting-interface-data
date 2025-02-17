package com.abc.us.accounting.collects.works.deposit

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.converter.JsonConverter
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class CollectsDepositWork(
    private val eventPublisher : ApplicationEventPublisher,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun collect(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }

        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}