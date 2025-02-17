package com.abc.us.accounting.collects.works.doc

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class TriggerDocumentListener(private val worker: CollectsDocumentWork) {
    @AsyncEventListener(listener = "trigger/accounts_payable")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.trigger(trailer)
    }
}