package com.abc.us.accounting.rentals.master.domain.repository

import com.abc.us.accounting.rentals.master.domain.entity.RentalFinancialInterestMasterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 금융리스 이자율 조회
 */
@Repository
interface RentalFinancialInterestMasterRepository : JpaRepository<RentalFinancialInterestMasterEntity, Long> {
}
