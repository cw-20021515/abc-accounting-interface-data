package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateKey
import com.abc.us.accounting.documents.domain.repository.DocumentTemplateItemRepository
import com.abc.us.accounting.documents.domain.repository.DocumentTemplateRepository
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface DocumentTemplateServiceable {
    fun findDocTemplate(companyCode: CompanyCode, docTemplateCode: DocumentTemplateCode): DocumentTemplate
    fun findDocTemplate(docTemplateKey: DocumentTemplateKey): DocumentTemplate

    fun findDocTemplates (companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode> = DocumentTemplateCode.entries): List<DocumentTemplate>
    fun findDocTemplates (docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplate>

    fun findDocTemplateItems(companyCode: CompanyCode, docTemplateCode: DocumentTemplateCode, accountSide: AccountSide? = null): List<DocumentTemplateItem>
    fun findDocTemplateItems(docTemplateKey: DocumentTemplateKey, accountSide: AccountSide? = null): List<DocumentTemplateItem>

    fun findDocTemplateItems(companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode>): List<DocumentTemplateItem>
    fun findDocTemplateItems(docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplateItem>


    fun findDocTemplateItemPairsForClearing(companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode> = DocumentTemplateCode.entries): List<Pair<DocumentTemplateItem, DocumentTemplateItem>>
}


@Service
class DocumentTemplateService(
    private val documentTemplateRepository: DocumentTemplateRepository,
    private val documentTemplateItemRepository: DocumentTemplateItemRepository,
) : DocumentTemplateServiceable {

    private val cachedDocumentTemplateMap :MutableMap<DocumentTemplateKey, DocumentTemplate > = mutableMapOf()
    private val cachedDocumentTemplateItemMap :MutableMap<DocumentTemplateKey, List<DocumentTemplateItem> > = mutableMapOf()
    private val cachedClearingPairs: MutableList<Pair<DocumentTemplateItem, DocumentTemplateItem>> = mutableListOf()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    init {
        initialize()
    }

    private final fun initialize() {
        logger.info("initialize loading cachedDocumentTemplateMap data for cache")
        var datasize = 0
        if (cachedDocumentTemplateMap.isEmpty()) {
            cachedDocumentTemplateMap.clear()
            val loadTemplateDate = documentTemplateRepository.findAll()
            datasize = loadTemplateDate.size
            loadTemplateDate.associateBy { it.docTemplateKey }.toMutableMap().also { cachedDocumentTemplateMap.putAll( it ) }

            logger.info("initialize cachedDocumentTemplateMap data:${datasize} done")
        }
        if (cachedDocumentTemplateItemMap.isEmpty()) {
            cachedDocumentTemplateItemMap.clear()
            val loadAllData = documentTemplateItemRepository.findAll()
            datasize = loadAllData.size
            cachedDocumentTemplateItemMap.putAll(loadAllData.groupBy { it.docTemplateKey })

            logger.info("initialize cachedDocumentItemAttributeMastersMap data:${datasize} done")
        }

        if (cachedClearingPairs.isEmpty()) {
            cachedClearingPairs.clear()

            // clearing 대상 template item 을 찾는다.
            val clearing = cachedDocumentTemplateItemMap.values.flatten()
                .filter { it.refDocTemplateCode != null }

            // cleared 대상 template item 을 찾는다.
            val cleared = cachedDocumentTemplateItemMap.values.flatten()
                .filter { item -> clearing.any{
                    it.accountCode == item.accountCode
                        && it.docTemplateKey.companyCode == item.docTemplateKey.companyCode
                        && it.refDocTemplateCode == item.docTemplateKey.docTemplateCode
                        && it.accountSide != item.accountSide
                } }

            val clearingPairs = clearing.mapNotNull { clearingItem ->
                val clearedItem = cleared.firstOrNull {
                    clearingItem.accountCode == it.accountCode
                            && clearingItem.docTemplateKey.companyCode == it.docTemplateKey.companyCode
                            && clearingItem.refDocTemplateCode == it.docTemplateKey.docTemplateCode
                            && clearingItem.accountSide != it.accountSide
                }
                if (clearedItem != null) {
                    Pair(clearingItem, clearedItem)
                } else {
                    null
                }
            }

            cachedClearingPairs.addAll(clearingPairs)
            datasize = cachedClearingPairs.size
            logger.info("initialize cachedClearingPairs data:${datasize} done")
        }
    }

    override fun findDocTemplate(companyCode: CompanyCode, docTemplateCode: DocumentTemplateCode): DocumentTemplate {
        val docTemplateKey = DocumentTemplateKey(companyCode, docTemplateCode)
        return findDocTemplate(docTemplateKey)
    }

    override fun findDocTemplate(docTemplateKey: DocumentTemplateKey): DocumentTemplate {
        if ( cachedDocumentTemplateMap.isNotEmpty() ) {
            val data = cachedDocumentTemplateMap[docTemplateKey] ?: throw IllegalArgumentException("DocumentTemplate not found by docTemplateKey:${docTemplateKey} with cached option")
            return data!!
        }
        return documentTemplateRepository.findById(docTemplateKey).orElseThrow { IllegalArgumentException("DocumentTemplate not found by docTemplateKey:${docTemplateKey}") }
    }

    override fun findDocTemplates(companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode>): List<DocumentTemplate> {
        val docTemplateKeys = docTemplateCodes.map { DocumentTemplateKey(companyCode, it) }
        return findDocTemplates(docTemplateKeys)
    }

    override fun findDocTemplates (docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplate> {
        if ( cachedDocumentTemplateMap.isNotEmpty() ) {
            return cachedDocumentTemplateMap.filterKeys { docTemplateKeys.contains(it) }.values.toList()
        }
        return documentTemplateRepository.findAllById(docTemplateKeys)
    }

    override fun findDocTemplateItems(companyCode: CompanyCode, docTemplateCode: DocumentTemplateCode, accountSide: AccountSide?): List<DocumentTemplateItem> {
        val docTemplateKey = DocumentTemplateKey(companyCode, docTemplateCode)
        return findDocTemplateItems(docTemplateKey, accountSide)
    }

    override fun findDocTemplateItems(docTemplateKey: DocumentTemplateKey, accountSide: AccountSide?): List<DocumentTemplateItem>{
        if ( cachedDocumentTemplateItemMap.isNotEmpty() ) {
            val filtered = cachedDocumentTemplateItemMap[docTemplateKey]?.filter { if (accountSide != null) it.accountSide == accountSide else true }
            if (!filtered.isNullOrEmpty()) {
                return filtered
            }
        }
        if ( accountSide != null ) {
            return documentTemplateItemRepository.findByDocTemplateKeyAndAccountSide(docTemplateKey, accountSide)
        }
        return documentTemplateItemRepository.findByDocTemplateKey(docTemplateKey)
    }

    override fun findDocTemplateItems(companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode>): List<DocumentTemplateItem> {
        val docTemplateKeys = docTemplateCodes.map { DocumentTemplateKey(companyCode, it) }
        return findDocTemplateItems(docTemplateKeys)
    }

    override fun findDocTemplateItems(docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplateItem> {
        if ( cachedDocumentTemplateItemMap.isNotEmpty() ) {
            return cachedDocumentTemplateItemMap.filterKeys { docTemplateKeys.contains(it) }.values.flatten()
        }
        return documentTemplateItemRepository.findAllByDocTemplateKeyIn(docTemplateKeys)
    }

    /**
     * companyCode는 N200, T200만 지원
     * 전표 항목 반제를 위한 후보 항목을 찾는다.
     */
    override fun findDocTemplateItemPairsForClearing(companyCode: CompanyCode, docTemplateCodes: List<DocumentTemplateCode>): List<Pair<DocumentTemplateItem, DocumentTemplateItem>>{
        require(companyCode.isSalesCompany() ) { "companyCode must be sales company, companyCode:${companyCode}"}

        val docTemplateKeys = docTemplateCodes.map { DocumentTemplateKey(companyCode, it) }

        if ( cachedClearingPairs.isNotEmpty() ) {
            return cachedClearingPairs.filter { docTemplateKeys.contains(it.first.docTemplateKey) }
        }
        return documentTemplateItemRepository.findDocTemplateItemPairsForClearing(docTemplateKeys)
    }
}

