package com.abc.us.accounting.rentals.master.domain

import com.abc.us.accounting.rentals.master.domain.entity.Distribution
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

class DistributionTests (
) : FunSpec({

                test("distribution test") {
                    val value = Distribution.of(BigDecimal(1180.00), null, null, null, BigDecimal(600.00))

                    val ratio = value.toRatio()

                    logger.info("value:${value}, ratio:${ratio}")
                    val expected = BigDecimal("0.6629")
                    ratio.m01.compareTo(expected) shouldBe 0
                    ratio.r01 shouldBe null
                    ratio.r02 shouldBe null
                    ratio.r03 shouldBe null
                    ratio.s01!!.compareTo(BigDecimal("0.3371")) shouldBe 0
                    ratio.total.compareTo(BigDecimal(1)) shouldBe 0

                    val price = ratio.toRentalPrice(BigDecimal(65))
                    logger.info("ratio:${ratio}, price:${price}")

                    price.m01.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("43.09")) shouldBe 0
                    price.r01 shouldBe null
                    price.r02 shouldBe null
                    price.r03 shouldBe null
                    price.s01!!.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("21.91")) shouldBe 0
                    price.total.compareTo(BigDecimal(65)) shouldBe 0
                }

            }) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
