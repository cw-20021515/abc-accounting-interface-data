package com.abc.us.accounting.supports.utils

import kotlin.reflect.full.memberProperties

@DslMarker
annotation class ToStringDsl

@ToStringDsl
class ToStringBuilder(private val className: String? = null) {
    private val fields = mutableListOf<Pair<String, Any>>()

    fun add(vararg pairs: Pair<String, Any?>) {
        fields.addAll(pairs.filter { it.second != null }.map { it.first to it.second!! })
    }

    fun addAll(map: Map<String, Any?>) {
        fields.addAll(map.filterValues { it != null }.map { it.key to it.value!! })
    }

    override fun toString(): String {
        val prefix = className?.let { "$it(" } ?: "("
        return fields.joinToString(
            prefix = prefix,
            separator = ", ",
            postfix = ")"
        ) { "${it.first}=${it.second}" }
    }
}

fun Any.buildToString(block: ToStringBuilder.() -> Unit): String =
    ToStringBuilder(this::class.simpleName).apply(block).toString()



/**
 * Any 타입의 확장 함수로 객체의 모든 속성을 문자열로 변환
 */
fun Any.toStringByReflection(): String {
    return this::class.memberProperties
        .filter { prop -> prop.getter.call(this) != null }  // null 값 필터링
        .joinToString(
            prefix = "${this::class.simpleName}(",
            postfix = ")",
            separator = ", "
        ) { prop ->
            "${prop.name}=${prop.getter.call(this)}"
        }
}

/**
 * 더 세밀한 제어가 필요한 경우를 위한 버전
 */
fun Any.toStringByReflection(
    prefix: String = "${this::class.simpleName}(",
    postfix: String = ")",
    separator: String = ", ",
    transform: ((String, Any?) -> String)? = null
): String {
    return this::class.memberProperties
        .joinToString(
            prefix = prefix,
            postfix = postfix,
            separator = separator
        ) { prop ->
            val value = prop.getter.call(this)
            if (transform != null) {
                transform(prop.name, value)
            } else {
                "${prop.name}=$value"
            }
        }
}

// null 안전성을 위한 추가 확장 함수
fun Any?.toSafeString(): String {
    return this?.toStringByReflection() ?: "null"
}


// 컬렉션을 위한 특별한 처리가 필요한 경우
fun Any.toDetailedString(): String {
    return this::class.memberProperties
        .joinToString(
            prefix = "${this::class.simpleName}(\n",
            postfix = "\n)",
            separator = ",\n"
        ) { prop ->
            val value = prop.getter.call(this)
            when (value) {
                is Collection<*> -> "  ${prop.name}=[${value.joinToString()}]"
                is Map<*, *> -> "  ${prop.name}={${value.entries.joinToString()}}"
                else -> "  ${prop.name}=$value"
            }
        }
}
