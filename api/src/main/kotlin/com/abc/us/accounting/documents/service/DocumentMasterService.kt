package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.DocumentItemAttributeMaster
import com.abc.us.accounting.documents.domain.repository.DocumentItemAttributeMasterRepository
import com.abc.us.accounting.documents.domain.type.AccountType
import com.abc.us.accounting.documents.domain.type.CompanyCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


interface DocumentMasterServiceable {
    fun getAllByAccountTypeIn(accountTypes: List<AccountType> = AccountType.entries): List<DocumentItemAttributeMaster>
    fun getAllByAccountCodeIn(companyCode: CompanyCode, accountCodes: List<String>): List<DocumentItemAttributeMaster>
}

@Service
class DocumentMasterService (
    private val documentItemAttributeMasterRepository: DocumentItemAttributeMasterRepository,
    private val accountService: AccountServiceable
    ): DocumentMasterServiceable{

    private val cachedDocumentItemAttributeMastersMap :MutableMap<AccountType, List<DocumentItemAttributeMaster> > = mutableMapOf()
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    init {
        initialize()
    }

    private final fun initialize() {
        logger.info("initialize loading DocumentItemAttributeMaster data for cache")
        var datasize = 0
        if (cachedDocumentItemAttributeMastersMap.isEmpty()) {
            val loadAllData = documentItemAttributeMasterRepository.findAllByAccountTypeIn()
            datasize = loadAllData.size
            cachedDocumentItemAttributeMastersMap.clear()
            cachedDocumentItemAttributeMastersMap.putAll(loadAllData.groupBy { it.accountType })
        }
        logger.info("initialize DocumentItemAttributeMaster data:${datasize} done")
    }

    override fun getAllByAccountTypeIn(accountTypes: List<AccountType>): List<DocumentItemAttributeMaster> {
        if (cachedDocumentItemAttributeMastersMap.isNotEmpty()) {
            val filtered = cachedDocumentItemAttributeMastersMap.filterKeys { accountTypes.contains(it) }.values.flatten().toList()
            if (filtered.isNotEmpty()) {
                return filtered
            }
        }
        return documentItemAttributeMasterRepository.findAllByAccountTypeIn(accountTypes, true)
    }

    override fun getAllByAccountCodeIn(companyCode: CompanyCode, accountCodes: List<String>): List<DocumentItemAttributeMaster> {
        val accounts = accountService.getValidAccounts(companyCode, accountCodes)
        val accountTypes = accounts.map { account -> account.accountType }.distinct()

        return getAllByAccountTypeIn(accountTypes)
    }

}