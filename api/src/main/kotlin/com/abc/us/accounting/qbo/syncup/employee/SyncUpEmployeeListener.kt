package com.abc.us.accounting.qbo.syncup.employee

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service


@Service
class SyncUpEmployeeListener(private val establisher: SyncUpEmployee) {
    @AsyncEventListener(listener = "qbo/syncup/employee")
    fun onSyncUpEvent(trailer: AsyncEventTrailer)  {
        establisher.syncup(trailer)
    }
}