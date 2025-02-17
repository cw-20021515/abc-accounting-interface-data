package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.repository.CompanyRepository
import com.abc.us.accounting.documents.domain.repository.FiscalClosingCriteria
import com.abc.us.accounting.documents.domain.repository.FiscalClosingHistoryRepository
import com.abc.us.accounting.documents.domain.repository.FiscalClosingRepository
import com.abc.us.accounting.documents.domain.type.ClosingStatus
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.exceptions.DocumentException
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Service
class FiscalClosingService(
    private val companyServiceable: CompanyServiceable,
    private val fiscalClosingRepository: FiscalClosingRepository,
    private val fiscalClosingHistoryRepository: FiscalClosingHistoryRepository,
) {
    private val lock: Any = Any()

    private val cachedFiscalClosing: MutableMap<FiscalKey, FiscalClosing> = mutableMapOf()

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    init {
        construct()
    }

    /**
     * 1분 단위로 실행
     */
    @Scheduled(fixedDelay = 300000)
    protected fun reload() {
        construct()
    }

    private final fun construct(from: YearMonth = YearMonth.now().minusMonths(6), to: YearMonth =YearMonth.now()) {
        val criteriaList = CompanyCode.entries.map { companyCode ->
            val fiscalFrom = companyServiceable.getCompanyFiscalYearMonth(companyCode, from)
            val fiscalTo = companyServiceable.getCompanyFiscalYearMonth(companyCode, to)

            FiscalClosingCriteria(
                companyCode = companyCode,
                from = fiscalFrom,
                to = fiscalTo
            )
        }
        synchronized(lock)  {
            try {
                val fiscalClosingList = fiscalClosingRepository.findByCriteria(criteriaList)
                cachedFiscalClosing.clear()
                cachedFiscalClosing.putAll(fiscalClosingList.associateBy { it.fiscalKey })
                logger.debug("construct cachedFiscalClosing:${cachedFiscalClosing.size} data done, by from:$from to:$to")
            } catch (ex:Exception) {
                logger.warn("exception occurred by construct", ex)
                throw ex
            }
        }
    }

    fun toYearMonth(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth): YearMonth {
        return fiscalYearMonth.toYearMonth(companyServiceable.getCompanyFiscalRule(companyCode))
    }

    fun toFiscalYearMonth(companyCode: CompanyCode, yearMonth: YearMonth): FiscalYearMonth {
        return FiscalYearMonth.from(yearMonth, companyServiceable.getCompanyFiscalRule(companyCode))
    }

    private fun toFiscalYearMonth (companyCode: CompanyCode, baseDate: LocalDate = LocalDate.now()): FiscalYearMonth {
        val fiscalRule = companyServiceable.getCompanyFiscalRule(companyCode)
        return FiscalYearMonth.from(baseDate, fiscalRule)
    }

    fun getFiscalClosing(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth): FiscalClosing? {
        val fiscalClosing =  cachedFiscalClosing[FiscalKey.of(companyCode, fiscalYearMonth)]
        return fiscalClosing
    }

    fun isFiscalClosed(companyCode: CompanyCode, baseDate:LocalDate=LocalDate.now()): Boolean {
        val fiscalClosing = getFiscalClosing(companyCode, toFiscalYearMonth(companyCode, baseDate))
            ?: return false
        return fiscalClosing.isClosed()
    }

    fun validateFiscalClosing(companyCode: CompanyCode, baseDate: LocalDate = LocalDate.now()) {
        if (isFiscalClosed(companyCode, baseDate)) {
            throw DocumentException.AlreadyFiscalClosedException("companyCode:$companyCode, baseDate:$baseDate is already closed")
        }
    }

    @Transactional
    fun openFiscalYear(companyCode: CompanyCode, fiscalYear: Int, userId: String = Constants.APP_NAME) {
        val fiscalYearMonth = FiscalYearMonth.of(fiscalYear, 1)

        // 12개월 기간 생성
        (0 until 12).forEach { monthIndex ->
            val currentFiscalYearMonth = fiscalYearMonth.plusMonths(monthIndex)
            openFiscalYearMonth(companyCode, currentFiscalYearMonth, userId)
        }
    }



    @Transactional
    fun openFiscalYearMonth(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth, userId: String = Constants.APP_NAME) {
        val yearMonth = fiscalYearMonth.toYearMonth(companyServiceable.getCompanyFiscalRule(companyCode))
        logger.info{"openFiscalYearMonth companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth, yearMonth:${yearMonth}, userId:$userId"}

        // 1. 해당 기간이 존재 하는지 확인
        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth)
        if (fiscalClosing != null) {
            throw DocumentException.FiscalClosingValidationException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is already exists")
        }

        // 2. 이전 기간이 존재 하는지 확인
        val previousFiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth.previous())
        if  (previousFiscalClosing == null) {       // 아직 이전 기간이 생성되지 않은 경우
            logger.warn { "companyCode:$companyCode, fiscalYearMonth:${fiscalYearMonth.previous()} is not exists, currentFiscalYearMonth:${fiscalYearMonth}" }
        }

        // 3. 이전 기간이 OPEN 상태인지 확인
        if (previousFiscalClosing != null && !previousFiscalClosing.isOpen()) {
            throw DocumentException.FiscalClosingValidationException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is not open")
        }

        // 4. 새로운 기간 생성
        val newFiscalClosing = FiscalClosing.of(FiscalKey.of(companyCode, fiscalYearMonth), userId)

        val savedFiscalClosing = fiscalClosingRepository.save(newFiscalClosing)
        fiscalClosingHistoryRepository.save(savedFiscalClosing.toHistory())
        cachedFiscalClosing[FiscalKey.of(companyCode, fiscalYearMonth)] = savedFiscalClosing
    }

    @Transactional
    fun startClosingFiscalYearMonth(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth, userId: String = Constants.APP_NAME):FiscalClosing {
        val yearMonth = toYearMonth(companyCode, fiscalYearMonth)
        logger.info{"startClosingFiscalYearMonth companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth, yearMonth:${yearMonth}, userId:$userId"}

        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth)
            ?: throw DocumentException.FiscalClosingValidationException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is not exists")

        validateCanStartClosing(fiscalClosing)

        // 이전 월이 마감 되었는지 확인
        validatePreviousPeriodClosed(companyCode, fiscalYearMonth)

        val newFiscalClosing = fiscalClosing.startClosing(userId)
        val history = newFiscalClosing.toHistory()

        val savedFiscalClosing = fiscalClosingRepository.save(newFiscalClosing)
        fiscalClosingHistoryRepository.save(history)
        cachedFiscalClosing[FiscalKey.of(companyCode, fiscalYearMonth)] = savedFiscalClosing

        return savedFiscalClosing
    }


    @Transactional
    fun closeFiscalYearMonth(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth, userId:String = Constants.APP_NAME) {
        val yearMonth = toYearMonth(companyCode, fiscalYearMonth)
        logger.info{"closeFiscalYearMonth companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth, yearMonth:${yearMonth}, userId:$userId"}

        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth)
            ?: throw DocumentException.FiscalClosingValidationException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is not exists")

        if (fiscalClosing.isClosed()) {
            throw DocumentException.AlreadyFiscalClosedException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is already closed")
        }

        val newFiscalClosing = fiscalClosing.completeClosing(userId)
        val savedFiscalClosing = fiscalClosingRepository.save(newFiscalClosing)
        fiscalClosingHistoryRepository.save(savedFiscalClosing.toHistory())
        cachedFiscalClosing[FiscalKey.of(companyCode, fiscalYearMonth)] = savedFiscalClosing
    }


    @Transactional
    fun reopenFiscalYearMonth(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth, userId: String = Constants.APP_NAME, reason: String) {
        val yearMonth = toYearMonth(companyCode, fiscalYearMonth)
        logger.info{"reopenFiscalYearMonth companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth, yearMonth:${yearMonth}, userId:$userId"}

        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth) ?:
            throw DocumentException.FiscalClosingValidationException("companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth is not exists")

        // 다음 기간이 마감되어 있으면 재오픈 불가
        validateNextPeriodNotClosed(companyCode, fiscalYearMonth)

        fiscalClosing.reopenPeriod(userId, reason)
        val savedFiscalClosing = fiscalClosingRepository.save(fiscalClosing)
        fiscalClosingHistoryRepository.save(savedFiscalClosing.toHistory())

        cachedFiscalClosing[FiscalKey.of(companyCode, fiscalYearMonth)] = savedFiscalClosing
    }

    private fun validateNextPeriodNotClosed(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        val next = fiscalYearMonth.next()
        val fiscalClosing = getFiscalClosing(companyCode, next)
            ?: throw DocumentException.FiscalClosingValidationException("Next period is not exists, companyCode:$companyCode, fiscalYearMonth:$next")
        if ( fiscalClosing.isClosed() ) {
            throw DocumentException.FiscalClosingValidationException("Cannot reopen when next period is closed")
        }
    }

    /**
     * TODO: 마감 데이터 정리 로직
     */
    @Transactional
    fun cleanup(fiscalYear:Int, companyCodes: List<CompanyCode> = CompanyCode.entries) {
        companyCodes.forEach { companyCode ->
            (1 .. 12).forEach { monthIndex ->
                cleanup(companyCode, FiscalYearMonth.of(fiscalYear, monthIndex))
            }
        }
    }

    /**
     * TODO: 마감 데이터 정리 로직
     */
    @Transactional
    fun cleanup (companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth)
        if ( fiscalClosing != null) {
            val fiscalKey = fiscalClosing.fiscalKey
            val history = fiscalClosingHistoryRepository.findAllByFiscalKeyIn(listOf(fiscalKey))
            fiscalClosingRepository.delete(fiscalClosing)
            fiscalClosingHistoryRepository.deleteAllById(history.map { it.id })
            cachedFiscalClosing.remove(fiscalKey)
        }
    }


    /**
     * TODO: 이월 데이터 생성 로직
     * 예) 전기의 마감 잔액을 이번 기의 기초 잔액으로 이월
     */
    private fun carryForwardBalances(from: FiscalClosing, to: FiscalClosing) {
//        // 1. 이월 잔액 계산
//        val carryForwardBalances = persistenceService.calculateCarryForwardBalances(from.fiscalKey, to.fiscalKey)
//
//        // 2. 이월 잔액 저장
//        val snapshot = FiscalClosingBalanceSnapshot(
//            fiscalClosingId = to.id,
//            balances = carryForwardBalances
//        )
//        fiscalClosingBalanceSnapshotRepository.save(snapshot)
    }


    private fun validateCanStartClosing(fiscalClosing: FiscalClosing) {
        val companyCode = fiscalClosing.fiscalKey.companyCode
        val fiscalYearMonth = fiscalClosing.fiscalKey.fiscalYearMonth

        when (fiscalClosing.status) {
            ClosingStatus.CLOSING -> throw DocumentException.FiscalClosingValidationException("Already in closing process, by companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth")
            ClosingStatus.CLOSED -> throw DocumentException.FiscalClosingValidationException("Period already closed, by companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth")
            else -> { /* OK */ }
        }
    }

    private fun validatePreviousPeriodClosed(companyCode: CompanyCode, fiscalYearMonth: FiscalYearMonth) {
        val previousFiscalYearMonth = fiscalYearMonth.previous()
        val temp = fiscalClosingRepository.findById(FiscalKey.of(companyCode, previousFiscalYearMonth))
        if (temp.isEmpty) {
            logger.info{"previous period is not found, companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth"}
            return
        }
        val previousClosing = temp.get()
        if (previousClosing.isClosed()) {
            throw DocumentException.PreviousFiscalClosingStatusException(companyCode, previousFiscalYearMonth)
        }
    }

    /**
     * TODO: 마감처리 관련 (추가해야 함)
     */
    @Transactional
    fun executeClosingProcess(companyCode: CompanyCode, yearMonth: YearMonth, userId: String) {
        val fiscalYearMonth = toFiscalYearMonth(companyCode, yearMonth)
        val fiscalClosing = getFiscalClosing(companyCode, fiscalYearMonth)

//        try {
//            // 1. 마감 전 검증
//            val validationResults = validationService.validateClosing(fiscalClosing)
//            if (validationResults.hasErrors()) {
//                throw ClosingValidationException(validationResults)
//            }
//
//            // 2. 미결 전표 확인
//            val unpostedDocuments = documentService.findUnpostedDocuments(companyCode, yearMonth)
//            if (unpostedDocuments.isNotEmpty()) {
//                throw BusinessException("Unposted documents exist")
//            }
//
//            // 3. 잔액 계산 및 이월
//            calculateAndCarryForwardBalances(fiscalClosing)
//
//            // 4. 마감 완료 처리
//            fiscalClosing.completeClosing(userId)
//            closingRepository.save(fiscalClosing)
//
//        } catch (e: Exception) {
//            // 오류 발생시 OPEN 상태로 롤백
//            fiscalClosing.status = ClosingStatus.OPEN
//            closingRepository.save(fiscalClosing)
//            throw e
//        }
    }



//    fun closeMonthly() {
//        val monthlyClosing = MonthlyClosing(
//            year = Year.now().value,
//            month = Month.now().value
//        )
//        val closingResult = persistenceService.saveMonthlyClosing(monthlyClosing)
//        val snapshot = MonthlyClosingBalanceSnapshot(
//            monthlyClosingId = closingResult.id
//        )
//        persistenceService.saveMonthlyClosingBalanceSnapshot(snapshot)
//    }

}