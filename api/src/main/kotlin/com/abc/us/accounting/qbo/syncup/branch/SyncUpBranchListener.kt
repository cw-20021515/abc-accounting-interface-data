package com.abc.us.accounting.qbo.syncup.branch

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.pubsub.annotations.AsyncEventListener
import org.springframework.stereotype.Service

@Service
class SyncUpBranchListener(private val submitter: SyncUpBranch) {
    @AsyncEventListener(listener = "qbo/syncup/branch")
    fun onEstablishEvent(trailer: AsyncEventTrailer)  {
        submitter.syncup(trailer)
    }
}
