package com.abc.us.accounting.collects.works.materials

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectsMaterialListener(private val worker: CollectsMaterialWork) {
    @AsyncEventListener(listener = "collects/material")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collect(trailer)
    }
}