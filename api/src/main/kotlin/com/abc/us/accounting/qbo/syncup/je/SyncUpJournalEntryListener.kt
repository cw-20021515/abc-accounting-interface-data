package com.abc.us.accounting.qbo.syncup.je

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpJournalEntryListener(private val establisher: SyncUpJournalEntry) {
    @AsyncEventListener(listener = "qbo/syncup/journal-entry")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        establisher.submit(trailer)
    }
}