package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.type.DocumentDateType
import com.abc.us.accounting.documents.model.SearchDocumentFilters
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository
import java.time.LocalDate


interface DocumentSearchRepository {
    fun searchDocuments(request: SearchDocumentFilters): Page<Document>
}

@Repository
class DocumentSearchRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : DocumentSearchRepository {

    override fun searchDocuments(request: SearchDocumentFilters): Page<Document> {
        val pageable = request.pageable.toPageable()

        var jpql = "SELECT a FROM Document a WHERE 1=1"
        var countJpql = "SELECT COUNT(a) FROM Document a WHERE 1=1"
        val params = mutableMapOf<String, Any>()

        val rangeCondition = rangeCondition(request.dateType, request.fromDate, request.toDate)
        jpql += " AND $rangeCondition"
        countJpql += " AND $rangeCondition"
        params["from"] = request.fromDate
        params["to"] = request.toDate

        if (request.companyCode != null) {
            jpql += " AND a.companyCode = :companyCode"
            countJpql += " AND a.companyCode = :companyCode"
            params["companyCode"] = request.companyCode
        }

        if (request.fiscalYear != null) {
            jpql += " AND a.fiscalYearMonth.year = :fiscalYear"
            countJpql += " AND a.fiscalYearMonth.year = :fiscalYear"
            params["fiscalYear"] = request.fiscalYear
        }

        if (request.fiscalMonth != null) {
            jpql += " AND a.fiscalYearMonth.month = :fiscalMonth"
            countJpql += " AND a.fiscalYearMonth.month = :fiscalMonth"
            params["fiscalMonth"] = request.fiscalMonth
        }

        if (request.docType != null) {
            jpql += " AND a.docType = :docType"
            countJpql += " AND a.docType = :docType"
            params["docType"] = request.docType
        }

        if (request.createdBy != null) {
            jpql += " AND a.createdBy = :createdBy"
            countJpql += " AND a.createdBy = :createdBy"
            params["createdBy"] = request.createdBy
        }

        if (request.docStatus != null) {
            jpql += " AND a.docStatus = :docStatus"
            countJpql += " AND a.docStatus = :docStatus"
            params["docStatus"] = request.docStatus
        }

        // 정렬조건 추가
        jpql += " ORDER BY a.${request.pageable.sortBy.field} ${request.pageable.sortDirection.toSortDirection()}"

        val query = entityManager.createQuery(jpql, Document::class.java)
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

    fun rangeCondition (dateType: DocumentDateType, from: LocalDate, to: LocalDate): String {
        return when(dateType) {
            DocumentDateType.DOCUMENT_DATE -> "a.documentDate BETWEEN :from AND :to"
            DocumentDateType.POSTING_DATE -> "a.postingDate BETWEEN :from AND :to"
            DocumentDateType.ENTRY_DATE -> "a.entryDate BETWEEN :from AND :to"
        }
    }
}
