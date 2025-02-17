package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.Account
import com.abc.us.accounting.documents.domain.type.OpenItemStatus
import com.abc.us.accounting.documents.model.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository

interface SettlementCustomRepository {
    fun searchSettlement(request: SearchSettlementFilters): Page<SettlementDefaultResult>
    fun searchAccountCode(request: SearchAccountFilters): Page<Account>
}

@Repository
class SettlementRepository(
    @PersistenceContext
    private val entityManager: EntityManager
) : SettlementCustomRepository {

    override fun searchSettlement(request: SearchSettlementFilters): Page<SettlementDefaultResult> {
        val pageable = request.pageable.toPageable()

        var jpql = """
            SELECT DISTINCT a.id as docItemId, a.docId, b.documentDate, b.postingDate
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            JOIN Account c ON a.companyCode = c.accountKey.companyCode AND a.accountCode = c.accountKey.accountCode AND c.isOpenItemMgmt = true
            WHERE 1=1
            AND EXISTS (
                SELECT 1
                FROM DocumentItem di
                LEFT JOIN DocumentItemRelation dir on dir.docItemId = di._id
                WHERE b._id = di.docId
                AND dir.relationType IS NULL
            )
        """.trimIndent()
         var countJpql = """
            SELECT COUNT(DISTINCT a)
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            JOIN Account c ON a.companyCode = c.accountKey.companyCode AND a.accountCode = c.accountKey.accountCode AND c.isOpenItemMgmt = true
            WHERE 1=1
            AND EXISTS (
                SELECT 1
                FROM DocumentItem di
                LEFT JOIN DocumentItemRelation dir on dir.docItemId = di._id
                WHERE b._id = di.docId
                AND dir.relationType IS NULL
            )
        """.trimIndent()
        val params = mutableMapOf<String, Any>()

        if (request.companyCode != null) {
            jpql += " AND b.companyCode = :companyCode"
            countJpql += " AND b.companyCode = :companyCode"
            params["companyCode"] = request.companyCode
        }

        jpql += " AND b.postingDate BETWEEN :from AND :to "
        countJpql += " AND b.postingDate BETWEEN :from AND :to "
        params["from"] = request.postingDateFrom
        params["to"] = request.postingDateTo

        if (request.accountCode != null) {
            jpql += " AND a.accountCode = :accountCode "
            countJpql += " AND a.accountCode = :accountCode "
            params["accountCode"] = request.accountCode
        }

        if (request.costCenter != null && request.costCenter.length > 0) {
            jpql += " AND a.costCenter LIKE CONCAT(TRIM(:costCenter), '%')"
            countJpql += " AND a.costCenter LIKE CONCAT(TRIM(:costCenter), '%')"
            params["costCenter"] = request.costCenter
        }

        if (request.customerId != null && request.customerId.length > 0) {
            jpql += " AND a.customerId LIKE CONCAT(TRIM(:customerId), '%')"
            countJpql += " AND a.customerId LIKE CONCAT(TRIM(:customerId), '%')"
            params["customerId"] = request.customerId
        }

        if (request.vendorId != null && request.vendorId.length > 0) {
            jpql += " AND a.vendorId LIKE CONCAT(TRIM(:vendorId), '%')"
            countJpql += " AND a.vendorId LIKE CONCAT(TRIM(:vendorId), '%')"
            params["vendorId"] = request.vendorId
        }

        // 정렬조건 추가
        jpql += " ORDER BY b.documentDate DESC"
        //jpql += " ORDER BY b.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, SettlementDefaultResult::class.java)
        val countQuery = entityManager.createQuery(countJpql, Long::class.java)

        params.forEach { (k, v) ->
            query.setParameter(k, v)
            countQuery.setParameter(k, v)
        }

        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        val settlementData = query.resultList
        val total = countQuery.singleResult

        return PageImpl(settlementData, pageable, total)
    }

    override fun searchAccountCode(request: SearchAccountFilters): PageImpl<Account> {
        val pageable = request.pageable.toPageable()

        var jpql = """
            SELECT a
            FROM Account a
            WHERE 1=1
        """.trimIndent()
        var countJpql = """
            SELECT COUNT(a)
            FROM Account a
            WHERE 1=1
        """.trimIndent()
        val params = mutableMapOf<String, Any>()

        // companyCode 필수조건
        jpql += " AND a.accountKey.companyCode = :companyCode"
        countJpql += " AND a.accountKey.companyCode = :companyCode"
        params["companyCode"] = request.companyCode

        if (request.accountCode != null) {
            jpql += " AND a.accountKey.accountCode = :accountCode "
            countJpql += " AND a.accountKey.accountCode = :accountCode "
            params["accountCode"] = request.accountCode
        }

        if (request.isOpenItemMgmt == OpenItemStatus.OPEN) {
            jpql += " AND a.isOpenItemMgmt = true "
            countJpql += " AND a.isOpenItemMgmt = true "
            //params["isOpenItemMgmt"] = request.isOpenItemMgmt
        }

        // 정렬조건 추가
        jpql += " ORDER BY a.accountKey.accountCode ASC"
        //jpql += " ORDER BY b.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

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



