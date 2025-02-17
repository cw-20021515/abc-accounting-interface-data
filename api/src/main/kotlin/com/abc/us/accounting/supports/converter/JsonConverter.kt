package com.abc.us.accounting.supports.converter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class JsonConverter {

    private val objectMapper = ObjectMapper()

    // OMS 에 일부 데이터 중 OffsetDatetime 형식으로 선언은 되어 있으나 실제 데이터는 LocalDateTime 형식으로
    // 사용되고 있는 케이스가 있음 따라서 OffsetDateTime 형식으로 만들어 주기 위해 Z 를 붙여서 파싱 시도하는 로직으로 구성됨
    class OffsetDateTimeWithDefaultZoneDeserializer : JsonDeserializer<OffsetDateTime>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OffsetDateTime {
            val rawValue = p.text.trim()
            val formattedValue = sanitizeNanoSeconds(rawValue).let { value ->
                if (!hasOffset(value)) {
                    "$value" + "Z" // 기본적으로 Z(UTC) 오프셋 추가
                } else value
            }
            return OffsetDateTime.parse(formattedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }

        private fun sanitizeNanoSeconds(value: String): String {
            val nanoRegex = Regex("\\.(\\d{1,9})\\d*")
            return value.replace(nanoRegex) { match ->
                ".${match.groupValues[1]}" // 최대 9자리까지만 유지
            }
        }

        private fun hasOffset(value: String): Boolean {
            // 오프셋이 포함되어 있는지 확인 (Z, +HH:mm, -HH:mm)
            return value.contains("Z") || value.contains("+") || value.matches(Regex(".*-\\d{2}:\\d{2}$"))
        }
    }
    init {
        objectMapper.registerModule(JavaTimeModule())
        //Jackson에서 OffsetDateTime을 처리할 때 나노초 자릿수 초과를 무시하도록 설정
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerModule(SimpleModule().apply {
            addDeserializer(OffsetDateTime::class.java, OffsetDateTimeWithDefaultZoneDeserializer())
        })
        objectMapper.registerKotlinModule()
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun <T> toJson(src: T): String? {
        try {
            return objectMapper.writeValueAsString(src)
        } catch (ex: Exception) {
            ex.printStackTrace()
            println(ex.message)
            return null
        }
    }
    @Throws(IOException::class)
    fun <R> toObjFromTypeRef(json: String, valueTypeRef: TypeReference<R> ): R {
        return objectMapper.readValue(json, valueTypeRef)
    }
    @Throws(IOException::class)
    fun <R> toObjFromInputStream(inputStream: InputStream, valueTypeRef: TypeReference<R>): R {
        return objectMapper.readValue(inputStream, valueTypeRef)
    }


    @Throws(IOException::class)
    fun <R> toObj(anyData: Any, type: Class<R>): R? {

        return try {
            when (anyData) {
                is Map<*, *> -> {
                    objectMapper.convertValue(anyData, type)
                }
                is String -> {
                    objectMapper.readValue(anyData, type)
                }
                else -> throw IllegalArgumentException("Unsupported data type: ${anyData::class.java}")
            }
        } catch (e: Exception) {
            logger.error { "Failure convert object - [${e.message}] - [${anyData}]" }
            null
        }
    }

    @Throws(IOException::class)
    fun <R> toObjFileToFromTypeRef(jsonFile: File, valueTypeRef: TypeReference<R>): R {
        return objectMapper.readValue(jsonFile, valueTypeRef)
    }

    @Throws(IOException::class)
    fun <R> toObjFromFile(jsonFile: File, type: Class<R>): R {
        return objectMapper.readValue(jsonFile, type)
    }
}