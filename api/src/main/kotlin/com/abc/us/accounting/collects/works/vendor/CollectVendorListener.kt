package com.abc.us.accounting.collects.works.vendor

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectVendorListener(private val worker: CollectsVendorWork) {
    @AsyncEventListener(listener = "collects/vendor")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collect(trailer)
    }
}