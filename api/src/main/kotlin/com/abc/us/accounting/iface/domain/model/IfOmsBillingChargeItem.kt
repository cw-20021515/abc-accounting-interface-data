package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.iface.domain.type.oms.IfChargeItemType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOmsBillingChargeItem {
    var chargeItemType: IfChargeItemType = IfChargeItemType.entries.first()
    var totalPrice: BigDecimal = BigDecimal.ZERO
    var priceDetail: IfOmsChargePriceDetail = IfOmsChargePriceDetail()
    @JsonDeserialize(using = CustomBooleanDeserializer::class)
    var isExcluded: Boolean? = null
    var materialId: String? = null
    var quantity: Int? = null
    @JsonDeserialize(using = CustomBooleanDeserializer::class)
    var isTaxExempt: Boolean? = null
    var remark: String? = null
    var serviceFlowId: String? = null
    var createTime: OffsetDateTime? = null
    var chargeItemId: String = ""
}

class CustomBooleanDeserializer: JsonDeserializer<Boolean?>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Boolean? {
        val text = p?.text?.trim()
        return when (text) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}

@Converter
class IfOmsBillingChargeItemConverter: AttributeConverter<List<IfOmsBillingChargeItem>, String> {
    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    }.registerModule(JavaTimeModule())

    override fun convertToDatabaseColumn(attribute: List<IfOmsBillingChargeItem>): String {
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            "[]"
        }
    }

    override fun convertToEntityAttribute(dbData: String): List<IfOmsBillingChargeItem> {
        return try {
            objectMapper.readValue(dbData, object : TypeReference<List<IfOmsBillingChargeItem>>() {})
        } catch (e: Exception) {
            try {
                val single = objectMapper.readValue(dbData, IfOmsBillingChargeItem::class.java)
                listOf(single)
            } catch (e: Exception) {
                logger.warn(e) { "Error parsing IfOmsBillingChargeItem" }
                emptyList()
            }
        }
    }


    companion object {
        private val logger = KotlinLogging.logger {}
    }
}