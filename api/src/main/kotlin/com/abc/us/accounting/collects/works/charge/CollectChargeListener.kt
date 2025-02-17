package com.abc.us.accounting.collects.works.charge

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectChargeListener(private val worker: CollectsChargeWork) {
    @AsyncEventListener(listener = "collects/charge")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collects(trailer)
    }
}