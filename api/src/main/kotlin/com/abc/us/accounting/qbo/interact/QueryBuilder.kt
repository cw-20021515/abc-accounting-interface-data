package com.abc.us.accounting.qbo.interact

import com.intuit.ipp.core.IEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

class QueryBuilder<T : IEntity>(private val entity : T) {

    private val query = StringBuilder("SELECT * FROM " + entity.javaClass.simpleName)

    fun where(field: String, value: Any,operator: String = "="): QueryBuilder<T> {
        query.append(" WHERE ").append(buildCondition(field,value,operator))
        return this
    }

    fun and(field: String, value: Any,operator: String = "="): QueryBuilder<T> {
        query.append(" AND ").append(buildCondition(field, value,operator))
        return this
    }

    fun or(field: String, value: Any,operator: String = "="): QueryBuilder<T> {
        query.append(" OR ").append(buildCondition(field, value,operator))
        return this
    }

    fun orderBy(field: String, ascending: Boolean = true): QueryBuilder<T> {
        query.append(" ORDERBY ").append(field)
            .append(if (ascending) " ASC" else " DESC")
        return this
    }

    fun parameters(parameters : Map<String,Any>) : QueryBuilder<T> {

        parameters.forEach { key, value ->
            val formedValue = when(value) {
                is String -> "'$value'"
                is Number, is Boolean -> value.toString()
                else -> throw IllegalArgumentException("Unsupported parameter type: ${value::class}")
            }
            query.append(" $key $formedValue ")
        }
        return this
    }

    fun startPosition(start : Int) : QueryBuilder<T> {
        require(start > 0) { "startPosition must be greater than 0." }
        query.append(" STARTPOSITION ").append(start)
        return this
    }

    fun maxResults(max: Int): QueryBuilder<T> {
        require(max > 0) { "Max results must be greater than 0." }
        query.append(" MAXRESULTS ").append(max)
        return this
    }
    // 입력된 값의 타입에 따라 조건을 구성하는 헬퍼 함수
    private fun buildCondition(
        field: String,
        value: Any,
        operator: String
    ): String {
        return when {
            operator.equals("IN", ignoreCase = true) && value is List<*> -> {
                val formattedValues = value.joinToString(", ") {
                    when (it) {
                        is String -> "'$it'"
                        is Number, is Boolean -> it.toString()
                        else -> throw IllegalArgumentException("Unsupported value type in IN clause: ${it?.javaClass}")
                    }
                }
                "$field IN ($formattedValues)"
            }
            value is String -> "$field $operator '$value'"
            value is Boolean -> "$field $operator ${if (value) "true" else "false"}"
            value is Number -> "$field $operator '${value.toString()}'"
            value is LocalDate -> "$field $operator '${value.toString()}'"
            value is BigDecimal -> "$field $operator '${value.toString()}'"
            value is OffsetDateTime -> "$field $operator '${value.toString()}'"
            else -> throw IllegalArgumentException("Unsupported value type: ${value::class}")
        }
    }


    fun build(): String = query.toString()
}