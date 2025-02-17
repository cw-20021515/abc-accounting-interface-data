package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable

enum class LeaseType(val symbol: String, val engName:String, val korName:String, val description: String): Serializable {
    OPERATING_LEASE("O", "Operating Lease", "운용리스", "운용리스 회계처리"),
    FINANCIAL_LEASE("F", "Financial Lease","금융리스", "금융리스 회계처리"),
    NONE("N", "None", "기타", "기타 리스가 아닌 유형 회계처리")
}
