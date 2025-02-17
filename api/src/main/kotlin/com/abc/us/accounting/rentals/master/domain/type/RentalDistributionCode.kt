package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable

/**
 * 렌탈 분할코드
 * @see https://docs.google.com/spreadsheets/d/1KoZeIvL1B2mDPYoCXuMKtr3T1UEZtgwy/edit?gid=1746885933#gid=1746885933
 */
enum class RentalDistributionCode(val description: String): Serializable {
    M01("주상품"),
    R01("무상 A/S 자재1"),
    R02("무상 A/S 자재2"),
    R03("무상 A/S 자재3"),
    S01("서비스1"),
    T01("전체")
}