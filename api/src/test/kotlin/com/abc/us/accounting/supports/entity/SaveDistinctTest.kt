package com.abc.us.accounting.supports.entity

import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.repository.CollectOrderItemRepository
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.utils.Hashs
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

//
@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SaveDistinctTest @Autowired constructor(
    private val orderItemRepository: CollectOrderItemRepository
) : AnnotationSpec() {

    private lateinit var saver: SaveDistinct<CollectOrderItem>

    @BeforeEach
    fun setUp() {
        saver = SaveDistinct(orderItemRepository)
    }

    fun jsonToEntity() : CollectOrderItem {
        val converter = JsonConverter()
        val order01 = """
            {
                "createTime":"2024-11-01T07:59:15.628Z",
                "updateTime":"2024-11-01T08:38:05.601Z",
                "isActive":true,
                "relation":{
                    "entity":"CollectOrder",
                    "field":"orderId",
                    "value":"0105430600018"
                },
                "orderId":"0105430600018",
                "orderItemId":"0105430600018-0101",
                "channelOrderId":"G-241101-000014",
                "channelOrderItemId":"S-241101-000010",
                "orderItemStatus":"CONTRACT_CONFIRMED",
                "orderItemType":"RENTAL",
                "materialId":"WP_113725",
                "contractId":"0105430600018-0101-30000A",
                "installId":"1YAL2oSwEqA",
                "quantity":1
            }
            """
        return converter.toObj(order01, CollectOrderItem::class.java)!!
    }
    @Test
    fun `동일 데이터 해시 일관성 유지 테스트`() {
        val orderItem1 =jsonToEntity()
        orderItem1.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId"
            }
        }

        val results = saver.execute(mutableListOf(orderItem1))
        var compHash = orderItem1.toEntityHash()
        orderItem1.hashCode!!.shouldBeEqual(compHash)

        results shouldHaveSize 1
        results[0].hashCode.shouldNotBeNull()
        compHash = orderItem1.toEntityHash()
        results[0].hashCode!!.shouldBeEqual(compHash)

        compHash = orderItem1.toEntityHash()
        val saves = saver.findByHashCodes(mutableListOf(compHash))

        compHash = orderItem1.toEntityHash()
        saves shouldHaveSize 1
        saves[0].hashCode.shouldNotBeNull()
        saves[0].hashCode!!.shouldBeEqual(compHash)
    }
    @Test
    fun `동일 해시 중복 제거 저장 테스트`() {
        val orderItem1 =jsonToEntity()
        orderItem1.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId-001"
            }
        }
        var results = saver.execute(mutableListOf(orderItem1))
        results.shouldNotBeEmpty()
        results[0].hashCode.shouldNotBeNull()
        results[0].hashCode!!.shouldBeEqual(orderItem1.hashCode!!)

        // hash code 무효화 및 저장
        // 저장시 새로 계산된 hashCode 는 직전의 데이터와 동일해야 한다.
        orderItem1.hashCode=null
        orderItem1.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId-001"
            }
        }
        saver.execute(mutableListOf(orderItem1))
        val saves = saver.findByHashCodes(mutableListOf(orderItem1.hashCode!!))
        saves.shouldNotBeEmpty()
        saves[0].hashCode.shouldNotBeNull()
        saves[0].hashCode!!.shouldBeEqual(orderItem1.toEntityHash())
    }

    @Test
    fun `다른 해시 데이터 저장 및 검색 이상 유무 테스트`() {
        val orderItem =jsonToEntity()
        orderItem.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId-002"
            }
        }

        val builder1 = StringBuilder()
        val input1 = builder1
            .append(CollectOrder::class.java.simpleName).append("|")
            .append("orderId").append("|")
            .append("orderId-002")
            .toString()
        val hashCode1 = Hashs.sha256Hash(input1)

        val builder2 = StringBuilder()
        val input2 = builder2
            .append(CollectOrder::class.java.simpleName).append("|")
            .append("orderId").append("|")
            .append("orderId-003")
            .toString()
        val hashCode2 = Hashs.sha256Hash(input2)

        hashCode1.equals(hashCode2).shouldBeEqual(false)

        val hashCodes = mutableSetOf<String>()
        saver.execute(mutableListOf(orderItem))
        hashCodes.add(orderItem.hashCode!!)
        orderItem.hashCode = null
        orderItem.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId-004"
            }
        }
        saver.execute(mutableListOf(orderItem))
        hashCodes.contains(orderItem.hashCode!!).shouldBeEqual(false)
        hashCodes.add(orderItem.hashCode!!)
        hashCodes shouldHaveSize 2

        var results = saver.findByHashCodes(hashCodes.toMutableList())
        results shouldHaveSize 2

        hashCodes.contains(results[0].hashCode!!).shouldBeEqual(true)
        hashCodes.contains(results[1].hashCode!!).shouldBeEqual(true)
    }
}