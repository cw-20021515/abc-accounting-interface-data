package com.abc.us.accounting.supports

import org.hibernate.transform.AliasToBeanResultTransformer
import java.math.BigDecimal
import java.sql.Date
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import java.util.regex.MatchResult
import java.util.regex.Pattern


class CamelResultformer(val resultClass: Class<*>?) : AliasToBeanResultTransformer<Any?>(resultClass as Class<Any?>?) {
    override fun transformTuple(tuple: Array<Any?>, aliases: Array<String>): Any? {
        // 별칭을 camelCase로 변환하고, Instant를 OffsetDateTime으로 변환
        aliases.forEachIndexed { index, alias ->
            val camelCaseAlias = convertToCamelCase(alias)
            aliases[index] = camelCaseAlias

            // Instant 타입을 OffsetDateTime으로 변환
            if (tuple[index] is Instant) {
                val instant = tuple[index] as Instant
                val offsetDateTime = instant.atOffset(ZoneOffset.UTC)
                if (alias.endsWith("date")) {
                    tuple[index] = offsetDateTime.toLocalDate()
                } else {
                    tuple[index] = offsetDateTime
                }
//                println("aliases[index] : ${aliases[index]}, tuple[index] : ${tuple[index]}")
            }
            // java.sql.Date to LocalDate
            if (tuple[index] is Date) {
                val date = tuple[index] as Date
                tuple[index] = date.toLocalDate()
            }

            // 숫자 타입의 변환 처리(Double 제외)
            if (
                tuple[index] is Number &&
                tuple[index] !is Double &&
                tuple[index] !is Float &&
                tuple[index] !is BigDecimal
                ) {
                tuple[index] = (tuple[index] as Number).toInt()
            }

            // enum 값 처리
            val propertyType = resultClass?.getDeclaredField(camelCaseAlias)?.type
            if (propertyType?.isEnum == true) {
                val propertyValue = tuple[index].toString()
                val enumValue = propertyType.enumConstants.find {
                    it.toString() == propertyValue
                }
                tuple[index] = enumValue
            }
        }
        return super.transformTuple(tuple, aliases)
    }

    private fun convertToCamelCase(alias: String): String {
        if (isCamelCase(alias)) {
            return alias
        }
        val camelCase = PATTERN.matcher(alias.lowercase(Locale.getDefault())).replaceAll { matchResult: MatchResult ->
            matchResult.group(
                1
            ).uppercase(Locale.getDefault())
        }
        return camelCase
    }

    private fun isCamelCase(alias: String): Boolean {
        val underscoreCount = alias.count {
            it == '_'
        }
        // _ 가 있으면 snake_case
        if (underscoreCount > 0) {
            return false
        }
        var upperCaseCount = 0
        var lowerCaseCount = 0
        for (char in alias) {
            if (char.isUpperCase()) {
                upperCaseCount++
            }
            if (char.isLowerCase()) {
                lowerCaseCount++
            }
        }
        // _ 가 없고 대소문자가 섞여 있으면 camelCase
        return upperCaseCount > 0 && lowerCaseCount > 0
    }

    companion object {
        private val PATTERN: Pattern = Pattern.compile("_(\\w)")
    }
}