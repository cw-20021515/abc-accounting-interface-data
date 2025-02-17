package com.abc.us.accounting.supports

import com.abc.us.accounting.config.Constants
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class NumberUtil {

    companion object {
        private val mc = Constants.MATH_CONTEXT
        private val scale = Constants.ACCOUNTING_SCALE
        private val roundingMode = RoundingMode.valueOf(Constants.MATH_ROUNDING_MODE)

        fun round(value: Double?, scale: Int): Double {
            return BigDecimal(value ?: 0.0)
                .setScale(scale, roundingMode)
                .toDouble()
        }

        fun round(value: Float?, scale: Int): Float {
            return BigDecimal(value?.toDouble() ?: 0.0)
                .setScale(scale, roundingMode)
                .toFloat()
        }

        fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal {
            return a.multiply(b, mc).setScale(scale, roundingMode)
        }

        fun multiply(vararg items: BigDecimal): BigDecimal {
            var res = BigDecimal(1)
            items.forEach {
                res = multiply(res, it)
            }
            return res
        }

        fun divide(a: BigDecimal, b: BigDecimal, noScale: Boolean = false): BigDecimal {
            return if (noScale) {
                a.divide(b, mc)
            } else {
                a.divide(b, mc).setScale(scale, roundingMode)
            }
        }

        fun minus(a: BigDecimal, b: BigDecimal): BigDecimal {
            return a.minus(b).setScale(scale, roundingMode)
        }

        fun plus(a: BigDecimal, b: BigDecimal): BigDecimal {
            return a.plus(b).setScale(scale, roundingMode)
        }

        fun plus(vararg items: BigDecimal): BigDecimal {
            var res = BigDecimal(0)
            items.forEach {
                res = plus(res, it)
            }
            return res
        }

        fun setScale(
            value: BigDecimal,
            scale: Int = NumberUtil.scale,
            roundingMode: RoundingMode = NumberUtil.roundingMode
        ): BigDecimal {
            return value.setScale(scale, roundingMode)
        }

        fun setScaleNullable(
            value: BigDecimal?,
            scale: Int = NumberUtil.scale,
            roundingMode: RoundingMode = NumberUtil.roundingMode
        ): BigDecimal? {
            return value?.setScale(scale, roundingMode)
        }

        /**
         * 일할계산 비율
         */
        fun getDailyRatio(
            baseDate: LocalDate
        ): BigDecimal {
            val lastDate = baseDate.with(TemporalAdjusters.lastDayOfMonth())
            val total = lastDate.dayOfMonth
            val current = baseDate.dayOfMonth
            return divide(
                BigDecimal(total - current + 1),
                BigDecimal(total),
                true
            )
        }
    }
}