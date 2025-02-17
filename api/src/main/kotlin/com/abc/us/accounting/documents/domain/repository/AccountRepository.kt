package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.commons.domain.repository.BaseRepository
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.SystemSourceType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AccountRepository: JpaRepository<Account, AccountKey> {

    fun findByAccountKeyIn(accountKeys:List<AccountKey>):List<Account>

    @Query(
        """
        select a from Account a
        where a.accountKey.companyCode = :companyCode and a.systemSource = :systemSource
    """
    )
    fun findBySystemSource(companyCode: CompanyCode, systemSource:SystemSourceType):List<Account>
    @Query(value = """
            select a from Account a
            where a.accountKey.companyCode = :companyCode
        """
    )
    fun findByCompanyCode(companyCode: CompanyCode):MutableList<Account>

    fun findByName(name:String):Account?


//    @Query(value = """
//            select a
//            FROM Account a
//            JOIN AccountGroup ag ON
//                a.accountKey.accountCode BETWEEN ag.startAccountCode AND ag.endAccountCode
//                AND a.accountKey.companyCode = ag.companyCode
//            where ag.id = :accountGroupId
//        """
//    )
//    fun findAllByAccountGroupId(accountGroupId:String):List<Account>
//
//    @Query(value = """
//            select a
//            FROM Account a
//            JOIN AccountGroup ag
//              ON a.accountKey.accountCode BETWEEN ag.startAccountCode AND ag.endAccountCode
//                and a.accountKey.companyCode = ag.companyCode
//            where ag.companyCode = :companyCode and ag.code = :accountGroupCode and ag.level = :level
//        """
//    )
//    fun findAllByAccountGroup(companyCode: CompanyCode, accountGroupCode:String, level:Int):List<Account>

}

@Repository
interface ConsolidationAccountRepository:JpaRepository<ConsolidationAccount, String>{

}


@Repository
interface AccountBalanceRepository: JpaRepository<AccountBalance, AccountKey>, BaseRepository<AccountBalance, AccountKey> {
}

@Repository
interface AccountBalanceRecordRepository : JpaRepository<AccountBalanceRecord, Long>, BaseRepository<AccountBalanceRecord, Long> {

    @Query(value = """
            SELECT a
            FROM AccountBalanceRecord a
            where a.accountKey.companyCode = :companyCode and a.accountKey.accountCode in :accountCodes
            and a.id = (
                SELECT MAX(b.id)
                FROM AccountBalanceRecord b
                WHERE b.accountKey.companyCode = a.accountKey.companyCode and b.accountKey.accountCode = a.accountKey.accountCode
            )
        """
    )
    fun findLastRecordByAccountCodes(
        companyCode: CompanyCode,
        accountCodes:List<String>):List<AccountBalanceRecord>

    @Query(
        """
        SELECT a FROM AccountBalanceRecord a
        WHERE a.accountKey IN :accountKeys
        AND a.id = (
            SELECT MAX(b.id)
            FROM AccountBalanceRecord b
            WHERE b.accountKey = a.accountKey
        )
    """
    )
    fun findLastRecordByKeys(accountKeys: List<AccountKey>):List<AccountBalanceRecord>

//    @Query("""
//    SELECT abr FROM AccountBalanceRecord abr
//    WHERE abr.companyCode = :companyCode
//    AND abr.accountCode IN :accountCodes
//    AND abr.id IN (
//        SELECT MAX(a.id)
//        FROM AccountBalanceRecord a
//        WHERE a.companyCode = :companyCode
//        AND a.accountCode IN :accountCodes
//        GROUP BY a.companyCode, a.accountCode
//    )
//""")
//    fun findLastRecordByKeys(accountKeys: List<AccountKey>):List<AccountBalanceRecord>

    @Query(value = """
        SELECT a FROM AccountBalanceRecord a
        WHERE a.postingDate = :postingDate
        AND (a.accountKey.accountCode, a.postingDate, a.id) IN (
            SELECT ab.accountKey.accountCode, ab.postingDate, MAX(ab.id) as maxId
            FROM AccountBalanceRecord ab
            WHERE ab.postingDate <= :postingDate
            GROUP BY ab.accountKey.accountCode, ab.postingDate
        )
    """
    )
    fun findLastRecordsByPostingDate(postingDate: LocalDate):List<AccountBalanceRecord>


    @Query(value = """
            SELECT a
            FROM AccountBalanceRecord a
            WHERE a.postingDate between :from and :to
            ORDER BY a.postingDate asc, a.recordTime desc
        """
    )
    fun findAllByPostingDateRange(from:LocalDate, to:LocalDate, pageable: Pageable):Slice<AccountBalanceRecord>
}

@Repository
interface CompanyRepository : JpaRepository<Company, CompanyCode> {

    fun findByCode(code : CompanyCode) : Company?
    fun findAllByIsActive(active: Boolean): MutableList<Company>

}
