package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.MathConfig
import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionCode
import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionType
import com.abc.us.accounting.supports.utils.buildToString
import jakarta.persistence.Embeddable
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.springframework.util.Assert
import java.math.BigDecimal

@Embeddable
data class Distribution(
    val m01: BigDecimal,
    val r01: BigDecimal?,
    val r02: BigDecimal?,
    val r03: BigDecimal?,
    val s01: BigDecimal?,
    val t01: BigDecimal?,
) {
    override fun toString(): String {
        return buildToString {
            add(
                "m01" to m01,
                "r01" to r01,
                "r02" to r02,
                "r03" to r03,
                "s01" to s01,
                "t01" to t01,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Distribution) return false

        return EqualsBuilder()
            .append(m01, other.m01)
            .append(r01, other.r01)
            .append(r02, other.r02)
            .append(r03, other.r03)
            .append(s01, other.s01)
            .append(t01, other.t01)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(m01)
            .append(r01)
            .append(r02)
            .append(r03)
            .append(s01)
            .append(t01)
            .toHashCode()
    }

    fun getValue(rentalDistributionCode: RentalDistributionCode): BigDecimal? {
        return when (rentalDistributionCode) {
            RentalDistributionCode.M01 -> m01
            RentalDistributionCode.R01 -> r01
            RentalDistributionCode.R02 -> r02
            RentalDistributionCode.R03 -> r03
            RentalDistributionCode.S01 -> s01
            RentalDistributionCode.T01 -> t01
        }
    }

    /**
     * 합계 계산
     */
    val total: BigDecimal
        get() {
            return sum(m01, r01, r02, r03, s01)
        }

    fun toRatio(): Distribution {
        val total = this.total


        val rM01 = m01.divide(total, mc).setScale(ratioScale, roundingMode)
        var sum:BigDecimal = rM01 ?: BigDecimal.ZERO

        val rR01 = if ((this.r01 == null)) null else r01.divide(total, mc).setScale(ratioScale, roundingMode)
        if (rR01 != null) {
            sum = sum.add(rR01)
        }
        val rR02 = if ((this.r02 == null)) null else r02.divide(total, mc).setScale(ratioScale, roundingMode)
        if (rR02 != null) {
            sum = sum.add(rR02)
        }
        val rR03 = if ((this.r03 == null)) null else r03.divide(total, mc).setScale(ratioScale, roundingMode)
        if (rR03 != null) {
            sum = sum.add(rR03)
        }
//        val rS01 = if ((this.s01 == null)) null else s01!!.divide(total, mc).setScale(scale, roundingMode)

        // 단수차이 조정
        val rTotal = BigDecimal(1.0).setScale(ratioScale, roundingMode)
        val rS01 = if ((this.s01 == null)) null else rTotal.minus(sum).setScale(ratioScale, roundingMode)

        return Distribution(rM01, rR01, rR02, rR03, rS01, rTotal)
    }

    fun toRentalPrice(rentalPrice: BigDecimal): Distribution {
        Assert.notNull(rentalPrice, "rentalPrice is null")

        val ratio = this.toRatio()
        Assert.notNull(ratio, "ratio is null")
        Assert.notNull(ratio.m01, "m1 is null")

        val pM01 = rentalPrice.multiply(ratio.m01, mc).setScale(scale, roundingMode)
        var sum:BigDecimal = pM01 ?: BigDecimal.ZERO

        val pR01 = if ((ratio.r01 == null)) null else rentalPrice.multiply(ratio.r01, mc).setScale(scale, roundingMode)
        if (pR01 != null) {
            sum = sum.add(pR01)
        }
        val pR02 = if ((ratio.r02 == null)) null else rentalPrice.multiply(ratio.r02, mc).setScale(scale, roundingMode)
        if (pR02 != null) {
            sum = sum.add(pR02)
        }
        val pR03 = if ((ratio.r03 == null)) null else rentalPrice.multiply(ratio.r03, mc).setScale(scale, roundingMode)
        if (pR03 != null) {
            sum = sum.add(pR03)
        }
        // 단수차이 조정
//        val pS01 = if ((ratio.s01 == null)) null else rentalPrice.multiply(ratio.s01, mc).setScale(scale, roundingMode)
        val pS01 = if ((ratio.s01 == null)) null else rentalPrice.subtract(sum).setScale(scale, roundingMode)

        return Distribution(pM01, pR01, pR02, pR03, pS01, rentalPrice)
    }


    companion object {
        private val config = MathConfig()
        private val scale = config.scale
        private val ratioScale = config.ratioScale
        private val roundingMode =  config.getRoundingMode()
        private val mc = Constants.MATH_CONTEXT

        private fun sum(m01: BigDecimal,
                          r01: BigDecimal? = null,
                          r02: BigDecimal? = null,
                          r03: BigDecimal? = null,
                          s01: BigDecimal? = null):BigDecimal{
            var total = m01
            if (r01 != null) {
                total = total.add(r01)
            }
            if (r02 != null) {
                total = total.add(r02)
            }
            if (r03 != null) {
                total = total.add(r03)
            }
            if (s01 != null) {
                total = total.add(s01)
            }

            return total
        }

        fun of (
            m01: BigDecimal,
            r01: BigDecimal? = null,
            r02: BigDecimal? = null,
            r03: BigDecimal? = null,
            s01: BigDecimal? = null
        ): Distribution {
            val t01 = sum(m01, r01, r02, r03, s01)
            return Distribution(m01, r01, r02, r03, s01, t01)
        }

        /**
         * 렌탈 안분(재화/서비스) 판매가치 계산
         * @param rentalDistributionType                재화배분유형코드
         * @param adjustedContractPeriod                조정 약정기간 (무약정은 60, 나머지는 약정개월수)
         * @param purchasePrice                         일시불 가격
         * @param membershipPrice                       멤버십 가격
         * @param adjustedMembershipPrice               조정 멤버십 가격(2년↑약정시 가격 할인)
         * @param freeServicePeriod                     무상 서비스 기간
         * @return
         */
        fun calculationDistributionSalesValue(
            rentalDistributionType: RentalDistributionType,
            adjustedContractPeriod: Int,
            purchasePrice: BigDecimal,
            membershipPrice: BigDecimal,
            adjustedMembershipPrice: BigDecimal,
            freeServicePeriod: Int
        ): Distribution {
            when (rentalDistributionType) {
                RentalDistributionType.SP01 -> {  // 재화만 있는 케이스
                    return Distribution(purchasePrice, null, null, null, null, purchasePrice)
                }

                RentalDistributionType.SP02 -> {  // 재화+서비스만 있는 케이스
                    /**
                     * - 재화 가치(M01): '일시불 가격' - '멤버십 무상 서비스 가격'
                     *    > '멤버십 무상 서비스 가격': '멤버십 가격' * 무상 서비스 기간(국내 1년, 미신사 일시불 3년 워런티 적용)
                     * - 서비스 가치(S01): 멤버십 할인 가격 (국내 2년 이상 약정시 할인) * 조정 약정 기간 (무약정: 5년 가정, 약정: 약정 기간)
                     *
                     * @see https://docs.google.com/spreadsheets/d/1KoZeIvL1B2mDPYoCXuMKtr3T1UEZtgwy/edit?gid=1746885933#gid=1746885933
                     */
                    val freeServiceCharge = membershipPrice.multiply(BigDecimal.valueOf(freeServicePeriod.toLong()))
                    val m01 = purchasePrice.subtract(freeServiceCharge)
                    val s01 = adjustedMembershipPrice.multiply(BigDecimal.valueOf(adjustedContractPeriod.toLong()))
                    val t01 = m01.add(s01)

                    return Distribution(m01, null, null, null, s01, t01)

                }
                else -> {
                    // TODO: 교체품(R01, R02, R03)은 추후 고민
                    throw IllegalArgumentException("Distribution type $rentalDistributionType not supported")
                }
            }
        }

        /**
         * 렌탈 안분(재화/서비스) 판매가치 에서 비중으로 변환
         * @param value
         * @return
         */
        fun valueToRatio(value: Distribution): Distribution {
            Assert.notNull(value, "value is null")
            return value.toRatio()
        }


        fun valueToRentalPrice(rentalPrice: BigDecimal, value: Distribution): Distribution {
            Assert.notNull(rentalPrice, "rentalPrice is null")
            Assert.notNull(value, "value is null")
            return value.toRentalPrice(rentalPrice)
        }
    }
}