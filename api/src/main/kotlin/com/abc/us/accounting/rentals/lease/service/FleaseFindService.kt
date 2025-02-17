package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.repository.RentalFinancialLeaseHistoryRepository
import com.abc.us.accounting.rentals.lease.domain.repository.RentalFinancialLeaseScheduleRepository
import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.MonthEndTarget
import com.abc.us.accounting.rentals.lease.model.RentalInstallGoodsTarget
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.ContractStatus
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class FleaseFindService(
    private val leaseFindService: LeaseFindService,

    private val collectOrderRepository: CollectOrderRepository,
    private val collectChannelRepository: CollectChannelRepository,
    private val collectContractRepository: CollectContractRepository,
    private val collectInstallationRepository: CollectInstallationRepository,
    private val collectMaterialRepository: CollectMaterialRepository,
    private val collectOrderItemRepository: CollectOrderItemRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalFinancialLeaseScheduleRepository: RentalFinancialLeaseScheduleRepository,
    private val rentalFinancialLeaseHistoryRepository: RentalFinancialLeaseHistoryRepository,
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
        leaseTypes: List<LeaseType>
    ): List<RentalInstallGoodsTarget> {
        val orderItems = collectOrderItemRepository.findAllByTimeRange(
            startTime = fromTime,
            endTime = toTime,
            orderItemStatuses = listOf(OrderItemStatus.INSTALL_COMPLETED)
        )
        val orderItemIds = orderItems.map {
            it.orderItemId
        }
        val contracts = collectContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            leaseTypes = leaseTypes
        )
        val materialIds = contracts.map {
            it.materialId!!
        }
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
        val histories = rentalFinancialLeaseHistoryRepository.findByContractIdInAndRentalEventType(
            contracts.map {
                it.contractId!!
            },
            RentalFinancialEventType.FLEASE_REGISTRATION.name
        )
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            contracts.map {
                it.orderItemId!!
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
            val history = histories.find {
                contract.contractId == it.contractId
            }
            if (history == null) {
                logger.warn("No history, contract:${contract.contractId}")
                return@mapNotNull null
            }
            val installation = installations.find {
                it.orderItemId == contract.orderItemId
            }
            if (installation == null) {
                logger.warn("No installation, contract:${contract.contractId}")
                return@mapNotNull null
            }
            RentalInstallGoodsTarget(
                contract,
                material,
                channel,
                rentalCodeMaster,
                history,
                installation
            )
        }
        return res
    }

    /**
     * 월마감작업 대상 조회
     */
    fun findMonthEndTarget(
        baseDate: LocalDate
    ): List<MonthEndTarget> {
        val validList = leaseFindService.findValidFinancialLeaseHistory(
            baseDate
        )
        val orderItemIds = validList.map {
            it.orderItemId!!
        }
        val installations = collectInstallationRepository.findValidByOrderItemIdIn(
            orderItemIds
        ).groupBy {
            it.serialNumber to it.orderItemId
        }.map {
            it.value.first()
        }
        val contracts = collectContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            contractStatuses = listOf(ContractStatus.ACTIVE.name),
        )
        val materialIds = contracts.map {
            it.materialId!!
        }
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
        val schedules = rentalFinancialLeaseScheduleRepository.findByContractIdsAndDate(
            contracts.map {
                it.contractId!!
            },
            baseDate
        )
        val res = validList.mapNotNull { valid ->
            val installation = installations.find {
                valid.serialNumber == it.serialNumber
            }
            if (installation == null) {
                logger.warn("No installation, valid:${valid.serialNumber}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                installation.orderItemId == it.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, installation:${installation.orderItemId}")
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
            val schedule = schedules.find {
                contract.contractId == it.contractId
            }
            if (schedule == null) {
                logger.warn("No schedule, contract:${contract.contractId}")
                return@mapNotNull null
            }
            MonthEndTarget(
                installation,
                contract,
                material,
                channel,
                rentalCodeMaster,
                schedule
            )
        }
        return res
    }
}