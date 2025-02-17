package com.abc.us.accounting.rentals.master.type

import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionCode
import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

class DistributionTypeTests(
) : FunSpec({

                test("distribution_type test") {
                    var rentalDistributionType = RentalDistributionType.SP01
                    logger.info("DistributionType.SP01 codes:" + rentalDistributionType.codes)
                    rentalDistributionType.description shouldBe "주상품 단독 렌탈"
                    rentalDistributionType.codes shouldContain RentalDistributionCode.M01
                    rentalDistributionType.codes shouldNotContain RentalDistributionCode.S01
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R01
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R02
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R03

                    rentalDistributionType = RentalDistributionType.SP02
                    logger.info("DistributionType.SP02 codes:" + rentalDistributionType.codes)
                    rentalDistributionType.description shouldBe "주상품+서비스"
                    rentalDistributionType.codes shouldContain RentalDistributionCode.M01
                    rentalDistributionType.codes shouldContain RentalDistributionCode.S01
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R01
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R02
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R03


                    rentalDistributionType = RentalDistributionType.SP03
                    logger.info("DistributionType.SP03 codes:" + rentalDistributionType.codes)
                    rentalDistributionType.description shouldBe "주상품+무상A/S자재+서비스"
                    rentalDistributionType.codes shouldContain RentalDistributionCode.M01
                    rentalDistributionType.codes shouldContain RentalDistributionCode.S01
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R01
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R02
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R03


                    rentalDistributionType = RentalDistributionType.SP04
                    logger.info("DistributionType.SP04 codes:" + rentalDistributionType.codes)
                    rentalDistributionType.description shouldBe "주상품+무상A/S자재1+2+서비스"
                    rentalDistributionType.codes shouldContain RentalDistributionCode.M01
                    rentalDistributionType.codes shouldContain RentalDistributionCode.S01
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R01
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R02
                    rentalDistributionType.codes shouldNotContain  RentalDistributionCode.R03

                    rentalDistributionType = RentalDistributionType.SP05
                    logger.info("DistributionType.SP05 codes:" + rentalDistributionType.codes)
                    rentalDistributionType.description shouldBe "주상품+무상A/S자재1+2+3+서비스"
                    rentalDistributionType.codes shouldContain RentalDistributionCode.M01
                    rentalDistributionType.codes shouldContain RentalDistributionCode.S01
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R01
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R02
                    rentalDistributionType.codes shouldContain  RentalDistributionCode.R03
                }

            }) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
