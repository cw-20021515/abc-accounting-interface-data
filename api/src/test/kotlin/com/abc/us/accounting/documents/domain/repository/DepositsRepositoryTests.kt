package com.abc.us.accounting.documents.domain.repository

import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class DepositsRepositoryTests(
    private val depositsRepository: DepositsRepository
): FunSpec({

    test("depositsRepository.findByReq") {
        val res = depositsRepository.findByReq()
        val res2 = depositsRepository.findByReq(
            "DEPOSIT_DATE",
            LocalDate.of(2024, 12, 1),
            LocalDate.of(2024, 12, 31),
            "ALL",
            "ALL",
            "ALL",
            "orderId",
            "customerId",
            "depositId",
            PageRequest.of(
                0,
                10
            )
        )
    }
})