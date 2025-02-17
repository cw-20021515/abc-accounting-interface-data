package com.abc.us.accounting.rentals.lease.domain.repository

import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationScheduleEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface RentalFinancialLeaseScheduleRepository : JpaRepository<RentalFinancialDepreciationScheduleEntity, String> {
    fun findByContractId(contractId:String): Optional<List<RentalFinancialDepreciationScheduleEntity>>

    @Modifying
    @Transactional
    @Query("""
        delete from
            RentalFinancialDepreciationScheduleEntity a
        where
            a.contractId in :contractIds
    """)
    fun deleteByContractIds(contractIds: List<String>): Int

    @Query("""
        select
            a
        from
            RentalFinancialDepreciationScheduleEntity a
        where
            a.contractId in :contractIds
            and (
                (:isBillYearMonth = true and a.depreciationBillYearMonth = to_char(date(:baseDate), 'YYYY-MM')) or
                (:isBillYearMonth = false and a.depreciationYearMonth = to_char(date(:baseDate), 'YYYY-MM'))
            )
            and a.depreciationBillYearMonth is not null
    """)
    fun findByContractIdsAndDate(
        contractIds: List<String>,
        baseDate: LocalDate,
        isBillYearMonth: Boolean = false
    ): List<RentalFinancialDepreciationScheduleEntity>
}
