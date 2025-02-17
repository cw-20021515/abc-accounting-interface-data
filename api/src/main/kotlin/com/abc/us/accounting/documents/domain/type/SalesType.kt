package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.rentals.master.domain.type.LeaseType

enum class SalesType (val symbol:String, val engName:String, val korName:String, val description:String, val leaseType: LeaseType) {
    ONETIME("T", "One Time", "일시불", "일시불 판매", LeaseType.NONE),
    OPERATING_LEASE("O", "Operating Lease", "운용리스", "운용리스 판매", LeaseType.OPERATING_LEASE),
    FINANCIAL_LEASE("F", "Financial Lease", "금융리스", "금융리스 판매", LeaseType.FINANCIAL_LEASE),
    MEMBERSHIP("M", "Membership", "멤버십", "멤버십 판메", LeaseType.NONE),
    CUSTOMER_SERVICE("C", "Customer Service", "고객서비스", "기타 서비스 판매", LeaseType.NONE),
}