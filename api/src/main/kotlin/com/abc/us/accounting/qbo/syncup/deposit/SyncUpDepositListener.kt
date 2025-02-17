package com.abc.us.accounting.qbo.syncup.deposit

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpDepositListener(private val establisher: SyncUpDeposit) {
    @AsyncEventListener(listener = "qbo/syncup/deposit")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}