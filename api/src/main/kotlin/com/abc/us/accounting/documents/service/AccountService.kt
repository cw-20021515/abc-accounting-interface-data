package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.Account
import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.entity.ConsolidationAccount
import com.abc.us.accounting.documents.domain.repository.AccountRepository
import com.abc.us.accounting.documents.domain.repository.ConsolidationAccountRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


interface AccountServiceable {
    fun getAccount(accountKey:AccountKey):Account
    fun getAccount(companyCode: CompanyCode, accountCode: String): Account

    fun getValidAccounts(accountKeys: List<AccountKey>): List<Account>
    fun getValidAccounts(companyCode: CompanyCode, accountCodes: List<String>): List<Account>
    fun getValidAccounts(companyCodes: List<CompanyCode>, accountCodes: List<String>): List<Account>

    fun getAllAccounts(): List<Account>
}

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val consolidationAccountRepository: ConsolidationAccountRepository,
    private val cachedAccounts:MutableList<Account> = mutableListOf(),
    private val cachedConsolidationAccounts:MutableList<ConsolidationAccount> = mutableListOf(),
) : AccountServiceable {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        initialize()
    }

    private final fun initialize() {
        logger.info("initialize loading account, accountGroup data for cache")
        if (cachedAccounts.isEmpty()) {
            cachedAccounts.addAll(accountRepository.findAll())
        }
        if (cachedConsolidationAccounts.isEmpty()) {
            cachedConsolidationAccounts.addAll(consolidationAccountRepository.findAll())
        }
        logger.info("initialize account:${cachedAccounts.size}, cachedConsolidationAccounts:${cachedConsolidationAccounts.size} data done")
    }

    override fun getAccount(accountKey: AccountKey): Account {
        if (cachedAccounts.isNotEmpty()) {
            cachedAccounts.find { it.accountKey  == accountKey}?.let {
                return it
            }
        }
        val account = accountRepository.findById(accountKey)
        if (account.isEmpty) {
            throw IllegalArgumentException("account not found, accountKey:$accountKey")
        }
        return account.get()
    }

    override fun getAccount(companyCode: CompanyCode, accountCode: String):Account {
        return getAccount(AccountKey.of(companyCode, accountCode))
    }

    override fun getValidAccounts(accountKeys: List<AccountKey>): List<Account> {
        if (cachedAccounts.isNotEmpty()) {
            val filtered = cachedAccounts.filter { accountKeys.contains(it.accountKey) }.toList()
            if (filtered.isNotEmpty()) {
                return filtered
            }
        }
        val accounts = accountRepository.findByAccountKeyIn(accountKeys)
        return accounts
    }

    override fun getValidAccounts(companyCode: CompanyCode, accountCodes: List<String>):List<Account> {
        if (cachedAccounts.isNotEmpty()) {
            val filtered = cachedAccounts.filter { it.accountKey.companyCode == companyCode && accountCodes.contains(it.accountKey.accountCode) }.toList()
            if (filtered.isNotEmpty()) {
                return filtered
            }
        }
        val accountKeys = accountCodes.map { AccountKey.of(companyCode, it)}
        val accounts = accountRepository.findByAccountKeyIn(accountKeys)
        return accounts
    }

    override fun getValidAccounts(companyCodes: List<CompanyCode>, accountCodes: List<String>): List<Account> {
        if (cachedAccounts.isNotEmpty()) {
            val filtered = cachedAccounts.filter { companyCodes.contains(it.accountKey.companyCode) && accountCodes.contains(it.accountKey.accountCode) }.toList()
            if (filtered.isNotEmpty()) {
                return filtered
            }
        }
        val accountKeys = accountCodes.map { accountCode ->
            companyCodes.map { companyCode ->
                AccountKey.of(companyCode, accountCode)
            }
        }.flatten()
        val accounts = accountRepository.findByAccountKeyIn(accountKeys)
        return accounts
    }

    override fun getAllAccounts():List<Account> {
        if ( cachedAccounts.isNotEmpty() ) {
            return cachedAccounts
        }
        val accounts = accountRepository.findAll()
        return accounts
    }
}