package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.iface.domain.repository.oms.*
import com.abc.us.accounting.iface.domain.type.oms.*
import com.abc.us.accounting.rentals.lease.domain.repository.*
import com.abc.us.accounting.rentals.lease.model.v2.RentalAssetDepreciationTargetDataV2
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.model.v2.RentalChangeStateTarget
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OleaseFindServiceV2(
    private val leaseFindServiceV2: LeaseFindServiceV2,

    private val ifServiceFlowRepository: IfServiceFlowRepository,
    private val ifContractRepository: IfContractRepository,
    private val ifMaterialRepository: IfMaterialRepository,
    private val ifOrderItemRepository: IfOrderItemRepository,
    private val ifChannelRepository: IfChannelRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
    private val rentalAssetDepreciationScheduleRepository: RentalAssetDepreciationScheduleRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 렌탈자산 상각 대상 조회
     */
    fun findDepreciationTarget(
        baseDate: LocalDate
    ): List<RentalAssetDepreciationTargetDataV2> {
        val validList = leaseFindServiceV2.findValidRentalAssetHistory(baseDate)
        val serialNumbers = validList.map {
            it.serialNumber
        }
        val serviceFlows = ifServiceFlowRepository.findBy(
            serviceTypes = listOf(IfServiceFlowType.INSTALL),
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED),
            serialNumbers = serialNumbers
        )
        val contracts = ifContractRepository.findAllByOrderItemIds(
            serviceFlows.map {
                it.orderItemId
            },
            IfContractStatus.entries
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
        val schedules = rentalAssetDepreciationScheduleRepository.findBySerialNumbersAndDate(
            serialNumbers,
            baseDate
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode
            }
        )
        val res = validList.mapNotNull { valid ->
            val orderItem = orderItemMap[valid.orderItemId]!!
            val serviceFlow = serviceFlows.find {
                valid.serialNumber == it.serialNumber
            }
            if (serviceFlow == null) {
                logger.warn("No serviceFlow, valid:${valid.serialNumber}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                serviceFlow.orderItemId == it.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, serviceFlow:${serviceFlow.serviceFlowId}")
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
            val schedule = schedules.find {
                valid.serialNumber == it.serialNumber
            }
            if (schedule == null) {
                logger.warn("No schedule, valid:${valid.serialNumber}")
                return@mapNotNull null
            }
            RentalAssetDepreciationTargetDataV2(
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

    /**
     * 자산 상태 변경 대상 조회
     */
    fun findChangeStateTarget(
        baseDate: LocalDate
    ): List<RentalChangeStateTarget> {
        // 자산 상태 변경 대상 조회(등록, 감가상각, 가해약 등록/취소)
        val rentalAssetList = leaseFindServiceV2.findValidRentalAssetHistory(
            baseDate,
            listOf(
                RentalAssetEventType.REGISTRATION,
                RentalAssetEventType.DEPRECIATION,
                RentalAssetEventType.TEMP_CLOSING_START,
                RentalAssetEventType.TEMP_CLOSING_END
            )
        )
        val orderItemIds = rentalAssetList.map { it.orderItemId }
        val contracts = ifContractRepository.findAllByOrderItemIds(
            orderItemIds = orderItemIds,
            leaseTypes = listOf(LeaseType.OPERATING_LEASE)
        )
        val orderItems = ifOrderItemRepository.findByOrderItemIdsIn(
            orderItemIds = orderItemIds
        )
        val res = rentalAssetList.mapNotNull { rentalAsset ->
            val orderItem = orderItems.find {
                rentalAsset.orderItemId == it.orderItemId
            }
            if (orderItem == null) {
                logger.warn("No orderItem, orderItemId:${rentalAsset.orderItemId}")
                return@mapNotNull null
            }
            val contract = contracts.find {
                orderItem.orderItemId == it.orderItemId
            }
            if (contract == null) {
                logger.warn("No contract, orderItemId:${orderItem.orderItemId}")
                return@mapNotNull null
            }
            /**
             * 1. 계약종료
             * 2. 주문철회
             * 3. 해지
             * 4. 가해약 등록
             * 5. 가해약 취소
             * 6. 추심전환
             */
            val contractStatus = contract.contractStatus
            val rentalAssetEventType = RentalAssetEventType.fromName(rentalAsset.eventType!!)
            val eventType = if (contractStatus == IfContractStatus.CONTRACT_ENDED) {
                // 계약종료
                RentalAssetEventType.CONTRACT_ENDED
            } else if (contractStatus == IfContractStatus.CONTRACT_WITHDRAWN) {
                // 주문철회
                RentalAssetEventType.CONTRACT_WITHDRAWN
            } else if (contractStatus == IfContractStatus.CONTRACT_CANCELLED) {
                // 해지
                RentalAssetEventType.CONTRACT_CANCELLED
            } else if (contractStatus == IfContractStatus.TRANSFER_TO_COLLECTION) {
                // 추심전환
                RentalAssetEventType.TRANSFER_TO_COLLECTION
            } else if (
                rentalAssetEventType != RentalAssetEventType.TEMP_CLOSING_START &&
                1 == 2
            ) {
                // todo: 가해약 등록
                RentalAssetEventType.TEMP_CLOSING_START
            } else if (
                rentalAssetEventType == RentalAssetEventType.TEMP_CLOSING_START &&
                1 == 2
            ) {
                // todo: 가해약 취소
                RentalAssetEventType.TEMP_CLOSING_END
            } else  {
                null
            }
            if (eventType == null) {
                logger.warn("No change, contractId:${rentalAsset.contractId}")
                return@mapNotNull null
            }

            RentalChangeStateTarget(
                eventType,
                rentalAsset,
                orderItem
            )
        }
        return res
    }
}