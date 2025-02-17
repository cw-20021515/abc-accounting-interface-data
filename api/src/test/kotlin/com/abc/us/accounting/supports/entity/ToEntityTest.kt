package com.abc.us.accounting.supports.entity

import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity
data class TestEntity(
    @Id
    @IgnoreHash
    @Column(name = "id", nullable = false, updatable = false)
    val id: String? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "value", nullable = false)
    val value: Int,

    @IgnoreHash
    @Column(name = "description")
    val description: String? = null
)

@Entity
data class AnotherEntity(
    @Id
    @IgnoreHash
    @Column(name = "id", nullable = false, updatable = false)
    val id: String? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "value", nullable = false)
    val value: Int
)
class ToEntityTest : AnnotationSpec() {
    @Test
    fun `한개 저장 테스트`() {
        val orderItem =  CollectOrderItem(orderId = "test-orderId-id",
                                          orderItemId = "est-order-item-id",
                                          orderItemStatus = OrderItemStatus.ORDER_RECEIVED,
                                          orderItemType = OrderItemType.RENTAL).apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.simpleName
                field = "order_id"
                value = "order-item-id-001"
            }
            orderId = "order-id"
            orderItemId = "order-item-id-001"
        }
        orderItem.apply { hashCode = toEntityHash() }
        val hash1 = orderItem.hashCode

        orderItem.apply { hashCode = toEntityHash() }
        val hash2 = orderItem.hashCode
        hash1!!.shouldBeEqual(hash2!!)
    }

    @Test
    fun `create hash validation`() {
        val order01 = """
            {"createTime":"2024-11-01T07:59:15.628Z","updateTime":"2024-11-01T08:38:05.601Z","isActive":true,"relation":{"entity":"CollectOrder","field":"orderId","value":"0105430600018"},"orderId":"0105430600018","orderItemId":"0105430600018-0101","channelOrderId":"G-241101-000014","channelOrderItemId":"S-241101-000010","orderItemStatus":"CONTRACT_CONFIRMED","orderItemType":"RENTAL","materialId":"WP_113725","contractId":"0105430600018-0101-30000A","installId":"1YAL2oSwEqA","quantity":1}
            """
        val converter = JsonConverter()

        val orderItem1 = converter.toObj(order01,CollectOrderItem::class.java)


        orderItem1!!.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId"
            }
        }.apply { hashCode = toEntityHash() }

        val hash1 = orderItem1.hashCode

        val orderItem2 = converter.toObj(order01,CollectOrderItem::class.java)
        orderItem2!!.apply {
            relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.java.simpleName
                field="orderId"
                value = "orderId"
            }
        }.apply { hashCode = toEntityHash() }
        val hash2 = orderItem2.hashCode

        hash1!!.shouldBeEqual(hash2!!)
    }
}
//
//    "should generate correct hash for TestEntity" {
//        val entity1 = TestEntity(
//            id = "1",
//            name = "Entity1",
//            value = 42,
//            description = "Description1"
//        )
//
//        val entity2 = TestEntity(
//            id = "2",
//            name = "Entity1",
//            value = 42,
//            description = "Description2"
//        )
//        // Verify hash is independent of ignored fields
//        entity1.toEntityHash() shouldBe entity2.toEntityHash()
//    }
//
//    "should generate correct hash for AnotherEntity" {
//        val entity1 = AnotherEntity(
//            id = "1",
//            name = "Entity2",
//            value = 99
//        )
//
//        val entity2 = AnotherEntity(
//            id = "2",
//            name = "Entity2",
//            value = 99
//        )
//
//        // Verify hash for AnotherEntity
//        entity1.toEntityHash() shouldBe entity2.toEntityHash()
//    }
//
//    "should compare entities correctly" {
//        val entity1 = TestEntity(
//            id = "1", // @IgnoreHash에 의해 해시에 영향을 주지 않음
//            name = "Entity3",
//            value = 100,
//            description = "Description3" // @IgnoreHash에 의해 해시에 영향을 주지 않음
//        )
//
//        val entity2 = TestEntity(
//            id = "1", // @IgnoreHash에 의해 해시에 영향을 주지 않음
//            name = "Entity3",
//            value = 100,
//            description = "Description4" // @IgnoreHash에 의해 해시에 영향을 주지 않음
//        )
//
//        val differentEntity = AnotherEntity(
//            id = "1", // @IgnoreHash 적용 여부 확인
//            name = "Entity3",
//            value = 100
//        )
//
//        // 수정된 로직: description은 무시되고 나머지 속성이 동일하면 같다고 간주
//        entity1.compareEntity(entity2) shouldBe true
//        entity1.compareHash(entity2.toEntityHash()) shouldBe true
//        entity1.compareEntity(differentEntity) shouldBe false
//    }
//
//    "should generate unique entity ID" {
//        val entity = TestEntity(
//            id = null,
//            name = "UniqueEntity",
//            value = 123,
//            description = "Description"
//        )
//
//        val entityId = entity.toEntityId()
//        entityId.startsWith("TE") shouldBe true // 'TE' prefix from `TestEntity`
//    }
//
//    "should handle null hashCode property" {
//        val entity = TestEntity(
//            id = null,
//            name = "EntityWithNullHash",
//            value = 0,
//            description = null
//        )
//
//        val hashCode = entity.toEntityHash()
//        hashCode.isNotEmpty() shouldBe true
//    }
//
//    "toEntityID not equal test" {
//        val order1 = CollectOrder().apply {
//            orderId = "orderId-1"
//            customerId = "customerId"
//            channelOrderId = "channelOrderId"
//        }.apply { hashCode = toEntityHash()}
//
//        val order2 = CollectOrder().apply {
//            orderId = "orderId"
//            customerId = "customerId"
//            channelOrderId = "channelOrderId"
//        }.apply { hashCode = toEntityHash()}
//
//        order1.hashCode.equals(order2.hashCode) shouldBeEqual(false)
//
//        order1.toEntityId().equals(order2.toEntityId()) shouldBeEqual(false)
//        order1.compareEntity(order2) shouldBeEqual(false)
//    }
//})