package com.abc.us.accounting.collects.works.order

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectOrderListener(private val worker: CollectsOrderWork) {
    @AsyncEventListener(listener = "collects/order")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collects(trailer)
    }
}