package com.abc.us.accounting.logistics.domain.repository

import com.abc.us.accounting.logistics.domain.entity.InventoryCosting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface InventoryCostingRepository: JpaRepository<InventoryCosting, Long> {
}
