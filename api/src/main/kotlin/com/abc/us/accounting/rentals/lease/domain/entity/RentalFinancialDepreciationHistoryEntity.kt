package com.abc.us.accounting.rentals.lease.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import com.abc.us.accounting.rentals.lease.model.ResRentalFinancialLeaseInqyScheduleTemp
import com.abc.us.accounting.rentals.lease.model.RentalAssetInstallationData
import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseSchedule
import com.abc.us.accounting.rentals.lease.model.v2.RentalAssetInstallationDataV2
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "RENTAL_FINANCIAL_DEPRECIATION_HISTORY")
class RentalFinancialDepreciationHistoryEntity(

    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("테이블 ID")
    var id: String? = null,

    @Comment("이벤트 구분")
    var rentalEventType: String? = null,

    @Comment("그룹 ID")
    var txId: String? = null,

    @Comment("해시코드")
    var docHashCode: String? = null,

    @Comment("주문 ID")
    var orderId: String? = null,

    @Comment("주문 아이템 ID")
    var orderItemId: String? = null,

    @Comment("고객 ID")
    var customerId: String? = null,

    @Comment("시리얼 번호")
    var serialNumber: String? = null,

    @Comment("계약 ID")
    var contractId: String? = null,

    @Comment("자재 ID")
    var materialId: String? = null,

    @Comment("기준일")
    var baseDate: LocalDate? = null,

    @Comment("계약일")
    var contractDate: LocalDate? = null,

    @Comment("품목 코드")
    var materialSeriesCode: String? = null,

    @Comment("만기일")
    var contractEndDate: LocalDate? = null,

    @Comment("약정 개월")
    var contractPeriod: Int? = null,

    @Comment("최초 장부 금액")
    var initialBookValue: BigDecimal? = null,

    @Comment("이자율")
    var interestRate: Double? = null,

    @Comment("렌탈료")
    var rentalAmount: BigDecimal? = null,

    @Comment("감가상각 회차")
    var depreciationCount: Int? = null,

    @Comment("년월 (예: 2024.01)")
    var depreciationYearMonth: String? = null,

    @Comment("청구 년월 (예: 2024.01)")
    var depreciationBillYearMonth: String? = null,

    @Comment("통화 (예: USD)")
    var currency: String? = null,

    @Comment("장부 금액")
    var depreciationBookValue: BigDecimal? = null,

    @Comment("현재 가치(PV)")
    var depreciationPresentValue: BigDecimal? = null,

    @Comment("현 할차")
    var depreciationCurrentDifference: BigDecimal? = null,

    @Comment("이자 수익")
    var depreciationInterestIncome: BigDecimal? = null,

    @Comment("이자 수익(누계)")
    var cumulativeInterestIncome: BigDecimal? = null,

    @Comment("최초 현재 가치(PV)")
    var initialPresentValue: BigDecimal? = null,

    @Comment("최초 현 할차")
    var initialCurrentDifference: BigDecimal? = null,

    @Comment("렌탈료(재화)")
    var rentalAmountForGoods: BigDecimal? = null,

    @Comment("수정일")
    var updateTime: OffsetDateTime? = OffsetDateTime.now(),

    @Comment("생성일")
    @CreationTimestamp
    var createTime: OffsetDateTime? = OffsetDateTime.now()
) {
    companion object {
        fun from(
            data: RentalAssetInstallationData,
            interestRate: Double?,
            schedule: RentalFinancialDepreciationScheduleEntity
        ): RentalFinancialDepreciationHistoryEntity {
            val installation = data.installation
            val contract = data.contract
            val material = data.material
            val rentalCodeMaster = data.rentalCodeMaster
            val rentalDistributionRule = data.rentalDistributionRule
            val contractPeriod = rentalCodeMaster.term1Period
            val rentalAmountForGoods = rentalDistributionRule.distributionPrice.m01
            val distributionPrice = rentalDistributionRule.distributionPrice
            val rentalAmount = distributionPrice.m01.add(distributionPrice.s01)
            return RentalFinancialDepreciationHistoryEntity(
                serialNumber = installation.serialNumber,
                cumulativeInterestIncome = BigDecimal(0),
                orderId = contract.orderId,
                orderItemId = contract.orderItemId,
                customerId = contract.customerId,
                contractId = contract.contractId,
                materialId = contract.materialId,
                baseDate = contract.startDate,
                contractDate = contract.startDate,
                materialSeriesCode = material.materialSeriesCode,
                contractEndDate = contract.endDate,
                contractPeriod = contractPeriod,
                initialBookValue = schedule.depreciationBookValue,
                interestRate = interestRate,
                rentalAmount = rentalAmount,
                initialPresentValue = schedule.depreciationPresentValue,
                initialCurrentDifference = schedule.depreciationCurrentDifference,
                rentalAmountForGoods = rentalAmountForGoods
            )
        }

        fun fromV2(
            data: RentalAssetInstallationDataV2,
            interestRate: Double?,
            schedule: RentalFinancialDepreciationScheduleEntity
        ): RentalFinancialDepreciationHistoryEntity {
            val contract = data.contract
            val serviceFlow = data.serviceFlow
            val orderItem = data.orderItem
            val material = data.material
            val rentalCodeMaster = data.rentalCodeMaster
            val rentalDistributionRule = data.rentalDistributionRule
            val contractPeriod = rentalCodeMaster.term1Period
            val rentalAmountForGoods = rentalDistributionRule.distributionPrice.m01
            val distributionPrice = rentalDistributionRule.distributionPrice
            val rentalAmount = distributionPrice.m01.add(distributionPrice.s01)
            val contractDate = contract.startDate
            return RentalFinancialDepreciationHistoryEntity(
                serialNumber = serviceFlow.serialNumber,
                cumulativeInterestIncome = BigDecimal(0),
                orderId = orderItem.orderId,
                orderItemId = contract.orderItemId,
                customerId = contract.customerId,
                contractId = contract.contractId,
                materialId = orderItem.materialId,
                baseDate = contract.startDate,
                contractDate = contract.startDate,
                materialSeriesCode = material.materialSeriesCode,
                contractEndDate = contract.endDate,
                contractPeriod = contractPeriod,
                interestRate = interestRate,
                rentalAmount = rentalAmount,
                initialPresentValue = schedule.depreciationPresentValue,
                initialCurrentDifference = schedule.depreciationCurrentDifference,
                rentalAmountForGoods = rentalAmountForGoods
            )
        }

        fun from(
            data: ResRentalFinancialLeaseInqyScheduleTemp,
            baseDate: LocalDate
        ): RentalFinancialDepreciationHistoryEntity {
            return RentalFinancialDepreciationHistoryEntity(
                serialNumber = data.serialNumber,
                cumulativeInterestIncome = data.cumulativeInterestIncome,
                orderId = data.orderId,
                orderItemId = data.orderItemId,
                customerId = data.customerId,
                contractId = data.contractId,
                materialId = data.materialId,
                baseDate = baseDate,
                contractDate = data.contractDate,
                materialSeriesCode = data.materialSeriesCode,
                contractEndDate = data.contractEndDate,
                contractPeriod = data.contractPeriod,
                initialBookValue = data.initialBookValue,
                interestRate = data.interestRate,
                rentalAmount = data.rentalAmount,
                initialPresentValue = data.initialPresentValue,
                initialCurrentDifference = data.initialCurrentDifference,
                rentalAmountForGoods = data.rentalAmountForGoods
            )
        }
    }
}
