package com.abc.us.accounting.rentals.master.domain.repository

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.rentals.master.domain.type.MaterialCareType
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.master.domain.type.ContractPricingType
import com.google.common.collect.Lists
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ImportAutoConfiguration(exclude = [SecurityAutoConfiguration::class, WebMvcAutoConfiguration::class])
//@Import(MockBeanHandler::class)
@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class RentalMasterRepositoryTests(
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalPricingMasterRepository: RentalPricingMasterRepository,
    private val rentalDistributionMasterRepository: RentalDistributionMasterRepository,
) : FunSpec({

    test("rental_master repository") {
        val data = rentalCodeMasterRepository.findAll()
        logger.info("data size:${data.size}")
        data.size shouldBe 34

        val activeList = data.stream().filter{ it -> it.isActive }.toList()
        activeList.size shouldBe 17

        val rentalCode = "NXXXXA"
        val rentalMaster = rentalCodeMasterRepository.findById(rentalCode)

        rentalMaster.get().rentalCode shouldBe rentalCode
        rentalMaster.get().rentalCodeName shouldBe "NO"
        rentalMaster.get().term1Period shouldBe 0
        rentalMaster.get().term2Period shouldBe null
        rentalMaster.get().term3Period shouldBe null
        rentalMaster.get().term4Period shouldBe null
        rentalMaster.get().term5Period shouldBe null
        rentalMaster.get().currentTerm shouldBe 1
        rentalMaster.get().contractPricingType shouldBe ContractPricingType.NO_COMMITMENT
        rentalMaster.get().contractDuration shouldBe 0
        rentalMaster.get().commitmentDuration shouldBe 0
        rentalMaster.get().leaseType shouldBe LeaseType.OPERATING_LEASE
    }


    test("rental_price_master repository") {
        val baseDate = LocalDate.of(2025, 2, 1)
        val data = rentalPricingMasterRepository.findByBaseDate(baseDate)
        data.size shouldBe 204

        val rentalCode = "NXXXXA"
        val activeList = data.stream().filter{ it -> it.materialModelNamePrefix=="CHP-1300L"
                && it.rentalCode== rentalCode
                && it.materialCareType == MaterialCareType.SELF_CARE}.toList()
        activeList.size shouldBe 1

        val rentalPrice = activeList.first()

        logger.info ("rentalPrice:$rentalPrice")

        rentalPrice.rentalCode shouldBe  rentalCode
        rentalPrice.materialCareType shouldBe MaterialCareType.SELF_CARE
        rentalPrice.materialModelNamePrefix shouldBe "CHP-1300L"
        rentalPrice.price shouldBe BigDecimal("65.0").setScale(Constants.ACCOUNTING_SCALE)
        rentalPrice.currency shouldBe Currency.getInstance(Locale.US)
    }

    test ( "rental_price_master with baseDate"){
        var baseDate = LocalDate.of(2024, 10, 2)
        var data = rentalPricingMasterRepository.findByBaseDate(baseDate)
        data.size shouldBe 204

        for ( rentalPriceMaster in data) {
            rentalPriceMaster.startDate shouldBe LocalDate.of(2024, 1, 1)
        }

        baseDate = LocalDate.of (2023, 1, 1)
        data = rentalPricingMasterRepository.findByBaseDate(baseDate)
        data.size shouldBe 0

        baseDate = LocalDate.of (2024, 1, 1)
        data = rentalPricingMasterRepository.findByBaseDate(baseDate)
        data.size shouldBe 204

        for ( rentalPriceMaster in data) {
            rentalPriceMaster.startDate shouldBe LocalDate.of(2024, 1, 1)
        }

        baseDate = LocalDate.of (2025, 1, 1)
        data = rentalPricingMasterRepository.findByBaseDate(baseDate)
        data.size shouldBe 204
        for ( rentalPriceMaster in data ) {
            rentalPriceMaster.startDate shouldBe LocalDate.of(2024, 1, 1)
        }

    }

    test ("rental_price_master with materialSeriesCodes") {
        val materialModelNamePrefixes = Lists.newArrayList("CHP-1300L", "CHP-1200N")
        materialModelNamePrefixes.size shouldBe 2

        val rentalPriceMasters = rentalPricingMasterRepository.findByMaterialModelNamePrefixContains(materialModelNamePrefixes)
        rentalPriceMasters.size shouldBe 68
    }

    test("rental_distribution_master repository") {
        val data = rentalDistributionMasterRepository.findAll()
        data.size shouldBe 6
        val materialModelNamePrefixes = Lists.newArrayList("CHP-1300L", "CHP-1200N")

        val activeList = data.stream().filter{ it -> materialModelNamePrefixes.contains(it.materialModelNamePrefix) }.toList()
        activeList.size shouldBe 2

        val expectedList = rentalDistributionMasterRepository.findByMaterialModelNamePrefixContains(materialModelNamePrefixes)

        activeList shouldBeEqual expectedList
    }
}) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
