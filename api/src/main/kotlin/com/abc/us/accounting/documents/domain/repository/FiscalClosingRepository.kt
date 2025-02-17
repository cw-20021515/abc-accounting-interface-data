package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentDateType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


data class FiscalClosingCriteria(
    val companyCode: CompanyCode,
    val from: FiscalYearMonth,
    val to: FiscalYearMonth
)

@Repository
interface FiscalClosingRepository : JpaRepository<FiscalClosing, FiscalKey> {

    @Query("""
       SELECT fc FROM FiscalClosing fc
       WHERE fc.fiscalKey.companyCode = :companyCode
       AND fc.fiscalKey.fiscalYearMonth >= :from AND fc.fiscalKey.fiscalYearMonth <= :to
   """)
    fun findByCompanyAndPeriod(companyCode: CompanyCode, from: FiscalYearMonth, to: FiscalYearMonth): List<FiscalClosing>

    @Query("""
        SELECT fc FROM FiscalClosing fc
        WHERE (
            fc.fiscalKey.companyCode IN :#{#criteriaList.![companyCode.code]} AND
            fc.fiscalKey.fiscalYearMonth >= :#{#criteriaList[0].from} AND fc.fiscalKey.fiscalYearMonth <= :#{#criteriaList[0].to}
        )
        ORDER BY fc.fiscalKey.companyCode, fc.fiscalKey.fiscalYearMonth
    """)
    fun findByCriteria(criteriaList: List<FiscalClosingCriteria>): List<FiscalClosing>
}

@Repository
interface FiscalClosingHistoryRepository : JpaRepository<FiscalClosingHistory, Long> {

    fun findAllByFiscalKeyIn(fiscalKeys: List<FiscalKey>): List<FiscalClosingHistory>
}

@Repository
interface FiscalClosingBalanceSnapshotRepository : JpaRepository<FiscalClosingBalanceSnapshot, Long> {

    @Query(value = """
            SELECT a 
            FROM FiscalClosingBalanceSnapshot a
            WHERE (a.fiscalKey.companyCode, a.fiscalKey.fiscalYearMonth.year, a.fiscalKey.fiscalYearMonth.month, a.snapshotType, a.createdTime) IN (
                SELECT a.fiscalKey.companyCode, a.fiscalKey.fiscalYearMonth.year, a.fiscalKey.fiscalYearMonth.month, a.snapshotType, MAX(a.createdTime) as maxCreatedTime
                FROM FiscalClosingBalanceSnapshot a
                WHERE a.fiscalKey = :fiscalKey and a.snapshotType = :snapshotType
                GROUP BY a.fiscalKey.companyCode, a.fiscalKey.fiscalYearMonth.year, a.fiscalKey.fiscalYearMonth.month, a.snapshotType
            )
        """
    )
    fun findAllByFiscalYearMonth(fiscalKey:FiscalKey, snapshotType: DocumentDateType):List<FiscalClosingBalanceSnapshot>
}