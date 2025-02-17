package com.abc.us.accounting.supports.mapper

import com.abc.us.accounting.configs.InstantTypeAdapter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.regex.MatchResult
import java.util.regex.Pattern


@Component
object MapperUtil {
    private val PATTERN: Pattern = Pattern.compile("_(\\w)")
    fun isEmpty(cs: CharSequence?): Boolean {
        return cs == null || cs.length == 0 || cs.equals("null")
    }

    fun convertToCamelCase(alias: String): String {
        val camelCase = PATTERN.matcher(alias).replaceAll { matchResult: MatchResult ->
            matchResult.group(
                1
            ).uppercase(Locale.getDefault())
        }
        return camelCase
    }

    // JSON 문자열에서 key만 camelCase로 변환하는 함수
    fun convertJsonKeysToCamelCase(json: String): String {
        val node = mapper.readTree(json)
        when {
            node.isObject -> {
                // ObjectNode 처리
                val objectNode = node as ObjectNode
                val fieldNames = mutableListOf<String>()

                // ObjectNode에서 필드 이름을 가져옴
                objectNode.fields().forEachRemaining { field ->
                    fieldNames.add(field.key)
                }

                // 필드 이름을 camelCase로 변환하여 다시 ObjectNode에 추가
                fieldNames.forEach { fieldName ->
                    val valueNode = objectNode.remove(fieldName)  // fieldName에 해당하는 값을 제거
                    val camelCaseKey = convertToCamelCase(fieldName)  // camelCase로 변환
                    objectNode.put(camelCaseKey, valueNode)  // 새로운 camelCase key로 값을 설정
                }
            }
            node.isArray -> {
                // ArrayNode 처리 (배열 안의 각 요소들을 재귀적으로 처리)
                node.forEach { convertJsonNodeKeysToCamelCase(it) }
            }
        }
        return mapper.writeValueAsString(node)
    }

    private fun convertJsonNodeKeysToCamelCase(node: JsonNode) {
        if (node.isObject) {
            val objectNode = node as ObjectNode // node를 ObjectNode로 안전하게 캐스팅

            val fieldNames = objectNode.fieldNames().asSequence().toList() // ObjectNode의 필드 이름들을 리스트로 가져옴
            fieldNames.forEach { fieldName ->
                val valueNode = objectNode.remove(fieldName)  // fieldName에 해당하는 값을 제거
                val camelCaseKey = convertToCamelCase(fieldName)  // camelCase로 변환
                objectNode.put(camelCaseKey, valueNode)  // 새로운 camelCase key로 값을 설정
            }

        } else if (node.isArray) {
            // 배열인 경우 재귀적으로 처리
            node.forEach { convertJsonNodeKeysToCamelCase(it) }
        }
    }

    // LocalDate를 OffsetDateTime으로 변환하는 함수
    fun localDateToOffsetDateTime(localDate: LocalDate): ZonedDateTime? {
        return localDate.atStartOfDay(ZoneOffset.UTC) // 기본 시간 00:00:00과 UTC 오프셋 사용
    }

    fun logMapCheck(result: Any?): String {
        // result는 ResponseEntity<?> 타입으로 가정합니다.
        val tempResult: Any? = result// your ResponseEntity instance here

        // result.body가 null이 아닌 경우에만 JSON 문자열로 변환합니다.
        val resultStr: String? = tempResult.let {
            mapper.writeValueAsString(it)
        }
        return resultStr.toString()
    }

    fun gsonBuilder(fieldType: String?): Gson {
        return GsonBuilder()
            .registerTypeAdapter(OffsetDateTime::class.java, AdapterOffsetDateTime(fieldType))
            .registerTypeAdapter(LocalDate::class.java, AdapterLocalDate(fieldType))
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create()
    }

    val gson = GsonBuilder()
        .serializeNulls()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()


    val mapper: ObjectMapper = ObjectMapper().apply {

        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)    // 날짜를 타임스탬프로 직렬화하지 않도록 설정합니다.
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)    // 빈 Beans에 대한 오류를 무시합니다.
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false)    // 맵의 엔트리 키를 정렬하지 않도록 설정합니다.
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)    // 기본값으로 null을 허용합니다.
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)    // 알 수 없는 프로퍼티를 무시합니다.
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)     // 빈 문자열을 null로 처리합니다.
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)     // 단일 값을 배열로 처리할 수 있도록 허용합니다.
    }

    @Throws(IOException::class)
    fun <T> parseDateTime(dateTimeStr: String, type: Class<T>): T {
        return mapper.readValue("\"$dateTimeStr\"", type)
    }

    // OffsetDateTime으로 변환하기 위한 포맷터
    private val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"), // ISO-8601 포맷 (밀리초 및 시간대 포함)
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ssXXX"), // 미국식 24시간 포맷
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss aXXX"), // 미국식 12시간 포맷
        DateTimeFormatter.ofPattern("MM/dd/yyyy"), // 날짜만
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), // ISO-8601 포맷 (시간대 정보 없음)
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a") // 미국식 12시간 포맷 (시간대 정보 없음)
    )

    // 변환 함수
    fun <T> convertToParseDateTime(cellValue: String, type: Class<T>): T? {
        // 각 포맷터로 시도
        for (formatter in formatters) {
            try {
                mapper.convertValue(cellValue, type)
            } catch (e: DateTimeParseException) {
            }
        }
        return null
    }

    fun parseJson(data: String): JsonObject {
        return gson.fromJson(data, JsonObject::class.java)
    }

    inline fun <reified T> convert(list: List<Map<String, Any>>): List<T> {
        return list.map { map ->
            convert(map)
        }
    }

    inline fun <reified T> convert(map: Map<String, Any>): T {
        val newMap: MutableMap<String, Any?> = mutableMapOf()
        map.keys.forEach { key ->
            newMap[convertToCamelCase(key)] = map[key]
        }
        return mapper.convertValue<T>(newMap)
    }

    inline fun <reified T> convert(page: Page<Map<String, Any>>): Page<T> {
        val newList: List<T> = convert(page.toList())
        return PageImpl(newList, page.pageable, page.totalElements)
    }

    inline fun <reified T> convert(string: String): T {
        val map: Map<String, Any> = convertToMap(string)
        return convert(map)
    }

    fun convertToMap(data: String): Map<String, Any> {
        return Gson().fromJson(data, Map::class.java) as Map<String, Any>
    }
}