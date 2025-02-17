package com.abc.us.accounting.rentals.lease.service.v2

import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.iface.domain.repository.oms.IfOrderItemRepository
import com.abc.us.accounting.rentals.lease.model.RentalAssetData
import com.abc.us.accounting.rentals.lease.utils.RentalUtil
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class OleaseBatchServiceV2(
    private val oleaseServiceV2: OleaseServiceV2,
    private val leaseFindServiceV2: LeaseFindServiceV2,
    private val ifOrderItemRepository: IfOrderItemRepository,
    private val oleaseFindServiceV2: OleaseFindServiceV2
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

        // 자산등록 대상 조회
        val registrationList = leaseFindServiceV2.findInstallationInfo(
            fromTime,
            toTime,
            LeaseType.OPERATING_LEASE
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                orderItem = it.orderItem
            )
        }
        oleaseServiceV2.registration(
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
        // 감가상각 대상 조회
        val validRentalAssetList = leaseFindServiceV2.findValidRentalAssetHistory(
            baseDate
        )
        val orderItems = ifOrderItemRepository.findByOrderItemIdsIn(
            validRentalAssetList.map {
                it.orderItemId
            }
        )
        val depreciationList = validRentalAssetList.filter {
            val orderItem = orderItems.find { orderItem ->
                it.orderItemId == orderItem.orderItemId
            }!!
            RentalUtil.checkFilteringRule(
                context = context,
                orderItem = orderItem
            )
        }
        oleaseServiceV2.depreciation(
            depreciationList.map {
                RentalAssetData.from(it)
            },
            baseDate
        )
    }

    /**
     * 자산 상태 변경 배치
     */
    fun changeStateBatch(
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

        // 해당월 마지막날
        val baseDate = RentalUtil.getLastDate(baseYearMonth)
        val list = oleaseFindServiceV2.findChangeStateTarget(
            baseDate
        ).filter {
            RentalUtil.checkFilteringRule(
                context = context,
                orderItem = it.orderItem
            )
        }
        oleaseServiceV2.changeState(
            list,
            baseDate
        )
    }
}