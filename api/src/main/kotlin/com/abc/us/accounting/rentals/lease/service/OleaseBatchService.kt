package com.abc.us.accounting.rentals.lease.service

import com.abc.us.accounting.collects.domain.entity.collect.CollectContract
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.rentals.lease.model.RentalAssetData
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class OleaseBatchService(
    private val oleaseService: OleaseService,
    private val leaseFindService: LeaseFindService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

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
            LeaseType.OPERATING_LEASE
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                contract = it.contract
            )
        }
        oleaseService.registration(
            registrationList.map {
                RentalAssetData.from(it)
            }
        )
    }

    /**
     * 감가상각 배치
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

        // 감가상각 기준일(기준월의 마지막날)
        val baseDate = RentalUtil.getLastDate(baseYearMonth)
        // 감가상각(일반) 대상 조회
        val depreciationList = leaseFindService.findValidRentalAssetHistory(
            baseDate
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                contract = CollectContract(
                    contractId = "",
                    orderId = it.orderId,
                    orderItemId = it.orderItemId,
                    materialId = it.materialId
                )
            )
        }
        oleaseService.depreciation(
            depreciationList.map {
                RentalAssetData.from(it)
            },
            baseDate
        )
    }
}