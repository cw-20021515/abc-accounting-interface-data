package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.AccountBalanceConfig
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.repository.AccountBalanceRecordRepository
import com.abc.us.accounting.documents.domain.repository.AccountBalanceRepository
import com.abc.us.accounting.documents.domain.repository.FiscalClosingBalanceSnapshotRepository
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.IdGenerator
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.retry.RecoveryCallback
import org.springframework.retry.RetryCallback
import org.springframework.retry.support.RetryTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime


@Component
class AccountBalanceEventHandle(
    private val accountBalanceService: AccountBalanceService
) {
    companion object {
        private val logger = KotlinLogging.logger {  }
    }
    // 실시간 잔액 업데이트 (한개만 돌아야 함 - 동시성 문제)
    @Async("accountBalanceEventExecutor")
    @EventListener
    fun handleAccountingEvent(event: AccountBalanceEvent) {
        try {
            logger.info("handleAccountingEvent, event:${eventInfo(event)} begin")
            when (event) {
                is AccountBalanceEvent.OpeningBalance -> accountBalanceService.processOpeningBalance(event)
                is AccountBalanceEvent.BalanceAdjustment -> accountBalanceService.processBalanceAdjustment(event)
                is AccountBalanceEvent.DocumentCreated -> accountBalanceService.processDocumentCreated(event)
                is AccountBalanceEvent.DocumentModified -> accountBalanceService.processDocumentModified(event)
                is AccountBalanceEvent.DocumentDeleted -> accountBalanceService.processDocumentDeleted(event)
                is AccountBalanceEvent.MonthClosed -> accountBalanceService.processMonthClosed(event)
            }
            logger.info("handleAccountingEvent, event:${eventInfo(event)} finished")
        }catch (e: Exception) {
            logger.error("Error processing accounting event: ${eventInfo(event)}", e)
            throw e
        }
    }

    private fun eventInfo(event: AccountBalanceEvent): String {
        return when (event) {
            is AccountBalanceEvent.OpeningBalance -> "OpeningBalance, requests: ${event.requests.size}"
            is AccountBalanceEvent.BalanceAdjustment -> "BalanceAdjustment, requests: ${event.requests.size}"
            is AccountBalanceEvent.DocumentCreated -> "DocumentCreate, requests: ${event.requests.size}"
            is AccountBalanceEvent.DocumentModified -> "DocumentModified, requests: ${event.requests.size}"
            is AccountBalanceEvent.DocumentDeleted -> "DocumentDeleted, requests: ${event.requests.size}"
            is AccountBalanceEvent.MonthClosed -> "MonthClosed, fiscalYearMonth: ${event.fiscalYearMonth}"
        }
    }
}




