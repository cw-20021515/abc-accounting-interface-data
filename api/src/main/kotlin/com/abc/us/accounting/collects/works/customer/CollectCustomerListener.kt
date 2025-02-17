package com.abc.us.accounting.collects.works.customer

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectCustomerListener(private val worker: CollectsCustomerWork) {
    @AsyncEventListener(listener = "collects/customer")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collects(trailer)
    }
}