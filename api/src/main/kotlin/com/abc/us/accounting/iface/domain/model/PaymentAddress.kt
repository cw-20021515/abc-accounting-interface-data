package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.supports.utils.buildToString
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import mu.KotlinLogging


@JsonIgnoreProperties(ignoreUnknown = true)
class PaymentAddress {
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var phone: String = ""
    var zipcode: String = ""
    var state: String = ""
    var city: String = ""
    var address1: String = ""
    var address2: String? = null

    constructor()

    constructor(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        zipcode: String,
        state: String,
        city: String,
        address1: String,
        address2: String?,
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.phone = phone
        this.zipcode = zipcode
        this.state = state
        this.city = city
        this.address1 = address1
        this.address2 = address2
    }

    override fun toString(): String {
        return buildToString {
            add(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phone" to phone,
                "zipcode" to zipcode,
                "state" to state,
                "city" to city,
                "address1" to address1,
                "address2" to address2,
            )
        }
    }
}

@Converter
class PaymentAddressConverter : AttributeConverter<PaymentAddress?, String> {
    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    }

    override fun convertToDatabaseColumn(attribute: PaymentAddress?): String? {
        return try {
            if (attribute == null) return null
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            "{}"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): PaymentAddress? {
        return try {
            when {
                dbData == null -> null
                dbData.isBlank() -> null
                dbData == "null" -> null
                dbData == "[]" -> null
                dbData == "{}" -> null
                else -> objectMapper.readValue(dbData, object : TypeReference<PaymentAddress>() {})
            }
        } catch (e: Exception) {
            logger.warn(e) { "Error parsing PaymentAddress" }
            null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}