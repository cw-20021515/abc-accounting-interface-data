package com.abc.us.accounting.qbo.helper.adjustment

import com.abc.us.accounting.config.Constants
import com.intuit.ipp.data.JournalEntry
import com.intuit.ipp.data.Line
import com.intuit.ipp.data.PostingTypeEnum
import mu.KotlinLogging
import java.math.BigDecimal
import java.math.RoundingMode

class RoundingAdjustment(
    private val scale : Int,
    private val roundingMode: RoundingMode
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var difference = BigDecimal.ZERO
    var debits = BigDecimal.ZERO
    var credits = BigDecimal.ZERO

    val hasDifference : Boolean get() = difference > BigDecimal.ZERO
    fun adjustLines(lines: List<Line>,
                    difference: BigDecimal,
                    originSum: BigDecimal) {

        if (originSum.compareTo(BigDecimal.ZERO) == 0)
            return
        lines.forEach { line ->
            val proportion = line.amount.divide(originSum, Constants.ACCOUNTING_PRECISION, RoundingMode.HALF_UP)
            val adjustment = difference.multiply(proportion).setScale(scale, RoundingMode.HALF_UP)
            line.amount = line.amount.add(adjustment)
        }
    }
    fun execute(je: JournalEntry) : Boolean{

        val lines = je.line ?: mutableListOf()

        // 1. 반올림 이전 원래 합계 계산 (Debit과 Credit 분리)
        val debitLines = lines.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.DEBIT }
        val creditLines = lines.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.CREDIT }

        // 2. 원본 합 계산
        val originalDebitSum = debitLines.sumOf { it.amount }
        val originalCreditSum = creditLines.sumOf { it.amount }

        //각 Line의 Amount를 2자리 소수로 반올림
        lines.forEach { line -> line.amount = line.amount.setScale(scale, roundingMode) }

        //반올림 후 합계 계산
        debits = debitLines.sumOf { it.amount }
        credits = creditLines.sumOf { it.amount }

        // 단수 차이 계산
        difference = debits - credits

        if(difference.compareTo(BigDecimal.ZERO) != 0) {
            // 단수 차이 발생
            // 단수 차이 보정의 기본 원칙은 부족한 라인에 차이를 보충해줌
            if(difference > BigDecimal.ZERO)
                adjustLines(creditLines, difference.abs(), originalCreditSum)
            else
                adjustLines(debitLines, difference.abs(), originalDebitSum)
        }
        val finalDebit = debitLines.sumOf { it.amount }
        val finalCredit = creditLines.sumOf { it.amount }
        return finalDebit == finalCredit
    }
}