package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.Constants
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

object MoneyDistributor {
    /**
     * 전체 금액을 n개의 랜덤한 금액으로 분배 (BigDecimal 사용)
     * @param totalAmount 분배할 전체 금액
     * @param count 분배할 개수
     * @param scale 소수점 자릿수 (기본값 2)
     * @return 분배된 금액 리스트
     */
    fun distribute(
        totalAmount: BigDecimal,
        count: Int,
        scale: Int = Constants.ACCOUNTING_SCALE,
        roundingMode: RoundingMode = RoundingMode.valueOf(Constants.MATH_ROUNDING_MODE)
    ): List<BigDecimal> {
        require(totalAmount > BigDecimal.ZERO) { "totalAmount must be positive, but ${totalAmount}" }
        require(count > 0) { "count must be positive, but ${count}" }

        if (count == 1) return listOf(totalAmount.setScale(scale, roundingMode))

        val random = Random.Default
        val points = mutableListOf<BigDecimal>()

        // 랜덤 포인트 생성
        repeat(count - 1) {
            val randomValue = BigDecimal(random.nextDouble())
                .multiply(totalAmount)
                .setScale(scale, roundingMode)
            points.add(randomValue)
        }

        points.sort()
        points.add(0, BigDecimal.ZERO)
        points.add(totalAmount)

        // 인접한 포인트 간의 차이를 계산하여 각 금액 결정
        return (1..points.lastIndex)
            .map { i ->
                points[i].subtract(points[i-1])
                    .setScale(scale, roundingMode)
            }
    }
}
