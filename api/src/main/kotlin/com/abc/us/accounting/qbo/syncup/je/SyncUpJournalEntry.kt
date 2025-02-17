package com.abc.us.accounting.qbo.syncup.je

import com.abc.us.accounting.collects.domain.repository.CollectCustomerRepository
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.qbo.service.DocumentCollectService
import com.abc.us.accounting.qbo.service.QboAccountService
import com.abc.us.accounting.qbo.service.QboJournalEntryService
import com.abc.us.accounting.qbo.service.QboCustomerService
import com.abc.us.accounting.iface.domain.entity.oms.IfCustomer
import com.abc.us.accounting.iface.domain.repository.oms.IfCustomerRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SyncUpJournalEntry(
    private val certifier : QBOCertifier,
    private val customerService: QboCustomerService,
    private val accountsService: QboAccountService,
    private val ifCustomerRepository: IfCustomerRepository,
    private val collectCustomerRepository: CollectCustomerRepository,
    private val documentCollectService : DocumentCollectService,
    private val journalEntryService : QboJournalEntryService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun chunked(submitTargets : MutableMap<String,Document>, block : (MutableMap<String,Document>)->Unit ) {
        val chunkMap = submitTargets.entries.chunked(QBOService.BATCH_SIZE).map { chunk ->
            chunk.associate {
                it.key to it.value
            }.toMutableMap()
        }
        chunkMap.forEach { chunk -> block(chunk) }
    }
    fun buildCustomerName(customer : IfCustomer) : String {

        return "ABC.R." + customer.customerId
    }

    fun raiseCustomers(docMap : MutableMap<String, Document>) {
        val customerIdMap = mutableMapOf<String,MutableSet<String>>()
        //val customerIdSet = mutableSetOf<String>()
        docMap.forEach{ docId,doc ->
            doc.items.forEach {item ->
                item.customerId?.let {

                    var idSet = customerIdMap[item.companyCode.code]
                    if(idSet == null) {
                        idSet = mutableSetOf()
                        customerIdMap[item.companyCode.code] = idSet
                    }
                    idSet.add(it)
                }
            }
        }

        customerIdMap.forEach { companyCode,customerIdSet ->
//            val displayCustomerIdSet = mutableSetOf<String>()
//            ifCustomerRepository.findActiveByCustomerIds(customerIdSet.toList()).forEach { customer ->
//
//                if(customer.accountType == IfCustomerType.CORPORATE)
//                    displayCustomerIdSet.add(customer.customerId)
//                else
//                    displayCustomerIdSet.add(buildCustomerName(customer))
//            }

            val bulkSaves = customerService.raise(companyCode,customerIdSet)
            customerService.bulkInsert(companyCode,bulkSaves)
        }
    }

//    fun raiseAccounts(credential : QboCredential, docMap : MutableMap<String, Document>) {
//        val accountCodeSet = mutableSetOf<String>()
//        docMap.forEach{ docId,doc ->
//            doc.items.forEach {item ->
//                accountCodeSet.add(item.accountCode)
//            }
//        }
//        val bulkSaves = accountsService.raise(credential.companyCode,accountCodeSet)
//        accountsService.bulkInsert(credential.companyCode,bulkSaves)
//    }

    fun submitJournalEntry(docMap : MutableMap<String, Document>) {
        val sepByCompany : MutableMap<String,MutableMap<String,Document>> = mutableMapOf()
        docMap.forEach { docId,doc ->
            var docs = sepByCompany[doc.companyCode.code]
            if(docs == null) {
                docs = mutableMapOf()
                sepByCompany[doc.companyCode.code] = docs
            }
            docs[doc.id] = doc
        }

        sepByCompany.forEach { companyCode , docs ->
            val splitData = journalEntryService.splitByAction(docMap)
            chunked(splitData.adds) { chunk ->
                val submitted = journalEntryService.submit(companyCode,chunk)
                journalEntryService.bulkInsert(companyCode, submitted)
            }
        }
    }

    fun submit(trailer: AsyncEventTrailer){
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        val reversing = trailer.reversing()
        logger.info { "SUBMIT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }

        val fromDateTime = timezone.convertTime(from,TimeZoneCode.UTC)
        val toDateTime = timezone.convertTime(to,TimeZoneCode.UTC)
        documentCollectService.collect(fromDateTime,toDateTime) { documents->
            //raiseAccounts(credential,documents)
            raiseCustomers(documents)
            submitJournalEntry(documents)
        }

//        certifier.visit { credential->
//            documentCollectService.collect(credential.companyCode,fromDateTime,toDateTime) { documents->
//                raiseAccounts(credential,documents)
//                raiseCustomers(credential,documents)
//                submitJournalEntry(credential,documents)
//            }
//            true
//        }
        logger.info { "SUBMIT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}