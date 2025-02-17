package com.abc.us.accounting.rentals.lease.model

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity

class RentalFinancialRegistrationData(
    val data: RentalAssetInstallationData,
    val interestRate: Double?,
    val schedule: RentalFinancialDepreciationScheduleEntity
)