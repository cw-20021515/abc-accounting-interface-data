package com.abc.us.accounting.collects.works.serviceflow

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectServiceFlowListener(private val worker: CollectsServiceFlowWork) {
    @AsyncEventListener(listener = "collects/service-flow")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collect(trailer)
    }
}