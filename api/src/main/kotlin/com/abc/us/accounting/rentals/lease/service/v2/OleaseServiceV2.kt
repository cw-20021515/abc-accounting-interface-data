package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationMaster
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetHistory
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetDepreciationMasterRepository
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetDepreciationScheduleRepository
import com.abc.us.accounting.rentals.lease.domain.repository.RentalAssetHistoryRepository
import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.abc.us.accounting.rentals.lease.model.*
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.accounting.iface.domain.repository.oms.IfContractRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfServiceFlowRepository
import com.abc.us.accounting.iface.domain.repository.oms.IfMaterialRepository
import com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowStatus
import com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType
import com.abc.us.accounting.rentals.lease.model.v2.RentalChangeStateTarget
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
class OleaseServiceV2(
    private val rentalAssetDepreciationMasterRepository: RentalAssetDepreciationMasterRepository,
    private val rentalAssetDepreciationScheduleRepository: RentalAssetDepreciationScheduleRepository,
    private val rentalAssetHistoryRepository: RentalAssetHistoryRepository,

    private val ifServiceFlowRepository: IfServiceFlowRepository,
    private val ifContractRepository: IfContractRepository,
    private val ifMaterialRepository: IfMaterialRepository,
    private val ifOrderItemRepository: IfOrderItemRepository,
) {

    companion object {
        /**
         * 운용리스 감가상각 스케줄 계산
         */
        fun generateDepreciationSchedule(
            param: RentalAssetDepreciationScheduleParam,
            scale: Int = 4
        ): List<RentalAssetDepreciationScheduleItemData> {
            val res = mutableListOf<RentalAssetDepreciationScheduleItemData>()
            var currentMonth = YearMonth.from(param.installationDate)
            var beginningBookValue = param.bookValue
            var depreciationExpense = NumberUtil.divide(param.bookValue, BigDecimal(param.usefulLife))
            var endingBookValue: BigDecimal
            var accumulatedDepreciation = BigDecimal(0)
            for (i in 1..param.usefulLife) {
                val depreciationDate = currentMonth.atEndOfMonth()
                // last
                if (i == param.usefulLife) {
                    depreciationExpense = NumberUtil.minus(beginningBookValue, param.salvageValue)
                }
                endingBookValue = NumberUtil.minus(beginningBookValue, depreciationExpense)
                accumulatedDepreciation = NumberUtil.plus(accumulatedDepreciation, depreciationExpense)
                res.add(
                    RentalAssetDepreciationScheduleItemData(
                        depreciationCount = i,
                        depreciationPeriod = "",
                        currency = param.currency,
                        beginningBookValue = beginningBookValue,
                        depreciationExpense = depreciationExpense,
                        endingBookValue = endingBookValue,
                        accumulatedDepreciation = accumulatedDepreciation,
                        serialNumber = param.serialNumber,
                        depreciationDate = depreciationDate
                    )
                )
                beginningBookValue = endingBookValue
                currentMonth = currentMonth.plusMonths(1)
            }
            return res.map {
                it.copy(
                    beginningBookValue = NumberUtil.setScale(it.beginningBookValue, scale),
                    depreciationExpense = NumberUtil.setScale(it.depreciationExpense, scale),
                    endingBookValue = NumberUtil.setScale(it.endingBookValue, scale),
                    accumulatedDepreciation = NumberUtil.setScale(it.accumulatedDepreciation, scale)
                )
            }
        }
    }

    /**
     * 렌탈자산 hash 생성
     */
    fun makeHash(
        data: RentalAssetMakeHistoryData
    ): String {
        return Hashs.hash(
            data.rentalAssetData.serialNumber,
            data.depreciationData.depreciationCount,
            data.eventType,
            data.baseDate
        )
    }

    /**
     * 렌탈자산 이력 생성
     */
    fun makeRentalAssetHistory(
        data: RentalAssetMakeHistoryData,
        historys: List<RentalAssetHistory> = listOf()
    ): RentalAssetHistory {
        val eventType = data.eventType
        val rentalAssetData = data.rentalAssetData
        val deprecationData = data.depreciationData

        var id: Int? = null
        val serialNumber = rentalAssetData.serialNumber
        val depreciationCount = deprecationData.depreciationCount
        val bookValue = deprecationData.bookValue ?: rentalAssetData.acquisitionCost

        // hash 값으로 중복 데이터 업데이트 처리
        val hash = makeHash(data)
        val res = historys.find {
            it.hash == hash
        }
        if (res != null) {
            id = res.id
        }
        val data = RentalAssetHistory(
            id = id,
            serialNumber = serialNumber,
            materialId = rentalAssetData.materialId,
            depreciationCount = depreciationCount,
            depreciationDate = deprecationData.depreciationDate,
            acquisitionCost = rentalAssetData.acquisitionCost,
            depreciationExpense = deprecationData.depreciationExpense,
            accumulatedDepreciation = deprecationData.accumulatedDepreciation,
            bookValue = bookValue,
            contractId = rentalAssetData.contractId,
            contractDate = rentalAssetData.contractDate,
            contractStatus = rentalAssetData.contractStatus,
            orderId = rentalAssetData.orderId,
            orderItemId = rentalAssetData.orderItemId,
            customerId = rentalAssetData.customerId,
            eventType = eventType,
            hash = hash
        )
        return data
    }

    /**
     * 렌탈자산 감가상각 스케줄 계산(serialNumber)
     */
    fun makeRentalAssetDepreciationSchedule(
        rentalAssetData: RentalAssetData,
        rentalAssetDepreciationMasters: List<RentalAssetDepreciationMaster>
    ): List<RentalAssetDepreciationSchedule> {
        // 감가상각 계산에 필요한 정보 조회
        val serialNumber = rentalAssetData.serialNumber
        val materialId = rentalAssetData.materialId
        val installationDate = rentalAssetData.installationDate!!
        val usefulLife: Int
        val currency: String
        val salvageValue: BigDecimal
        val res = rentalAssetDepreciationMasters.filter {
            it.materialId == materialId &&
            (
                it.startDate.isBefore(installationDate) ||
                it.startDate.isEqual(installationDate)
            )
        }.maxBy {
            it.startDate
        }
        if (res != null) {
            usefulLife = res.usefulLife
            currency = res.currency
            salvageValue = res.salvageValue
        } else {
            // default
            usefulLife = 60
            currency = "USD"
            salvageValue = BigDecimal("0.1")
        }
        return makeRentalAssetDepreciationSchedule(
            RentalAssetDepreciationScheduleParam(
                bookValue = rentalAssetData.acquisitionCost,
                usefulLife = usefulLife,
                installationDate = installationDate,
                currency = currency,
                salvageValue = salvageValue,
                serialNumber = serialNumber
            )
        )
    }

    /**
     * 운용리스 감가상각 스케줄 계산/저장(data, serialNumber)
     */
    fun makeRentalAssetDepreciationSchedule(
        param: RentalAssetDepreciationScheduleParam
    ): List<RentalAssetDepreciationSchedule> {
        val list = generateDepreciationSchedule(
            param
        ).map {
            it.toRentalAssetDepreciationSchedule()
        }
        return list
    }

    /**
     * 운용리스 이력 조회
     */
    fun getRentalAssetDepreciationHistory(
        req: RentalAssetHistoryRequest
    ): Page<RentalAssetHistoryItemData> {
        return findByReq(
            req,
            PageRequest.of(
                req.current - 1,
                req.size
            )
        )
    }

    /**
     * 운용리스 감가상각 스케줄 조회
     */
    fun getRentalAssetDepreciationSchedule(
        serialNumber: String
    ): RentalAssetDepreciationScheduleData? {
        // 기본 정보 조회
        val base = findRentalAssetDepreciationScheduleBaseData(serialNumber) ?: return null
        val depreciationSchedule = rentalAssetDepreciationScheduleRepository.findBySerialNumber(
            serialNumber
        ).map {
            RentalAssetDepreciationScheduleItemData.from(it)
        }
        if (depreciationSchedule.isEmpty()) {
            return null
        }
        return RentalAssetDepreciationScheduleData(
            base = base,
            depreciationSchedule = depreciationSchedule
        )
    }

    /**
     * 운용리스 이력 조회 엑셀 다운로드
     */
    fun getRentalAssetDepreciationHistoryExcelDownload(
        req: RentalAssetHistoryRequest,
        response: HttpServletResponse
    ) {
        val res = findByReq(req).toList()
        val headers = listOf(
            "시리얼번호",
            "자재ID",
            "모델명",
            "감가상각회차",
            "감가상각일",
            "취득원가",
            "감가상각비(차수)",
            "감가상각누계액",
            "장부가액",
            "계약ID",
            "계약일",
            "계약상태",
            "주문ID",
            "주문아이템ID",
            "고객ID"
        )
        val datas = res.map {
            listOf(
                it.serialNumber,
                it.materialId,
                it.modelName,
                it.depreciationCount,
                it.depreciationDate,
                it.acquisitionCost,
                it.depreciationExpense,
                it.accumulatedDepreciation,
                it.bookValue,
                it.contractId,
                it.contractDate,
                it.contractStatus,
                it.orderId,
                it.orderItemId,
                it.customerId
            )
        } as List<List<Any>>
        val fileName = "렌탈자산 현황"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    /**
     * 운용리스 감가상각 스케줄 엑셀 다운로드
     */
    fun getRentalAssetDepreciationScheduleExcelDownload(
        serialNumber: String,
        response: HttpServletResponse
    ) {
        val depreciationSchedule = rentalAssetDepreciationScheduleRepository.findBySerialNumber(
            serialNumber
        ).map {
            RentalAssetDepreciationScheduleItemData.from(it)
        }
        val headers = listOf(
            "회차",
            "년월",
            "통화",
            "기초",
            "감가상각비",
            "기말",
            "감가상각누계액"
        )
        val datas = depreciationSchedule.map {
            listOf(
                it.depreciationCount,
                it.depreciationPeriod,
                it.currency,
                it.beginningBookValue,
                it.depreciationExpense,
                it.endingBookValue,
                it.accumulatedDepreciation
            )
        } as List<List<Any>>
        val fileName = "렌탈자산 감가상각 스케줄"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    fun findByReq(
        req: RentalAssetHistoryRequest,
        pageable: Pageable? = null
    ): Page<RentalAssetHistoryItemData> {
        val page = rentalAssetHistoryRepository.findByReq(
            req.baseDate,
            req.contractFromDate,
            req.contractToDate,
            req.customerId,
            req.orderIdFrom,
            req.orderIdTo,
            req.serialNumber,
            req.materialId,
            req.materialCategory?.name,
            pageable
        )
        val res: Page<RentalAssetHistoryItemData> = MapperUtil.convert(page)
        val content = res.content.map {
            it.copy(
                acquisitionCost = NumberUtil.setScale(it.acquisitionCost, 2),
                depreciationExpense = NumberUtil.setScaleNullable(it.depreciationExpense, 2),
                accumulatedDepreciation = NumberUtil.setScaleNullable(it.accumulatedDepreciation, 2),
                bookValue = NumberUtil.setScale(it.bookValue, 2)
            )
        }
        return PageImpl(content, res.pageable, res.totalElements)
    }

    /**
     * 운용리스 자산등록
     */
    fun registration(
        datas: List<RentalAssetData>
    ) {
        // 기존 렌탈자산 감가상각 스케줄 삭제(중복방지)
        deleteRentalAssetDepreciationSchedule(
            datas.map {
                it.serialNumber
            }
        )
        // 렌탈자산 감가상각 스케줄 계산/저장
        val rentalAssetDepreciationMasters = rentalAssetDepreciationMasterRepository.findAll()
        rentalAssetDepreciationScheduleRepository.saveAll(
            datas.map {
                makeRentalAssetDepreciationSchedule(
                    it,
                    rentalAssetDepreciationMasters
                )
            }.flatten()
        )
        // 렌탈자산 이력 저장
        val makeHistoryDatas = datas.map {
            RentalAssetMakeHistoryData(
                RentalAssetEventType.REGISTRATION,
                it,
                baseDate = it.installationDate!!
            )
        }
        val rentalAssetHistorys = rentalAssetHistoryRepository.findByHashs(
            makeHistoryDatas.map {
                makeHash(it)
            }
        )
        rentalAssetHistoryRepository.saveAll(
            makeHistoryDatas.map {
                makeRentalAssetHistory(
                    it,
                    historys = rentalAssetHistorys
                )
            }
        )
    }

    /**
     * 운용리스 감가상각 처리
     */
    fun depreciation(
        datas: List<RentalAssetData>,
        baseDate: LocalDate
    ) {
        // 감가상각 테이블 정보 조회
        val list = rentalAssetDepreciationScheduleRepository.findBySerialNumbersAndDate(
            datas.map {
                it.serialNumber
            },
            baseDate
        )
        val depreciationDataMap: MutableMap<String, RentalAssetDepreciationData> = mutableMapOf()
        list.forEach {
            depreciationDataMap[it.serialNumber] = RentalAssetDepreciationData(
                depreciationCount = it.depreciationCount,
                depreciationDate = it.depreciationDate,
                depreciationExpense = it.depreciationExpense,
                accumulatedDepreciation = it.accumulatedDepreciation,
                bookValue = it.endingBookValue
            )
        }
        // 렌탈자산 이력 저장
        val makeHistoryDatas = datas.filter {
            depreciationDataMap[it.serialNumber] != null
        }.map {
            RentalAssetMakeHistoryData(
                RentalAssetEventType.DEPRECIATION,
                it,
                depreciationDataMap[it.serialNumber]!!,
                baseDate = baseDate
            )
        }
        val rentalAssetHistorys = rentalAssetHistoryRepository.findByHashs(
            makeHistoryDatas.map {
                makeHash(it)
            }
        )
        rentalAssetHistoryRepository.saveAll(
            makeHistoryDatas.map {
                makeRentalAssetHistory(
                    it,
                    historys = rentalAssetHistorys
                )
            }
        )
    }

    /**
     * 운용리스 감가상각 스케줄 삭제
     */
    fun deleteRentalAssetDepreciationSchedule(
        serialNumbers: List<String>
    ): Int {
        val res = rentalAssetDepreciationScheduleRepository.deleteBySerialNumbers(serialNumbers)
        return res
    }

    /**
     * 운용리스 감가상각 기본 정보 조회
     */
    fun findRentalAssetDepreciationScheduleBaseData(
        serialNumber: String
    ): RentalAssetDepreciationScheduleBaseData? {
        val serviceFlows = ifServiceFlowRepository.findBy(
            serviceTypes = listOf(IfServiceFlowType.INSTALL),
            serviceStatuses = listOf(IfServiceFlowStatus.SERVICE_COMPLETED),
            serialNumbers = listOf(serialNumber)
        )
        if (serviceFlows.isEmpty()) {
            return null
        }
        val serviceFlow = serviceFlows.minBy {
            it.createTime
        }
        val orderItems = ifOrderItemRepository.findByOrderItemIdsIn(
            listOf(serviceFlow.orderItemId)
        )
        if (orderItems.isEmpty()) {
            return null
        }
        val orderItem = orderItems.first()
        val contracts = ifContractRepository.findAllByOrderItemIds(
            listOf(orderItem.orderItemId)
        )
        if (contracts.isEmpty()) {
            return null
        }
        val materials = ifMaterialRepository.findAllByMaterialIdIn(
            listOf(orderItem.materialId)
        )
        if (materials.isEmpty()) {
            return null
        }
        val contract = contracts.first()
        val material = materials.first()
        val rentalAssetDepreciationMaster = rentalAssetDepreciationMasterRepository.findByMaterialId(
            orderItem.materialId
        ) ?: return null
        val res = RentalAssetDepreciationScheduleBaseData(
            materialId = orderItem.materialId,
            modelName = material.materialModelName,
            materialSeriesCode =  material.materialSeriesCode,
            orderId = orderItem.orderId,
            orderItemId = orderItem.orderItemId,
            contractId = orderItem.contractId,
            orderDate = orderItem.createTime.toLocalDate(),
            installationDate = serviceFlow.createTime.toLocalDate(),
            contractDate = contract.startDate,
            depreciationMethod = rentalAssetDepreciationMaster.depreciationMethod,
            usefulLife = rentalAssetDepreciationMaster.usefulLife,
            salvageValue = rentalAssetDepreciationMaster.salvageValue
        )
        return res.copy(
            salvageValue = NumberUtil.setScaleNullable(res.salvageValue, 2)
        )
    }

    /**
     * 운용리스 자산 상태 변경
     */
    fun changeState(
        list: List<RentalChangeStateTarget>,
        baseDate: LocalDate
    ) {
        // 렌탈자산 이력 저장
        val makeHistoryDatas = list.map {
            RentalAssetMakeHistoryData(
                it.eventType,
                RentalAssetData.from(it.rentalAsset),
                baseDate = baseDate
            )
        }
        val rentalAssetHistorys = rentalAssetHistoryRepository.findByHashs(
            makeHistoryDatas.map {
                makeHash(it)
            }
        )
        rentalAssetHistoryRepository.saveAll(
            makeHistoryDatas.map {
                makeRentalAssetHistory(
                    it,
                    historys = rentalAssetHistorys
                )
            }
        )
    }
}
