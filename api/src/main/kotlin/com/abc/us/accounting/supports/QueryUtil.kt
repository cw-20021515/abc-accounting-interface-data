package com.abc.us.accounting.supports

import com.abc.us.accounting.model.ReqQueryNative
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.hibernate.jpa.spi.NativeQueryMapTransformer
import org.hibernate.query.NativeQuery
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class QueryUtil {

    companion object {
        fun getEqualsQuery(
            parameter: String,
            dbColumn: String,
        ): String {
            return """
                (:${parameter} IS NULL OR :${parameter} = '')
                OR (${dbColumn} = :${parameter})
            """.trimIndent()
        }

        fun getWhere(noWhere: Boolean): String {
            return if (noWhere) {
                ""
            } else {
                """
                    WHERE
                        TRUE
                """.trimIndent()
            }
        }

        fun where(noWhere: Boolean = false): Where {
            return Where(noWhere)
        }

        fun setParameter(query: Query, name: String, value: Any?) {
            if (
                !query.parameters.isNullOrEmpty() &&
                query.parameters.any { it.name == name }
            ) {
                query.setParameter(name, value)
            } else {
            }
        }

        fun getQueryWrapper(query: Query): QueryWrapper {
            return QueryWrapper(query)
        }

        // Helper function to set parameters from a map
        fun setParameters(query: NativeQuery<*>, parameters: MutableList<ReqQueryNative>) {
            parameters.forEach { param ->
                query.setParameter(param.key, param.value)
            }
        }

        fun getColumnsForPositiveGroup(list: List<ReqQueryNative>): List<Int> {
            return list.map { it.group }.filter { it >= 0 }.distinct()
        }

        fun getColumnsForPositive(groupNumber: Int, list: List<ReqQueryNative>): List<ReqQueryNative> {
            return list.filter { it.group == groupNumber }
        }

        /**
        val validTimestamp = "2024-08-26T15:30:00Z"
        val invalidTimestamp = "2024-08-26T15:30:00"
        println(isValidTimestamp(validTimestamp)) // true
        println(isValidTimestamp(invalidTimestamp)) // false
         */
        fun isValidTimestamp(value: Any?): Boolean {
            return try {
                value?.let {
                    when (it) {
                        is String -> {
                            return try {
                                ZonedDateTime.parse(it)
                                true
                            } catch (e: DateTimeParseException) {
                                false
                            }
                        }

                        is LocalDateTime,
                        is ZonedDateTime,
                        is LocalDate,
                        is OffsetDateTime,
                        -> true

                        else -> false
                    }
                } ?: false
            } catch (e: DateTimeParseException) {
                false
                e.printStackTrace()
                throw Exception("Date Time Format Error value : $value")
            }
        }

        fun queryTimestamp(key: String, valueData: Any?): String {
            return if (isValidTimestamp(valueData)) {
                " CAST(:$key AS TIMESTAMPTZ)"
            } else {
                " :$key"
            }
        }

        fun getQuery(
            entityManager: EntityManager,
            queryString: String,
            resultClass: Class<*>?,
        ): NativeQuery<*> {
            return entityManager
                .createNativeQuery(queryString)
                .unwrap(NativeQuery::class.java)
                .setResultTransformer(CamelResultformer(resultClass))
        }

        fun getJsonQuery(
            entityManager: EntityManager,
            queryString: String,
            resultClass: Class<*>?,
        ): NativeQuery<*> {
            return entityManager
                .createNativeQuery(queryString)
                .unwrap(NativeQuery::class.java)
                .setTupleTransformer(NativeQueryMapTransformer.INSTANCE)
        }

        fun makeWhere(
            reqData: Any,
            excludedProperties: Set<*> = setOf<String>(),
            callback: (key: String, value: Any?) -> ReqQueryNative,
        ): Where {
            val where = where()
            reqData::class.declaredMemberProperties
                .filter { it.name !in excludedProperties } // 제외할 필드를 필터링
                .forEach { property ->
                    val key = property.name
                    val value = (property as KProperty1<Any, *>).get(reqData)
                    where.add(callback(key, value))
                }
            return where
        }
    }

    class QueryWrapper(val query: Query) {
        fun setParameter(name: String, value: Any?): QueryWrapper {
            setParameter(query, name, value)
            return this
        }
    }

    class Where {
        private var whereSql: String = ""
        var whereAndParamList: MutableList<ReqQueryNative> = mutableListOf()

        constructor(noWhere: Boolean) {
            whereSql = getWhere(noWhere)
        }

        fun add(queryString: String): Where {
            whereSql += """
                $queryString
            """
            return this
        }

        fun add(reqQuery: ReqQueryNative): Where {
            val isDuplicate = whereAndParamList.any { it.key == reqQuery.key }

            if (isDuplicate) {
                println("Duplicate key found: ${reqQuery.key}")
            } else {
                var isAdd = reqQuery.value?.let { value ->
                    when (value) {
                        is Boolean -> value
                        else -> value.toString().trim().isNotEmpty()
                    }
                } ?: false

                // 조건이 참인 경우에만 queryString을 추가
                if (isAdd) {
                    whereAndParamList.add(reqQuery)
                    this.add(reqQuery.column)
                }
            }
            return this
        }

        fun add(value: Any?, queryString: String): Where {
            var isAdd = false
            if (value != null) {
                if (value is Boolean) {
                    if (value) {
                        isAdd = true
                    }
                } else if (value.toString().trim() != "") {
                    isAdd = true
                }
            }
            if (isAdd) {
                this.add(queryString)
            }
            return this
        }

        fun get(): String {
            return whereSql
        }

        fun getGroup(): String {
            var whereMultiSql = getWhere(false)

            // Positive group numbers extracted from the list
            val positiveGroups = getColumnsForPositiveGroup(whereAndParamList)

            positiveGroups.forEach { groupNumber ->
                val conditions: List<ReqQueryNative> = getColumnsForPositive(groupNumber, whereAndParamList)
                val queryPart = if (groupNumber == 0) {
                    // Handle the case where groupNumber is 0
                    val andConditions = conditions.joinToString(separator = " AND ") { it.column }
                    " AND $andConditions "
                } else {
                    // Handle the case where groupNumber is not 0
                    val orConditions = conditions.joinToString(separator = " OR ") { it.column }
                    " AND ($orConditions) "
                }

                whereMultiSql += queryPart
            }

            return whereMultiSql
        }

        fun setParameter(query: NativeQuery<*>) {
            val uniqueParams = whereAndParamList
                .groupBy { it.key }
                .mapValues { entry -> entry.value.last() } // Keep the last value for each key
            // Set parameters to the query
            val queryWrapper = QueryWrapper(query)
            uniqueParams.values.forEach { param ->
                queryWrapper.setParameter(param.key, param.value)
            }
        }
    }
}