package com.abc.us.accounting.qbo.syncup.vendor

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service


@Service
class SyncUpVendorListener(private val establisher: SyncUpVendor) {
    @AsyncEventListener(listener = "qbo/syncup/vendor")
    fun onSyncUpEvent(trailer: AsyncEventTrailer)  {
        establisher.syncup(trailer)
    }
}