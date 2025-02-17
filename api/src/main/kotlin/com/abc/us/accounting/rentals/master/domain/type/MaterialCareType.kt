package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable


enum class MaterialCareType(val symbol: String, val description: String): Serializable {
    SELF_CARE("S", "자가관리"),
    HOME_CARE("H", "방문관리"),
}

