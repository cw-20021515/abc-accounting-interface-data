package com.abc.us.accounting.supports.entity

import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.abc.us.accounting.supports.utils.Hashs
import com.github.f4b6a3.tsid.Tsid
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import java.time.LocalDate
import java.util.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

fun Any.toEntityString(): String {
    val clazz = this::class

    // Check if the class has the @Entity annotation
    if (clazz.findAnnotation<Entity>() == null) {
        throw IllegalStateException("Class ${clazz.simpleName} is not annotated with @Entity")
    }

    // Collect all fields and their values, ignoring those with @IgnoreHash
    val fields = clazz.java.declaredFields
    val fieldStrings = fields.filter { field ->
        // Exclude fields annotated with @IgnoreHash
        field.isAccessible = true
        field?.getAnnotation(IgnoreHash::class.java) == null
    }.joinToString(", ") { field ->
        val value = field.get(this)
        "${field.name}=$value"
    }

    return "${clazz.simpleName}{$fieldStrings}"
}

fun Any.toEntityId() : String {
    val clazz = this::class

    // Check if the class has the @Entity annotation
    if (clazz.findAnnotation<Entity>() == null) {
        throw IllegalStateException("Class ${clazz.simpleName} is not annotated with @Entity")
    }

    val prefix = clazz.java.simpleName.filter { it.isUpperCase() }
    val seed = LocalDate.now().toString().substringBeforeLast("-")
    val uuid = UUID.nameUUIDFromBytes("$seed${toEntityString()}".toByteArray())
    val mostSignificantBits = uuid.leastSignificantBits // 양수로 변환
    // 두 값을 결합하여 10진수 문자열로 변환
    return "$prefix-${Tsid.from(mostSignificantBits)}"
}
fun Any.toEntityHash(): String {

    val clazz = this::class

    // @Entity 애너테이션이 있는지 확인
    if (!clazz.hasAnnotation<Entity>()) {
        throw IllegalArgumentException("The class ${clazz.simpleName} is not annotated with @Entity.")
    }

    val input = clazz.memberProperties
        .filter { property ->
            property.isAccessible = true
            // 필드(@FIELD) 애너테이션 확인
            val field = property.javaField
            field?.getAnnotation(IgnoreHash::class.java) == null
        }
        .map { property ->
            property.isAccessible = true // private 필드 접근 허용
            val value = property.getter.call(this) // 현재 인스턴스의 필드 값 가져오기

            val field = property.javaField
            if (field?.isAnnotationPresent(Embedded::class.java) == true && value is Hashable) {
                value.hashValue() // Hashable 인터페이스의 hashValue 호출
            } else {
                value?.toString() ?: "" // null 처리 및 기본 문자열 반환
            }

        }
        .joinToString(separator = "|") // 속성 값을 하나의 문자열로 결합

    return Hashs.sha256Hash(input)
}
fun Any.compareEntity(other: Any): Boolean {
    if (this::class != other::class) return false // 클래스 타입이 다르면 false
    return this.toEntityHash() == other.toEntityHash()
}

fun Any.compareHash(hash: String): Boolean {
    return this.toEntityHash().equals(hash)
}