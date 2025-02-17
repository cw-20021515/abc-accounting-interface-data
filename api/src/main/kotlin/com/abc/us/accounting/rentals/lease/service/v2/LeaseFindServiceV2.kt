package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.collects.domain.entity.collect.CollectDeposit
import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.*
import com.abc.us.accounting.rentals.lease.model.v2.*
import com.abc.us.accounting.rentals.lease.service.OleaseService
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalDistributionRuleRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalPricingMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.iface.domain.entity.oms.IfChargePayment
import com.abc.us.accounting.iface.domain.model.PaymentAddress
import com.abc.us.accounting.iface.domain.repository.oms.*
import com.abc.us.accounting.iface.domain.type.oms.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class LeaseFindServiceV2(
    private val oleaseService: OleaseService,
    private val fleaseServiceV2: FleaseServiceV2,

    private val ifOrderItemRepository: IfOrderItemRepository,
    private val ifChannelRepository: IfChannelRepository,
    private val ifContractRepository: IfContractRepository,
    private val ifServiceFlowRepository: IfServiceFlowRepository,
    private val ifMaterialRepository: IfMaterialRepository,
    private val ifChargeRepository: IfChargeRepository,
    private val ifChargeItemRepository: IfChargeItemRepository,
    private val ifChargeInvoiceRepository: IfChargeInvoiceRepository,
    private val ifInvoiceRepository: IfInvoiceRepository,
    private val ifChargePaymentRepository: IfChargePaymentRepository,

    private val collectDepositRepository: CollectDepositRepository,
    private val collectInventoryValuationRepository: CollectInventoryValuationRepository,
    private val rentalDistributionRuleRepository: RentalDistributionRuleRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalPricingMasterRepository: RentalPricingMasterRepository,
    private val rentalFinancialDepreciationHistoryRepository: RentalFinancialDepreciationHistoryRepository,
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
    ): List<RentalAssetInstallationDataV2> {
        val orderItems = ifOrderItemRepository.findAllByTimeRange(
            startTime = fromTime,
            endTime = toTime,
            orderItemTypes = listOf(IfOrderItemType.RENTAL),
            orderItemStatuses = listOf(IfOrderItemStatus.INSTALL_COMPLETED),
        ).toList()
        val orderItemIds = orderItems.map {
            it.orderItemId
        }
        val contracts = ifContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            contractStatuses = listOf(IfContractStatus.ACTIVE),
            leaseTypes = listOf(leaseType)
        )
        val serviceFlows = ifServiceFlowRepository.findByOrderItemIdIn(
            orderItemIds = orderItemIds,
            serviceTypes = listOf(IfServiceFlowType.INSTALL),
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED)
        )
        val materialIds = orderItems.map {
            it.materialId
        }
        val inventoryValues = collectInventoryValuationRepository.findAllBy(
            materialIds,
            issueTime = toTime
        ).groupBy { it.materialId }
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            materialIds
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            orderItemIds
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val res = orderItems.mapNotNull { orderItem ->
            val serviceFlow = serviceFlows.find {
                orderItem.orderItemId == it.orderItemId
            }
            if (serviceFlow == null) {
                logger.warn("No serviceFlow, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                it.orderItemId == serviceFlow.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, serviceFlow:${serviceFlow.serviceFlowId}")
                return@mapNotNull null
            }
            val inventoryValue = inventoryValues[orderItem.materialId]?.firstOrNull()
            if (inventoryValue?.stockAvgUnitPrice == null) {
                logger.warn("No inventoryValue, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val material = materials.find {
                orderItem.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.channel
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
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                orderItem
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalAssetInstallationDataV2(
                serviceFlow,
                contract,
                orderItem,
                inventoryValue,
                material,
                channel,
                rentalCodeMaster,
                rentalDistributionRule,
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
    ): List<RentalAssetFilterTargetDataV2> {
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
        val serviceFlow = ifServiceFlowRepository.findSameMonth(
            orderItemIds,
            yearMonth.substring(0, 4),
            yearMonth.substring(5, 7)
        )
        val contracts = ifContractRepository.findAllByOrderItemIds(
            serviceFlow.map {
                it.orderItemId
            },
            listOf(IfContractStatus.ACTIVE)
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            contracts.map {
                it.orderItemId
            }
        )
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            orderItemMap.values.map {
                it.materialId
            }
        )
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val res = serviceFlow.mapNotNull { serviceFlow ->
            val contract = contracts.find {
                it.orderItemId == serviceFlow.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, serviceFlow:${serviceFlow.installId}")
                return@mapNotNull null
            }
            val orderItem = orderItemMap[contract.orderItemId]
            if (orderItem == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                orderItem.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.let { it.channel }
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
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                orderItem
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalAssetFilterTargetDataV2(
                serviceFlow,
                contract,
                orderItem,
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
        baseDate: LocalDate,
        eventTypes: List<RentalAssetEventType> = listOf(
            RentalAssetEventType.REGISTRATION,
            RentalAssetEventType.DEPRECIATION,
            RentalAssetEventType.TEMP_CLOSING_END
        )
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
        // 기본값은 자산등록, 감가상각, 가해약 취소 케이스만 처리
        val res = rentalAssetList.filter {
            val eventType = RentalAssetEventType.fromName(it.eventType!!)
            eventTypes.contains(eventType)
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
     * 유효한 금융리스이력 조회
     */
    fun findValidFinancialLeaseHistoryV2(
        baseDate: LocalDate
    ): List<ResRentalFinancialLeaseInqySchedule> {
        val list = fleaseServiceV2.findByReq(
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
    ): List<RentalProductShippedTargetV2> {
        val serviceFlows = ifServiceFlowRepository.findBy(
            serviceTypes = listOf(IfServiceFlowType.INSTALL),
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_SCHEDULED)
        ).groupBy {
            it.serviceFlowId
        }.map {
            it.value.first()
        }
        val orderItemIds = serviceFlows.map {
            it.orderItemId
        }
        val contracts = ifContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            leaseTypes = leaseTypes
        )
        val orderItems = ifOrderItemRepository.findAllByTimeRangeAndOrderItemIds(
            startTime = fromTime,
            endTime = toTime,
            orderItemIds = orderItemIds,
            orderItemTypes = listOf(IfOrderItemType.RENTAL),
            orderItemStatuses = listOf(IfOrderItemStatus.BOOKING_CONFIRMED)
        )
        val materialIds = orderItems.map {
            it.materialId
        }
        val inventoryValues = collectInventoryValuationRepository.findAllBy(
            materialIds,
            issueTime = toTime
        ).groupBy { it.materialId }
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            materialIds
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            orderItemIds
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
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
            val inventoryValue = inventoryValues[orderItem.materialId]?.firstOrNull()
            if (inventoryValue?.stockAvgUnitPrice == null) {
                logger.warn("No inventoryValue, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val material = materials.find { orderItem.materialId == it.materialId }
            if (material == null) {
                logger.warn("No material, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.channel
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
            RentalProductShippedTargetV2(
                serviceFlow,
                contract,
                orderItem,
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
        leaseTypes: List<LeaseType>,
        cancelChargeIds: List<String> = listOf()
    ): List<RentalBillingTargetV2> {
        val targetMonth = RentalUtil.getYearMonth(baseYearMonth)
        // 청구/청구 취소 구분
        val charges = if (cancelChargeIds.isEmpty()) {
            ifChargeRepository.findByTargetMonth(
                targetMonth = targetMonth,
                leaseTypes = leaseTypes
            )
        } else {
            ifChargeRepository.findByTargetMonth(
                targetMonth = targetMonth,
                leaseTypes = leaseTypes,
                chargeIds = cancelChargeIds
            )
        }
        val chargeIds = charges.map {
            it.chargeId
        }
        val rentalChargeMap = findRentalChargeMap(chargeIds)
        val contracts = ifContractRepository.findAllByChargeIds(
            chargeIds,
            listOf(IfContractStatus.ACTIVE)
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            contracts.map {
                it.orderItemId
            }
        )
        val serviceFlows = ifServiceFlowRepository.findByOrderItemIdIn(
            orderItemIds = contracts.map {
                it.orderItemId
            },
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED)
        )
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            orderItemMap.values.map {
                it.materialId
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val rentalPricingMasters = rentalPricingMasterRepository.findAll()
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val res = charges.mapNotNull { charge ->
            val rentalCharge = rentalChargeMap[charge.chargeId]
            if (rentalCharge == null) {
                logger.warn("No rentalCharge, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                it.contractId == charge.contractId
            }
            if (contract == null) {
                logger.warn("No contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val orderItem = orderItemMap[contract.orderItemId]
            if (orderItem == null) {
                logger.warn("No orderItem, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val subServiceFlows = serviceFlows.filter {
                orderItem.orderItemId == it.orderItemId
            }
            if (subServiceFlows.isEmpty()) {
                logger.warn("No serviceFlows, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val material = materials.find {
                orderItem.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.let { it.channel }
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
            val rentalPricingMaster = RentalUtil.getRentalPricingMaster(
                rentalPricingMasters,
                contract,
                material,
                orderItem
            )
            if (rentalPricingMaster == null) {
                logger.warn("No rentalPricingMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                orderItem
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalBillingTargetV2(
                charge,
                rentalCharge.chargeItems,
                rentalCharge.invoice,
                contract,
                orderItem,
                subServiceFlows,
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
    ): List<RentalPaymentTargetV2> {
        if (test) {
            val nextYearMonth = fromTime.toLocalDate().plusMonths(1)
            val list = findRentalBillingTarget(
                fromTime.toLocalDate(),
                leaseTypes
            ).map {
                val chargePayment = IfChargePayment(
                    id = "",
                    paymentId = it.charge.chargeId + "chargePayment",
                    transactionType = IfTransactionType.entries.first(),
                    chargeId = it.charge.chargeId,
                    totalPrice = it.invoice.totalPrice,
                    tax = BigDecimal.ZERO,
                    subtotalPrice = BigDecimal.ZERO,
                    currency = "USD",
                    paymentTime = nextYearMonth
                        .withDayOfMonth(20)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC),
                    chargeItems = listOf(),
                    address = PaymentAddress()
                )
                RentalPaymentTargetV2(
                    chargePayment = chargePayment,
                    contract = it.contract,
                    orderItem = it.orderItem,
                    serviceFlows = it.serviceFlows,
                    material = it.material,
                    channel = it.channel,
                    charge = it.charge,
                    invoice = it.invoice,
                    rentalCodeMaster = it.rentalCodeMaster,
                    rentalDistributionRule = it.rentalDistributionRule
                )
            }
            return list
        }
        val chargePayments = ifChargePaymentRepository.findAllByTimeRange(
            fromTime,
            toTime,
            leaseTypes
        )
        val chargeIds = chargePayments.map {
            it.chargeId
        }
        val charges = ifChargeRepository.findAllByIds(chargeIds)
        val rentalChargeMap = findRentalChargeMap(chargeIds)
        val contracts = ifContractRepository.findAllByChargeIds(
            chargeIds,
            listOf(IfContractStatus.ACTIVE)
        )
        val serviceFlows = ifServiceFlowRepository.findByOrderItemIdIn(
            orderItemIds = contracts.map {
                it.orderItemId
            },
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED)
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            contracts.map {
                it.orderItemId
            }
        )
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            orderItemMap.values.map {
                it.materialId
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val rentalDistributionRules = rentalDistributionRuleRepository.findAll()
        val res = chargePayments.mapNotNull { chargePayment ->
            val charge = charges.find { it.chargeId == chargePayment.chargeId }
            if (charge == null) {
                logger.warn("No charge, chargePayment:${chargePayment.paymentId}")
                return@mapNotNull null
            }
            val rentalCharge = rentalChargeMap[charge.chargeId]
            if (rentalCharge == null) {
                logger.warn("No rentalCharge, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val contract = contracts.find { it.contractId == charge.contractId }
            if (contract == null) {
                logger.warn("No contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val orderItem = orderItemMap[contract.orderItemId]
            if (orderItem == null) {
                logger.warn("No orderItem, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val subServiceFlows = serviceFlows.filter {
                orderItem.orderItemId == it.orderItemId
            }
            if (subServiceFlows.isEmpty()) {
                logger.warn("No serviceFlows, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val material = materials.find {
                orderItem.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.let { it.channel }
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
            val rentalDistributionRule = RentalUtil.getRentalDistributionRule(
                rentalDistributionRules,
                contract,
                orderItem
            )
            if (rentalDistributionRule == null) {
                logger.warn("No rentalDistributionRule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalPaymentTargetV2(
                chargePayment,
                contract,
                orderItem,
                subServiceFlows,
                material,
                channel,
                charge,
                rentalCharge.invoice,
                rentalCodeMaster,
                rentalDistributionRule
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
    ):List<RentalDepositTargetV2> {
        if (test) {
            val list = findRentalBillingTarget(
                fromTime.toLocalDate(),
                leaseTypes
            ).map {
                val nextYearMonth = fromTime.toLocalDate().plusMonths(1)
                val deposit = CollectDeposit()
                deposit.depositId = it.charge.chargeId + "_deposit"
                deposit.depositDate = nextYearMonth.withDayOfMonth(23)
                deposit.amount = it.invoice.totalPrice.toString()
                RentalDepositTargetV2(
                    deposit = deposit,
                    contract = it.contract,
                    orderItem = it.orderItem,
                    serviceFlows = it.serviceFlows,
                    material = it.material,
                    channel = it.channel,
                    charge = it.charge,
                    invoice = it.invoice,
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
        val chargePayments = ifChargePaymentRepository.findAll()
//        val receipts = collectReceiptRepository.findAllByTransactionIds(
//            deposits.filter {
//                !it.transactionId.isNullOrEmpty()
//            }.map {
//                it.transactionId!!
//            }.distinct()
//        )
        val chargeIds = chargePayments.map {
            it.chargeId
        }
        val charges = ifChargeRepository.findAllByIds(chargeIds)
        val rentalChargeMap = findRentalChargeMap(chargeIds)
        val contracts = ifContractRepository.findAllByChargeIds(
            chargeIds,
            IfContractStatus.entries
        )
        val serviceFlows = ifServiceFlowRepository.findByOrderItemIdIn(
            orderItemIds = contracts.map {
                it.orderItemId
            },
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED)
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            contracts.map {
                it.orderItemId
            }
        )
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            orderItemMap.values.map {
                it.materialId
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val rentalPricingMasters = rentalPricingMasterRepository.findAll()
        val res = deposits.mapNotNull { deposit ->
            val chargePayment = chargePayments.find {
                it.paymentId == deposit.transactionId
            }
            if (chargePayment == null) {
                logger.warn("No chargePayment, deposit:${deposit.depositId}")
                return@mapNotNull null
            }
            val charge = charges.find { it.chargeId == chargePayment.chargeId }
            if (charge == null) {
                logger.warn("No charge, chargePayment:${chargePayment.paymentId}")
                return@mapNotNull null
            }
            val rentalCharge = rentalChargeMap[charge.chargeId]
            if (rentalCharge == null) {
                logger.warn("No rentalCharge, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val contract = contracts.find { it.contractId == charge.contractId }
            if (contract == null) {
                logger.warn("No contract, charge:${charge.chargeId}")
                return@mapNotNull null
            }
            val orderItem = orderItemMap[contract.orderItemId]
            if (orderItem == null) {
                logger.warn("No orderItem, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val subServiceFlows = serviceFlows.filter {
                orderItem.orderItemId == it.orderItemId
            }
            if (subServiceFlows.isEmpty()) {
                logger.warn("No serviceFlows, orderItem:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val material = materials.find {
                orderItem.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val channel = orderItemMap[contract.orderItemId]?.let { it.channel }
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
            val rentalPricingMaster = RentalUtil.getRentalPricingMaster(
                rentalPricingMasters,
                contract,
                material,
                orderItem
            )
            if (rentalPricingMaster == null) {
                logger.warn("No rentalPricingMaster, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalDepositTargetV2(
                deposit,
                contract,
                orderItem,
                subServiceFlows,
                material,
                channel,
                charge,
                rentalCharge.invoice,
                rentalCodeMaster,
                rentalPricingMaster
            )
        }
        return res
    }

    /**
     * charge 정보 조회
     */
    fun findRentalChargeMap(
        chargeIds: List<String>
    ): Map<String, RentalCharge> {
        val chargeItems = ifChargeItemRepository.findByChargeIds(chargeIds)
        val chargeInvoices = ifChargeInvoiceRepository.findByChargeIds(chargeIds)
        val invoices = ifInvoiceRepository.findByIds(
            chargeInvoices.map {
                it.invoiceId
            }
        )
        val res: MutableMap<String, RentalCharge> = mutableMapOf()
        chargeIds.mapNotNull { chargeId ->
            val subChargeItems = chargeItems.filter {
                it.chargeId == chargeId
            }
            if (subChargeItems.isEmpty()) {
                logger.warn("No chargeItems, chargeId:${chargeId}")
                return@mapNotNull null
            }
            val chargeInvoice = chargeInvoices.find {
                it.chargeId == chargeId
            }
            if (chargeInvoice == null) {
                logger.warn("No chargeInvoice, chargeId:${chargeId}")
                return@mapNotNull null
            }
            val invoice = invoices.find {
                it.invoiceId == chargeInvoice.invoiceId
            }
            if (invoice == null) {
                logger.warn("No invoice, chargeInvoice:${chargeInvoice.id}")
                return@mapNotNull null
            }
            res.put(
                chargeId,
                RentalCharge(
                    subChargeItems,
                    chargeInvoice,
                    invoice
                )
            )
        }
        return res
    }
}