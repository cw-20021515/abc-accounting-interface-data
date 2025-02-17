package com.abc.us.accounting.supports.utils

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 제외
data class Range<T : Comparable<T>> (
    @get:JsonProperty("from")
    val from: T? = null,

    @get:JsonProperty("to")
    val to: T? = null,
){

    init {
        if (from != null && to != null && from > to) {
            throw IllegalStateException("Range is invalid for $from and $to")
        }
    }

    override fun toString(): String {
        return toStringByReflection()
    }


//    fun isInRange(value: T): Boolean {
//        if ( to != null ) {
//            return value >= from && value <= to
//        }
//
//        return value >= from
//    }

fun isInRange(value: T): Boolean {
        if (from == null && to == null)  return true

        if (from != null && to != null) {
            return value >= from && value <= to
        }

        if ( from != null ) {
            return value >= from
        }

        if ( to != null ) {
            return value <= to
        }

        return false
    }
}
