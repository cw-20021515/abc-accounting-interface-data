package com.abc.us.accounting.qbo.helper.builder

import com.abc.us.accounting.collects.works.JsonHelper
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.qbo.helper.adjustment.RoundingAdjustment
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.JournalEntry
import com.intuit.ipp.data.PostingTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import mu.KotlinLogging
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.math.BigDecimal
import java.math.RoundingMode

class RoundingAdjustmentTest : AnnotationSpec()  {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `JournalEntry greaterThenDebit TEST`() {

        val jsonData = JsonHelper.readFromFile("journal-entry-greaterThenDebit.json", RoundingAdjustmentTest::class)
        val converter = JsonConverter()
        val je = converter.toObj(jsonData, JournalEntry::class.java)
        je.shouldNotBeNull()

        val adjustment = RoundingAdjustment(Constants.QBO_SCALE,RoundingMode.HALF_UP)
        adjustment.execute(je)

        val debits = je.line.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.DEBIT }
            .associateBy({ it.lineNum }, { it.amount }).toMutableMap()
        val credits = je.line.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.CREDIT }
            .associateBy({ it.lineNum }, { it.amount }).toMutableMap()

        val newScale = Constants.QBO_SCALE

        val totalDebits = debits.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount.setScale(newScale, RoundingMode.HALF_UP)) }
        val totalCredits = credits.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount.setScale(newScale, RoundingMode.HALF_UP)) }

        (totalDebits - totalCredits).setScale(Constants.QBO_SCALE,RoundingMode.HALF_UP).shouldBeEqual(BigDecimal.ZERO.setScale(newScale))
    }

    @Test
    fun `JournalEntry greaterThenCredit TEST`() {

        val jsonData = JsonHelper.readFromFile("journal-entry-greaterThenCredit.json", RoundingAdjustmentTest::class)
        val converter = JsonConverter()
        val je = converter.toObj(jsonData, JournalEntry::class.java)
        je.shouldNotBeNull()
        val adjustment = RoundingAdjustment(Constants.QBO_SCALE,RoundingMode.HALF_UP)
        adjustment.execute(je)

        val debits = je.line.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.DEBIT }
            .associateBy({ it.lineNum }, { it.amount }).toMutableMap()
        val credits = je.line.filter { it.journalEntryLineDetail.postingType == PostingTypeEnum.CREDIT }
            .associateBy({ it.lineNum }, { it.amount }).toMutableMap()

        val newScale = Constants.QBO_SCALE

        val totalDebits = debits.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount.setScale(newScale, RoundingMode.HALF_UP)) }
        val totalCredits = credits.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount.setScale(newScale, RoundingMode.HALF_UP)) }

        (totalDebits - totalCredits).setScale(Constants.QBO_SCALE,RoundingMode.HALF_UP).shouldBeEqual(BigDecimal.ZERO.setScale(newScale))
    }
    @Test
    fun `RoundingAdjustment credit TEST`() {
        val jsonData = JsonHelper.readFromFile("journal-entry-adjust-credit.json", RoundingAdjustmentTest::class)
        val converter = JsonConverter()
        val je = converter.toObj(jsonData, JournalEntry::class.java)
        je.shouldNotBeNull()
        val adjustment = RoundingAdjustment(Constants.QBO_SCALE,RoundingMode.HALF_UP)
        val companyCode = "N200"
        val docId = "DR2500700307"
        adjustment.execute(je).shouldBeEqual(true)
        logger.info { "ROUNDING-ADJUSTMENT[${companyCode}.${docId}]-DIFFERENCE[${adjustment.difference}]" }
    }

    @Test
    fun `RoundingAdjustment debit TEST`() {
        val jsonData = JsonHelper.readFromFile("journal-entry-adjust-debit.json", RoundingAdjustmentTest::class)
        val converter = JsonConverter()
        val je = converter.toObj(jsonData, JournalEntry::class.java)
        je.shouldNotBeNull()
        val adjustment = RoundingAdjustment(Constants.QBO_SCALE,RoundingMode.HALF_UP)
        val companyCode = "N200"
        val docId = "DR2500700307"
        adjustment.execute(je).shouldBeEqual(true)
        logger.info { "ROUNDING-ADJUSTMENT[${companyCode}.${docId}]-DIFFERENCE[${adjustment.difference}]" }
    }

//    @Test
//    fun `JournalEntry difference credit-debit TEST`() {
//        val jsonData = JsonHelper.readFromFile("journal-entry-adjust-credit.json", RoundingAdjustmentTest::class)
//        val converter = JsonConverter()
//        val je = converter.toObj(jsonData, JournalEntry::class.java)
//        je.shouldNotBeNull()
//        val adjustment = RoundingAdjustment(Constants.QBO_SCALE,RoundingMode.HALF_UP)
//        val companyCode = "N200"
//        val docId = "DR2500700307"
//        val exception = shouldThrow<ObjectOptimisticLockingFailureException> {
//            if(!adjustment.execute(je)) {
////                logger.error {
////                    "FAILED-ROUNDING-ADJUSTMENT[${companyCode}.${docId}]-" +
////                            "DEBIT(${adjustment.adjustedDebit()}) " +
////                            "does not equal " +
////                            "CREDIT(${adjustment.adjustedCredit()})" }
//            }
//
//            if(adjustment.hasDifference()) {
//                logger.info { "ROUNDING-ADJUSTMENT[${companyCode}.${docId}]-" +
//                        "DEBIT(${adjustment.debit()})-" +
//                        "CREDIT[${adjustment.credit()}]" }
//            }
//        }
//    }

}