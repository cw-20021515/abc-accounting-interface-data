package com.abc.us.accounting.supports.utils

import com.abc.us.accounting.config.Constants.BASE_YEAR
import com.github.f4b6a3.tsid.Tsid
import com.github.f4b6a3.tsid.TsidCreator
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass

object IdGenerator {

    fun generateId(): String {
        return TsidCreator.getTsid().toString()
    }

    fun generateNumericId(): Long {
        return TsidCreator.getTsid().toLong()
    }

    fun generateId(clazz: KClass<*>, contents: String): String {
        val prefix = clazz.java.simpleName.filter { it.isUpperCase() }
        val seed = LocalDate.now().toString().substringBeforeLast("-")
        val uuid = UUID.nameUUIDFromBytes("$seed$contents".toByteArray())
        val mostSignificantBits = uuid.leastSignificantBits // 양수로 변환

        // 두 값을 결합하여 10진수 문자열로 변환
        return "$prefix-${Tsid.from(mostSignificantBits)}"
    }


    fun generateId(prefix:String, date: LocalDate= LocalDate.now(), seq:Long): String {
        // 총 12자리: prefix: 2자리, yearCode: 2자리, dayOfYear: 3자리, sequence: 5자리

        val prefix = prefix.padStart(2, '0')
        val yearCode = (date.year - BASE_YEAR).toString().padStart(2, '0')
        val dayOfYear = date.dayOfYear.toString().padStart(3, '0')
        val sequence = seq.toString().padStart(5, '0')
        return "${prefix}${yearCode}${dayOfYear}${sequence}"
    }

    fun getDateKey (date: LocalDate): String {
        val yearCode = (date.year - BASE_YEAR).toString().padStart(2, '0')
        val dayOfYear = date.dayOfYear.toString().padStart(3, '0')
        return "${yearCode}${dayOfYear}"
    }
}