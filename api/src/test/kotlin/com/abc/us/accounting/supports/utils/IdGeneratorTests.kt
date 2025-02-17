package com.abc.us.accounting.supports.utils

import com.github.f4b6a3.tsid.Tsid
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import kotlin.test.fail

class IdGeneratorTests(
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec(){

    @Test
    fun `생성된 ID는 시간순으로 정렬되어야 한다`() {
        val ids = (1..2000).map {
            val id = IdGenerator.generateId()
            id
        }

        val sortedIds = ids.sorted()

        ids shouldBe sortedIds
    }

    @Test
    fun `ID는 유니크해야 한다`() {
        val ids = (1..2000).map {
            val id = IdGenerator.generateId()
//            logger.debug("idx:$it, id:$id")
            id
        }
        val uniqueIds = ids.toSet()

        ids.size shouldBe uniqueIds.size
    }

    @Test
    fun `ID는 숫자로 변환 가능해야 한다`() {
        val ids = (1..2000).map {
            val id = IdGenerator.generateNumericId()
//            logger.info("idx:$it, id:$id")
            id
        }
        val uniqueIds = ids.toSet()
        ids.size shouldBe uniqueIds.size
    }


    /**
     * 비동기로 여러개의 id 값을 생성하는 테스트
     */
    @Test
    fun `ID는 동시 부하 테스트 상황에서도 유일해야 한다`() = runBlocking {
        val concurrentRequests = 2000

        val results = mutableSetOf<Long>()
        timeLogger.measureAndLog {
            withContext(Dispatchers.IO.limitedParallelism(50)) {
                val values = (1..concurrentRequests).map {
                    async {
                        try {
                            val value = IdGenerator.generateNumericId()
                            value
                        } catch (e: Exception) {
                            fail("Error while getting sequence value", e)
                        }
                    }
                }.awaitAll().sorted()
                results.addAll(values)
            }

            val uniqueIds = results.toSet()

            logger.info("size:${results.size}, uniqueIds: ${uniqueIds.size}")
            uniqueIds.size shouldBe concurrentRequests
        }

    }

    @Test
    fun `ID는 클래스와 내용을 포함해야 한다`() {
        val clazz = IdGeneratorTests::class
        val contents = "test"

        val id = IdGenerator.generateId(clazz, contents)
        val seed = LocalDate.now().toString().substringBeforeLast("-")
        val uuid = UUID.nameUUIDFromBytes("$seed$contents".toByteArray())
        val mostSignificantBits = uuid.leastSignificantBits // 양수로 변환
        val prefix = clazz.java.simpleName.filter { it.isUpperCase() }

        prefix shouldBe "IGT"
        id shouldBe "$prefix-${Tsid.from(mostSignificantBits)}"
        logger.info("ID: $id")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