@Service
class AccountBalanceService(
    private val accountBalanceConfig: AccountBalanceConfig,
    private val accountService: AccountService,
    private val accountBalanceRepository: AccountBalanceRepository,
    private val accountBalanceRecordRepository: AccountBalanceRecordRepository,
    private val fiscalClosingBalanceSnapshotRepository: FiscalClosingBalanceSnapshotRepository,
    @Qualifier("accountBalanceServiceRetry") private val retryTemplate: RetryTemplate
) {

    companion object {
        private val logger = KotlinLogging.logger {  }
    }

    /**
     * Opening Balance 처리
     */
    @Transactional
    fun processOpeningBalance (event: AccountBalanceEvent.OpeningBalance): List<AccountBalanceRecordResult> {
        return retryTemplate.execute( RetryCallback  { retryContext ->
            logger.info("processOpeningBalance: begin, event:${event.requests.size}, accountBalanceConfig:${accountBalanceConfig}")

            val balanceRecordType = BalanceRecordType.OPENING_BALANCE

            val requests = event.requests
            require(requests.isNotEmpty()) { "requests is empty" }

            val duplicateAccountKeys = requests.groupBy { AccountKey.of(it.companyCode, it.accountCode) }.filter { it.value.size > 1 }.keys
            require(duplicateAccountKeys.isEmpty()) { "duplicate accountId must be empty, duplicateAccountIds:${duplicateAccountKeys}" }
            val accountKeys = requests.map { AccountKey.of(it.companyCode, it.accountCode) }.distinct()
            val accounts = accountService.getValidAccounts(accountKeys).associateBy { it.accountKey }
            val missedAccountCodes = accountKeys.filter { !accounts.containsKey(it) }
            require(accounts.size == accountKeys.size) { "account information not found by missedAccountCodes:${missedAccountCodes}" }


            val documentDate = LocalDate.now()
            val postingDate = LocalDate.now()
            val entryDate = LocalDate.now()

            val openBalanceRecords:MutableList<AccountBalanceRecord> = mutableListOf()

            requests.map { request ->
                val accountKey = AccountKey.of(request.companyCode, request.accountCode)
                val account = accounts[accountKey]
                requireNotNull(account) { "invalid account code, by accountId:${accountKey}" }

                val accountNature = account.accountClass.natualAccountSide
                val amount = request.amount.toScale()
                val debitAmount = accountNature.debitAmount(amount, accountNature)
                val creditAmount = accountNature.creditAmount(amount, accountNature)

                val openingBalanceRecord = AccountBalanceRecord(
                    id = IdGenerator.generateNumericId(),
                    accountKey = accountKey,
                    accountNature = accountNature,
                    docItemId = balanceRecordType.name,
                    documentDate = documentDate,
                    postingDate = postingDate,
                    entryDate = entryDate,
                    recordType = balanceRecordType,
                    changeAmount = amount,
                    balanceAfterChange = amount,
                    accumulatedDebitAfterChange = debitAmount,
                    accumulatedCreditAfterChange = creditAmount,
                    recordTime = OffsetDateTime.now()
                )

                openBalanceRecords.add (openingBalanceRecord)

            }

            if (event.context.isSave) {
                val newBalances = openBalanceRecords.map { record -> record.toAccountBalance() }
                accountBalanceRepository.saveAll(newBalances)
                accountBalanceRecordRepository.saveAll(openBalanceRecords)
            }

            logger.info("processOpeningBalance - finished: records:${openBalanceRecords.size}, accountBalanceConfig:${accountBalanceConfig}")

            openBalanceRecords.map { it.toAccountBalanceRecordResult() }
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    /**
     * Balance Adjustment 처리 (잔액을 신규로 조정)
     */
    @Transactional
    fun processBalanceAdjustment (event: AccountBalanceEvent.BalanceAdjustment): List<AccountBalanceRecordResult> {
        return retryTemplate.execute( RetryCallback  { retryContext ->
            logger.info("processBalanceAdjustment: begin, event:${event.requests.size}, accountBalanceConfig:${accountBalanceConfig}")

            val balanceRecordType = BalanceRecordType.BALANCE_ADJUSTMENT

            val requests = event.requests
            require(requests.isNotEmpty()) { "requests is empty" }

            val duplicateAccountKeys = requests.groupBy { AccountKey.of(it.companyCode, it.accountCode) }.filter { it.value.size > 1 }.keys
            require(duplicateAccountKeys.isEmpty()) { "duplicate accountCode must be empty, duplicateAccountIds:${duplicateAccountKeys}" }
            val accountKeys = requests.map { AccountKey.of(it.companyCode, it.accountCode) }.distinct()
            val accounts = accountService.getValidAccounts(accountKeys).associateBy { it.accountKey }
            val missedAccountCodes = accountKeys.filter { !accounts.containsKey(it) }
            require(accounts.size == accountKeys.size) { "account information not found by missedAccountCodes:${missedAccountCodes}" }

            val documentDate = LocalDate.now()
            val postingDate = LocalDate.now()
            val entryDate = LocalDate.now()
            val adjustmentBalanceRecords:MutableList<AccountBalanceRecord> = mutableListOf()

            requests.map { request ->
                val accountKey = AccountKey.of(request.companyCode, request.accountCode)
                val account = accounts[accountKey]
                requireNotNull(account) { "account not found, by accountId:${accountKey}" }
                val accountNature = account.accountClass.natualAccountSide

                val amount = request.amount.toScale()
                val debitAmount = accountNature.debitAmount(amount, accountNature)
                val creditAmount = accountNature.creditAmount(amount, accountNature)

                val openingBalanceRecord = AccountBalanceRecord(
                    id = IdGenerator.generateNumericId(),
                    accountKey = accountKey,
                    accountNature = accountNature,
                    docItemId = balanceRecordType.name,
                    documentDate = documentDate,
                    postingDate = postingDate,
                    entryDate = entryDate,
                    recordType = balanceRecordType,
                    changeAmount = amount,
                    balanceAfterChange = amount,
                    accumulatedDebitAfterChange = debitAmount,
                    accumulatedCreditAfterChange = creditAmount,
                    recordTime = OffsetDateTime.now()
                )
                openingBalanceRecord
            }


            if (event.context.isSave) {
                val newBalances = adjustmentBalanceRecords.map { record -> record.toAccountBalance() }
                accountBalanceRepository.saveAll(newBalances)
                accountBalanceRecordRepository.saveAll(adjustmentBalanceRecords)
            }

            logger.info("processBalanceAdjustment - finished: records:${adjustmentBalanceRecords.size}, accountBalanceConfig:${accountBalanceConfig}")
            adjustmentBalanceRecords.map { it.toAccountBalanceRecordResult() }
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }


    fun constructCurrentBalanceMap (accountKeys: List<AccountKey>, balanceMap:Map<AccountKey, AccountBalance>): MutableMap<AccountKey, AccountBalanceCalculation> {
        // 최신 잔액을 확인 하기 위한 구조
        val currentBalanceMap = balanceMap.map { (accountCode, balance) ->
            accountCode to AccountBalanceCalculation(
                balance = balance.balance,
                accumulatedDebit = balance.accumulatedDebit,
                accumulatedCredit = balance.accumulatedCredit
            )
        }.toMap().toMutableMap()
        return currentBalanceMap
    }

    fun toAccountBalanceRecord(newDocItem:DocumentItemResult,
                               documentDate:LocalDate,
                               postingDate:LocalDate,
                               balanceRecordType:BalanceRecordType,
                               currentBalanceMap:MutableMap<AccountKey, AccountBalanceCalculation>,
                               accountMap:Map<AccountKey, Account>,
                               oldDocItem:DocumentItemResult?=null):AccountBalanceRecord{
        if ( oldDocItem != null) {
            require(newDocItem.lineNumber == oldDocItem.lineNumber) {
                "lineNumber must be equal, " +
                        "but newLineNumber:${newDocItem.lineNumber}, originalLineNumber:${oldDocItem.lineNumber}"
            }
            require(newDocItem.accountCode == oldDocItem.accountCode) {
                "accountCode must be equal, " +
                        "but newAccountCode:${newDocItem.accountCode}, originalAccountCode:${oldDocItem.accountCode}"
            }
        }

        val accountKey = newDocItem.toAccountKey()
        val account = accountMap[accountKey]
        requireNotNull(account) { "account not found by accountKey:${accountKey}" }
        val accountNature = account.accountClass.natualAccountSide

        // accountBalance가 없는 경우에 초기화
        val lastAccountBalance = currentBalanceMap[accountKey] ?: AccountBalanceCalculation.zero()

        // 금액차이 계산
        val newAccountBalance = lastAccountBalance.add(accountNature, newDocItem, oldDocItem)
        val changeAmount = newAccountBalance.balance - lastAccountBalance.balance

        val record = AccountBalanceRecord(
            id = IdGenerator.generateNumericId(),
            accountKey = accountKey,
            accountNature = accountNature,
            docItemId = newDocItem.docItemId,
            documentDate = documentDate,
            postingDate = postingDate,
            entryDate = LocalDate.now(),
            recordType = balanceRecordType,
            changeAmount = changeAmount,
            balanceAfterChange = newAccountBalance.balance,
            accumulatedDebitAfterChange = newAccountBalance.accumulatedDebit,
            accumulatedCreditAfterChange = newAccountBalance.accumulatedCredit,
            recordTime = OffsetDateTime.now()
        )

        currentBalanceMap[accountKey] = newAccountBalance
        return record
    }


    /**
     * DocumentCreated 처리
     */
    @Transactional
    fun processDocumentCreated (event: AccountBalanceEvent.DocumentCreated):List<AccountBalanceRecordResult>{
        return retryTemplate.execute( RetryCallback  { retryContext ->
            try {
                logger.info("processDocumentCreated: begin, event:${event.requests.size}, accountBalanceConfig:${accountBalanceConfig}")
                val balanceRecordType = BalanceRecordType.DOCUMENT_CREATED

                require(event.requests.isNotEmpty()) { "processDocumentCreated: requests must not be empty" }
                val allDocItems = event.requests.map { it.newDocumentResult.docItems }.flatten()
                val docIds = event.requests.map { it.newDocumentResult.docId }.distinct()
                require(allDocItems.isNotEmpty()) { "documentItems must not be empty, by docIds:${docIds}" }

                val keys = allDocItems.map { AccountKey.of(it.companyCode, it.accountCode) }.distinct()
                val accountKeys = allDocItems.map { it.toAccountKey() }.distinct()
                val accountMap = accountService.getValidAccounts(accountKeys).associateBy { it.accountKey }
                logger.debug("accountMap:{} by accountKeys:{}", accountMap.size, accountKeys)

                require(accountMap.size == accountKeys.size) { "invalid account code, by accountIds:${accountKeys}" }

                // init, draft, review 상태는 처리하지 않음
                val documentResults = event.requests.map { it.newDocumentResult }.filter { it.docStatus.isAcceptable }


                // 기존 최신 잔액 조회
                val balanceMap = accountBalanceRepository.findAllById(accountKeys).associateBy { it.accountKey }
                // 최신 잔액을 확인 하기 위한 구조
                val currentBalanceMap = constructCurrentBalanceMap(keys, balanceMap)

                val records = documentResults.mapNotNull{ documentResult ->
                    val docItems = documentResult.docItems
                    val documentDate = documentResult.documentDate
                    val postingDate = documentResult.postingDate

                    // 임시전표는 잔액 계산에서 제외
                    val currentRecords = if ( !documentResult.docStatus.isAcceptable ) {
                        logger.debug { "document: ${documentResult.docId}, docStatus:${documentResult.docStatus} is ignored for balance calculation" }
                        null
                    } else {
                        docItems.map { docItemResult ->
                            toAccountBalanceRecord(
                                docItemResult,
                                documentDate,
                                postingDate,
                                balanceRecordType,
                                currentBalanceMap,
                                accountMap
                            )
                        }
                    }
                    currentRecords
                }.flatten()

                if (event.context.isSave) {
                    val newBalances = records.map { record -> record.toAccountBalance() }
                    if ( newBalances.isNotEmpty() ) {
                        accountBalanceRepository.saveAll(newBalances)
                    }
                    if ( records.isNotEmpty() ) {
                        accountBalanceRecordRepository.saveAll(records)
                    }
                }

                logger.info("processDocumentCreated - finished: records:${records.size}, accountBalanceConfig:${accountBalanceConfig}")
                records.map { it.toAccountBalanceRecordResult() }
            }catch (ex:Exception){
                logger.error { "processDocumentCreated - error: ${ex.message}" }
                throw ex
            }
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    /**
     * DocumentModified 처리 (기존 데이터를 차감하고 신규 데이터를 추가)
     */
    @Transactional
    fun processDocumentModified(event: AccountBalanceEvent.DocumentModified): List<AccountBalanceRecordResult> {
        return retryTemplate.execute( RetryCallback  { retryContext ->
            try {
                logger.info("processDocumentModified: begin, event:${event.requests.size}, accountBalanceConfig:${accountBalanceConfig}")
                require(event.requests.isNotEmpty()) { "processDocumentModified: requests must not be empty" }

                val balanceRecordType = BalanceRecordType.DOCUMENT_MODIFIED

                val allDocItems = event.requests.map { it.newDocumentResult.docItems }.flatten()
                val docIds = event.requests.map { it.newDocumentResult.docId }.distinct()
                require(allDocItems.isNotEmpty()) { "documentItems must not be empty, by docIds:${docIds}" }

                val keys = allDocItems.map { AccountKey(it.companyCode, it.accountCode) }.distinct()
                val accountKeys = allDocItems.map { AccountKey.of(it.companyCode, it.accountCode) }.distinct()

                val accountMap = accountService.getValidAccounts(accountKeys).associateBy { it.accountKey }
                logger.debug("accountMap:{} by accountKeys:{}", accountMap.size, accountKeys)

                require(accountMap.size == accountKeys.size) { "invalid account codes, by accountIds:${accountKeys}" }


                // 신규, 이전 데이터를 묶어서 처리
                // init, draft, review 상태는 처리하지 않음
                val newResults = event.requests.map { it.newDocumentResult }.filter { it.docStatus.isAcceptable }
                val originalResults = event.requests.map { it.oldDocumentResult }.filter { it?.docStatus?.isAcceptable ?:  false}

                val mappedResults = newResults.zip(originalResults)

                // 기존 최신 잔액 조회
                val balanceMap = accountBalanceRepository.findAllById(accountKeys).associateBy { it.accountKey }
                // 최신 잔액을 확인 하기 위한 구조
                val currentBalanceMap = constructCurrentBalanceMap(keys, balanceMap)

                val records = mappedResults.mapNotNull { (newDocumentResult, originalDocumentResult) ->
                    if ( !newDocumentResult.docStatus.isAcceptable ) {
                        logger.debug { "document: ${newDocumentResult.docId}, docStatus:${newDocumentResult.docStatus} is ignored for balance calculation" }
                        null
                    } else {
                        val newDocItems = newDocumentResult.docItems
                        val originalDocItems = originalDocumentResult?.docItems ?: mutableListOf()

                        val documentDate = newDocumentResult.documentDate
                        val postingDate = newDocumentResult.postingDate

                        // 기존 데이터 차감 처리
                        require(newDocItems.size == originalDocItems.size) {
                            "newDocItems.size must be equal to originalDocItems.size, " +
                                    "but newDocItemSize:${newDocItems.size}, originalDocItemSize:${originalDocItems.size}"
                        }
                        val size = newDocItems.size

                        // lineNumber 정렬
                        newDocItems.sortBy { it.lineNumber }
                        originalDocItems.sortBy { it.lineNumber }

                        val currentRecords = (0 until size).map {

                            // originalDocument가 임시전표이면 차액계산에서 제외
                            val oldDocItem = if ( !originalDocumentResult?.docStatus?.isAcceptable!!) null else originalDocItems[it]

                            toAccountBalanceRecord(newDocItems[it], documentDate, postingDate, balanceRecordType, currentBalanceMap, accountMap, oldDocItem)
                        }
                        currentRecords
                    }
                }.flatten()

                if (event.context.isSave) {
                    val newBalances = records.map { record -> record.toAccountBalance() }
                    if ( newBalances.isNotEmpty() ) {
                        accountBalanceRepository.saveAll(newBalances)
                    }
                    if ( records.isNotEmpty() ) {
                        accountBalanceRecordRepository.saveAll(records)
                    }
                }
                logger.info("processDocumentModified - finished: records:${records.size}, accountBalanceConfig:${accountBalanceConfig}")
                records.map { it.toAccountBalanceRecordResult() }
            }catch (ex:Exception){
                logger.error { "processDocumentModified: error: ${ex.message}"  }
                throw ex
            }
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    @Transactional
    fun processDocumentDeleted(event: AccountBalanceEvent.DocumentDeleted): List<AccountBalanceRecordResult> {
        return retryTemplate.execute( RetryCallback  { retryContext ->
            try {
                logger.info("processDocumentDeleted: begin, event:${event.requests.size}, accountBalanceConfig:${accountBalanceConfig}")
                val balanceRecordType = BalanceRecordType.DOCUMENT_DELETED
                require(event.requests.isNotEmpty()) { "processDocumentDeleted: requests must not be empty" }

                val allDocItems = event.requests.map { it.newDocumentResult.docItems }.flatten()
                require(allDocItems.isNotEmpty()) { "documentItems is empty" }

                val keys = allDocItems.map { AccountKey(it.companyCode, it.accountCode) }.distinct()
                val accountKeys = allDocItems.map { it.toAccountKey() }.distinct()
                val accountMap = accountService.getValidAccounts(accountKeys).associateBy { it.accountKey }
                require(accountMap.size == accountKeys.size) { "invalid account code, by accountKeys:${accountKeys}" }

                val documentResults = event.requests.map { it.newDocumentResult }

                // 기존 최신 잔액 조회
                val balanceMap = accountBalanceRepository.findAllById(accountKeys).associateBy { it.accountKey }
                // 최신 잔액을 확인 하기 위한 구조
                val currentBalanceMap = constructCurrentBalanceMap(keys, balanceMap)

                val records = documentResults.mapNotNull{ documentResult ->
                    val docItems = documentResult.docItems
                    val documentDate = documentResult.documentDate
                    val postingDate = documentResult.postingDate

                    val currentRecords = if ( !documentResult.docStatus.isAcceptable ) {
                            logger.debug { "document: ${documentResult.docId}, docStatus:${documentResult.docStatus} is ignored for balance calculation" }
                            null
                        } else {
                            docItems.map { docItem ->
                                toAccountBalanceRecord(
                                    docItem,
                                    documentDate,
                                    postingDate,
                                    balanceRecordType,
                                    currentBalanceMap,
                                    accountMap
                                )
                            }
                        }
                    currentRecords
                }.flatten()

                if (event.context.isSave) {
                    val newBalances = records.map { record -> record.toAccountBalance() }
                    if ( newBalances.isNotEmpty() ) {
                        accountBalanceRepository.saveAll(newBalances)
                    }
                    accountBalanceRecordRepository.saveAll(records)
                }
                logger.info("processDocumentDeleted - finished: records:${records.size}, accountBalanceConfig:${accountBalanceConfig}")
                records.map { it.toAccountBalanceRecordResult() }
            }catch (ex:Exception){
                logger.error { "processDocumentDeleted: error: ${ex.message}" }
                throw ex
            }
        }, RecoveryCallback { retryContext ->
            logger.error("All retry attempts failed. Recovery callback executed")
            throw retryContext.lastThrowable
        })
    }

    fun processMonthClosed(event: AccountBalanceEvent.MonthClosed) {
//        val fiscalYearMonth = event.fiscalYearMonth
//        val yearMonth = fiscalYearMonth.toYearMonth(event.fiscalRule)
//
//        val closingDate = LocalDate.now()
//        val lastPostingDate = yearMonth.atEndOfMonth()
//        val closingBalanceRecords = accountBalanceRecordRepository.findLastRecordsByPostingDate(lastPostingDate)
//
//
//        val closingBalances = closingBalanceRecords.map { record ->
//
//        }
//
//
//        MonthlyClosingBalanceSnapshot{
//            id: Long,                       // 자동으로 생성되는 값 (IdGenerator 이용)
//            val accountCode,
//
//            @Comment("계정 차대구분")
//            @Column(name = "account_side", nullable = false)
//            val accountSide: AccountSide,
//
//            @Embedded
//            @AttributeOverrides(
//                AttributeOverride(name = "fiscal_year", column = Column(name = "fiscal_year")),
//                AttributeOverride(name = "fiscal_month", column = Column(name = "fiscal_month"))
//            )
//            val fiscalYearMonth: FiscalYearMonth,
//
//            @Comment("잔액")
//            @Column(name = "balance", precision = Constants.MATH_PRECISION, scale = Constants.ACCOUNTING_SCALE)
//            val balance: BigDecimal,
//
//            @Comment("차변 누계액")
//            @Column(name = "accumulated_debit", precision = Constants.MATH_PRECISION, scale = Constants.ACCOUNTING_SCALE)
//            val accumulatedDebit: BigDecimal,
//
//            @Comment("대변 누계액")
//            @Column(name = "accumulated_credit", precision = Constants.MATH_PRECISION, scale = Constants.ACCOUNTING_SCALE)
//            val accumulatedCredit: BigDecimal,
//
//            @Comment("스냅샷 날짜")
//            @Column(name = "snapshot_date")
//            val snapshotDate: LocalDate,
//
//            @Comment("스냅샷 유형")
//            @Column(name = "snapshot_type")
//            @Enumerated(EnumType.STRING)
//            val snapshotType: DocumentDateType,     // 현재는 postingDate만 있음
//
//            @Comment("생성 시간")
//            val createdTime: OffsetDateTime = OffsetDateTime.now()
//        }
//
//        val monthlyClosing = MonthlyClosing(
//            fiscalYearMonth = fiscalYearMonth,
//            closingDate = closingDate,
//            closingBalances = closingBalances
//        )
//
//        monthlyClosingRepository.save(monthlyClosing)
//
//        val history = MonthlyClosingHistory(
//            fiscalYearMonth = fiscalYearMonth,
//            closingDate = closingDate,
//            closingBalances = closingBalances
//        )
//
//        monthlyClosingHistoryRepository.save(history)
//
//        val snapshots = closingBalances.map { it.toMonthlyClosingBalanceSnapshot() }
//        monthlyClosingBalanceSnapshotRepository.saveAll(snapshots)
    }

    /**
     * 월별 마감 잔액 스냅샷 생성
     * : 해당 월의 마지막 날짜에 대한 잔액 스냅샷을 생성
     */
    @Transactional
    fun processMonthlyClosingBalanceSnapshot (context: DocumentServiceContext, companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth,
                                              fiscalRule: FiscalRule, snapshotDate: LocalDate = LocalDate.now()) {
        val yearMonth = fiscalYearMonth.toYearMonth(fiscalRule)
        val fromDate = yearMonth.atDay(1)
        val toDate = yearMonth.atEndOfMonth()

        val snapshotType = DocumentDateType.POSTING_DATE

        // 해당 월의 모든 거래내역 조회
        val accounts = accountService.getAllAccounts().associateBy { it.accountKey }
        val previousFiscalYearMonth = fiscalYearMonth.previous()
        val previousFiscalKey = FiscalKey(companyCode, previousFiscalYearMonth)

        // 이전 회계연월 snapshot 정보 확인
        val lastSnapshots = fiscalClosingBalanceSnapshotRepository.findAllByFiscalYearMonth(previousFiscalKey, snapshotType)
        val lastSnapshotsMap = lastSnapshots.associateBy { AccountKey.of(it.fiscalKey.companyCode, it.accountCode) }
        val currentBalanceMap = lastSnapshotsMap.map { (accountKey, record) ->
            accountKey to AccountBalanceCalculation(
                balance = record.balance,
                accumulatedDebit = record.accumulatedDebit,
                accumulatedCredit = record.accumulatedCredit
            )
        }.toMap().toMutableMap()

        var pageNumber = 0
        var slice: Slice<AccountBalanceRecord>
        do {
            val pageable = PageRequest.of(pageNumber, Constants.PAGE_SIZE)
            slice = accountBalanceRecordRepository.findAllByPostingDateRange(fromDate, toDate, pageable)
            slice.forEach { record ->
                val accountKey = record.accountKey
                val account = accounts[accountKey]
                requireNotNull(account) { "invalid account code, by accountKey:${accountKey}" }
                val companyCode = record.accountKey.companyCode
                val accountNature = record.accountNature

                // accountBalance 없는 경우에 초기화
                val lastAccountBalance = currentBalanceMap[accountKey] ?: AccountBalanceCalculation.zero()

                // 변경 금액은 반대로 처리
                val balance = lastAccountBalance.balance.add(record.changeAmount)
                val accumulatedDebit = lastAccountBalance.accumulatedDebit.add(accountNature.debitAmount(record.changeAmount, accountNature))
                val accumulatedCredit = lastAccountBalance.accumulatedCredit.add(accountNature.creditAmount(record.changeAmount, accountNature))

                currentBalanceMap[accountKey] = lastAccountBalance.copy(
                    balance = balance,
                    accumulatedDebit = accumulatedDebit,
                    accumulatedCredit = accumulatedCredit
                )
            }
            pageNumber ++
        } while(slice.hasNext())


        // snapshot records 구성
        val snapshotRecords = currentBalanceMap.map { (accountKey, record) ->

            val accountKey = accountKey
            val balance = record.balance
            val accumulatedDebit = record.accumulatedDebit
            val accumulatedCredit = record.accumulatedCredit
            val accountSide = accounts[accountKey]?.accountClass?.natualAccountSide ?: AccountSide.DEBIT

            val snapshotRecord = FiscalClosingBalanceSnapshot(
                id = IdGenerator.generateNumericId(),
                fiscalKey = FiscalKey(companyCode, fiscalYearMonth),
                accountCode = accountKey.accountCode,
                accountNature = accountSide,
                balance = balance,
                accumulatedDebit = accumulatedDebit,
                accumulatedCredit = accumulatedCredit,
                snapshotDate = snapshotDate,
                snapshotType = snapshotType,
                createdTime = OffsetDateTime.now()
            )
            snapshotRecord
        }

        if ( context.isSave ) {
            fiscalClosingBalanceSnapshotRepository.saveAll(snapshotRecords)
        }
    }

}