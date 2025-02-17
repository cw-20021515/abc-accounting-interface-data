package com.abc.us.accounting.supports.utils

import com.abc.us.accounting.supports.utils.StringRange
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

class TestStringRange
    : AnnotationSpec(){
    @Test
    fun `easy case`() {
        val stringRange = StringRange("1", "2")
        stringRange.contains("2") shouldBe true
        stringRange.contains("1") shouldBe true

        stringRange.contains("3") shouldBe false
        logger.info("normal case complete")
    }

    @Test
    fun `code range case`() {

        val stringRange = StringRange("1123000", "1123999")
        stringRange.contains("1123100") shouldBe true
        stringRange.contains("1123999") shouldBe true

        stringRange.contains("1122000") shouldBe false
        stringRange.contains("1124000") shouldBe false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

