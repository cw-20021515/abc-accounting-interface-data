package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity
import com.abc.us.accounting.rentals.lease.domain.repository.*
import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.*

import com.abc.us.accounting.rentals.lease.model.v2.RentalAssetInstallationDataV2
import com.abc.us.accounting.rentals.lease.model.v2.RentalFinancialRegistrationDataV2
import com.abc.us.accounting.rentals.lease.utils.RentalFinancialLeaseUtil
import com.abc.us.accounting.rentals.master.domain.entity.RentalFinancialInterestMasterEntity
import com.abc.us.accounting.supports.NumberUtil
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class FleaseServiceV2(
    private val rentalFinancialLeaseScheduleRepository: RentalFinancialLeaseScheduleRepository,
    private val rentalFinancialLeaseHistoryRepository: RentalFinancialLeaseHistoryRepository,
    private val rentalFinancialLeaseMasterRepository: RentalFinancialLeaseMasterRepository,
    private val rentalFinancialLeaseHistoryDepRepository: RentalFinancialLeaseHistoryDepRepository,
){

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 금융리스 상각 스케줄 상세조회
     */
    fun getFinancialLeaseScheduleInfo(
        contractId:String
    ): ResRentalFinancialLeaseSchedule? {
        var rentals = rentalFinancialLeaseMasterRepository.selectBySearchRentalsInfo(contractId)
        var rentalItems = rentalFinancialLeaseScheduleRepository.findByContractId(contractId)
        var fLeaseItems = rentalItems?.get()?.map { item ->
            RentalFinancialLeaseScheduleInfo(
                depreciationCount               = item.depreciationCount,
                depreciationYearMonth           = item.depreciationYearMonth ?: "",
                depreciationBillYearMonth       = item.depreciationBillYearMonth ?: "",
                currency                        = item.currency ?: "USD",
                depreciationRentalAmount        = item.depreciationRentalAmount?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal("0.0"),
                depreciationBookValue           = item.depreciationBookValue?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal("0.0"),
                depreciationPresentValue        = item.depreciationPresentValue?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal("0.0"),
                depreciationCurrentDifference   = item.depreciationCurrentDifference?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal("0.0"),
                depreciationInterestIncome      = item.depreciationInterestIncome?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal("0.0")
            )
        }

        if (fLeaseItems != null) {
            rentals?.depreciationSchedule = fLeaseItems
        }
        return rentals
    }

    /**
     * 금융리스 상각 스케줄 리스트조회
     */
    fun getFinancialLeaseScheduleList(
        req: ReqRentalFinancialLeaseInqySchedule
    ): Page<ResRentalFinancialLeaseInqySchedule> {
        return findByReq(
            req,
            PageRequest.of(
                req.current - 1,
                req.size
            )
        )
    }

    /**
     * 금융리스 자산등록
     */
    fun registration(
        datas: List<RentalAssetInstallationDataV2>,
        interestRates: List<RentalFinancialInterestMasterEntity>
    ) {
        // 기존 상각 스케줄 삭제(중복방지)
        deleteDepreciationSchedule(
            datas.map {
                it.contract.contractId
            }
        )
        // 금융리스 상각 스케줄 계산/저장
        val schedules = datas.map { data ->
            val orderDate = data.orderItem.createTime!!.toLocalDate()
            val orderYearMonth = orderDate.toString().substring(0, 7)
            makeDepreciationSchedule(
                ReqRentalFinancialLeaseSchedule.fromV2(
                    data,
                    interestRates.filter {
                        it.targetMonth!! <= orderYearMonth
                    }.maxBy {
                        it.targetMonth!!
                    }.interestRate
                )
            )
        }
        rentalFinancialLeaseScheduleRepository.saveAll(schedules.flatten())
        val registrationDatas = datas.mapIndexed { index, data ->
            val orderDate = data.orderItem.createTime!!.toLocalDate()
            val orderYearMonth = orderDate.toString().substring(0, 7)
            RentalFinancialRegistrationDataV2(
                data,
                interestRates.filter {
                    it.targetMonth!! <= orderYearMonth
                }.maxBy {
                    it.targetMonth!!
                }.interestRate,
                schedules[index].first()
            )
        }
        // 금융리스 이력 저장
        val historys = rentalFinancialLeaseHistoryRepository.findByDocHashCodes(
            registrationDatas.map { registrationData ->
                val data = registrationData.data
                val interestRate = registrationData.interestRate
                val schedule = registrationData.schedule
                makeHash(
                    RentalFinancialEventType.FLEASE_REGISTRATION,
                    RentalFinancialDepreciationHistoryEntity.fromV2(
                        data,
                        interestRate,
                        schedule
                    )
                )
            }
        )
        rentalFinancialLeaseHistoryRepository.saveAll(
            registrationDatas.map { registrationData ->
                val data = registrationData.data
                val interestRate = registrationData.interestRate
                val schedule = registrationData.schedule
                makeHistory(
                    RentalFinancialEventType.FLEASE_REGISTRATION,
                    RentalFinancialDepreciationHistoryEntity.fromV2(
                        data,
                        interestRate,
                        schedule
                    ),
                    historys = historys
                )
            }
        )
    }

    /**
     * 금융리스 이력 hash 생성
     */
    fun makeHash(
        eventType: RentalFinancialEventType,
        data: RentalFinancialDepreciationHistoryEntity,
        depreciationData: RentalFinancialDepreciationScheduleEntity = RentalFinancialDepreciationScheduleEntity()
    ): String {
        return Hashs.hash(
            data.serialNumber,
            depreciationData.depreciationCount,
            eventType
        )
    }

    /**
     * 금융리스 이력 생성
     */
    fun makeHistory(
        eventType: RentalFinancialEventType,
        data: RentalFinancialDepreciationHistoryEntity,
        depreciationData: RentalFinancialDepreciationScheduleEntity = RentalFinancialDepreciationScheduleEntity(),
        historys: List<RentalFinancialDepreciationHistoryEntity> = listOf()
    ): RentalFinancialDepreciationHistoryEntity {
        var id: String? = null
        val serialNumber = data.serialNumber
        val depreciationCount = depreciationData.depreciationCount

        // hash 값으로 중복 데이터 업데이트 처리
        val hash = makeHash(
            eventType,
            data,
            depreciationData
        )
        val res = historys.find {
            it.docHashCode == hash
        }
        var createTime: OffsetDateTime? = null
        if (res != null) {
            id = res.id
            createTime = res.createTime
        }
        val cumulativeInterestIncome = NumberUtil.plus(
            data.cumulativeInterestIncome ?: BigDecimal(0),
            depreciationData.depreciationInterestIncome ?: BigDecimal(0)
        )
        val dbData = RentalFinancialDepreciationHistoryEntity(
            id = id,
            rentalEventType = eventType.name,
            docHashCode = hash,
            orderId = data.orderId,
            orderItemId = data.orderItemId,
            customerId = data.customerId,
            serialNumber = serialNumber,
            contractId = data.contractId,
            materialId = data.materialId,
            baseDate = data.baseDate,
            contractDate = data.contractDate,
            materialSeriesCode = data.materialSeriesCode,
            contractEndDate = data.contractEndDate,
            contractPeriod = data.contractPeriod,
            initialBookValue = data.initialBookValue,
            interestRate = data.interestRate,
            rentalAmount = data.rentalAmount,
            depreciationCount = depreciationCount,
            depreciationYearMonth = depreciationData.depreciationYearMonth,
            depreciationBillYearMonth = depreciationData.depreciationBillYearMonth,
            currency = depreciationData.currency,
            depreciationBookValue = depreciationData.depreciationBookValue,
            depreciationPresentValue = depreciationData.depreciationPresentValue,
            depreciationCurrentDifference = depreciationData.depreciationCurrentDifference,
            depreciationInterestIncome = depreciationData.depreciationInterestIncome,
            cumulativeInterestIncome = cumulativeInterestIncome,
            initialPresentValue = data.initialPresentValue,
            initialCurrentDifference = data.initialCurrentDifference,
            rentalAmountForGoods = data.rentalAmountForGoods,
            createTime = createTime
        )
        return dbData
    }

    /**
     * 상각 스케줄 삭제
     */
    fun deleteDepreciationSchedule(
        contractIds: List<String>
    ): Int {
        val res = rentalFinancialLeaseScheduleRepository.deleteByContractIds(contractIds)
        return res
    }

    /**
     * 상각 스케줄 계산
     */
    fun makeDepreciationSchedule(
        reqRentalFinancialLeaseSchedule: ReqRentalFinancialLeaseSchedule
    ): List<RentalFinancialDepreciationScheduleEntity> {
        val schedule = RentalFinancialLeaseUtil.generateLeaseSchedule(
            reqRentalFinancialLeaseSchedule
        )
        reqRentalFinancialLeaseSchedule.initialPresentValue = schedule.first().depreciationPresentValue
        reqRentalFinancialLeaseSchedule.initialCurrentDifference = schedule.first().depreciationCurrentDifference
        reqRentalFinancialLeaseSchedule.initialBookValue = schedule.first().depreciationBookValue
        reqRentalFinancialLeaseSchedule.initFirstRentalAmount = schedule.first().initFirstRentalAmount
        val scheduleEntity = schedule.map { request -> request.fromItems(
            "",
            reqRentalFinancialLeaseSchedule.orderItemId ?: "",
            reqRentalFinancialLeaseSchedule.contractId?:"",
            RentalFinancialDepreciationScheduleEntity()
        )}
        return scheduleEntity
    }

    /**
     * 금융리스 상각 처리
     */
    fun depreciation(
        datas: List<ResRentalFinancialLeaseInqyScheduleTemp>,
        baseDate: LocalDate,
        prevHistoryList: List<RentalFinancialDepreciationHistoryEntity>
    ) {
        // 상각 테이블 정보 조회
        val list = rentalFinancialLeaseScheduleRepository.findByContractIdsAndDate(
            datas.map {
                it.contractId!!
            },
            baseDate,
            true
        )
        val depreciationDataMap: MutableMap<String, RentalFinancialDepreciationScheduleEntity> = mutableMapOf()
        list.forEach {
            depreciationDataMap[it.contractId!!] = it
        }
        val depreciationDatas = datas.mapNotNull { data ->
            val depreciationData = depreciationDataMap[data.contractId] ?: return@mapNotNull null
            RentalFInancialDepreciationData(
                // 같은 상각 회차일 경우 그 이전 history 설정
                if (data.depreciationCount == depreciationData.depreciationCount) {
                    val filteredList = prevHistoryList.filter {
                        it.contractId == data.contractId && (
                                depreciationData.depreciationCount?.minus(1) == it.depreciationCount ||
                                        it.rentalEventType == RentalFinancialEventType.FLEASE_REGISTRATION.name
                                )
                    }
                    (
                            filteredList.find {
                                it.rentalEventType == RentalFinancialEventType.FLEASE_DEPRECIATION.name
                            } ?:
                            filteredList.find {
                                it.rentalEventType == RentalFinancialEventType.FLEASE_REGISTRATION.name
                            }
                            )!!.apply {
                            this.baseDate = baseDate
                        }
                } else {
                    RentalFinancialDepreciationHistoryEntity.from(
                        data,
                        baseDate,
                    )
                },
                depreciationData
            )
        }
        // 금융리스 이력 저장
        val historys = rentalFinancialLeaseHistoryRepository.findByDocHashCodes(
            depreciationDatas.map { depreciationData ->
                val data = depreciationData.data
                val schedule = depreciationData.schedule
                makeHash(
                    RentalFinancialEventType.FLEASE_DEPRECIATION,
                    data,
                    schedule
                )
            }
        )
        rentalFinancialLeaseHistoryRepository.saveAll(
            depreciationDatas.map { depreciationData ->
                val data = depreciationData.data
                val schedule = depreciationData.schedule
                makeHistory(
                    RentalFinancialEventType.FLEASE_DEPRECIATION,
                    data,
                    schedule,
                    historys = historys
                )
            }
        )
    }

    fun findByReq(
        req: ReqRentalFinancialLeaseInqySchedule,
        pageable: Pageable? = null
    ):Page<ResRentalFinancialLeaseInqySchedule> {
        val page = rentalFinancialLeaseHistoryDepRepository.findByReq(
            req.baseDate,
            req.contractFromDate,
            req.contractToDate,
            req.customerId,
            req.orderItemId,
            req.serialNumber,
            req.contractId,
            req.materialId,
            req.materialCategory?.name,
            pageable
        )
        val res: Page<ResRentalFinancialLeaseInqyScheduleTemp> = MapperUtil.convert(page)
        val content = res.content.map {
            ResRentalFinancialLeaseInqySchedule.from(
                it.copy(
                    initialBookValue = NumberUtil.setScaleNullable(it.initialBookValue, 2),
                    interestRate = NumberUtil.setScaleNullable(BigDecimal(it.interestRate!!), 2)?.toDouble(),
                    rentalAmount = NumberUtil.setScaleNullable(it.rentalAmount, 2),
                    initialPresentValue = NumberUtil.setScaleNullable(it.initialPresentValue, 2),
                    initialCurrentDifference = NumberUtil.setScaleNullable(it.initialCurrentDifference, 2),
                    depreciationBookValue = NumberUtil.setScaleNullable(it.depreciationBookValue, 2),
                    depreciationPresentValue = NumberUtil.setScaleNullable(it.depreciationPresentValue, 2),
                    depreciationCurrentDifference = NumberUtil.setScaleNullable(it.depreciationCurrentDifference, 2),
                    depreciationInterestIncome = NumberUtil.setScaleNullable(it.depreciationInterestIncome, 2),
                    cumulativeInterestIncome = NumberUtil.setScaleNullable(it.cumulativeInterestIncome, 2),
                    rentalAmountForGoods = NumberUtil.setScaleNullable(it.rentalAmountForGoods, 2),
                )
            )
        }
        return PageImpl(content, res.pageable, res.totalElements)
    }

    /**
     * 금융상각 스케줄 엑셀 다운로드
     */
    fun getRentalFinancialLeaseScheduleExcelDownload(
        contractId: String,
        response: HttpServletResponse
    ) {
        var rentalItems = rentalFinancialLeaseScheduleRepository.findByContractId(contractId)
        val depreciationSchedule = rentalItems?.get()?.map {item ->
            RentalFinancialLeaseScheduleInfo(
                depreciationCount               = item.depreciationCount,
                depreciationYearMonth           = item.depreciationYearMonth ?: "",
                depreciationBillYearMonth       = item.depreciationBillYearMonth ?: "",
                currency                        = item.currency ?: "USD",
                depreciationRentalAmount        = item.depreciationRentalAmount ?: BigDecimal("0.0"),
                depreciationBookValue           = item.depreciationBookValue ?: BigDecimal("0.0"),
                depreciationPresentValue        = item.depreciationPresentValue ?: BigDecimal("0.0"),
                depreciationCurrentDifference   = item.depreciationCurrentDifference ?: BigDecimal("0.0"),
                depreciationInterestIncome      = item.depreciationInterestIncome ?: BigDecimal("0.0")
            )
        }
        val headers = listOf("청구회차", "년월", "청구년월", "통화", "렌탈료(재화)", "장부금액", "PV", "현할차", "이자수익")
        val datas = depreciationSchedule?.map {
            listOf(
                it.depreciationCount
                ,it.depreciationYearMonth
                ,it.depreciationBillYearMonth
                ,it.currency
                ,it.depreciationRentalAmount
                ,it.depreciationBookValue
                ,it.depreciationPresentValue
                ,it.depreciationCurrentDifference
                ,it.depreciationInterestIncome
            )
        } as List<List<Any>>
        val fileName = "excel-rental-financial-schedule"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    /**
     * 금융상각 현황 엑셀 다운로드
     */
    fun getRentalFinancialLeaseExcelDownload(
        reqData: ReqRentalFinancialLeaseInqySchedule,
        response: HttpServletResponse
    ) {
        val res = findByReq(reqData).toList()
        val headers = listOf(
            "계약ID",
            "계약일",
            "기준일",
            "청구회차",
            "최초장부금액",
            "장부금액",
            "렌탈료(재화)",
            "현재가치(PV)",
            "현할차",
            "이자수익",
            "이자수익누계액",
            "주문아이템ID",
            "고객ID",
            "시리얼번호",
            "자재ID",
            "모델명"
        )
        val datas = res.map {
            listOf(
                it.contractId,
                it.contractDate,
                it.baseDate,
                it.depreciationCount,
                it.initialBookValue,
                it.depreciationBookValue,
                it.rentalAmountForGoods,
                it.depreciationPresentValue,
                it.depreciationCurrentDifference,
                it.depreciationInterestIncome,
                it.cumulativeInterestIncome,
                it.orderItemId,
                it.customerId,
                it.serialNumber,
                it.materialId,
                it.modelName
            )
        } as List<List<Any>>
        val fileName = "excel-rental-financial"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }
}