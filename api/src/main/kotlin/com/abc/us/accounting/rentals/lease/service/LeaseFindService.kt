package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.collects.domain.entity.collect.CollectDeposit
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.repository.RentalFinancialDepreciationHistoryRepository
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.*
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalDistributionRuleRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalPricingMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class LeaseFindService(
    private val oleaseService: OleaseService,

    private val collectReceiptRepository: CollectReceiptRepository,
    private val collectChargeRepository: CollectChargeRepository,
    private val collectChargeItemRepository: CollectChargeItemRepository,
    private val collectContractRepository: CollectContractRepository,
    private val collectDepositRepository: CollectDepositRepository,
    private val collectInventoryValuationRepository: CollectInventoryValuationRepository,
    private val collectServiceFlowRepository: CollectServiceFlowRepository,
    private val collectInstallationRepository: CollectInstallationRepository,
    private val rentalDistributionRuleRepository: RentalDistributionRuleRepository,
    private val collectMaterialRepository: CollectMaterialRepository,
    private val collectChannelRepository: CollectChannelRepository,
    private val collectOrderItemRepository: CollectOrderItemRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalPricingMasterRepository: RentalPricingMasterRepository,
    private val collectOrderRepository: CollectOrderRepository,
    private val rentalFinancialDepreciationHistoryRepository: RentalFinancialDepreciationHistoryRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 렌탈 설치 정보 조회
     */
    fun findInstallationInfo(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        leaseType: LeaseType
    ): List<RentalAssetInstallationData> {
        val orderItems = collectOrderItemRepository.findAllByTimeRange(
            startTime = fromTime,
            endTime = toTime,
            orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)
        )
        val orderItemIds = orderItems.map {
            it.orderItemId!!
        }
        val contracts = collectContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            contractStatuses = listOf(ContractStatus.ACTIVE.name),
            leaseTypes = listOf(leaseType)
        )
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            orderItemIds
        ).groupBy {
            it.serialNumber to it.orderItemId
        }.map {
            it.value.first()
        }
        val materialIds = contracts.map {
            it.materialId!!
        }
        val inventoryValues = collectInventoryValuationRepository.findAllBy(
            materialIds,
            issueTime = toTime
        ).groupBy { it.materialId }
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            materialIds
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val res = orderItems.mapNotNull { orderItem ->
            val installation = installations.find {
                orderItem.orderItemId == it.orderItemId
            }
            if (installation == null) {
                logger.warn("No installation, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                it.orderItemId == installation.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, installation:${installation.installId}")
                return@mapNotNull null
            }
            val inventoryValue = inventoryValues[contract.materialId]?.firstOrNull()
            if (inventoryValue?.stockAvgUnitPrice == null) {
                logger.warn("No inventoryValue, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                contract.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.channel
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val order = orderMap[contract.orderId]
            if (order == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                order
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalAssetInstallationData(
                installation,
                contract,
                inventoryValue,
                material,
                channel,
                rentalCodeMaster,
                rentalDistributionRule,
                order
            )
        }
        return res
    }

    /**
     * 필터배송 대상 조회
     */
    fun findFilterTarget(
        baseYearMonth: LocalDate,
        leaseType: LeaseType
    ): List<RentalAssetFilterTargetData> {
        val baseDate = RentalUtil.getLastDate(baseYearMonth)
        val yearMonth = RentalUtil.getYearMonth(baseYearMonth)
        val orderItemIds = if (leaseType == LeaseType.OPERATING_LEASE) {
            findValidRentalAssetHistory(baseDate).map {
                it.orderItemId
            }
        } else {
            findValidFinancialLeaseHistory(baseDate).map {
                it.orderItemId!!
            }
        }
        val installations = collectInstallationRepository.findSameMonth(
            orderItemIds,
            yearMonth.substring(0, 4),
            yearMonth.substring(5, 7)
        ).groupBy {
            it.serialNumber to it.orderItemId
        }.map {
            it.value.first()
        }
        val contracts = collectContractRepository.findAllByOrderItemIds(
            installations.map {
                it.orderItemId!!
            },
            listOf(ContractStatus.ACTIVE.name)
        )
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            contracts.map {
                it.materialId!!
            }
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val res = installations.mapNotNull { installation ->
            val contract = contracts.find {
                it.orderItemId == installation.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, installation:${installation.installId}")
                return@mapNotNull null
            }
            val material = materials.find {
                contract.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.let { it.channel }
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val order = orderMap[contract.orderId]
            if (order == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                order
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalAssetFilterTargetData(
                installation,
                contract,
                material,
                channel,
                rentalCodeMaster,
                RentalAssetPriceData(
                    productPrice = rentalDistributionRule.distributionPrice.m01,
                    servicePrice = rentalDistributionRule.distributionPrice.s01!!
                )
            )
        }
        return res
    }

    /**
     * 유효한 렌탈자산이력 조회
     */
    fun findValidRentalAssetHistory(
        baseDate: LocalDate
    ): List<RentalAssetHistoryItemData> {
        // 렌탈자산이력 조회
        val rentalAssetList = oleaseService.findByReq(
            RentalAssetHistoryRequest(
                baseDate
            )
        ).groupBy {
            it.serialNumber to it.orderItemId
        }.map {
            it.value.first()
        }
        // 자산등록, 감가상각(일반) 케이스만 처리
        val res = rentalAssetList.filter {
            val eventType = RentalAssetEventType.fromName(it.eventType!!)
            when (eventType) {
                RentalAssetEventType.REGISTRATION,
                RentalAssetEventType.DEPRECIATION
                -> true
                else
                -> false
            }
        }.toList()
        return res
    }

    /**
     * 유효한 금융리스이력 조회
     */
    fun findValidFinancialLeaseHistory(
        baseDate: LocalDate
    ): List<ResRentalFinancialLeaseInqyScheduleTemp> {
        val list = rentalFinancialDepreciationHistoryRepository.selectBySearchRentalsList(
            ReqRentalFinancialLeaseInqySchedule(
                baseDate = baseDate
            ),
            null
        )
        // 자산등록, 상각 케이스만 처리
        val res = list?.filter {
            val eventType = RentalFinancialEventType.fromString(it.rentalEventType)
            when (eventType) {
                RentalFinancialEventType.FLEASE_REGISTRATION,
                RentalFinancialEventType.FLEASE_DEPRECIATION
                -> true
                else
                -> false
            }
        }?.toList() ?: listOf()
        return res
    }

    /**
     * 제품출고 조회
     */
    fun findProductShippedTarget(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        leaseTypes: List<LeaseType>
    ): List<RentalProductShippedTarget> {
        val serviceFlows = collectServiceFlowRepository.findBy(
            serviceTypes = listOf(ServiceFlowType.INSTALL),
            serviceStatues = listOf(ServiceFlowStatus.SERVICE_SCHEDULED)
        ).groupBy {
            it.serviceFlowId
        }.map {
            it.value.first()
        }
        val orderItemIds = serviceFlows.map {
            it.orderItemId!!
        }
        val contracts = collectContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            leaseTypes = leaseTypes
        )
        val orderItems = collectOrderItemRepository.findAllByTimeRangeAndOrderItemIds(
            startTime = fromTime,
            endTime = toTime,
            orderItemIds = orderItemIds,
            orderItemStatuses = listOf(OrderItemStatus.BOOKING_CONFIRMED)
        )
        val materialIds = contracts.map {
            it.materialId!!
        }
        val inventoryValues = collectInventoryValuationRepository.findAllBy(
            materialIds,
            issueTime = toTime
        ).groupBy { it.materialId }
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            materialIds
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val res = orderItems.mapNotNull { orderItem ->
            val contract = contracts.find {
                orderItem.orderItemId == it.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val serviceFlow = serviceFlows.find {
                contract.orderItemId == it.orderItemId
            }
            if (serviceFlow == null) {
                logger.warn("No serviceFlow, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val inventoryValue = inventoryValues[contract.materialId]?.firstOrNull()
            if (inventoryValue?.stockAvgUnitPrice == null) {
                logger.warn("No inventoryValue, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find { contract.materialId == it.materialId }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.channel
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalProductShippedTarget(
                serviceFlow,
                contract,
                inventoryValue,
                material,
                channel,
                rentalCodeMaster
            )
        }
        return res
    }

    /**
     * 청구 대상 조회
     */
    fun findRentalBillingTarget(
        baseYearMonth: LocalDate,
        leaseTypes: List<LeaseType>
    ): List<RentalBillingTarget> {
        val targetMonth = RentalUtil.getYearMonth(baseYearMonth)
        val charges = collectChargeRepository.findByTargetMonth(
            targetMonth = targetMonth,
//            listOf(ChargeStatusEnum.SCHEDULED),
            leaseTypes = leaseTypes
        )
        val chargeIds = charges.map {
            it.chargeId
        }
        val chargeItemsGroup = collectChargeItemRepository.findByChargeIds(
            chargeIds
        ).groupBy {
            it.chargeId
        }
        val contracts = collectContractRepository.findAllByChargeIds(
            chargeIds,
            listOf(ContractStatus.ACTIVE.name)
        )
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            contracts.map {
                it.orderItemId!!
            }
        )
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            contracts.map {
                it.materialId!!
            }
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val rentalPricingMasters = rentalPricingMasterRepository.findAll()
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val res = charges.mapNotNull { charge ->
            val chargeItems = chargeItemsGroup[charge.chargeId]
            val contract = contracts.find {
                it.contractId == charge.contractId
            }
            if (chargeItems == null || contract == null) {
                logger.warn("No chargeItems or contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val installation = installations.find {
                it.orderItemId == contract.orderItemId
            }
            if (installation == null) {
                logger.warn("No installation, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                contract.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.let { it.channel }
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val order = orderMap[contract.orderId]
            if (order == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalPricingMaster = RentalUtil.getRentalPricingMaster(
                rentalPricingMasters,
                contract,
                material,
                order
            )
            if (rentalPricingMaster == null) {
                logger.warn("No rentalPricingMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                order
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalBillingTarget(
                charge,
                chargeItems,
                contract,
                installation,
                material,
                channel,
                rentalCodeMaster,
                rentalPricingMaster,
                rentalDistributionRule
            )
        }
        return res
    }

    /**
     * 수납 대상 조회
     */
    fun findPaymentTarget(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        leaseTypes: List<LeaseType>,
        test: Boolean = false
    ): List<RentalPaymentTarget> {
        if (test) {
            val list = findRentalBillingTarget(
                fromTime.toLocalDate(),
                leaseTypes
            ).map {
                val receipt = CollectReceipt(
                    relation = EmbeddableRelation(),
                    name = EmbeddableName(),
                    location = EmbeddableLocation(),
                    price = EmbeddablePrice(
                        totalPrice = it.charge.price.totalPrice,
                        currency = "USD"
                    )
                ).apply {
                    val nextYearMonth = fromTime.toLocalDate().plusMonths(1)
                    receiptId = it.charge.chargeId + "_receipt"
                    receiptTime = nextYearMonth.withDayOfMonth(20)
                        .atStartOfDay().atOffset(ZoneOffset.UTC)
                }
                RentalPaymentTarget(
                    receipt = receipt,
                    contract = it.contract,
                    installation = it.installation,
                    material = it.material,
                    channel = it.channel,
                    charge = it.charge,
                    rentalCodeMaster = it.rentalCodeMaster,
                    rentalPricingMaster = it.rentalPricingMaster
                )
            }
            return list
        }
        val receipts = collectReceiptRepository.findAllByTimeRange(
            fromTime,
            toTime,
            leaseTypes
        )
        val chargeIds = receipts.filter {
            !it.chargeId.isNullOrEmpty()
        }.map {
            it.chargeId!!
        }.distinct()
        val charges = collectChargeRepository.findAllByIds(
            chargeIds
        )
        val contracts = collectContractRepository.findAllByChargeIds(
            chargeIds,
            listOf(ContractStatus.ACTIVE.name)
        )
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            contracts.map {
                it.orderItemId!!
            }
        )
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            contracts.map {
                it.materialId!!
            }
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val rentalPricingMasters = rentalPricingMasterRepository.findAll()
        val res = receipts.mapNotNull { receipt ->
            val charge = charges.find { it.chargeId == receipt.chargeId }
            if (charge == null) {
                logger.warn("No charge, receipt:${receipt.receiptId}")
                return@mapNotNull null
            }
            val contract = contracts.find { it.contractId == charge.contractId }
            if (contract == null) {
                logger.warn("No contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val installation = installations.find { it.orderItemId == contract.orderItemId }
            if (installation == null) {
                logger.warn("No installation, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                contract.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.let { it.channel }
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val order = orderMap[contract.orderId]
            if (order == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalPricingMaster = RentalUtil.getRentalPricingMaster(
                rentalPricingMasters,
                contract,
                material,
                order
            )
            if (rentalPricingMaster == null) {
                logger.warn("No rentalPricingMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalPaymentTarget(
                receipt,
                contract,
                installation,
                material,
                channel,
                charge,
                rentalCodeMaster,
                rentalPricingMaster
            )
        }
        return res
    }

    /**
     * 입금 대상 조회
     */
    fun findDepositTarget(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        leaseTypes: List<LeaseType>,
        test: Boolean = false
    ):List<RentalDepositTarget> {
        if (test) {
            val list = findRentalBillingTarget(
                fromTime.toLocalDate(),
                leaseTypes
            ).map {
                val nextYearMonth = fromTime.toLocalDate().plusMonths(1)
                val deposit = CollectDeposit()
                deposit.depositId = it.charge.chargeId + "_deposit"
                deposit.depositDate = nextYearMonth.withDayOfMonth(23)
                deposit.amount = it.charge.price.totalPrice.toString()
                RentalDepositTarget(
                    deposit = deposit,
                    contract = it.contract,
                    installation = it.installation,
                    material = it.material,
                    channel = it.channel,
                    charge = it.charge,
                    rentalCodeMaster = it.rentalCodeMaster,
                    rentalPricingMaster = it.rentalPricingMaster
                )
            }
            return list
        }
        val deposits = collectDepositRepository.findAllByTimeRange(
            fromTime,
            toTime,
            leaseTypes
        )
        val receipts = collectReceiptRepository.findAllByTransactionIds(
            deposits.filter {
                !it.transactionId.isNullOrEmpty()
            }.map {
                it.transactionId!!
            }.distinct()
        )
        val chargeIds = receipts.filter {
            !it.chargeId.isNullOrEmpty()
        }.map {
            it.chargeId!!
        }.distinct()
        val charges = collectChargeRepository.findAllByIds(
            chargeIds
        )
        val contracts = collectContractRepository.findAllByChargeIds(
            chargeIds,
            listOf(ContractStatus.ACTIVE.name)
        )
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            contracts.map {
                it.orderItemId!!
            }
        )
        val materials = collectMaterialRepository.findAllByMaterialIdIn(
            contracts.map {
                it.materialId!!
            }
        )
        val orderMap = RentalUtil.findChannelsByOrder(
            collectOrderRepository,
            collectChannelRepository,
            contracts.map {
                it.orderId!!
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val rentalPricingMasters = rentalPricingMasterRepository.findAll()
        val res = deposits.mapNotNull { deposit ->
            val receipt = receipts.find { it.transactionId == deposit.transactionId }
            if (receipt == null) {
                logger.warn("No receipt, deposit:${deposit.depositId}")
                return@mapNotNull null
            }
            val charge = charges.find { it.chargeId == receipt.chargeId }
            if (charge == null) {
                logger.warn("No charge, receipt:${receipt.receiptId}")
                return@mapNotNull null
            }
            val contract = contracts.find { it.contractId == charge.contractId }
            if (contract == null) {
                logger.warn("No contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val installation = installations.find {
                it.orderItemId == contract.orderItemId
            }
            if (installation == null) {
                logger.warn("No installation, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                contract.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderMap[contract.orderId]?.let { it.channel }
            if (channel == null) {
                logger.warn("No channel, contract:${contract.contractId}")
            }
            val rentalCodeMaster = rentalCodeMasters.find {
                contract.rentalCode == it.rentalCode
            }
            if (rentalCodeMaster == null) {
                logger.warn("No rentalCodeMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val order = orderMap[contract.orderId]
            if (order == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalPricingMaster = RentalUtil.getRentalPricingMaster(
                rentalPricingMasters,
                contract,
                material,
                order
            )
            if (rentalPricingMaster == null) {
                logger.warn("No rentalPricingMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalDepositTarget(
                deposit,
                contract,
                installation,
                material,
                channel,
                charge,
                rentalCodeMaster,
                rentalPricingMaster
            )
        }
        return res
    }
}