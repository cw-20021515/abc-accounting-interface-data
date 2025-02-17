package com.abc.us.accounting.supports.utils

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.springframework.util.Assert

data class StringRange(private val startInclusive: String, private val endInclusive: String){
    init {
        val comp = startInclusive.compareTo(this.endInclusive)
        Assert.state(comp <= 0, "invalid condition!, start:$startInclusive end:$endInclusive, comp:$comp")
    }

    fun contains(code: String): Boolean {
        val left = startInclusive.compareTo(code)
        val right = endInclusive.compareTo(code)

        if (left <= 0 && right >= 0) return true
        return false
    }

    fun isEmpty(): Boolean = startInclusive > endInclusive

    // 포함 관계 확인 함수들
    fun contains(other: StringRange): Boolean {
        return this.startInclusive <= other.startInclusive && this.endInclusive >= other.endInclusive
    }

    fun isContainedIn(other: StringRange): Boolean {
        return other.contains(this)
    }

    fun overlaps(other: StringRange): Boolean {
        return this.startInclusive <= other.endInclusive && other.startInclusive <= this.endInclusive
    }

    override fun toString(): String {
        return "StringRange(startInclusive='$startInclusive', endInclusive='$endInclusive')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is StringRange) return false

        return EqualsBuilder()
            .append(startInclusive, other.startInclusive)
            .append(endInclusive, other.endInclusive)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(startInclusive)
            .append(endInclusive)
            .toHashCode()
    }

}
