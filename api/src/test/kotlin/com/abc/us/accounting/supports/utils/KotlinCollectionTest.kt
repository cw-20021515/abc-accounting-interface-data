package com.abc.us.accounting.supports.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

class KotlinCollectionTest
    : AnnotationSpec(){

    @Test
    fun `collection empty test`() {
        val list = listOf<String>()
        list.isEmpty() shouldBe true
        list.isNotEmpty() shouldBe false


        val exception1 = shouldThrow<NoSuchElementException> {
            list.first { it == "1" }
        }
        exception1.message shouldBe "Collection contains no element matching the predicate."

        val nullableValue:String? =  if (list.isEmpty())  null else list.first { it == "1" }
        nullableValue shouldBe null

        val nullOrValue = list.firstOrNull { it == "1" }
        nullOrValue shouldBe null

        logger.info("empty test complete")
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

