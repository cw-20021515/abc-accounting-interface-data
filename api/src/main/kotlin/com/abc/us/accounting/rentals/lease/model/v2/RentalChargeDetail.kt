package com.abc.us.accounting.rentals.lease.model.v2

import java.math.BigDecimal

data class RentalChargeDetail(
    val chargeId: String,
    val targetMonth: String,
    val rentalFee: BigDecimal, // 렌탈료(재화 + 서비스)
    val rentalFeeForProduct: BigDecimal, // 렌탈료 재화 부분
    val rentalFeeForService: BigDecimal, // 렌탈료 서비스 부분
    val arService: BigDecimal, // 외상매출금-용역수입(공임, 부품비, 설치비, 해체비, 이전 설치비)
    val asFees: List<BigDecimal>, // 유상(단순방문), 공임
    val partsFees: List<BigDecimal>, // 유상(부품교체), 부품비
    val laborFees: List<BigDecimal>, // 유상(이사, 이전설치, 해체), 설치, 해체비, 이전 설치비
    val lostFee: BigDecimal, // 분실료
    val penaltyFee: BigDecimal, // 위약금
    val lateFee: BigDecimal, // 연체료
)
