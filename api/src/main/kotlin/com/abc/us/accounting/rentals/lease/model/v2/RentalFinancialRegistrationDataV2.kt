package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity

class RentalFinancialRegistrationDataV2 (
    val data: RentalAssetInstallationDataV2,
    val interestRate: Double?,
    val schedule: RentalFinancialDepreciationScheduleEntity
)