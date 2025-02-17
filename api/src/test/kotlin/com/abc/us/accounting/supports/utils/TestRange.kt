package com.abc.us.accounting.supports.utils

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class TestRange
    : AnnotationSpec(){

    @Test
    fun `test string range`() {
        run {
            // 전체 성공
            val range = Range<String> ()
            range.isInRange("") shouldBe true
            range.isInRange("1124000") shouldBe true
        }

        run {
            val range = Range<String> (from = "1124000")
            range.isInRange("") shouldBe false
            range.isInRange("1123000") shouldBe false
            range.isInRange("1124000") shouldBe true
            range.isInRange("1124010") shouldBe true
        }

        run {
            val range = Range<String> (to = "1124000")
            range.isInRange("") shouldBe true
            range.isInRange("1123000") shouldBe true
            range.isInRange("1124000") shouldBe true
            range.isInRange("1124010") shouldBe false
        }


        run {
            val range = Range<String> (from="1123000", to = "1123999")
            range.isInRange("1123100") shouldBe true
            range.isInRange("1123999") shouldBe true
            range.isInRange("1122000") shouldBe false
            range.isInRange("1124000") shouldBe false
        }

    }

    @Test
    fun `test int range `() {
        run {
            // 전체 성공
            val range = Range<Int> ()
            range.isInRange(0) shouldBe true
            range.isInRange(1) shouldBe true
            range.isInRange(-1) shouldBe true
        }

        run {
            val range = Range<Int> (from = 10)
            range.isInRange(0) shouldBe false
            range.isInRange(9) shouldBe false
            range.isInRange(10) shouldBe true
            range.isInRange(20) shouldBe true
        }

        run {
            val range = Range<Int> (to = 100000)
            range.isInRange(0) shouldBe true
            range.isInRange(100) shouldBe true
            range.isInRange(100000) shouldBe true
            range.isInRange(200000) shouldBe false
        }


        run {
            val range = Range<Int> (from=10, to = 20)
            range.isInRange(0) shouldBe false
            range.isInRange(9) shouldBe false
            range.isInRange(10) shouldBe true
            range.isInRange(15) shouldBe true
            range.isInRange(20) shouldBe true
            range.isInRange(21) shouldBe false
            range.isInRange(30) shouldBe false
        }
    }


    @Test
    fun `test bigdecimal range `() {
        run {
            // 전체 성공
            val range = Range<BigDecimal> ()
            range.isInRange(BigDecimal.ZERO) shouldBe true
            range.isInRange(BigDecimal("-10.0")) shouldBe true
            range.isInRange(BigDecimal("10")) shouldBe true
        }

        run {
            val range = Range<BigDecimal> (from = BigDecimal(10.0))
            range.isInRange(BigDecimal(0)) shouldBe false
            range.isInRange(BigDecimal(9)) shouldBe false
            range.isInRange(BigDecimal(10)) shouldBe true
            range.isInRange(BigDecimal(10.00000)) shouldBe true
            range.isInRange(BigDecimal(20)) shouldBe true
        }

        run {
            val range = Range<BigDecimal> (to = BigDecimal(1000))
            range.isInRange(BigDecimal(0)) shouldBe true
            range.isInRange(BigDecimal(100)) shouldBe true
            range.isInRange(BigDecimal(1000)) shouldBe true
            range.isInRange(BigDecimal(1000.000)) shouldBe true
            range.isInRange(BigDecimal(2000)) shouldBe false
        }


        run {
            val range = Range<BigDecimal> (from=BigDecimal(10), to = BigDecimal(20))
            range.isInRange(BigDecimal(0)) shouldBe false
            range.isInRange(BigDecimal(9)) shouldBe false
            range.isInRange(BigDecimal(10)) shouldBe true
            range.isInRange(BigDecimal(15)) shouldBe true
            range.isInRange(BigDecimal(19.99)) shouldBe true
            range.isInRange(BigDecimal(20)) shouldBe true
            range.isInRange(BigDecimal(20.00)) shouldBe true
            range.isInRange(BigDecimal(20.01)) shouldBe false
            range.isInRange(BigDecimal(21)) shouldBe false
            range.isInRange(BigDecimal(30)) shouldBe false
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

