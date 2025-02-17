package com.abc.us.accounting.rentals.olease.domain.repository

import com.abc.us.accounting.rentals.master.domain.type.ContractStatus
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetHistory
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetDepreciationScheduleRepository
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetHistoryRepository
//import com.abc.us.accounting.rentals.olease.domain.repository.RentalAssetRepository
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class RentalAssetRepositoryTests(
    private val rentalAssetHistoryRepository: RentalAssetHistoryRepository,
    private val rentalAssetDepreciationScheduleRepository: RentalAssetDepreciationScheduleRepository,
    //private val rentalAssetRepository: RentalAssetRepository
): FunSpec({

    test("rentalAssetHistoryRepository") {
        val res = rentalAssetHistoryRepository.save(
            RentalAssetHistory(
                serialNumber = "serial1",
                materialId = "material1",
                acquisitionCost = BigDecimal(0),
                bookValue = BigDecimal(510),
                contractId = "contract1",
                contractDate = LocalDate.of(2024, 11, 19),
                contractStatus = ContractStatus.ACTIVE.code,
                orderId = "order1",
                orderItemId = "orderItem1",
                customerId = "customer1",
                eventType = RentalAssetEventType.REGISTRATION,
                hash = "1"
            )
        )
        res.serialNumber shouldBe "serial1"

        val res2 = rentalAssetHistoryRepository.findAll()
        res2.size shouldBe 1

        val res3 = rentalAssetHistoryRepository.findByReq()
        res3.toList().size shouldBe 1

        rentalAssetHistoryRepository.save(
            RentalAssetHistory(
                serialNumber = "serial2",
                materialId = "material1",
                acquisitionCost = BigDecimal(0),
                bookValue = BigDecimal(510),
                contractId = "contract2",
                contractDate = LocalDate.of(2024, 11, 19),
                contractStatus = ContractStatus.ACTIVE.code,
                orderId = "order2",
                orderItemId = "orderItem2",
                customerId = "customer1",
                eventType = RentalAssetEventType.REGISTRATION,
                hash = "2"
            )
        )
        rentalAssetHistoryRepository.save(
            RentalAssetHistory(
                serialNumber = "serial2",
                materialId = "material1",
                acquisitionCost = BigDecimal(0),
                bookValue = BigDecimal(510),
                contractId = "contract2",
                contractDate = LocalDate.of(2024, 11, 19),
                contractStatus = ContractStatus.ACTIVE.code,
                orderId = "order2",
                orderItemId = "orderItem2",
                customerId = "customer1",
                eventType = RentalAssetEventType.DEPRECIATION,
                hash = "3"
            )
        )

        run {
            val res = rentalAssetHistoryRepository.findAll()
            res.size shouldBe 3
        }

        val res4 = rentalAssetHistoryRepository.findByReq()
        res4.toList().size shouldBe 2

        val res5 = rentalAssetHistoryRepository.findByReq(
            serialNumber = "serial1"
        )
        res5.toList().size shouldBe 1
        res5.toList()[0]["serial_number"] shouldBe "serial1"

        run {
            val res = rentalAssetHistoryRepository.findByReq(
                serialNumber = "serial2"
            )
            res.toList().size shouldBe 1
            res.toList()[0]["serial_number"] shouldBe "serial2"
            res.toList()[0]["event_type"] shouldBe RentalAssetEventType.DEPRECIATION.name
        }

        val res6 = rentalAssetHistoryRepository.findByReq(
            pageable = PageRequest.of(
                0,
                1
            )
        )
        res6.toList().size shouldBe 1
        res6.pageable.pageNumber shouldBe 0
        res6.pageable.pageSize shouldBe 1

        val res7 = rentalAssetHistoryRepository.findByReq(
            pageable = PageRequest.of(
                1,
                1
            )
        )
        res7.toList().size shouldBe 1
        res7.pageable.pageNumber shouldBe 1
        res7.pageable.pageSize shouldBe 1

        val res8 = rentalAssetHistoryRepository.findByReq(
            serialNumber = "no_data",
            pageable = PageRequest.of(
                0,
                1
            )
        )
        res8.toList().size shouldBe 0
        res8.pageable.pageNumber shouldBe 0
        res8.pageable.pageSize shouldBe 1
    }

    test("RentalAssetDepreciationScheduleRepository find,save") {
        val serialNumber = "RentalAssetDepreciationScheduleRepository_serial1"
        val res = rentalAssetDepreciationScheduleRepository.findBySerialNumber(serialNumber)
        res.size shouldBe 0

        rentalAssetDepreciationScheduleRepository.save(
            RentalAssetDepreciationSchedule(
                serialNumber = serialNumber,
                depreciationCount = 1,
                depreciationDate = LocalDate.of(2024, 11, 14),
                currency = "USD",
                beginningBookValue = BigDecimal("123.45"),
                depreciationExpense = BigDecimal("1.23"),
                endingBookValue = BigDecimal("121.12"),
                accumulatedDepreciation = BigDecimal("2.34")
            )
        )

        val res2 = rentalAssetDepreciationScheduleRepository.findBySerialNumber(serialNumber)
        res2.size shouldBe 1
        val data = res2[0]
        data.serialNumber shouldBe serialNumber
        data.depreciationCount shouldBe 1
        data.depreciationDate shouldBe LocalDate.of(2024, 11, 14)
        data.currency shouldBe "USD"
        data.beginningBookValue.setScale(2) shouldBe BigDecimal("123.45")
        data.depreciationExpense.setScale(2) shouldBe BigDecimal("1.23")
        data.endingBookValue.setScale(2) shouldBe BigDecimal("121.12")
        data.accumulatedDepreciation.setScale(2) shouldBe BigDecimal("2.34")
    }
})