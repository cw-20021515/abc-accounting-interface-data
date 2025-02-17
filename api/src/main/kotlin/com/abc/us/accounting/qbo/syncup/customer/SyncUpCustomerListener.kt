package com.abc.us.accounting.logistics.processing.establish.account

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.syncup.customer.SyncUpCustomer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpCustomerListener(private val establisher: SyncUpCustomer) {
    @AsyncEventListener(listener = "qbo/syncup/customer")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}
