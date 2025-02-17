package com.abc.us.accounting.qbo.syncup.account

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.documents.domain.repository.AccountRepository
import com.abc.us.accounting.documents.domain.repository.CompanyRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.qbo.domain.entity.QboAccount
import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.qbo.domain.entity.key.QboAccountKey
import com.abc.us.accounting.qbo.domain.repository.QboAccountRepository
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.qbo.service.QboAccountService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.Account
import com.intuit.ipp.data.AccountClassificationEnum
import com.intuit.ipp.data.AccountTypeEnum
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import com.abc.us.accounting.documents.domain.entity.Account as AbcAccount

@Service
class SyncUpAccount (
    private val qboService : QBOService,
    private val qboCertifier: QBOCertifier,
    private val abcAccountRepository: AccountRepository,
    private val qboAccountRepository: QboAccountRepository,
    private val qboAccountCacheService : QboAccountService
    )  {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }

    fun inquiryAlreadyExistAccounts(companyCode : String) : MutableMap<String, Account> {

        val qboAccounts = mutableMapOf<String,Account>()

        qboService.selectAll(companyCode,Account::class) { account ->
            qboAccounts[account.name] = account
        }
        return qboAccounts
    }
    fun buildQboAccount(account : AbcAccount) : Account {

        return Account().apply {
            name = account.name
            acctNum = account.accountKey.accountCode
            accountType = account.qboAccountType?.let { AccountTypeEnum.fromValue(it.name) }
            accountSubType = account.qboAccountSubType?.value
            classification = account.accountClass.let { cls -> AccountClassificationEnum.fromValue(cls.engName) }
            description = account.description
        }
    }

    fun buildSubmittedAccount(crednetial : QboCredential, account : Account) : QboAccount{
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

    fun updateAccounts(companyCode:String,
                       updateTargets : MutableList<Account>,
                       errmsgs : MutableList<String>) : MutableList<QboAccount>{
        val submittedAccount = mutableListOf<QboAccount>()
        qboService.getCredential(companyCode)?.let {credential ->
            updateTargets.forEach { account ->
                try {
                    qboService.update(companyCode,account)?.let {result ->
                        val submitted = buildSubmittedAccount(credential,result)
                        submittedAccount.add(submitted)
                        logger.info("Update Exists QboAccount-[${account.acctNum}]-[${account.id}.${account.name}]")
                    }
                }
                catch (e : Exception) {
                    errmsgs.add("Failure Update QboAccount-[${account.acctNum}]-[${account.name}]-[${e.message}]")
                }
            }
        }
        return submittedAccount
    }

    fun addAccounts(companyCode:String,
                    addTargets : MutableList<Account>,
                    errmsgs : MutableList<String>)  : MutableList<QboAccount> {
        val submittedAccount = mutableListOf<QboAccount>()
        qboService.getCredential(companyCode)?.let { credential ->
            addTargets.chunked(QBOService.BATCH_SIZE).forEach { accounts ->
                try {
                    val results = qboService.batchAdd(companyCode, Account::class.java,accounts)
                    val submitteds = mutableListOf<QboAccount>()
                    results?.let {
                        results.forEach { result ->
                            val submitted = buildSubmittedAccount(credential, result)
                            submitteds.add(submitted)
                            logger.info("Add QboAccount-[${result.acctNum}]-[${result.id}.${result.name}]")
                        }
                    }
                    submittedAccount.addAll(submitteds)
                } catch (e: Exception) {
                    errmsgs.add("Failure Add QboAccount-[${e.message}]")
                }
            }
        }
        return submittedAccount
    }

    fun syncup(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime

        logger.info { "QBO-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        qboCertifier.visit { credential ->

            val companyCode = credential.companyCode
            logger.info { "QBO-SYNCUP(${credential.activeProfile}-${credential.companyCode})-Account-From(${from}) ~ To(${to})" }

            val addTargets = mutableMapOf<String,Account>()
            val abcAccounts = abcAccountRepository.findByCompanyCode(CompanyCode.of(companyCode))
            abcAccounts.forEach { abcAccount ->
                val account = buildQboAccount(abcAccount)
                addTargets[account.name] = account
            }

            val submitted = qboAccountRepository.findByCompanyCode(companyCode)
            submitted?.let {
                it.forEach { account ->
                    addTargets[account.displayName]?.let { abcAccount ->
                        addTargets.remove(account.displayName)
                    }
                }
            }

            val qboAccounts = inquiryAlreadyExistAccounts(companyCode)
            val updateTargets = mutableMapOf<String,Account>()
            qboAccounts.forEach { accountName,existAccount ->
                addTargets.remove(existAccount.name)
                if(existAccount.acctNum == null) {
                    val abcAccount = abcAccountRepository.findByName(existAccount.name)
                    if(abcAccount == null) {
                        logger.error("NotFound ABC-Account-[${existAccount.name}]")
                    }
                    else {
                        existAccount.acctNum = abcAccount.accountKey.accountCode
                        //TODO : hschoi --> qboAccountType,qboAccountSubType nullable 처리 해결후 복구 예정
//                        existAccount.accountType = AccountTypeEnum.fromValue(abcAccount.qboAccountType.value)
//                        existAccount.accountSubType = abcAccount.qboAccountSubType.value
                        updateTargets[existAccount.name] = existAccount
                    }
                }
            }

            val errmsgs = mutableListOf<String>()
            val results = mutableListOf<QboAccount>()

            results.addAll( addAccounts(companyCode,addTargets.values.toMutableList(),errmsgs) )
            qboAccountRepository.bulkInsert(results)

            results.addAll( updateAccounts(companyCode,updateTargets.values.toMutableList(),errmsgs) )
            qboAccountRepository.bulkInsert(results)

            errmsgs.forEach { msg -> logger.error { msg } }
        }

        qboAccountCacheService.refresh()
        logger.info { "QBO-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}