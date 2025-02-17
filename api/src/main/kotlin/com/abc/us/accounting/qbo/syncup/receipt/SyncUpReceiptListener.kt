package com.abc.us.accounting.qbo.syncup.receipt

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpReceiptListener(private val establisher: SyncUpReceipt) {
    @AsyncEventListener(listener = "qbo/syncup/receipt")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}