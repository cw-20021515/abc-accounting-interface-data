package com.abc.us.accounting.qbo.service

import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.repository.AccountRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.entity.Account as DocumentAccount
import com.abc.us.accounting.qbo.domain.entity.QboAccount
import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.qbo.domain.entity.key.QboAccountKey
import com.abc.us.accounting.qbo.domain.repository.QboAccountRepository
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.Account
import com.intuit.ipp.data.AccountClassificationEnum
import com.intuit.ipp.data.AccountTypeEnum

import mu.KotlinLogging
import okio.withLock
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.concurrent.locks.ReentrantLock

@Service
class QboAccountService(
    private val qboService : QBOService,
    private val accountRepository: AccountRepository,
    private val qboAccountRepository: QboAccountRepository
) : ApplicationListener<ContextRefreshedEvent> {

    companion object {
        private val converter = JsonConverter()
        private val logger = KotlinLogging.logger {}
    }

    var accountMap : MutableMap<String, MutableMap<String,QboAccount>> = mutableMapOf()
    private val lock = ReentrantLock()

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        refresh()
    }

    fun refresh() {

        accountMap.clear()

        val findAll = qboAccountRepository.findAll()
        findAll.forEach { account ->

            var accountsMap = accountMap[ account.companyCode ]
            if(accountsMap == null) {
                accountsMap = mutableMapOf()
                accountMap[account.companyCode] = accountsMap
            }
            account.submitResult?.let { result ->
                val submittedAccount = converter.toObj(result, Account::class.java)
                submittedAccount?.let { submitted ->
                    accountsMap[submitted.acctNum] = account
                }
            }
        }
    }

    fun buildAccount(account : DocumentAccount) : Account {

        return Account().apply {
            name = account.name
            acctNum = account.accountKey.accountCode
            accountType = account.qboAccountType.let { type -> AccountTypeEnum.fromValue(type!!.value) }
            accountSubType = account.qboAccountSubType.let { type-> type!!.value}
            classification = account.accountClass.let { cls -> AccountClassificationEnum.fromValue(cls.engName) }
            description = account.description
        }
    }
    fun makeSubmittedAccount(crednetial : QboCredential, account : Account) : QboAccount{
        val submitJson = converter.toJson(account)
        return QboAccount(
            key = QboAccountKey(qboId = account.id,
                accountCode = account.acctNum,
                accountName = account.name),
            displayName = account.name,
            realmId = crednetial.realmId,
            companyCode = crednetial.companyCode,
            syncToken = account.syncToken,
            submitResult = submitJson?.let { it }?:"",
            createTime = OffsetDateTime.now(),
            updateTime = OffsetDateTime.now()
        )
    }


    fun findAccount(companyCode : String,documentAccount : DocumentAccount) :  Account? {
        if ( documentAccount.qboAccountType == null || documentAccount.qboAccountSubType == null) {
            return null
        }

        val account = buildAccount(documentAccount)
        var result : Account? = null
        qboService.select(companyCode, mutableMapOf(Pair("name",account.name)),Account()) {result = it}
        return result
    }

    fun flush(companyCode : String,account : QboAccount) : QboAccount {
        var map = accountMap[companyCode]
        val acctNum = account.key.accountCode
        lock.withLock {
            if(map == null) {
                map = mutableMapOf()
                accountMap[companyCode] = map!!
            }
            map!![acctNum] = account
        }
        return account
    }

    fun findQboAccount(companyCode : String, accountCode : String) : QboAccount? {
        var qboAccount : QboAccount? = null
        val companyAccount = accountMap[companyCode]
        if(companyAccount != null)
            qboAccount = companyAccount[accountCode]

        if(qboAccount != null)
            return qboAccount

        if(qboAccount == null) {
            qboAccount = qboAccountRepository.findByAccountCode(companyCode,accountCode)
            if(qboAccount != null){
                return flush(companyCode,qboAccount)
            }
        }

        val documentAccount = accountRepository.findById(AccountKey(CompanyCode.of(companyCode),accountCode))
        documentAccount.orElse(null)?.let { docAccount ->
            val account = findAccount(companyCode, docAccount)
            if(account != null){
                qboAccount = makeSubmittedAccount(qboService.getCredential(companyCode)!!,account)
                qboAccount?.let { flush(companyCode, it)}
            }else {
                qboAccount = addAccount(companyCode, buildAccount(docAccount))
                qboAccount?.let { flush(companyCode, it)}
            }
        } ?: run {
            // documentAccount가 Optional.empty()일 때 실행할 로직
            logger.error("NotFound DocumentAccount-[${companyCode}]-[${accountCode}]")
        }
        return qboAccount
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun bulkInsert(companyCode: String,submitted : MutableList<QboAccount>) {
        if(submitted.isEmpty())
            return

        logger.info { "BULK-INSERT-ACCOUNT[${companyCode}.${submitted.size}])" }
        qboAccountRepository.saveAll(submitted)
    }

    fun addAccount(companyCode : String, account : Account) : QboAccount ? {
        try {
            val result =  qboService.add(companyCode, account)
            val submitted = makeSubmittedAccount(qboService.getCredential(companyCode)!!, result!!)
            logger.info("Add QboAccount-[${result.acctNum}]-[${result.id}.${result.name}]")
            return submitted
        }
        catch (e: Exception) {
            logger.error { "Failure Update QboAccount-[${account.acctNum}]-[${account.name}]-[${e.message}]"}
        }
        return null
    }

    fun bulkAdd(companyCode: String,accountMap : MutableMap<String,Account>) : MutableList<QboAccount> {
        val credential = qboService.getCredential(companyCode)
        val bulkSaves = mutableListOf<QboAccount>()
        try {
            if(accountMap.isEmpty())
                return bulkSaves

            qboService.selectsIn(companyCode,"name",Account(),accountMap.keys.toMutableList()) { result ->
                // 퀵북에 확인 진행 후 이미 등록되어 있다면 batchAdd 에서 제외함
                accountMap[result.name]?.let { exits ->
                    val submitted = makeSubmittedAccount(credential!!,result)
                    flush(companyCode,submitted)
                    accountMap.remove(result.name)
                }
            }
            if(accountMap.isNotEmpty()) {
                val results =
                    qboService.batchAdd(companyCode, Account::class.java, accountMap.values.toMutableList())
                results?.let {
                    results.forEach { result ->
                        val submitted = makeSubmittedAccount(credential!!, result)
                        bulkSaves.add(submitted)
                        flush(companyCode, submitted)
                        logger.info("Add QboAccount-[${result.name}]-[${result.id}]")
                    }
                }
            }
        }
        catch (e: Exception) {
            // 이쪽으로 들어오면 좀 난감한데 ㅠㅠ
            logger.error { "Failure bulkAdd QboAccount-[${e.message}]"}
        }
        return bulkSaves
    }
    fun raise(companyCode : String,accountCodeSet : MutableSet<String>) : MutableList<QboAccount> {
        if(accountCodeSet.isEmpty())
            return mutableListOf()

        // 이미 퀵북에 등록되어 있는지 여부 확인
        val qboAccounts = qboAccountRepository.findAccountCodesWithinAccountCode(accountCodeSet)
        var codeSet = accountCodeSet.toMutableSet()
        qboAccounts?.let { accounts ->
            accounts.forEach {account ->
                flush(companyCode,account)
                codeSet.remove(account.key.accountCode)
            }
        }

        val accountKeys = mutableListOf<AccountKey>()
        codeSet.forEach { code ->
            accountKeys.add(AccountKey(companyCode=CompanyCode.of(companyCode),accountCode=code))
        }
        val submitAccounts = mutableMapOf<String,Account>()
        accountRepository.findByAccountKeyIn(accountKeys).forEach{ account ->
            val buildedAccount = buildAccount(account)
            submitAccounts[buildedAccount.name] = buildedAccount
        }
        return bulkAdd(companyCode,submitAccounts)
    }
}