package com.abc.us.accounting.rentals.master.service

import com.abc.us.accounting.commons.domain.type.DuplicateHandlingPolicy
import com.abc.us.accounting.config.AppConfig
import com.abc.us.accounting.iface.domain.repository.oms.IfMaterialRepository
import com.abc.us.accounting.rentals.master.domain.repository.*
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.supports.utils.TimeLogger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.util.Lists
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class RentalDistributionRuleServiceTests (
    private val rentalDistributionRuleService: RentalDistributionRuleService,
    private val rentalDistributionRuleRepository: RentalDistributionRuleRepository,
    private val collectMaterialRepository: IfMaterialRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalPricingMasterRepository: RentalPricingMasterRepository,
    private val rentalDistributionMasterRepository: RentalDistributionMasterRepository,

    private val props:AppConfig = AppConfig(),
    private val timeLogger: TimeLogger = TimeLogger()
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun `data verify by 품목코드 - CHP-1300L, 렌탈코드 - NXXXXA`() {
        val materialModelNamePrefix = "CHP-1300L"
        val materialId = "WP_113627"
        val rentalCode = "NXXXXA"
        val materialCareType = MaterialCareType.SELF_CARE

        val rentalMaster = rentalCodeMasterRepository.findById(rentalCode).get()
        val material = collectMaterialRepository.findAllByMaterialIdIn(listOf(materialId)).firstOrNull()

        val rentalDistributionMaster = rentalDistributionMasterRepository.findByMaterialModelNamePrefixContains(Lists.list(materialModelNamePrefix)).first();
        val rentalPriceMaster = rentalPricingMasterRepository.findByMaterialModelNamePrefixAndRentalCodeAndCareType(materialModelNamePrefix, rentalCode).first();

        material shouldNotBe null

        rentalMaster.rentalCode shouldBe rentalCode
        rentalMaster.rentalCodeName shouldBe "NO"
        rentalMaster.rentalCodeDescription shouldBe "무약정"
        rentalMaster.term1Period shouldBe 0
        rentalMaster.term2Period shouldBe null
        rentalMaster.term3Period shouldBe null
        rentalMaster.term4Period shouldBe null
        rentalMaster.term5Period shouldBe null
        rentalMaster.currentTerm shouldBe 1
        rentalMaster.contractPricingType shouldBe ContractPricingType.NO_COMMITMENT
        rentalMaster.contractDuration shouldBe 0
        rentalMaster.commitmentDuration shouldBe 0
        rentalMaster.leaseType shouldBe LeaseType.OPERATING_LEASE
        rentalMaster.isActive shouldBe true

        rentalDistributionMaster.rentalDistributionType shouldBe RentalDistributionType.SP02
        rentalDistributionMaster.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalDistributionMaster.freeServiceDuration shouldBe 36
        rentalDistributionMaster.onetimePrice.compareTo(BigDecimal("1000")) shouldBe 0
        rentalDistributionMaster.membershipPrice.compareTo(BigDecimal("10")) shouldBe 0
        rentalDistributionMaster.membershipDiscountPriceC24.compareTo(BigDecimal("9")) shouldBe 0
        rentalDistributionMaster.startDate shouldBe LocalDate.parse("2024-01-01")

        rentalPriceMaster.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalPriceMaster.rentalCode shouldBe rentalCode
        rentalPriceMaster.materialCareType shouldBe materialCareType
        rentalPriceMaster.price!!.compareTo(BigDecimal("65")) shouldBe 0
        rentalPriceMaster.currency shouldBe Currency.getInstance("USD")
        rentalPriceMaster.taxIncluded shouldBe false
        rentalPriceMaster.periodType shouldBe PeriodType.MONTHLY
        rentalPriceMaster.startDate shouldBe LocalDate.parse("2025-02-01")

        val rentalDistributionRule = rentalDistributionRuleService.generateRentalDistributionRule (
            rentalDistributionMaster,
            rentalMaster,
            rentalPriceMaster,
            material!!
        )

        rentalDistributionRule.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalDistributionRule.rentalCode shouldBe rentalCode
        rentalDistributionRule.materialCareType shouldBe MaterialCareType.SELF_CARE
        rentalDistributionRule.commitmentDuration shouldBe 0
        rentalDistributionRule.adjustedCommitmentDuration shouldBe 60

        // 렌탈 안분(재화/서비스) 판매가치 가격 계산
        rentalDistributionRule.distributionValue.m01.compareTo(BigDecimal("640")) shouldBe 0
        rentalDistributionRule.distributionValue.r01 shouldBe null
        rentalDistributionRule.distributionValue.r02 shouldBe null
        rentalDistributionRule.distributionValue.r03 shouldBe null
        rentalDistributionRule.distributionValue.s01!!.compareTo(BigDecimal("600")) shouldBe 0
        rentalDistributionRule.distributionValue.total.compareTo(BigDecimal("1240")) shouldBe 0

        // 렌탈 안분(재화/서비스) 판매가치 비중 계산
        rentalDistributionRule.distributionRatio.m01.compareTo(BigDecimal("0.5161")) shouldBe 0
        rentalDistributionRule.distributionRatio.r01 shouldBe null
        rentalDistributionRule.distributionRatio.r02 shouldBe null
        rentalDistributionRule.distributionRatio.r03 shouldBe null
        rentalDistributionRule.distributionRatio.s01!!.compareTo(BigDecimal("0.4839")) shouldBe 0
        rentalDistributionRule.distributionRatio.total.compareTo(BigDecimal("1")) shouldBe 0

        // 렌탈 안분(재화/서비스) 판매가치 비중 계산
        rentalDistributionRule.distributionPrice.m01.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("33.55")) shouldBe 0
        rentalDistributionRule.distributionPrice.r01 shouldBe null
        rentalDistributionRule.distributionPrice.r02 shouldBe null
        rentalDistributionRule.distributionPrice.r03 shouldBe null
        rentalDistributionRule.distributionPrice.s01!!.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("31.45")) shouldBe 0
        rentalDistributionRule.distributionPrice.total.compareTo(BigDecimal("65")) shouldBe 0
    }

    @Test
    fun `data verify by 품목코드 - HP-1100R, 렌탈코드 - 7XXXXA`() {
        val materialModelNamePrefix = "HP-1100R"
        val materialId = "WP_113784"
        val rentalCode = "7XXXXA"
        val materialCareType = MaterialCareType.SELF_CARE

        // Master Data Loading
        val rentalMaster = rentalCodeMasterRepository.findById(rentalCode).get()
        val materialMaster = collectMaterialRepository.findAllByMaterialIdIn(listOf(materialId)).firstOrNull()
        val rentalDistributionMaster = rentalDistributionMasterRepository.findByMaterialModelNamePrefixContains(Lists.list(materialModelNamePrefix)).first();
        val rentalPriceMaster = rentalPricingMasterRepository.findByMaterialModelNamePrefixAndRentalCodeAndCareType(materialModelNamePrefix, rentalCode).first();

        materialMaster shouldNotBe null

        rentalMaster.rentalCode shouldBe rentalCode
        rentalMaster.rentalCodeName shouldBe "7Y"
        rentalMaster.rentalCodeDescription shouldBe "7년"
        rentalMaster.term1Period shouldBe 84
        rentalMaster.term2Period shouldBe null
        rentalMaster.term3Period shouldBe null
        rentalMaster.term4Period shouldBe null
        rentalMaster.term5Period shouldBe null
        rentalMaster.currentTerm shouldBe 1
        rentalMaster.contractPricingType shouldBe ContractPricingType.NEW_COMMITMENT
        rentalMaster.contractDuration shouldBe 84
        rentalMaster.commitmentDuration shouldBe 84
        rentalMaster.leaseType shouldBe LeaseType.FINANCIAL_LEASE
        rentalMaster.isActive shouldBe true

        rentalDistributionMaster.rentalDistributionType shouldBe RentalDistributionType.SP02
        rentalDistributionMaster.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalDistributionMaster.freeServiceDuration shouldBe 36
        rentalDistributionMaster.onetimePrice.compareTo(BigDecimal("1000")) shouldBe 0
        rentalDistributionMaster.membershipPrice.compareTo(BigDecimal("10")) shouldBe 0
        rentalDistributionMaster.membershipDiscountPriceC24.compareTo(BigDecimal("9")) shouldBe 0
        rentalDistributionMaster.startDate shouldBe LocalDate.parse("2024-01-01")

        rentalPriceMaster.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalPriceMaster.rentalCode shouldBe rentalCode
        rentalPriceMaster.materialCareType shouldBe materialCareType
        rentalPriceMaster.price!!.compareTo(BigDecimal("30")) shouldBe 0
        rentalPriceMaster.currency shouldBe Currency.getInstance("USD")
        rentalPriceMaster.taxIncluded shouldBe false
        rentalPriceMaster.periodType shouldBe PeriodType.MONTHLY
        rentalPriceMaster.startDate shouldBe LocalDate.parse("2025-02-01")

        val rentalDistributionRule = rentalDistributionRuleService.generateRentalDistributionRule (
            rentalDistributionMaster,
            rentalMaster,
            rentalPriceMaster,
            materialMaster!!
        )

        rentalDistributionRule.materialModelNamePrefix shouldBe materialModelNamePrefix
        rentalDistributionRule.rentalCode shouldBe rentalCode
        rentalDistributionRule.materialCareType shouldBe MaterialCareType.SELF_CARE
        rentalDistributionRule.commitmentDuration shouldBe 84
        rentalDistributionRule.adjustedCommitmentDuration shouldBe 84

        // 렌탈 안분(재화/서비스) 판매가치 가격 계산
        rentalDistributionRule.distributionValue.m01.compareTo(BigDecimal("640")) shouldBe 0
        rentalDistributionRule.distributionValue.r01 shouldBe null
        rentalDistributionRule.distributionValue.r02 shouldBe null
        rentalDistributionRule.distributionValue.r03 shouldBe null
        rentalDistributionRule.distributionValue.s01!!.compareTo(BigDecimal("756")) shouldBe 0
        rentalDistributionRule.distributionValue.total.compareTo(BigDecimal("1396")) shouldBe 0

        // 렌탈 안분(재화/서비스) 판매가치 비중 계산
        rentalDistributionRule.distributionRatio.m01.compareTo(BigDecimal("0.4585")) shouldBe 0
        rentalDistributionRule.distributionRatio.r01 shouldBe null
        rentalDistributionRule.distributionRatio.r02 shouldBe null
        rentalDistributionRule.distributionRatio.r03 shouldBe null
        rentalDistributionRule.distributionRatio.s01!!.compareTo(BigDecimal("0.5415")) shouldBe 0
        rentalDistributionRule.distributionRatio.total.compareTo(BigDecimal("1")) shouldBe 0

        // 렌탈 안분(재화/서비스) 판매가치 비중 계산
        rentalDistributionRule.distributionPrice.m01.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("13.76")) shouldBe 0
        rentalDistributionRule.distributionPrice.r01 shouldBe null
        rentalDistributionRule.distributionPrice.r02 shouldBe null
        rentalDistributionRule.distributionPrice.r03 shouldBe null
        rentalDistributionRule.distributionPrice.s01!!.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal("16.24")) shouldBe 0
        rentalDistributionRule.distributionPrice.total.compareTo(BigDecimal("30")) shouldBe 0
    }

    @Test
    fun `generates test`() {
        val baseDate = LocalDate.now()
        val rentalDistributionRules1  = rentalDistributionRuleService.generateRentalDistributionRulesByQuery(baseDate)
        var index = 1
        for ( rule in rentalDistributionRules1 ) {
            logger.info("index:${index++}, rule:${rule}")
        }

        // CollectMaster 기준 748개 (34*22)
        /**
         * select cm.*, dm.*, rcm.*, rpm.*
         * from
         *     rental_distribution_master dm
         *     INNER JOIN rental_pricing_master rpm ON dm.material_series_code = rpm.material_series_code
         *     INNER JOIN rental_code_master rcm ON rcm.rental_code = rpm.rental_code
         *     INNER JOIN collect_material cm ON dm.material_series_code = cm.material_series_code
         * where cm.material_type = 'PRODUCT' and dm.start_date = '2024-01-01' and rpm.start_date = '2024-01-01'
         */
        rentalDistributionRules1.size shouldBeGreaterThanOrEqual   34*20

        val rentalDistributionRules2 = rentalDistributionRuleService.generateRentalDistributionRules (baseDate)

        rentalDistributionRules2.size shouldBeGreaterThanOrEqual 34*20
        rentalDistributionRules1.size shouldBe rentalDistributionRules2.size
    }

    @Test
    fun `generate test with save option`() {
        val baseDate = LocalDate.now()
        var candidates = rentalDistributionRuleService.generate(baseDate, DuplicateHandlingPolicy.ALWAYS_OVERWRITE, false)
        candidates.size shouldBeGreaterThanOrEqual 680
        candidates = rentalDistributionRuleService.generate(baseDate, DuplicateHandlingPolicy.ALWAYS_OVERWRITE, false)
        candidates.size shouldBeGreaterThanOrEqual 680

        candidates = rentalDistributionRuleService.generate(baseDate, DuplicateHandlingPolicy.ALWAYS_OVERWRITE, true)
        candidates.size shouldBeGreaterThanOrEqual 680
        rentalDistributionRuleRepository.findByBaseDate(baseDate).size shouldBeGreaterThanOrEqual 680

    }


    @Test
    fun `generate with save test with preserve existing rule`() {
        val baseDate = LocalDate.now()
        val duplicateHandlingPolicy = DuplicateHandlingPolicy.PRESERVE_EXISTING
        timeLogger.measureAndLog {
            val candidates = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            candidates.size shouldBeGreaterThanOrEqual 0
            val updated = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            updated.size shouldBe  0
        }
    }

    @Test
    fun `generate with save test with always overwrite rule`() {
        val baseDate = LocalDate.now()
        val duplicateHandlingPolicy = DuplicateHandlingPolicy.ALWAYS_OVERWRITE

        timeLogger.measureAndLog {
            val candidates = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            candidates.size shouldBeGreaterThanOrEqual 680
            val updated = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            updated.size shouldBeGreaterThanOrEqual  680

            for ( rule in candidates ) {
                val duplicateItem = updated.find { it.hashCode() == rule.hashCode() }
                rule.updateTime shouldBeLessThan duplicateItem!!.updateTime
            }
        }
    }


    @Test
    fun `generate with save test with version based rule`() {
        val baseDate = LocalDate.now()
        val duplicateHandlingPolicy = DuplicateHandlingPolicy.VERSION_BASED

        timeLogger.measureAndLog {
            val candidates = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            candidates.size shouldBeGreaterThanOrEqual 0
            val updated = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)
            updated.size shouldBe  0

            for ( rule in candidates ) {
                val duplicateItem = updated.find { it.hashCode() == rule.hashCode() }
                logger.debug("duplicateItem:${duplicateItem} by ${duplicateItem.hashCode()} == ${rule.hashCode()}, rule:${rule}")
                if ( duplicateItem != null ) {
                    rule.updateTime shouldBeLessThan duplicateItem.updateTime
                }
            }
        }
    }

    @Test
    fun `generate 2 dates`() {
        val baseDate = LocalDate.of(2025,1,1)
        val duplicateHandlingPolicy = DuplicateHandlingPolicy.ALWAYS_OVERWRITE
        val gen1 = rentalDistributionRuleService.generate(baseDate, duplicateHandlingPolicy, true)

        val baseDate2 = LocalDate.of(2025,2,1)
        val gen2 = rentalDistributionRuleService.generate(baseDate2, duplicateHandlingPolicy, true)

        gen1.size shouldBe gen2.size
    }
}
