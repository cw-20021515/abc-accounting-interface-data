package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.DocumentDateType
import com.abc.us.accounting.documents.model.LedgerDefaultResult
import com.abc.us.accounting.documents.model.SearchLedgerFilters
import com.abc.us.generated.models.AccountingLedgerState
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository
import java.time.LocalDate

interface LedgerCustomRepository {
    fun searchLedger(request: SearchLedgerFilters): Page<LedgerDefaultResult>
}

@Repository
class LedgerSearchRepository(
    @PersistenceContext
    private val entityManager: EntityManager
) : LedgerCustomRepository {

    override fun searchLedger(request: SearchLedgerFilters): Page<LedgerDefaultResult> {
        val pageable = request.pageable.toPageable()

        var jpql = """
            SELECT DISTINCT a.id as docItemId, a.docId, b.postingDate
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            JOIN DocumentItemAttribute c ON a.id = c.attributeId.docItemId
            WHERE 1=1
        """.trimIndent()
        // JOIN DocumentItemAttribute c ON a.id = c.docItemId
         var countJpql = """
            SELECT COUNT(DISTINCT a)
            FROM DocumentItem a
            JOIN Document b ON a.docId = b.id
            JOIN DocumentItemAttribute c ON a.id = c.attributeId.docItemId
            WHERE 1=1           
        """.trimIndent()
        val params = mutableMapOf<String, Any>()

        val rangeCondition = rangeCondition(request.dateType, request.fromDate, request.toDate)
        jpql += " AND $rangeCondition"
        countJpql += " AND $rangeCondition"
        params["from"] = request.fromDate
        params["to"] = request.toDate

        if (request.companyCode != null) {
            jpql += " AND b.companyCode = :companyCode"
            countJpql += " AND b.companyCode = :companyCode"
            params["companyCode"] = request.companyCode
        }

        if (request.accountCodeFrom != null && request.accountCodeTo == null) {
            jpql += " AND a.accountCode = :accountCode "
            countJpql += " AND a.accountCode = :accountCode "
            params["accountCode"] = request.accountCodeFrom
        }

        if (request.accountCodeFrom != null && request.accountCodeTo != null) {
            jpql += " AND a.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            countJpql += " AND a.accountCode BETWEEN :accountCodeFrom AND :accountCodeTo "
            params["accountCodeFrom"] = request.accountCodeFrom
            params["accountCodeTo"] = request.accountCodeTo
        }

        if (request.costCenter != null && request.costCenter.length > 0) {
            jpql += " AND a.costCenter LIKE CONCAT(TRIM(:costCenter), '%')"
            countJpql += " AND a.costCenter LIKE CONCAT(TRIM(:costCenter), '%')"
            params["costCenter"] = request.costCenter
        }

        if (request.orderId != null && request.orderId.length > 0) {
            jpql += " AND c.attributeId.attributeType = 'ORDER_ID' AND c.value LIKE CONCAT(TRIM(:orderId), '%')"
            countJpql += " AND c.attributeId.attributeType = 'ORDER_ID' AND c.value LIKE CONCAT(TRIM(:orderId), '%')"
            params["orderId"] = request.orderId
        }

        if (request.customerId != null && request.customerId.length > 0) {
            jpql += " AND a.customerId LIKE CONCAT(TRIM(:customerId), '%')"
            countJpql += " AND a.customerId LIKE CONCAT(TRIM(:customerId), '%')"
            params["customerId"] = request.customerId
        }

        if (request.materialId != null && request.materialId.length > 0) {
            jpql += " AND c.attributeId.attributeType = 'MATERIAL_ID' AND c.value LIKE CONCAT(TRIM(:materialId), '%')"
            countJpql += " AND c.attributeId.attributeType = 'MATERIAL_ID' AND c.value LIKE CONCAT(TRIM(:materialId), '%')"
            params["materialId"] = request.materialId
        }

        if (request.serialNumber != null && request.serialNumber.length > 0) {
            jpql += " AND c.attributeId.attributeType = 'SERIAL_NUMBER' AND c.value LIKE CONCAT(TRIM(:serialNumber), '%')"
            countJpql += " AND c.attributeId.attributeType = 'SERIAL_NUMBER' AND c.value LIKE CONCAT(TRIM(:serialNumber), '%')"
            params["serialNumber"] = request.serialNumber
        }

        if (request.vendorId != null && request.vendorId.length > 0) {
            jpql += " AND a.vendorId LIKE CONCAT(TRIM(:vendorId), '%')"
            countJpql += " AND a.vendorId LIKE CONCAT(TRIM(:vendorId), '%')"
            params["vendorId"] = request.vendorId
        }

        if (request.payoutId != null && request.payoutId.length > 0) {
            jpql += " AND c.attributeId.attributeType = 'PAYOUT_ID' AND c.value LIKE CONCAT(TRIM(:payoutId), '%')"
            countJpql += " AND c.attributeId.attributeType = 'PAYOUT_ID' AND c.value LIKE CONCAT(TRIM(:payoutId), '%')"
            params["payoutId"] = request.payoutId
        }

//        if (request.purchaseOrderId != null && request.purchaseOrderId.length > 0) {
//            jpql += " AND c.attributeId.attributeType = 'ORDER_ID' AND c.value = :purchaseOrderId"
//            countJpql += " AND c.attributeId.attributeType = 'ORDER_ID' AND c.value = :purchaseOrderId"
//            params["purchaseOrderId"] = request.purchaseOrderId
//        }

        if (request.attributeType == DocumentAttributeType.CUSTOMER_ID && request.attributeTypeValue != null) {
            jpql += " AND a.customerId LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            countJpql += " AND a.customerId LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            params["attributeTypeValue"] = request.attributeTypeValue
        }

        if (request.attributeType == DocumentAttributeType.VENDOR_ID && request.attributeTypeValue != null) {
            jpql += " AND a.vendorId LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            countJpql += " AND a.vendorId LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            params["attributeTypeValue"] = request.attributeTypeValue
        }

        if (request.attributeType != DocumentAttributeType.CUSTOMER_ID && request.attributeType != DocumentAttributeType.VENDOR_ID
            && request.attributeType != null && request.attributeTypeValue != null) {
            jpql += " AND c.attributeId.attributeType = :attributeType AND c.value LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            countJpql += " AND c.attributeId.attributeType = :attributeType AND c.value LIKE CONCAT(TRIM(:attributeTypeValue), '%')"
            params["attributeType"] = request.attributeType
            params["attributeTypeValue"] = request.attributeTypeValue
        }

        if (request.accountingLedgerState == AccountingLedgerState.ALL) {
            jpql += " AND a.docItemStatus IN ('NO','CP','CD','CL','RD','RL') "
            countJpql += " AND a.docItemStatus IN ('NO','CP','CD','CL','RD','RL') "
        }
        if (request.accountingLedgerState == AccountingLedgerState.OPEN) {
            jpql += " AND a.docItemStatus IN ('NO','CP') "
            countJpql += " AND a.docItemStatus IN ('NO','CP') "
        }
        if (request.accountingLedgerState == AccountingLedgerState.CLEARED) {
            jpql += " AND a.docItemStatus IN ('CD','CL','RD','RL') "
            countJpql += " AND a.docItemStatus IN ('CD','CL','RD','RL') "
        }

        // 정렬조건 추가
        jpql += " ORDER BY b.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, LedgerDefaultResult::class.java)
        val countQuery = entityManager.createQuery(countJpql, Long::class.java)

        params.forEach { (k, v) ->
            query.setParameter(k, v)
            countQuery.setParameter(k, v)
        }

        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        val documents = query.resultList
        val total = countQuery.singleResult

        return PageImpl(documents, pageable, total)
    }

    fun rangeCondition (dateType: DocumentDateType, from:LocalDate, to:LocalDate): String {
        return when(dateType) {
            DocumentDateType.DOCUMENT_DATE -> "b.documentDate BETWEEN :from AND :to"
            DocumentDateType.POSTING_DATE -> "b.postingDate BETWEEN :from AND :to"
            DocumentDateType.ENTRY_DATE -> "b.entryDate BETWEEN :from AND :to"
        }
    }
}



