package com.abc.us.accounting.collects.works.payout

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class TriggerAccountsPayableListener(private val worker: CollectsAccountsPayableListenerWork) {
    @AsyncEventListener(listener = "trigger/accounts_payable")
    fun onCollectEvent(trailer: AsyncEventTrailer)  {
        worker.trigger(trailer)
    }
}