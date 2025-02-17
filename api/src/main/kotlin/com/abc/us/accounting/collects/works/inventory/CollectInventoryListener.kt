package com.abc.us.accounting.collects.works.inventory

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectInventoryListener(private val worker: CollectsInventoryWork) {
    @AsyncEventListener(listener = "collects/inventory-valuation")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collect(trailer)
    }
}