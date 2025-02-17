package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetDepreciationScheduleRepository
import com.abc.us.accounting.rentals.lease.model.RentalAssetDepreciationTargetData
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.repository.RentalCodeMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.ContractStatus
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OleaseFindService(
    private val leaseFindService: LeaseFindService,

    private val collectOrderRepository: CollectOrderRepository,
    private val collectChannelRepository: CollectChannelRepository,
    private val rentalAssetDepreciationScheduleRepository: RentalAssetDepreciationScheduleRepository,
    private val collectContractRepository: CollectContractRepository,
    private val collectInstallationRepository: CollectInstallationRepository,
    private val collectMaterialRepository: CollectMaterialRepository,
    private val rentalCodeMasterRepository: RentalCodeMasterRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 렌탈자산 상각 대상 조회
     */
    fun findDepreciationTarget(
        baseDate: LocalDate
    ): List<RentalAssetDepreciationTargetData> {
        val validList = leaseFindService.findValidRentalAssetHistory(baseDate)
        val serialNumbers = validList.map {
            it.serialNumber
        }
        val installations = collectInstallationRepository.findAllBySerialNumber(
            serialNumbers
        )
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
        val schedules = rentalAssetDepreciationScheduleRepository.findBySerialNumbersAndDate(
            serialNumbers,
            baseDate
        )
        val rentalCodeMasters = rentalCodeMasterRepository.findByRentalCodes(
            contracts.map {
                it.rentalCode!!
            }
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
            val schedule = schedules.find {
                valid.serialNumber == it.serialNumber
            }
            if (schedule == null) {
                logger.warn("No schedule, valid:${valid.serialNumber}")
                return@mapNotNull null
            }
            RentalAssetDepreciationTargetData(
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