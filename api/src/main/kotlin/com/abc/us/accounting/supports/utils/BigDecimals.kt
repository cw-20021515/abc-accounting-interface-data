package com.abc.us.accounting.supports.utils

import com.abc.us.accounting.config.Constants
import java.math.BigDecimal
import java.math.RoundingMode

object BigDecimals {
    /**
     * toScale the scale of a BigDecimal
     * 숫자 계산할때 이 함수를 무조건 호출해야 함
     *
     * @param newScale the new scale
     * @param roundingMode the rounding mode
     */
    fun BigDecimal.toScale(newScale:Int = Constants.ACCOUNTING_SCALE, roundingMode:String = Constants.MATH_ROUNDING_MODE): BigDecimal {
        return this.setScale(newScale, RoundingMode.valueOf(roundingMode))
    }

    fun BigDecimal.equalsWithScale(other: BigDecimal) : Boolean {
        return this.stripTrailingZeros() == other.stripTrailingZeros()
    }

    fun BigDecimal.diff(other: BigDecimal): BigDecimal{
        return this.subtract(other).abs()
    }
}