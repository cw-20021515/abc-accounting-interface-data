package com.abc.us.accounting.qbo.service

import com.abc.us.accounting.config.MathConfig
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.qbo.domain.entity.QboJournalEntry
import com.abc.us.accounting.qbo.domain.repository.QboJournalEntryRepository
import com.abc.us.accounting.qbo.helper.builder.JournalEntryBuilder
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class QboJournalEntryService(
    private val matchConfig : MathConfig,
    private val submittedJournalEntryRepository : QboJournalEntryRepository,
    private val qboService: QBOService,
    private val customerService: QboCustomerService,
    private val qboAccountService: QboAccountService
) {
    companion object {
        private val converter = JsonConverter()
        private val logger = KotlinLogging.logger {}
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun bulkInsert(companyCode: String,submitted : MutableList<QboJournalEntry>) {
        if(submitted.isEmpty())
            return

        logger.info { "BULK-INSERT-JOURNAL-ENTRY[${companyCode}.${submitted.size}])" }
        submittedJournalEntryRepository.saveAll(submitted)
    }
    data class SplitData(
        val adds : MutableMap<String,Document> = mutableMapOf(),
        val updates : MutableMap<String,Document> = mutableMapOf()
    ) {
        fun excludeDuplicate(submitts : MutableList<QboJournalEntry>?,
                            docMap : MutableMap<String,Document>) : MutableMap<String,Document>{
            val remainDocMap = docMap.toMutableMap()
            submitts?.let {
                it.forEach {submitted ->
                    val document = docMap[submitted.key.docId]
                    if(document != null &&
                        submitted.key.docId == document.id &&
                        submitted.updateTime == document.updateTime) {
                        remainDocMap.remove(document.id)
                        logger.info { "EXCLUDE-DUPLICATE-JOURNAL-ENTRY[${document.companyCode}.${document.id}.${document.updateTime}])" }
                    }
                }
            }
            return remainDocMap
        }

        fun selectAdd(submitts : MutableList<QboJournalEntry>?,
                      docMap : MutableMap<String,Document>) : MutableMap<String,Document>{

            val selectDoc = docMap.toMutableMap()
            submitts?.let{
                it.forEach { submitted ->
                    val document = docMap[submitted.key.docId]
                    if(document !=null) {
                        selectDoc.remove(submitted.key.docId)
                        logger.info { "SPLIT-ADD-JOURNAL-ENTRY[${document.companyCode}.${document.id}.${document.docHash}])" }
                    }
                }
            }
            return selectDoc
        }

        fun split(submitts : MutableList<QboJournalEntry>?,
                  docMap : MutableMap<String,Document>) {
            val excludes = excludeDuplicate(submitts, docMap)
            adds.putAll(selectAdd(submitts,excludes))
        }
    }

    fun splitByAction(docMap : MutableMap<String,Document>) : SplitData {
        val action = SplitData()
        val docIdSet = docMap.values.map { it.id }.toMutableSet()
        val submitts = submittedJournalEntryRepository.findByDocIdIn(docIdSet)
        action.split(submitts,docMap)
        return action
    }
    fun submit(companyCode : String,
               docMap : MutableMap<String,Document>)  : MutableList<QboJournalEntry>{

        val entries = mutableMapOf<String,JournalEntry>()
        JournalEntryBuilder(matchConfig,customerService,qboAccountService).build(companyCode,docMap) {doc,je,adjustment ->
            doc.roundingDifference = adjustment.difference
            if(adjustment.hasDifference) {
                logger.info { "ROUNDING-ADJUSTMENT[${companyCode}.${doc.id}]-DIFFERENCE[${adjustment.difference}]" }
            }
            entries[doc.id] = je
        }

        if(entries.isEmpty()) {
            logger.warn { "No journal entries were added" }
            return mutableListOf()
        }
        val results = qboService.batchAdd(companyCode, JournalEntry::class.java,entries.values.toMutableList())
        return JournalEntryBuilder.buildSubmit(companyCode,results,docMap)
    }
}