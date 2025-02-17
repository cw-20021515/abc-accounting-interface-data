package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.Account
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.repository.*
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
class TrialBalanceService   (
    private val persistenceService: DocumentPersistenceService,

    private val trialBalanceSearchRepository: TrialBalanceSearchRepository,
    private val customAccountSearchRepository: CustomAccountSearchRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Transactional(readOnly = true)
    fun searchTrialBalance(context: DocumentServiceContext, filters: SearchTrialBalanceFilters): Page<TrialBalanceResult> {
        logger.info { "searchTrialBalance, context:$context, filters: ${filters.toStringByReflection()}" }
        val accounts = customAccountSearchRepository.searchAccounts(filters)
        //logger.info {"searchTrialBalance accounts : ${MapperUtil.logMapCheck(accounts)} "}
        val docItems = trialBalanceSearchRepository.searchTrialBalanceDocItems(filters)
        val documents = trialBalanceSearchRepository.searchTrialBalanceDocuments(filters)
        //logger.info {"searchTrialBalance docItems : ${MapperUtil.logMapCheck(docItems)} "}

        val data = createTrialBalanceResults(context, accounts.toList(), docItems, documents, filters)

        return PageImpl(data, accounts.pageable, accounts.totalElements)
    }

    @Transactional(readOnly = true)
    fun searchTrialBalanceExcelDownload(context: DocumentServiceContext,
                                        filters: SearchTrialBalanceFilters,
                                        response: HttpServletResponse
    ) {
        logger.info { "searchTrialBalanceExcelDownload, context:$context, filters: ${filters.toStringByReflection()}" }
        val accounts = customAccountSearchRepository.searchAccounts(filters)
        //logger.info {"searchTrialBalance accounts : ${MapperUtil.logMapCheck(accounts)} "}
        val docItems = trialBalanceSearchRepository.searchTrialBalanceDocItems(filters)
        val documents = trialBalanceSearchRepository.searchTrialBalanceDocuments(filters)
        //logger.info {"searchTrialBalance docItems : ${MapperUtil.logMapCheck(docItems)} "}

        val data = createTrialBalanceResults(context, accounts.toList(), docItems, documents, filters)

        val headers = listOf(
            "코드",
            "이름",
            "차변기초잔액",
            "차변합계",
            "차변기말잔액",
            "대변기초잔액",
            "대변합계",
            "대변기말잔액"
        )
        val datas = data.map {
            listOf(
                it.accountCode,
                it.accountName,
                it.beginningDebitBalance,
                it.totalDebitAmount,
                it.endingDebitBalance,
                it.beginningCreditBalance,
                it.totalCreditAmount,
                it.endingCreditBalance
            )
        } as List<List<Any>>
        val fileName = "합계잔액시산표조회"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    // 결과 목록 생성
    fun createTrialBalanceResults(context: DocumentServiceContext,
                                  accounts: List<Account>,
                                  docItems: List<DocumentItem>,
                                  documents: List<Document>,
                                  filters: SearchTrialBalanceFilters ): List<TrialBalanceResult> {
        return accounts.map { account ->
            val items = docItems.filter { it.toAccountKey() == account.accountKey }

            val itemResults = items.map { item ->
                val postingDate = documents.filter { it.id == item.docId }
                                           .map {it.postingDate}
                                           .first()
                TrialBalanceDocumentItemResult.toResult(item, postingDate)
            }
            val result = createTrialBalanceResult(context, account, itemResults, filters)
            result
        }
    }

    // 계정코드 별 결과 생성
    fun createTrialBalanceResult(context: DocumentServiceContext,
                                 account: Account,
                                 itemResults: List<TrialBalanceDocumentItemResult>,
                                 filters: SearchTrialBalanceFilters ): TrialBalanceResult {
        val beginningFromDate = filters.beginningFromDate
        val beginningToDate = filters.beginningToDate
        val fromDate = filters.fromDate
        val toDate = filters.toDate
        val debitDocumentItems = itemResults.filter {
                it.accountSide == AccountSide.DEBIT &&
                it.postingDate!! in fromDate..toDate
        }
        val creditDocumentItems = itemResults.filter {
                it.accountSide == AccountSide.CREDIT &&
                it.postingDate!! in fromDate..toDate
        }
        val totalDebitAmount = debitDocumentItems.sumOf { it.amount }
        val totalCreditAmount = creditDocumentItems.sumOf { it.amount }

        var beginningDebitBalance = BigDecimal(0)       //차변 기초잔액 - 현재는 계산된 금액, 추후 balance data 적용
        var beginningCreditBalance = BigDecimal(0)      //대변 기초잔액 - 현재는 계산된 금액, 추후 balance data 적용

        var beginningDebitDocumentItems: List<TrialBalanceDocumentItemResult>? = null
        var beginningCreditDocumentItems: List<TrialBalanceDocumentItemResult>? = null
        if (fromDate > beginningFromDate){
            beginningDebitDocumentItems = itemResults.filter {
                it.accountSide == AccountSide.DEBIT &&
                it.postingDate!! in beginningFromDate..beginningToDate
            }
            beginningCreditDocumentItems = itemResults.filter {
                it.accountSide == AccountSide.CREDIT &&
                it.postingDate!! in beginningFromDate..beginningToDate
            }
        }
        beginningDebitBalance = (beginningDebitDocumentItems?.sumOf { it.amount } ?: 0) as BigDecimal
        beginningCreditBalance = (beginningCreditDocumentItems?.sumOf { it.amount } ?: 0) as BigDecimal

        val trialBalanceResult = TrialBalanceResult(
            companyCode = account.accountKey.companyCode,
            accountCode = account.accountKey.accountCode,
            accountName = account.name,
//            level = 5,
            //차변
            beginningDebitBalance = beginningDebitBalance,
            totalDebitAmount = totalDebitAmount,
            endingDebitBalance = beginningDebitBalance.add(totalDebitAmount),
            // 대변
            beginningCreditBalance = beginningCreditBalance,
            totalCreditAmount = totalCreditAmount,
            endingCreditBalance = beginningCreditBalance.add(totalCreditAmount)
        )
        logger.info {"createTrialBalanceResult trialBalanceResult : ${MapperUtil.logMapCheck(trialBalanceResult)} "}
//        logger.info {"companyCode = " + account.accountKey.companyCode}
//        logger.info {"accountCode = " + account.accountKey.accountCode}
//        logger.info {"accountName = " + account.name}
//        logger.info {"accountDesciption = " + account.description}
//        logger.info {"totalDebitAmount  = " + totalDebitAmount}
//        logger.info {"totalCreditAmount = " + totalCreditAmount}
        return trialBalanceResult
    }

    fun convertToFirstDayOfMonth(month: String): LocalDate {
        // 입력받은 'YYYY-MM' 형식을 LocalDate로 변환하고, 1일로 설정
        val input = month
        val yearMonth = YearMonth.parse(input)
        val firstDayofMonth = yearMonth.atDay(1)

        return firstDayofMonth
    }

    fun convertToLastDayOfMonth(month: String): LocalDate {
        // 입력받은 'YYYY-MM' 형식을 LocalDate로 변환하고, 마지막일로 설정
        val input = month
        val yearMonth = YearMonth.parse(input)
        val lastDayofMonth = yearMonth.atEndOfMonth()

        return lastDayofMonth
    }

    fun calculateBeginingToDate(fromDate: LocalDate): LocalDate {
        val beginingToDate = fromDate.minusDays(1)  // 1일 이전 날짜 계산
        return beginingToDate
    }
}