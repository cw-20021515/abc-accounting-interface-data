package com.abc.us.accounting.qbo.syncup.item

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

//
@Service
class SyncUpItemListener(private val establisher: SyncUpItem) {
    @AsyncEventListener(listener = "qbo/syncup/item")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}
