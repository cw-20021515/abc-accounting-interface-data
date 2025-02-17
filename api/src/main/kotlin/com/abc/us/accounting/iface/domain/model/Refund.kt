package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.iface.domain.entity.oms.IfOrderItem
import com.abc.us.accounting.supports.utils.buildToString
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mu.KotlinLogging
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class Price {
    var currency: String = ""
    var totalPrice: BigDecimal = BigDecimal.ZERO

    constructor()

    constructor(
        currency: String,
        totalPrice: BigDecimal
    ) {
        this.currency = currency
        this.totalPrice = totalPrice
    }


    override fun toString(): String {
        return buildToString {
            add(
                "currency" to currency,
                "totalPrice" to totalPrice
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Price) return false

        return EqualsBuilder()
            .append(currency, other.currency)
            .append(totalPrice, other.totalPrice)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(currency)
            .append(totalPrice)
            .toHashCode()
    }
}

enum class RefundKind {
    VOID,
    REFUND,
    ;

    @JsonValue
    fun getValue(): String = name
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Refund {
    var transactionId: String = ""
    var kind: RefundKind = RefundKind.VOID
    var paymentTime: OffsetDateTime = OffsetDateTime.now()
    var orderItemId: String = ""
    var price: Price = Price()
    var acquirerReferenceNumber: String = ""

    constructor()

    constructor(
        transactionId: String,
        kind: RefundKind,
        paymentTime: OffsetDateTime,
        orderItemId: String,
        price: Price,
        acquirerReferenceNumber: String
    ) {
        this.transactionId = transactionId
        this.kind = kind
        this.paymentTime = paymentTime
        this.orderItemId = orderItemId
        this.price = price
        this.acquirerReferenceNumber = acquirerReferenceNumber
    }



    override fun toString(): String {
        return buildToString {
            add(
                "transactionId" to transactionId,
                "kind" to kind,
                "paymentTime" to paymentTime,
                "orderItemId" to orderItemId,
                "price" to price,
            )
        }
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Refund) return false

        return EqualsBuilder()
            .append(transactionId, other.transactionId)
            .append(kind, other.kind)
            .append(paymentTime, other.paymentTime)
            .append(orderItemId, other.orderItemId)
            .append(price, other.price)
            .append(acquirerReferenceNumber, other.acquirerReferenceNumber)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(transactionId)
            .append(kind.name)
            .append(paymentTime)
            .append(orderItemId)
            .append(price)
            .append(acquirerReferenceNumber)
            .toHashCode()
    }

    companion object{
        fun parse(json:String):Refund{
            val objectMapper = ObjectMapper().apply {
                registerModule(JavaTimeModule())
                // 알 수 없는 속성 무시
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // null 값에 대한 처리
                configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            }
            return objectMapper.readValue(json, object : TypeReference<Refund>() {})
        }
    }
}

@Converter
class RefundListConverter : AttributeConverter<List<Refund>?, String> {
    private val objectMapper = ObjectMapper().apply {
        // ISO-8601 날짜 형식 지원을 위한 JavaTimeModule 등록
        registerModule(JavaTimeModule())

        // 알 수 없는 속성 무시
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // null 값에 대한 처리
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    }

    override fun convertToDatabaseColumn(attribute: List<Refund>?): String {
        return try {
            if (attribute == null || attribute.isEmpty()) {
                "[]"
            } else {
                objectMapper.writeValueAsString(attribute)
            }
        } catch (e: Exception) {
            "[]"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Refund>? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return try {
            objectMapper.readValue(dbData, object : TypeReference<List<Refund>>() {})
        } catch (e: Exception) {
            logger.warn(e) { "Error parsing refund list" }
            null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
