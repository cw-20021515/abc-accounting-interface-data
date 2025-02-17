package com.abc.us.accounting.collects.works.orderitem

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectOrderItemListener(private val worker: CollectsOrderItemWork) {
    @AsyncEventListener(listener = "collects/order-item")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collects(trailer)
    }
}