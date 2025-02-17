package com.abc.us.accounting.rentals.olease.service

import com.abc.us.accounting.rentals.lease.model.RentalAssetDepreciationScheduleParam
import com.abc.us.accounting.rentals.lease.service.OleaseService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class RentalAssetServiceTests(
): FunSpec({

    test("generateDepreciationSchedule1") {
        val res = OleaseService.generateDepreciationSchedule(
            RentalAssetDepreciationScheduleParam(
                bookValue = BigDecimal("510.00"),
                usefulLife = 60,
                installationDate = LocalDate.of(2024, 11, 10),
                currency = "USD",
                salvageValue = BigDecimal("0.1"),
                serialNumber = "generateDepreciationSchedule1_serial"
            ),
            2
        )

        res.size shouldBe 60
        val first = res.first()
        first.depreciationCount shouldBe 1
        first.depreciationDate shouldBe LocalDate.of(2024, 11, 30)
        first.currency shouldBe "USD"
        first.beginningBookValue shouldBe BigDecimal("510.00")
        first.depreciationExpense shouldBe BigDecimal("8.50")
        first.endingBookValue shouldBe BigDecimal("501.50")
        first.accumulatedDepreciation shouldBe BigDecimal("8.50")

        val second = res[1]
        second.depreciationCount shouldBe 2
        second.depreciationDate shouldBe LocalDate.of(2024, 12, 31)
        second.currency shouldBe "USD"
        second.beginningBookValue shouldBe BigDecimal("501.50")
        second.depreciationExpense shouldBe BigDecimal("8.50")
        second.endingBookValue shouldBe BigDecimal("493.00")
        second.accumulatedDepreciation shouldBe BigDecimal("17.00")

        val last = res.last()
        last.depreciationCount shouldBe 60
        last.depreciationDate shouldBe LocalDate.of(2029, 10, 31)
        last.currency shouldBe "USD"
        last.beginningBookValue shouldBe BigDecimal("8.50")
        last.depreciationExpense shouldBe BigDecimal("8.40")
        last.endingBookValue shouldBe BigDecimal("0.10")
        last.accumulatedDepreciation shouldBe BigDecimal("509.90")
    }

    test("generateDepreciationSchedule2") {
        val res = OleaseService.generateDepreciationSchedule(
            RentalAssetDepreciationScheduleParam(
                bookValue = BigDecimal("770.00"),
                usefulLife = 60,
                installationDate = LocalDate.of(2024, 1, 10),
                currency = "USD",
                salvageValue = BigDecimal("0.30"),
                serialNumber = "generateDepreciationSchedule2_serial"
            ),
            2
        )

        res.size shouldBe 60
        val first = res.first()
        first.depreciationCount shouldBe 1
        first.depreciationDate shouldBe LocalDate.of(2024, 1, 31)
        first.currency shouldBe "USD"
        first.beginningBookValue shouldBe BigDecimal("770.00")
        first.depreciationExpense shouldBe BigDecimal("12.83")
        first.endingBookValue shouldBe BigDecimal("757.17")
        first.accumulatedDepreciation shouldBe BigDecimal("12.83")

        val second = res[1]
        second.depreciationCount shouldBe 2
        second.depreciationDate shouldBe LocalDate.of(2024, 2, 29)
        second.currency shouldBe "USD"
        second.beginningBookValue shouldBe BigDecimal("757.17")
        second.depreciationExpense shouldBe BigDecimal("12.83")
        second.endingBookValue shouldBe BigDecimal("744.34")
        second.accumulatedDepreciation shouldBe BigDecimal("25.66")

        val last = res.last()
        last.depreciationCount shouldBe 60
        last.depreciationDate shouldBe LocalDate.of(2028, 12, 31)
        last.currency shouldBe "USD"
        last.beginningBookValue shouldBe BigDecimal("13.03")
        last.depreciationExpense shouldBe BigDecimal("12.73")
        last.endingBookValue shouldBe BigDecimal("0.30")
        last.accumulatedDepreciation shouldBe BigDecimal("769.70")
    }
})