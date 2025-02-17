package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable

/**
 * 렌탈 재화 배분 유형
 * @see https://docs.google.com/spreadsheets/d/1KoZeIvL1B2mDPYoCXuMKtr3T1UEZtgwy/edit?gid=1746885933#gid=1746885933
 */
enum class RentalDistributionType(val description: String, val codes: List<RentalDistributionCode>) : Serializable {
    SP01("주상품 단독 렌탈", listOf(RentalDistributionCode.M01) ),
    SP02("주상품+서비스", listOf(RentalDistributionCode.M01, RentalDistributionCode.S01)),
    SP03("주상품+무상A/S자재+서비스", listOf(RentalDistributionCode.M01, RentalDistributionCode.R01, RentalDistributionCode.S01)),
    SP04(
        "주상품+무상A/S자재1+2+서비스",
        listOf(RentalDistributionCode.M01, RentalDistributionCode.R01, RentalDistributionCode.R02, RentalDistributionCode.S01)
    ),
    SP05("주상품+무상A/S자재1+2+3+서비스", RentalDistributionCode.entries),
}
