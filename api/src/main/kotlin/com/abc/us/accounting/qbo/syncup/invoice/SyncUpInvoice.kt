package com.abc.us.accounting.qbo.syncup.invoice

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import org.springframework.stereotype.Service

@Service
class SyncUpInvoice {

    //TODO : invoices 로직 개발 예정
    fun submit(trailer: AsyncEventTrailer){
        val deposits = trailer.freights()["invoices"] as MutableList<*>
        deposits.forEach { any ->
//            val collectsVendor = any as OriginVendor
//            establish(collectsVendor)
        }
        // TODO : trigger closuer 추가 작업 필요
        //closure.applyComplete(event as ProcureEntityAuditLog)
    }
}