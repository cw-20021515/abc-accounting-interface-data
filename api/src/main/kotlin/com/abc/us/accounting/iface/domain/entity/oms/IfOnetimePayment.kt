package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.iface.domain.model.*
import com.abc.us.accounting.iface.domain.type.oms.IfPaymentMethod
import com.abc.us.accounting.iface.domain.type.oms.IfTransactionType
import com.abc.us.accounting.supports.utils.buildToString
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

/**
 * 일시불 결제정보 테이블
 */
@Entity
@Table(name = "if_onetime_payment")
@Comment("일시불 결제정보")
class IfOnetimePayment(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "payment_id")
    val paymentId: String,

    @Comment("거래유형(Shopify Payments의 거래유형(결제))")
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val transactionType: IfTransactionType,

    @Comment("주문ID")
    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Comment("채널 거래ID")
    @Column(name = "transaction_id", nullable = false)
    val transactionId: String,

    @Comment("결제수단")
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    val paymentMethod: IfPaymentMethod,

    @Comment("결제시간")
    @Column(name = "payment_time")
    val paymentTime: OffsetDateTime,

    @Comment("거래통화")
    @Column(name = "currency", nullable = false)
    val currency: String,

    @Comment("결제금액(세금포함)")
    @Column(name = "total_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val totalPrice: BigDecimal,

    @Comment("판매세")
    @Column(name = "tax", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val tax: BigDecimal,

    @Comment("결제금액(세금미포함)")
    @Column(name = "subtotal_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val subtotalPrice: BigDecimal,

    @Comment("개별상품가격")
    @Column(name = "item_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val itemPrice: BigDecimal,

    @Comment("할인가격")
    @Column(name = "discount_price", nullable = false, precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val discountPrice: BigDecimal,

    @Comment("선결제금액")
    @Column(name = "prepaid_amount", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val prepaidAmount: BigDecimal? = null,

    @Comment("등록비")
    @Column(name = "registration_price", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)
    val registrationPrice: BigDecimal? = null,

    @Comment("프로모션 정보")
    @Type(JsonType::class)
    @Column(name = "promotions", columnDefinition = "jsonb")
    val promotions: MutableMap<String,Any> = mutableMapOf(),

    @Comment("판매세 정보")
    @Convert(converter = TaxLinesConverter::class)
    @Column(name = "tax_lines", columnDefinition = "json")
    val taxLines: List<TaxLine>? = null,

    @Comment("주소정보")
    @Convert(converter = PaymentAddressConverter::class)
    @Column(name = "address", columnDefinition = "json")
    val address: PaymentAddress? = null,

    @Comment("취소/환불정보")
    @Convert(converter = RefundListConverter::class)
    @Column(name = "refund", columnDefinition = "json")
    val refunds: List<Refund>? = null,

    @Comment("수정시간")
    @Column(name = "update_time")
    val updateTime: OffsetDateTime
) {

    override fun toString(): String {
        return buildToString {
            add(
                "id" to id,
                "paymentId" to paymentId,
                "transactionType" to transactionType,
                "orderId" to orderId,
                "transactionId" to transactionId,
                "paymentMethod" to paymentMethod,
                "refunds" to refunds,
                "paymentTime" to paymentTime,
                "updateTime" to updateTime
            )
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is IfOnetimePayment) return false

        return EqualsBuilder()
            .append(paymentId, other.paymentId)
            .append(transactionType, other.transactionType)
            .append(paymentMethod, other.paymentMethod)
            .append(orderId, other.orderId)
            .append(transactionId, other.transactionId)
            .append(refunds, refunds)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(paymentId)
            .append(transactionType.name)
            .append(paymentMethod.name)
            .append(orderId)
            .append(transactionId)
            .append(refunds)
            .toHashCode()
    }

    companion object {
        fun parse(json: String): IfOnetimePayment {
            val objectMapper = ObjectMapper().apply {
                registerModule(JavaTimeModule())
                // 알 수 없는 속성 무시
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // null 값에 대한 처리
                configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)


                registerModule(SimpleModule().apply {
                    addDeserializer(OffsetDateTime::class.java, object : JsonDeserializer<OffsetDateTime>() {
                        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OffsetDateTime {
                            val dateStr = p.valueAsString

                            try {
                                // ISO-8601 형식으로 직접 파싱 시도
                                return if (dateStr.endsWith("Z")) {
                                    val instant = Instant.parse(dateStr)
                                    instant.atOffset(ZoneOffset.UTC)
                                } else {
                                    OffsetDateTime.parse(dateStr)
                                }
                            } catch (e: DateTimeParseException) {
                                // ISO-8601 파싱 실패 시 커스텀 포맷 시도
                                val formatter = DateTimeFormatterBuilder()
                                    .appendPattern("yyyy-MM-dd[ ]['T']HH:mm:ss")
                                    .optionalStart()
                                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                                    .optionalEnd()
                                    .optionalStart()
                                    .appendOffset("+HH:MM", "Z")
                                    .optionalEnd()
                                    .toFormatter()

                                try {
                                    val temporal = formatter.parseBest(
                                        dateStr,
                                        OffsetDateTime::from,
                                        LocalDateTime::from
                                    )

                                    return when (temporal) {
                                        is OffsetDateTime -> temporal
                                        is LocalDateTime -> temporal.atOffset(ZoneOffset.UTC)
                                        else -> throw DateTimeParseException(
                                            "Unable to parse date: $dateStr",
                                            dateStr,
                                            0
                                        )
                                    }
                                } catch (e2: DateTimeParseException) {
                                    println("Failed to parse date: $dateStr")
                                    throw e2
                                }
                            }
                        }
                    })
                })
            }
            return objectMapper.readValue(json, object : TypeReference<IfOnetimePayment>() {})
        }
    }
}
