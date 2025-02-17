package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.collects.domain.entity.collect.CollectInventoryValuation
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.master.domain.entity.RentalCodeMaster

data class RentalInstallGoodsTargetV2(
    val serviceFlow: IfServiceFlow,
    val contract: IfContract,
    val orderItem: IfOrderItem,
    val material: IfMaterial,
    val channel: IfChannel?,
    val rentalCodeMaster: RentalCodeMaster,
    val history: RentalFinancialDepreciationHistoryEntity,
)
