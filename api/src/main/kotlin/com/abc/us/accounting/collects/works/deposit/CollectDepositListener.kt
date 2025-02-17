package com.abc.us.accounting.collects.works.deposit

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class CollectDepositListener(private val worker: CollectsDepositWork) {
    @AsyncEventListener(listener = "collects/deposit")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.collect(trailer)
    }
}