package com.abc.us.accounting.qbo.domain.repository

import com.abc.us.accounting.commons.domain.repository.BaseRepository
import com.abc.us.accounting.qbo.domain.entity.*
import com.abc.us.accounting.qbo.domain.entity.key.QboDepartmentKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QboCompanyRepository : JpaRepository<QboCompany,String> { }

@Repository
interface QboCredentialRepository : JpaRepository<QboCredential,String> {
    fun findByCompanyCode(companyCode : String) : List<QboCredential>?

    fun findByCompanyCodeAndActiveProfile(
        companyCode : String,
        activeProfile : String
    ) : QboCredential?
}

@Repository
interface QboAccountRepository : JpaRepository<QboAccount, String> ,BaseRepository<QboAccount, String>{

    fun findByCompanyCode(companyCode : String) : MutableList<QboAccount>?


    @Query(
        value = """
            SELECT * 
            FROM qbo_account
            WHERE company_code = :companyCode AND account_code = :accountCode 
        """,
        nativeQuery = true
    )
    fun findByAccountCode(companyCode : String,accountCode : String) : QboAccount?

    @Query(
        value = """
            SELECT * 
            FROM qbo_account
            WHERE account_code IN (:accountCodes)
        """,
        nativeQuery = true
    )
        fun findAccountCodesWithinAccountCode(accountCodes : MutableSet<String>) : MutableList<QboAccount>?

}


@Repository
interface QboItemRepository : JpaRepository<QboItem, String>  { }
@Repository
interface QboCustomerRepository : JpaRepository<QboCustomer, String> {

    @Query(
        value = """
            SELECT * 
            FROM qbo_customer
            WHERE customer_id = :customerId
            ORDER BY create_time DESC LIMIT 1;
        """,
        nativeQuery = true
    )
    fun findByCustomerId(customerId : String) : QboCustomer?

    @Query(
        value = """
            SELECT * 
            FROM qbo_customer
            WHERE customer_id IN (:customerIds)
        """,
        nativeQuery = true
    )
    fun findCustomerWithinCustomerId(customerIds : MutableSet<String>) : MutableList<QboCustomer>?
}

@Repository
interface QboJournalEntryRepository : JpaRepository<QboJournalEntry, String>{
    @Query(
        value = """
            SELECT *
            FROM qbo_journal_entry qje
            WHERE qje.doc_id IN (:docIds)
        """,
        nativeQuery = true
    )
    fun findByDocIdIn(docIds : MutableSet<String>) : MutableList<QboJournalEntry>?
}
@Repository
interface QboClassRepository : JpaRepository<QboClass, String>{

}


@Repository
interface QboVendorRepository : JpaRepository<QboVendor, String>{

}

@Repository
interface QboDepartmentRepository : JpaRepository<QboDepartment, String>{
    fun findByKey(key: QboDepartmentKey): QboDepartment?
}

