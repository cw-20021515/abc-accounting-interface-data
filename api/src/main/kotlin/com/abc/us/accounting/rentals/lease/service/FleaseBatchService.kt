package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.collects.domain.entity.collect.CollectContract
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.domain.repository.RentalFinancialLeaseHistoryRepository
import com.abc.us.accounting.rentals.master.domain.repository.RentalFinancialInterestMasterRepository
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class FleaseBatchService(
    val fleaseService: FleaseService,
    val leaseFindService: LeaseFindService,

    val rentalFinancialLeaseHistoryRepository: RentalFinancialLeaseHistoryRepository,
    val rentalFinancialInterestMasterRepository: RentalFinancialInterestMasterRepository
) {
    /**
     * 자산등록 배치
     */
    fun registrationBatch(
        fromTime: OffsetDateTime,
        toTime: OffsetDateTime,
        companyCodes: List<CompanyCode> = listOf(),
        docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        orderIds: List<String> = listOf(),
        orderItemIds: List<String> = listOf(),
        customerIds: List<String> = listOf(),
        materialIds: List<String> = listOf(),
        serviceFlowIds: List<String> = listOf(),
        contractIds: List<String> = listOf(),
        bisSystems: List<BizSystemType> = listOf(),
        bizTxIds: List<String> = listOf(),
        bizProcesses: List<BizProcessType> = listOf(),
        bizEvents: List<BizEventType> = listOf(),
        accountingEvents: List<String> = listOf()
    ) {
        // context 설정
        val context = RentalUtil.getContext(
            companyCodes,
            docTemplateCodes,
            orderIds,
            orderItemIds,
            customerIds,
            materialIds,
            serviceFlowIds,
            contractIds,
            bisSystems,
            bizTxIds,
            bizProcesses,
            bizEvents,
            accountingEvents
        )

        // collect 데이터로 이력 대상 조회
        val registrationList = leaseFindService.findInstallationInfo(
            fromTime,
            toTime,
            LeaseType.FINANCIAL_LEASE
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                contract = it.contract
            )
        }
        val interestRates = rentalFinancialInterestMasterRepository.findAll()
        fleaseService.registration(
            registrationList,
            interestRates
        )
    }

    /**
     * 상각 배치
     */
    fun depreciationBatch(
        baseYearMonth: LocalDate,
        companyCodes: List<CompanyCode> = listOf(),
        docTemplateCodes: List<DocumentTemplateCode> = listOf(),
        orderIds: List<String> = listOf(),
        orderItemIds: List<String> = listOf(),
        customerIds: List<String> = listOf(),
        materialIds: List<String> = listOf(),
        serviceFlowIds: List<String> = listOf(),
        contractIds: List<String> = listOf(),
        bisSystems: List<BizSystemType> = listOf(),
        bizTxIds: List<String> = listOf(),
        bizProcesses: List<BizProcessType> = listOf(),
        bizEvents: List<BizEventType> = listOf(),
        accountingEvents: List<String> = listOf()
    ) {
        // context 설정
        val context = RentalUtil.getContext(
            companyCodes,
            docTemplateCodes,
            orderIds,
            orderItemIds,
            customerIds,
            materialIds,
            serviceFlowIds,
            contractIds,
            bisSystems,
            bizTxIds,
            bizProcesses,
            bizEvents,
            accountingEvents
        )

        // 상각 기준일(기준월의 다음달 첫날)
        val baseDate = RentalUtil.getNextFirstDate(baseYearMonth)
        // 상각(일반) 대상 조회
        val depreciationList = leaseFindService.findValidFinancialLeaseHistory(
            baseDate
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                contract = CollectContract(
                    contractId = it.contractId!!,
                    orderId = it.orderId,
                    orderItemId = it.orderItemId,
                    materialId = it.materialId
                )
            )
        }
        // 상각 이전 history 조회
        val prevHistoryList = rentalFinancialLeaseHistoryRepository.findByContractIdsAndBaseDate(
            depreciationList.map {
                it.contractId!!
            },
            baseDate
        )
        fleaseService.depreciation(
            depreciationList,
            baseDate,
            prevHistoryList
        )
    }
}
