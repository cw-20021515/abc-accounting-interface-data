package com.abc.us.accounting.collects.works.payout

import com.abc.us.accounting.collects.domain.entity.audit.AuditEntityLog
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service


@Service
class CollectsAccountsPayableListenerWork(
    private val eventPublisher : ApplicationEventPublisher,
) {

    fun trigger(trailer: AsyncEventTrailer) {

        val vendorIds = mutableSetOf<String>()

        trailer.freights().forEach{ key,value ->
            val entityLog = value as AuditEntityLog
            vendorIds.add(entityLog.eventTableId!!)
        }

        val trailer = AsyncEventTrailer.Builder()
            .listener("establish/accounts_payable")
            //.addFreight("vendorIds", vendorIds)
            .addFreight("logs",trailer.freights())
            .build(this)
        eventPublisher.publishEvent(trailer)
    }
}