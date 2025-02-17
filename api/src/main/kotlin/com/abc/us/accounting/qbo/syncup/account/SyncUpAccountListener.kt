package com.abc.us.accounting.logistics.processing.establish.account

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.syncup.account.SyncUpAccount
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpAccountListener(private val establisher: SyncUpAccount) {
    @AsyncEventListener(listener = "qbo/syncup/account")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.syncup(trailer)
    }
}
