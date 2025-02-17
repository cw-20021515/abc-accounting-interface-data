package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.supports.utils.buildToString
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mu.KotlinLogging
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class TaxLine {
    var title: String = ""
    var rate: BigDecimal = BigDecimal.ZERO
    var price: BigDecimal = BigDecimal.ZERO

    constructor()

    constructor(
        title: String,
        rate: BigDecimal,
        price: BigDecimal
    ) {
        this.title = title
        this.rate = rate
        this.price = price
    }

    fun getTaxLineType():SalesTaxType {
        return when {
            title.contains("State", ignoreCase = true) -> SalesTaxType.STATE
            title.contains("County", ignoreCase = true) -> SalesTaxType.COUNTY
            title.contains("City", ignoreCase = true) -> SalesTaxType.CITY
            else -> SalesTaxType.SPECIAL
        }
    }

    override fun toString(): String {
        return buildToString {
            add(
                "title" to title,
                "rate" to rate,
                "price" to price
            )
        }
    }
}

enum class SalesTaxType {
    STATE,
    COUNTY,
    CITY,
    SPECIAL
}


@Converter
class TaxLinesConverter : AttributeConverter<List<TaxLine>?, String> {
    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    }

    override fun convertToDatabaseColumn(attribute: List<TaxLine>?): String {
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            "[]"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<TaxLine>? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return try {
            when {
                dbData == "null" -> null
                dbData == "[]" -> null
                dbData == "{}" -> null
                else -> objectMapper.readValue(dbData, object : TypeReference<List<TaxLine>>() {})
            }
        } catch (e: Exception) {
            try {
                val singleTaxLine = objectMapper.readValue(dbData, TaxLine::class.java)
                listOf(singleTaxLine)
            } catch (e: Exception) {
                logger.warn(e) { "Error parsing TaxLine" }
                emptyList()
            }
        }
    }
    companion object {
        private val logger = KotlinLogging.logger {}
    }
}