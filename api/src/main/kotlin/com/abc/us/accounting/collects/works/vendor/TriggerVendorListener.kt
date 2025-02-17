package com.abc.us.accounting.collects.works.vendor

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class TriggerVendorListener(private val worker: CollectsVendorWork) {
    @AsyncEventListener(listener = "trigger/vendor_master")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.trigger(trailer)
    }
}