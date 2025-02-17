package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * 전체 금액을 n개의 랜덤한 금액으로 분배 (BigDecimal 사용)
 */
class MoneyDistributorTests (
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec(){

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `single distribute test`() {
        val scale = Constants.ACCOUNTING_SCALE
        val roundingMode = RoundingMode.valueOf(Constants.MATH_ROUNDING_MODE)

        val totalAmount = BigDecimal(1000).setScale(scale, roundingMode)
        val count = 5

        val amounts = MoneyDistributor.distribute(totalAmount, count, scale, roundingMode)
        logger.info ("amounts:$amounts")
        amounts.size shouldBe count

        val sum = amounts.reduce { acc, bigDecimal -> acc + bigDecimal }
        sum shouldBe totalAmount
    }

    fun randomBigDecimalInRange(min: BigDecimal, max: BigDecimal): BigDecimal {
        val range = max.subtract(min)
        val randomFactor = Math.random()
        return min.add(range.multiply(BigDecimal.valueOf(randomFactor)))
    }

    @Test
    fun `multiple distribute test`() {
        val scale = Constants.ACCOUNTING_SCALE
        val roundingMode = RoundingMode.valueOf(Constants.MATH_ROUNDING_MODE)

        val repeatCount = 1000

        timeLogger.measureAndLog {
            for (i in 1..repeatCount) {
                val count = Random.nextInt(1, 10)

                val totalAmount = randomBigDecimalInRange(BigDecimal(0), BigDecimal(10000)).setScale(scale, roundingMode)
                val amounts = MoneyDistributor.distribute(totalAmount, count, scale, roundingMode)
                amounts.size shouldBe count

                val sum = amounts.reduce { acc, bigDecimal -> acc + bigDecimal }
                sum shouldBe totalAmount
            }
        }
    }
}