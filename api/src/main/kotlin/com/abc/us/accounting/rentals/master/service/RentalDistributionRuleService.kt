package com.abc.us.accounting.rentals.master.service

import com.abc.us.accounting.commons.domain.type.DuplicateHandlingPolicy
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.iface.domain.repository.oms.*
import com.abc.us.accounting.iface.domain.type.oms.*
import com.abc.us.accounting.rentals.master.domain.entity.*
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalDistributionMasterRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalDistributionRuleRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalPricingMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionType
import com.abc.us.accounting.supports.utils.IdGenerator
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.math.BigDecimal
import java.time.LocalDate


/**
 * RentalDistributionRule 서비스 (RentalDistributionRule 생성, 조회 등)
 * rental_master, rental_distribution_master, rental_price_master를 통해서 rental_distribution_rule 생성
 */
@Service
class RentalDistributionRuleService @Autowired constructor(
    private val materialRepository: IfMaterialRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalPricingMasterRepository: RentalPricingMasterRepository,
    private val rentalDistributionMasterRepository: RentalDistributionMasterRepository,
    private val rentalDistributionRuleRepository: RentalDistributionRuleRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 렌탈 안분규칙 생성
     * 중복 데이터 적제 방지를 위해 이미 적재된 데이터는 제외하고 생성
     */
    @Transactional
    fun generate(baseDate: LocalDate = LocalDate.now(),
                 duplicateHandlingPolicy: DuplicateHandlingPolicy = DuplicateHandlingPolicy.ALWAYS_OVERWRITE,
                 isSave:Boolean = true): List<RentalDistributionRule> {
        logger.info { "generate baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy, isSave=$isSave" }

        when (duplicateHandlingPolicy) {
            DuplicateHandlingPolicy.PRESERVE_EXISTING -> {
                val exists = rentalDistributionRuleRepository.findByBaseDate(baseDate)
                val candidates = generateRentalDistributionRulesByQuery(baseDate)
                val filtered = candidates.filter { !exists.contains(it) }

                logger.info {"baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy, exists=${exists.size}, candidates=${candidates.size}, filtered=${filtered.size}"}
                if (isSave && filtered.isNotEmpty()) {
                    rentalDistributionRuleRepository.saveAll(filtered)
                }
                return filtered
            }
            DuplicateHandlingPolicy.ALWAYS_OVERWRITE -> {
                logger.info { "baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy" }

                val candidates = generateRentalDistributionRulesByQuery(baseDate).sortedWith(
                    compareBy<RentalDistributionRule> {it.materialId}
                        .thenBy{it.rentalCode}
                        .thenBy{it.materialCareType}
                        .thenBy{it.startDate }
                )
                logger.info { "after generate candidates:${candidates.size} by baseDate=$baseDate" }

                val exists = rentalDistributionRuleRepository.findByBaseDate(baseDate).sortedWith(
                    compareBy<RentalDistributionRule> {it.materialId}
                        .thenBy{it.rentalCode}
                        .thenBy{it.materialCareType}
                        .thenBy{ it.startDate }
                ).toMutableList()
                logger.info { "after find exists:${exists.size} by baseDate=$baseDate" }

                var index = 0
                val merged = candidates.map { candidate ->
                    val exist = exists.firstOrNull{
                        val compare = it == candidate
                        compare
                    }
                    val selected = exist ?: candidate
                    val by = if (exist == null){ "candidate" } else {"exist"}

                    logger.debug{"index:${++index}, selected by ${by}, exist=$exist, candidate=$candidate"}
                    selected
                }
                if ( isSave ) {
//                    rentalDistributionRuleRepository.deleteByBaseDate(baseDate)
                    rentalDistributionRuleRepository.saveAll(merged)

                    logger.info {"baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy, candidates=${candidates.size}"}
                }
                return candidates
            }
            DuplicateHandlingPolicy.VERSION_BASED -> {
                logger.info { "baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy" }
                val exists = rentalDistributionRuleRepository.findByBaseDate(baseDate)
                val generates = generateRentalDistributionRulesByQuery(baseDate)
                logger.info { "after generate size:${generates.size} by baseDate=$baseDate" }

                // update 대상 확인
                val candidates = generates.filter { !exists.contains(it) }.filter { it.startDate < baseDate }.map { item ->
                    val target = exists.firstOrNull { it.hashCode() == item.hashCode() }
                    logger.debug { "target=$target, item=$item" }
                    if ( target == null ) {
                        item
                    } else {
                        item.overwrite(target!!)
                    }
                }
                logger.info { "after candidates size:${candidates.size} by baseDate=$baseDate" }

                if ( isSave ) {
                    rentalDistributionRuleRepository.saveAll(candidates)
                    logger.info {"baseDate=$baseDate, duplicateHandingPolicy=$duplicateHandlingPolicy, exists=${exists.size}, generates=${generates.size}, candidates=${candidates.size}"}
                }
                return candidates
            }
        }
    }

    fun generateRentalDistributionRules(baseDate:LocalDate = LocalDate.now()): List<RentalDistributionRule> {

        // 현재 유효한 일자로 필터링 해야 함
        val rentalDistributionMasters = rentalDistributionMasterRepository.findByBaseDate(baseDate)

        // 렌탈 마스터는 전체 로딩
        val rentalMasters = rentalCodeMasterRepository.findAll()

        // 렌탈 가격마스터 - 현재 유효한 것만 로딩
        val rentalPriceMasters = rentalPricingMasterRepository.findByBaseDate(baseDate)

        // 자재 마스터 - 전체 로딩
        val materialMasters = materialRepository.findAllByMaterialTypeIn(listOf(IfMaterialType.PRODUCT));

        /**
         * select cm.*, dm.*, rcm.*, rpm.*
         * from
         *     rental_distribution_master dm
         *     INNER JOIN rental_pricing_master rpm ON dm.material_series_code = rpm.material_series_code
         *     INNER JOIN rental_code_master rcm ON rcm.rental_code = rpm.rental_code
         *     INNER JOIN collect_material cm ON dm.material_series_code = cm.material_series_code
         * where cm.material_type = 'PRODUCT' and dm.start_date = '2024-01-01' and rpm.start_date = '2024-01-01';
         */

        /**
         * select cm.*, dm.*, rcm.*, rpm.*
         * from
         *     rental_distribution_master dm
         *         INNER JOIN rental_pricing_master rpm ON dm.material_series_code = rpm.material_series_code
         *         INNER JOIN rental_code_master rcm ON rcm.rental_code = rpm.rental_code
         *         INNER JOIN material cm ON dm.material_series_code = cm.material_series_code
         * where cm.material_type = 'PRODUCT' and dm.start_date = '2024-01-01' and rpm.start_date = '2024-01-01';
         */

        return generateRentalDistributionRules(rentalDistributionMasters, rentalMasters, rentalPriceMasters, materialMasters)
    }

    fun generateRentalDistributionRulesByQuery(baseDate:LocalDate = LocalDate.now()): List<RentalDistributionRule> {
        val rentalPriceRuleMappings = rentalDistributionRuleRepository.findRentalPriceRuleInfo(baseDate)
        logger.debug{"rentalPriceRuleInfos:${rentalPriceRuleMappings.size} by baseDate=$baseDate"}

        val results = rentalPriceRuleMappings.map {
            generateRentalDistributionRule(it.rentalDistributionMaster, it.rentalCodeMaster, it.rentalPricingMaster, it.material)
        }
        return results
    }


    /**
     * 렌탈 안분(재화/서비스) 분할규칙 생성
     */
    fun generateRentalDistributionRules(rentalDistributionMasters: List<RentalDistributionMaster>,
                                        rentalCodeMasters: List<RentalCodeMaster>,
                                        rentalPricingMasters: List<RentalPricingMaster>,
                                        materials: List<IfMaterial>
    ) : List<RentalDistributionRule> {

        logger.info("rentalDistributeMasters:${rentalDistributionMasters.size}, rentalCodeMasters:${rentalCodeMasters.size}, rentalPricingMasters:${rentalPricingMasters.size}, materials:${materials.size}")

        val rentalMasterMap = rentalCodeMasters.associateBy { it.rentalCode }
        val results = ArrayList<RentalDistributionRule>()

        for ( rentalDistributionMaster in rentalDistributionMasters ) {

            // materialModelNamePrefix에 해당하는 materialMasters 필터링 (materialId 확인용)
            val filteredMaterialMasters = materials.filter { it.materialModelNamePrefix == rentalDistributionMaster.materialModelNamePrefix }

            for ( materialMaster in filteredMaterialMasters ) {
                for ( rentalPriceMaster in rentalPricingMasters ) {
                    // materialModelNamePrefix가 다르면 skip
                    if ( rentalPriceMaster.materialModelNamePrefix != rentalDistributionMaster.materialModelNamePrefix ) {
                        logger.debug { "materialModelNamePrefix is different, skip!!" +
                            "rentalPriceMaster.materialModelNamePrefix=${rentalDistributionMaster.materialModelNamePrefix}, "+
                            "rentalDistributionMaster.materialModelNamePrefix=${rentalDistributionMaster.materialModelNamePrefix} "
                        }
                        continue;
                    }

                    // rentalCode 로 rentalMaster 확인
                    val rentalCode = rentalPriceMaster.rentalCode
                    val rentalMaster = rentalMasterMap[rentalCode]
                    if ( rentalMaster == null ) {
                        logger.debug { "rentalMaster must not be null, skip!!" }
                        continue;
                    }

                    val rentalDistributionRule = generateRentalDistributionRule(rentalDistributionMaster, rentalMaster, rentalPriceMaster, materialMaster)
                    results.add(rentalDistributionRule)
                }
            }
        }
        return results
    }


    /**
     * 렌탈 안분(재화/서비스 분할) 규칙 생성
     * rentalDistributionMaster를 기반으로 materialId 별로 데이터 생성
     */
    fun generateRentalDistributionRule(rentalDistributionMaster: RentalDistributionMaster,
                                       rentalCodeMaster: RentalCodeMaster,
                                       rentalPricingMaster: RentalPricingMaster,
                                       material: IfMaterial
    ): RentalDistributionRule {

        require(rentalDistributionMaster.materialModelNamePrefix == rentalPricingMaster.materialModelNamePrefix){
                      "materialModelNamePrefix must be same, but rentalDistributionMaster:${rentalDistributionMaster.materialModelNamePrefix}, rentalPriceMaster:${rentalPricingMaster.materialModelNamePrefix}"
        }

        //TODO: if table로 변경 필요
        require(material.materialModelNamePrefix == rentalPricingMaster.materialModelNamePrefix){
            "materialModelNamePrefix must be same, but materialMaster:${material.materialName}, rentalPriceMaster:${rentalPricingMaster.materialModelNamePrefix}"
        }

        require(rentalPricingMaster.rentalCode == rentalCodeMaster.rentalCode) {
                      "rentalCode must be same, but rentalPriceMaster:${rentalPricingMaster.rentalCode}, rentalMaster:${rentalCodeMaster}"
        }

        //TODO: 현재는 안분유형(DistributionType)은 SP02(재화+서비스) 만 지원
        val distributionType = rentalDistributionMaster.rentalDistributionType
        require(distributionType == RentalDistributionType.SP02) {
                      "distributionType must be SP02, but distributionType:${distributionType}"
        }


        // 조정 시작일자 (rental_distribution_master와 rental_price_master의 시작일 중 큰거)
        val adjustedStartDate = if ( rentalDistributionMaster.startDate compareTo rentalPricingMaster.startDate >= 0 ) rentalDistributionMaster.startDate else rentalPricingMaster.startDate

        // materialId 확인
        val materialId = material.materialId

        // 품목코드 확인
        val materialModelNamePrefix = rentalDistributionMaster.materialModelNamePrefix
        val rentalCode = rentalPricingMaster.rentalCode
        // 관리유형
        val careType = rentalPricingMaster.materialCareType
        // 리스유형 (운용리스, 금융리스)
        val leaseType = rentalCodeMaster.leaseType
        val contractPeriod = rentalCodeMaster.commitmentDuration
        val adjustedContractPeriod = rentalCodeMaster.adjustedContractPeriod
        val rentalPrice = rentalPricingMaster.price
        val purchasePrice = rentalDistributionMaster.onetimePrice
        val membershipPrice = rentalDistributionMaster.membershipPrice
        val membershipDiscountPriceC24 = rentalDistributionMaster.membershipDiscountPriceC24
        val freeServicePeriod = rentalDistributionMaster.freeServiceDuration

        val freeServiceValue = calcFreeServiceValue(membershipPrice, freeServicePeriod)
        val adjustedMembershipPrice = calcAdjustedMembershipPrice(membershipPrice, membershipDiscountPriceC24, contractPeriod)

        // 안분(재화/서비스) 판매가치 계산
        val distributionValue = calcDistributionValue(distributionType, purchasePrice, freeServiceValue,
                                                      adjustedContractPeriod, adjustedMembershipPrice)

        Assert.notNull(distributionValue, "distributionValue must be same, but distributionType:${distributionType}")

        // 안분(재화/서비스) 판매가지 비중 계산
        val distributionRatio = distributionValue.toRatio()

        // 안분(재화/서비스) 월 렌탈료 계산
        val distributionPrice = distributionRatio.toRentalPrice(rentalPrice!!)

        return RentalDistributionRule(
            id = IdGenerator.generateNumericId(),
            materialId = materialId,
            materialModelNamePrefix = materialModelNamePrefix,
            rentalCode = rentalCode!!,
            materialCareType = careType!!,
            leaseType = leaseType,
            commitmentDuration = contractPeriod,
            adjustedCommitmentDuration  = adjustedContractPeriod,
            distributionValue = distributionValue,
            distributionRatio = distributionRatio,
            distributionPrice = distributionPrice,
            startDate = adjustedStartDate!!
        )
    }



    /**
     * 조정 멤버십 가격
     * 약정개월수가 24개월 이상이면 할인가격 적용, 이하면 일반가격 적용
     */
    fun calcAdjustedMembershipPrice(membershipPrice:BigDecimal, membershipDiscountPriceC24:BigDecimal, contractPeriod:Int):BigDecimal{
        if (contractPeriod >= 24) {
            return membershipDiscountPriceC24
        }
        return membershipPrice
    }


    /**
     * 무상서비스 요금 계산
     * 멤버십가격($10) * 무상 서비스 기간(36개월)
     */
    fun calcFreeServiceValue(membershipPrice: BigDecimal, freeServicePeriod: Int):BigDecimal{
        return membershipPrice.multiply(BigDecimal.valueOf(freeServicePeriod.toLong()))
    }

    /**
     * 렌탈 안분(재화/서비스) 판매가치 계산
     */
    fun calcDistributionValue (rentalDistributionType: RentalDistributionType, purchasePrice:BigDecimal, freeServiceValue:BigDecimal,
                               adjustedContractPeriod:Int, adjustedMembershipPrice:BigDecimal): Distribution {
        Assert.isTrue(rentalDistributionType == RentalDistributionType.SP02, "distributionType must be SP02, but distributionType:${rentalDistributionType}")

        val m01 = purchasePrice.subtract(freeServiceValue)
        val s01 = adjustedMembershipPrice.multiply(BigDecimal(adjustedContractPeriod.toLong()))
        val t01 = m01.add(s01)
        return Distribution(m01, null, null, null, s01, t01)
    }

}