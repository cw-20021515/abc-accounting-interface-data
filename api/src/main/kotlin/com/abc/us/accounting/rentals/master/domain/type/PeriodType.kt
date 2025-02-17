package com.abc.us.accounting.rentals.master.domain.type

import java.io.Serializable

enum class PeriodType : Serializable {
    ONCE,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BIANNUAL,
    YEARLY
}
