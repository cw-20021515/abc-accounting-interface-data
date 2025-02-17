package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.collects.domain.repository.CollectInventoryValuationRepository
import com.abc.us.accounting.iface.domain.repository.oms.*
import com.abc.us.accounting.rentals.lease.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseInqySchedule
import com.abc.us.accounting.rentals.lease.model.v2.MonthEndTargetV2
import com.abc.us.accounting.rentals.lease.model.v2.RentalInstallGoodsTargetV2
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.iface.domain.type.oms.*
import com.abc.us.accounting.rentals.lease.service.FleaseFindService
import com.abc.us.accounting.rentals.lease.service.v2.LeaseFindServiceV2.Companion
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class FleaseFindServiceV2(
    private val leaseFindServiceV2: LeaseFindServiceV2,

    private val ifOrderItemRepository: IfOrderItemRepository,
    private val ifChannelRepository: IfChannelRepository,
    private val ifContractRepository: IfContractRepository,
    private val ifServiceFlowRepository: IfServiceFlowRepository,
    private val ifMaterialRepository: IfMaterialRepository,
    private val collectInventoryValuationRepository: CollectInventoryValuationRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalFinancialLeaseHistoryRepository: RentalFinancialLeaseHistoryRepository,
    private val rentalFinancialLeaseScheduleRepository: RentalFinancialLeaseScheduleRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 재화매출 조회
     */
    fun findInstallGoodsTarget(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        leaseType: LeaseType
    ): List<RentalInstallGoodsTargetV2> {
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
        val histories = rentalFinancialLeaseHistoryRepository.findByContractIdInAndRentalEventType(
            contracts.map {
                it.contractId!!
            },
            RentalFinancialEventType.FLEASE_REGISTRATION.name
        )
        val res = orderItems.mapNotNull { orderItem ->
            val serviceFlow = serviceFlows.find {
                orderItem.orderItemId == it.orderItemId
            }
            if(serviceFlow == null) {
                logger.warn("No service flow found for ${orderItem.orderItemId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                it.orderItemId == serviceFlow.orderItemId
            }
            if(contract == null) {
                logger.warn("No contract found for ${serviceFlow.orderItemId}")
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
            val history = histories.find {
                contract.contractId == it.contractId
            }
            if (history == null) {
                logger.warn("No history, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalInstallGoodsTargetV2(
                serviceFlow,
                contract,
                orderItem,
                material,
                channel,
                rentalCodeMaster,
                history,
            )
        }
        return res
    }

    /**
     * 월마감작업 대상 조회
     */
    fun findMonthEndTarget(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        baseDate: LocalDate,
        leaseType: LeaseType
    ): List<MonthEndTargetV2> {
        val validList = leaseFindServiceV2.findValidFinancialLeaseHistoryV2(baseDate)
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
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            materialIds
        )
        val orderItemMap = RentalUtil.findChannelsByOrderItem(
            ifOrderItemRepository,
            ifChannelRepository,
            contracts.map {
                it.orderItemId
            }
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
        )
        val schedules = rentalFinancialLeaseScheduleRepository.findByContractIdsAndDate(
            contracts.map {
                it.contractId!!
            },
            baseDate
        )
        val res = validList.mapNotNull { valid ->
            val serviceFlow = serviceFlows.find {
                valid.orderItemId == it.orderItemId
            }
            if (serviceFlow == null) {
                logger.warn("No serviceFlow, orderItem:${valid.orderItemId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                it.orderItemId == serviceFlow.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, serviceFlow:${serviceFlow.serviceFlowId}")
                return@mapNotNull null
            }
            val orderItem = orderItemMap[contract.orderItemId]
            if (orderItem == null) {
                logger.warn("No order, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val material = materials.find {
                valid.materialId == it.materialId
            }
            if (material == null) {
                logger.warn("No material, orderItem:${valid.orderItemId}")
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
            val schedule = schedules.find {
                contract.contractId == it.contractId
            }
            if (schedule == null) {
                logger.warn("No schedule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            MonthEndTargetV2(
                serviceFlow,
                contract,
                orderItem,
                material,
                channel,
                rentalCodeMaster,
                schedule
            )
        }
        return res
    }
}