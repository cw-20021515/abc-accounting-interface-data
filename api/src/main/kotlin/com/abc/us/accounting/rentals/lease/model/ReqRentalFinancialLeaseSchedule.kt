package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.type.RentalFinancialEventType
import com.abc.us.accounting.rentals.lease.model.v2.RentalAssetInstallationDataV2
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(name = "요청_금융 리스_렌탈스케쥴_등록")
class ReqRentalFinancialLeaseSchedule(

    @Schema(description = "주문ID", example = "12345")
    var orderId: String?=null,

    @Schema(description = "주문아이템ID", example = "98765")
    var orderItemId: String?=null,

    @Schema(description = "고객ID", example = "CUST001")
    var customerId: String?=null,

    @Schema(description = "시리얼번호", example = "SN123456")
    var serialNumber: String?=null,

    @Schema(description = "계약ID", example = "CONTRACT001")
    var contractId: String?=null,

    @Schema(description = "자재ID", example = "MATERIAL001")
    var materialId: String?=null,

    @Schema(description = "기준일 (예: 2024-01-01)", example = "2024-01-01")
    var baseDate: LocalDate?=null,

    @Schema(description = "계약일,설치일자 (예: 2024-01-01)", example = "2024-01-10")
    var contractDate: LocalDate?=null,

    @Schema(description = "품목코드", example = "ITEM1234")
    var materialSeriesCode: String?=null,

    @Schema(description = "만기일 (예: 2027-01-01)", example = "2027-01-01")
    var contractEndDate: LocalDate?=null,

    @Schema(description = "약정개월", example = "36")
    var contractPeriod: Int?=null,

    @Schema(description = "이자율", example = "6.85")
    var interestRate: Double?=null,

    @Schema(description = "렌탈료", example = "45.00")
    var rentalAmount: BigDecimal?=null,

    @Schema(description = "렌탈료(재화)- 필수", example = "37.08100")
    var rentalAmountForGoods: BigDecimal? = null,

    @Schema(description = "최초장부금액", example = "510.00")
    var initialBookValue: BigDecimal?=null,

    @Schema(description = "최초 현재 가치(PV)", example = "1214.29")
    var initialPresentValue: BigDecimal? = null,

    @Schema(description = "최초 현 할차", example = "132.55")
    var initialCurrentDifference: BigDecimal? = null,

    @Schema(description = "최초 1회차 렌탈료", defaultValue = "0.0")
    var initFirstRentalAmount: BigDecimal? = null,
    @Schema(description = "최초 1회차 렌탈료(재화)", defaultValue = "0.0")
    var initFirstRentalAmountForGoods: BigDecimal? = null,
    @Schema(description = "최초 1회차 렌탈료(서비스-선수금)", defaultValue = "0.0")
    var initFirstRentalAmountForService: BigDecimal? = null,


    @Schema(description = "이벤트 타입")
    var rentalEventType: RentalFinancialEventType? = null,

    ){

    companion object {
        fun from(
            data: RentalAssetInstallationData,
            interestRate: Double?
        ): ReqRentalFinancialLeaseSchedule {
            val contract = data.contract
            val installation = data.installation
            val material = data.material
            val rentalCodeMaster = data.rentalCodeMaster
            val rentalDistributionRule = data.rentalDistributionRule
            val contractPeriod = rentalCodeMaster.term1Period
            val rentalAmountForGoods = rentalDistributionRule.distributionPrice.m01
            val distributionPrice = rentalDistributionRule.distributionPrice
            val rentalAmount = distributionPrice.m01.add(distributionPrice.s01)
            val contractDate = contract.startDate
            return ReqRentalFinancialLeaseSchedule(
                orderId = contract.orderId,
                orderItemId = contract.orderItemId,
                customerId = contract.customerId,
                serialNumber = installation.serialNumber,
                contractId = contract.contractId,
                materialId = contract.materialId,
                baseDate = null,
                contractDate = contractDate,
                materialSeriesCode = material.materialSeriesCode,
                contractEndDate = contract.endDate,
                contractPeriod = contractPeriod,
                interestRate = interestRate,
                rentalAmount = rentalAmount,
                rentalAmountForGoods = rentalAmountForGoods
            )
        }

        fun fromV2(
            data: RentalAssetInstallationDataV2,
            interestRate: Double?
        ): ReqRentalFinancialLeaseSchedule {
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
            return ReqRentalFinancialLeaseSchedule(
                orderId = orderItem.orderId,
                orderItemId = contract.orderItemId,
                customerId = contract.customerId,
                serialNumber = serviceFlow.serialNumber,
                contractId = contract.contractId,
                materialId = orderItem.materialId,
                baseDate = null,
                contractDate = contractDate,
                materialSeriesCode = material.materialSeriesCode,
                contractEndDate = contract.endDate,
                contractPeriod = contractPeriod,
                interestRate = interestRate,
                rentalAmount = rentalAmount,
                rentalAmountForGoods = rentalAmountForGoods
            )
        }
    }
}