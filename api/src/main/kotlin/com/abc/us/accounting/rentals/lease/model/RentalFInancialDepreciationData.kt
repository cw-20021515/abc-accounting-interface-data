package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity

class RentalFInancialDepreciationData(
    val data: RentalFinancialDepreciationHistoryEntity,
    val schedule: RentalFinancialDepreciationScheduleEntity
)