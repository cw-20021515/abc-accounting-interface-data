package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.model.SearchTrialBalanceFilters
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository

interface CustomAccountSearchRepository {
    fun searchAccounts(request: SearchTrialBalanceFilters): Page<Account>
}

@Repository
class CustomAccountSearchRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : CustomAccountSearchRepository {

    override fun searchAccounts(request: SearchTrialBalanceFilters): Page<Account> {
        val pageable = request.pageable.toPageable()

        var jpql = """
            SELECT DISTINCT a 
            FROM Account a
            JOIN DocumentItem b ON a.accountKey.companyCode = b.companyCode AND a.accountKey.accountCode = b.accountCode
            JOIN Document c ON b.docId = c.id 
            WHERE 1=1
        """.trimIndent()
        var countJpql = """
            SELECT COUNT(DISTINCT a) 
            FROM Account a
            JOIN DocumentItem b ON a.accountKey.companyCode = b.companyCode AND a.accountKey.accountCode = b.accountCode
            JOIN Document c ON b.docId = c.id 
            WHERE 1=1
        """.trimIndent()
        val params = mutableMapOf<String, Any>()

        if (request.fromDate <= request.beginningFromDate) {
            jpql += " AND c.postingDate BETWEEN :from AND :to "
            countJpql += " AND c.postingDate BETWEEN :from AND :to "
            params["from"] = request.fromDate
            params["to"] = request.toDate
        } else {
            jpql += " AND c.postingDate BETWEEN :from AND :to "
            countJpql += " AND c.postingDate BETWEEN :from AND :to "
            params["from"] = request.beginningFromDate
            params["to"] = request.toDate
        }

        if (request.companyCode != null) {
            jpql += " AND a.accountKey.companyCode = :companyCode "
            countJpql += " AND a.accountKey.companyCode = :companyCode "
            params["companyCode"] = request.companyCode
        }

        if (request.accountCodeFrom != null && request.accountCodeTo == null) {
            jpql += " AND a.accountKey.accountCode = :accountCode "
            countJpql += " AND a.accountKey.accountCode = :accountCode "
            params["accountCode"] = request.accountCodeFrom
        }

        if (request.accountCodeFrom != null && request.accountCodeTo != null) {
            jpql += " AND a.accountKey.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            countJpql += " AND a.accountKey.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            params["accountCodeFrom"] = request.accountCodeFrom
            params["accountCodeTo"] = request.accountCodeTo
        }

        // 정렬조건 추가
        jpql += " ORDER BY a.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, Account::class.java)
        val countQuery = entityManager.createQuery(countJpql, Long::class.java)

        params.forEach { (k, v) ->
            query.setParameter(k, v)
            countQuery.setParameter(k, v)
        }

        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        val accounts = query.resultList
        val total = countQuery.singleResult

        return PageImpl(accounts, pageable, total)
    }

}

interface TrialBalanceCustomRepository {
    fun searchTrialBalanceDocItems(request: SearchTrialBalanceFilters): List<DocumentItem>
    fun searchTrialBalanceDocuments(request: SearchTrialBalanceFilters): List<Document>
}

@Repository
class TrialBalanceSearchRepository(
    @PersistenceContext
    private val entityManager: EntityManager
) : TrialBalanceCustomRepository {

    override fun searchTrialBalanceDocItems(request: SearchTrialBalanceFilters): List<DocumentItem> {
        var jpql = """
            SELECT DISTINCT a
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            WHERE 1=1
        """.trimIndent()

        val params = mutableMapOf<String, Any>()

        if (request.companyCode != null) {
            jpql += " AND b.companyCode = :companyCode"
            params["companyCode"] = request.companyCode
        }

        if (request.fromDate <= request.beginningFromDate) {
            jpql += " AND b.postingDate BETWEEN :from AND :to"
            params["from"] = request.fromDate
            params["to"] = request.toDate
        } else {
            jpql += " AND b.postingDate BETWEEN :from AND :to"
            params["from"] = request.beginningFromDate
            params["to"] = request.toDate
        }

        if (request.accountCodeFrom != null && request.accountCodeTo == null) {
            jpql += " AND a.accountCode = :accountCode "
            params["accountCode"] = request.accountCodeFrom
        }

        if (request.accountCodeFrom != null && request.accountCodeTo != null) {
            jpql += " AND a.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            params["accountCodeFrom"] = request.accountCodeFrom
            params["accountCodeTo"] = request.accountCodeTo
        }

        // 정렬조건 추가
        //jpql += " ORDER BY b.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, DocumentItem::class.java)
        params.forEach { (k, v) ->
            query.setParameter(k, v)
        }
        val documentItems = query.resultList

        return documentItems
    }

    override fun searchTrialBalanceDocuments(request: SearchTrialBalanceFilters): List<Document> {
        var jpql = """
            SELECT DISTINCT b
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            WHERE 1=1
        """.trimIndent()

        val params = mutableMapOf<String, Any>()

        if (request.companyCode != null) {
            jpql += " AND b.companyCode = :companyCode"
            params["companyCode"] = request.companyCode
        }

        if (request.fromDate <= request.beginningFromDate) {
            jpql += " AND b.postingDate BETWEEN :from AND :to"
            params["from"] = request.fromDate
            params["to"] = request.toDate
        } else {
            jpql += " AND b.postingDate BETWEEN :from AND :to"
            params["from"] = request.beginningFromDate
            params["to"] = request.toDate
        }

        if (request.accountCodeFrom != null && request.accountCodeTo == null) {
            jpql += " AND a.accountCode = :accountCode "
            params["accountCode"] = request.accountCodeFrom
        }

        if (request.accountCodeFrom != null && request.accountCodeTo != null) {
            jpql += " AND a.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            params["accountCodeFrom"] = request.accountCodeFrom
            params["accountCodeTo"] = request.accountCodeTo
        }

        // 정렬조건 추가
        //jpql += " ORDER BY b.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, Document::class.java)
        params.forEach { (k, v) ->
            query.setParameter(k, v)
        }
        val documents = query.resultList

        return documents
    }

}
