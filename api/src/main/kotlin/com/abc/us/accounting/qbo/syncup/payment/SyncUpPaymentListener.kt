package com.abc.us.accounting.qbo.syncup.payment

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpPaymentListener(private val establisher: SyncUpPayment) {
    @AsyncEventListener(listener = "qbo/syncup/payment")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}