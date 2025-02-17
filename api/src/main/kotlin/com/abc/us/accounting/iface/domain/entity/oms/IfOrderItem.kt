package com.abc.us.accounting.iface.domain.entity.oms


import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.iface.domain.model.*
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemStatus
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemType
import com.abc.us.accounting.iface.domain.type.oms.IfOrderProductType
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.abc.us.accounting.supports.utils.buildToString
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import kotlin.jvm.Transient

/**
 * 주문 정보 테이블
 */
@Entity
@Table(name = "if_order_item")
@Comment("주문 정보")
class IfOrderItem(
    @Id
    @Comment("ID")
    @Column(name = "id")
    val id: String,           // 회사 코드

    @Comment("주문항목ID")
    @Column(name = "order_item_id")
    val orderItemId: String,                // 계정 코드

    @Comment("주문항목상태")
    @Column(name = "order_item_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val orderItemStatus: IfOrderItemStatus,

    @Comment("직전주문항목상태")
    @Column(name = "last_order_item_status")
    @Enumerated(EnumType.STRING)
    val lastOrderItemStatus: IfOrderItemStatus? = null,

    @Comment("주문 상품 유형(설치/배송 구분)")
    @Column(name = "order_product_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val orderProductType: IfOrderProductType,

    @Comment("주문항목유형(일시불/렌탈 구분)")
    @Column(name = "order_item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val orderItemType: IfOrderItemType,

    @Comment("주문번호")
    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Comment("채널ID")
    @Column(name = "channel_id", nullable = false)
    val channelId: String,

    @Comment("고객ID")
    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Comment("레퍼럴코드")
    @Column(name = "referrer_code")
    val referrerCode: String? = null,

    @Comment("계약ID(렌탈 주문만 해당)")
    @Column(name = "contract_id")
    val contractId: String? = null,

    @Comment("자재ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("수량")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("주소정보")
    @Convert(converter = OrderAddressConverter::class)
    @Column(name = "address", nullable = false, columnDefinition = "json")
    val address: OrderAddress,

    @Comment("판매세")
    @Column(name = "tax", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val tax: BigDecimal,

    @Comment("판매세 정보")
    @Convert(converter = TaxLinesConverter::class)
    @Column(name = "tax_lines", columnDefinition = "json")
    val taxLines: List<TaxLine>?,

    @Comment("결제금액(세금미포함)")
    @Column(name = "subtotal_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val subtotalPrice: BigDecimal,

    @Comment("개별상품가격")
    @Column(name = "item_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val itemPrice: BigDecimal,

    @Comment("할인가격")
    @Column(name = "discount_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val discountPrice: BigDecimal,

    @Comment("등록비")
    @Column(name = "registration_price", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val registrationPrice: BigDecimal? = null,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime,

    @IgnoreHash
    @Transient
    var channel: IfChannel? = null,
) {

    override fun toString(): String {
        return buildToString {
            add(
                "orderItemId" to orderItemId,
                "orderItemStatus" to orderItemStatus,
                "lastOrderItemStatus" to lastOrderItemStatus,
                "createTime" to createTime,
                "updateTime" to updateTime
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is IfOrderItem) return false

        return EqualsBuilder()
            .append(orderItemId, other.orderItemId)
            .append(orderItemStatus, other.orderItemStatus)
            .append(lastOrderItemStatus, other.lastOrderItemStatus)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(orderItemId)
            .append(orderItemStatus.name)
            .append(lastOrderItemStatus?.name)
            .toHashCode()
    }

    companion object{
        fun parse (json:String):IfOrderItem{
            val objectMapper = ObjectMapper().apply {
                registerModule(JavaTimeModule())
                // 알 수 없는 속성 무시
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // null 값에 대한 처리
                configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

                // DateTime 모듈에 커스텀 Deserializer 추가
                registerModule(SimpleModule().apply {
                    addDeserializer(OffsetDateTime::class.java, object : JsonDeserializer<OffsetDateTime>() {
                        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OffsetDateTime {
                            val dateStr = p.valueAsString
                            val formatter = DateTimeFormatterBuilder()
                                .appendPattern("yyyy-MM-dd HH:mm:ss")
                                .optionalStart()
                                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                                .optionalEnd()
                                .optionalStart()
                                .appendZoneOrOffsetId()
                                .optionalEnd()
                                .toFormatter()

                            return try {
                                if (dateStr.endsWith("Z")) {
                                    LocalDateTime.parse(dateStr.removeSuffix("Z"), formatter)
                                        .atOffset(ZoneOffset.UTC)
                                } else {
                                    LocalDateTime.parse(dateStr, formatter)
                                        .atOffset(ZoneOffset.UTC)
                                }
                            } catch (e: DateTimeParseException) {
                                println("Failed to parse date: $dateStr")
                                throw e
                            }
                        }
                    })
                })
            }
            return objectMapper.readValue(json, object : TypeReference<IfOrderItem>() {})
        }
    }
}

