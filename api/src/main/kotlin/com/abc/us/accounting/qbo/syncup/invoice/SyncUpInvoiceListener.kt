package com.abc.us.accounting.qbo.syncup.invoice

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpInvoiceListener(private val establisher: SyncUpInvoice) {
    @AsyncEventListener(listener = "qbo/syncup/invoice")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}