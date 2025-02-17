package com.abc.us.accounting.iface.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOmsBillingInvoiceCharge {
    var chargeId: String = ""
    var billingCycle: Int = 0
    var targetMonth: String = ""
    var totalPrice: BigDecimal = BigDecimal.ZERO
    var chargeItems: List<IfOmsBillingChargeItem> = mutableListOf()
    var createTime: OffsetDateTime = OffsetDateTime.now()
    var updateTime: OffsetDateTime = OffsetDateTime.now()
    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
}

@Converter
class IfOmsBillingInvoiceChargeConverter: AttributeConverter<List<IfOmsBillingInvoiceCharge>, String> {
    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    }.registerModule(JavaTimeModule())

    override fun convertToDatabaseColumn(attribute: List<IfOmsBillingInvoiceCharge>): String {
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            "[]"
        }
    }

    override fun convertToEntityAttribute(dbData: String): List<IfOmsBillingInvoiceCharge> {
        return try {
            objectMapper.readValue(dbData, object : TypeReference<List<IfOmsBillingInvoiceCharge>>() {})
        } catch (e: Exception) {
            try {
                val single = objectMapper.readValue(dbData, IfOmsBillingInvoiceCharge::class.java)
                listOf(single)
            } catch (e: Exception) {
                logger.warn(e) { "Error parsing IfOmsBillingInvoiceCharge" }
                emptyList()
            }
        }
    }


    companion object {
        private val logger = KotlinLogging.logger {}
    }
}