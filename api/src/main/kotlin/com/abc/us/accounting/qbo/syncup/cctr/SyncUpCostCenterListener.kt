package com.abc.us.accounting.qbo.syncup.cctr

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.syncup.customer.SyncUpCustomer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service


@Service
class SyncUpCostCenterListener(private val establisher: SyncUpCostCenter) {
    @AsyncEventListener(listener = "qbo/syncup/cost-center")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.syncup(trailer)
    }
}
