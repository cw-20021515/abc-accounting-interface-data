package com.abc.us.accounting.qbo.syncup.payment

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import org.springframework.stereotype.Service

@Service
class SyncUpPayment {

    //TODO : payments 로직 개발 예정
    fun submit(trailer: AsyncEventTrailer){
        val payments = trailer.freights()["payments"] as MutableList<*>
        payments.forEach { any ->
//            val collectsVendor = any as OriginVendor
//            establish(collectsVendor)
        }
        // TODO : trigger closuer 추가 작업 필요
        //closure.applyComplete(event as ProcureEntityAuditLog)
    }
}