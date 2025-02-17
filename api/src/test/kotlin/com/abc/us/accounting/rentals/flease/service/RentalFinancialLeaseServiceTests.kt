package com.abc.us.accounting.rentals.flease.service

import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseSchedule
import com.abc.us.accounting.rentals.lease.utils.RentalFinancialLeaseUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.LocalDate

class RentalFinancialLeaseServiceTests(
): FunSpec({

    test("financial Lease Schedule") {
        val contractDate = LocalDate.of(2024, 1, 10)
        val reqRentalFinancialLeaseSchedule = ReqRentalFinancialLeaseSchedule(
            rentalAmountForGoods = BigDecimal(37.08100) ,       // 월렌탈료(재화)
            rentalAmount = BigDecimal(45),                      // 렌탈료
            interestRate = 6.85,                                    // 이자율
            contractDate = contractDate,                            // 설치일자
            contractPeriod = 36,                                    // 약정개월
        )

        val schedule = RentalFinancialLeaseUtil.generateLeaseSchedule(reqRentalFinancialLeaseSchedule)

        schedule shouldNotBe null
        // Then: 첫 번째 레코드 (회차: 0) 확인
        val firstInfo = schedule.first()
        firstInfo.depreciationCount shouldBe 0                    // 회차: 0
        firstInfo.depreciationYearMonth shouldBe "2024-01"               // 년월: 2024.01
        firstInfo.depreciationBillYearMonth shouldBe null             // 청구년월: null
        firstInfo.depreciationRentalAmount?.toDouble() shouldBe 0.0      // 렌탈료: 0.0
        firstInfo.depreciationBookValue.toDouble() shouldBe 1334.9160   // 장부금액: 1334.9160
        firstInfo.depreciationPresentValue.toDouble() shouldBe 1203.5360 // 현재가치: 1203.5360
        firstInfo.depreciationCurrentDifference.toDouble() shouldBe 131.38 // 현할차: 131.38
        firstInfo.depreciationInterestIncome shouldBe null               // 이자수익: null

        // Then: 마지막 레코드 (회차: 37) 확인
        val lastInfo = schedule.last()
        lastInfo.depreciationCount shouldBe 37                    // 회차: 37
        lastInfo.depreciationYearMonth shouldBe "2027-01"                // 년월: 2027.01
        lastInfo.depreciationBillYearMonth shouldBe "2027-02"         // 청구년월: 2027.02
        lastInfo.depreciationRentalAmount?.toDouble() shouldBe 10.7654   // 렌탈료: 10.7654
        lastInfo.depreciationInterestIncome?.toDouble() shouldBe 0.0182  // 이자수익: 0.0182

        // 결과 출력
        schedule.forEach { info ->
            println("회차: ${info.depreciationCount}, 년월: ${info.depreciationYearMonth}, 청구년월: ${info.depreciationBillYearMonth}, 렌탈료: ${info.depreciationRentalAmount}, 장부금액: ${info.depreciationBookValue}, 현재가치: ${info.depreciationPresentValue}, 현할차: ${info.depreciationCurrentDifference}, 이자수익: ${info.depreciationInterestIncome}")
        }
    }

})