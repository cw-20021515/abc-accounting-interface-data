package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.commons.domain.repository.BaseRepository
import com.abc.us.accounting.documents.domain.entity.*
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.model.RefDocItemResult
import jakarta.persistence.EntityManager
import mu.KotlinLogging
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface DocumentRepository : JpaRepository<Document, String>, BaseRepository<Document, String> {

    fun findByDocHash(docHash: String): Document?
    fun findAllByDocHashIn(docHashes: List<String>): List<Document>


//    @Query("""
//        SELECT d FROM Document d
//        JOIN DocumentItem di ON d.id = di.docId
//        LEFT JOIN DocumentItemRelation dir ON di.id = dir.docItemId
//        WHERE d.createTime BETWEEN :startTime AND :endTime
//        AND d.companyCode = :companyCode AND di.companyCode = :companyCode
//        AND dir.relationType is NULL
//    """)
@Query("""
        SELECT d FROM Document d
        WHERE d.createTime BETWEEN :startTime AND :endTime
              AND d.companyCode = :companyCode
        AND EXISTS (
            SELECT 1
            FROM DocumentItem di
            LEFT JOIN DocumentItemRelation dir on dir.docItemId = di._id
            WHERE d._id = di.docId
                AND dir.relationType IS NULL
        )
    """)
//    @Query("""
//        SELECT * FROM document d
//        WHERE d.create_time BETWEEN :startTime AND :endTime
//              AND d.company_code = :companyCode
//        AND EXISTS (
//            SELECT 1
//            FROM document_item di
//            LEFT JOIN document_item_relation dir ON dir.doc_item_id = di.id
//            WHERE d.id = di.doc_id
//                AND dir.relation_type IS NULL
//        )
//    """, nativeQuery = true)
    fun lookupForClearing(companyCode: CompanyCode, startTime: OffsetDateTime, endTime: OffsetDateTime): List<Document>
}

//@Repository
//class CustomDocumentRepository(
//    private val entityManager: EntityManager
//) {
//    fun lookupForClearing(startTime: OffsetDateTime, endTime: OffsetDateTime): List<Document> {
//        val query = entityManager.createNativeQuery("""
//            SELECT * FROM document
//            WHERE doc_hash = ANY(:docHashes)
//        """, Document::class.java
//        )
//        return query.setParameter("docHashes", docHashes.toTypedArray()).resultList.map { it as Document }
//    }
//}


@Repository
interface DocumentOriginRepository : JpaRepository<DocumentOrigin, String>, BaseRepository<DocumentOrigin, String> {
    fun findByDocId(docId: String): DocumentOrigin?

    fun findAllByDocIdIn(docIds: List<String>): List<DocumentOrigin>
}

interface DocumentRelationRepository : JpaRepository<DocumentRelation, String>,
    BaseRepository<DocumentRelation, String> {
    fun findAllByDocIdIn(docIds: List<String>): List<DocumentRelation>
}

interface CustomDocumentRelationRepository {
    fun findRelations(docIds: List<String>, refDocIds:List<String>, relationTypes: List<RelationType> ): List<DocumentRelation>
}

@Repository
class CustomDocumentRelationRepositoryImpl(
    private val entityManager: EntityManager
) : CustomDocumentRelationRepository {

    override fun findRelations(docIds: List<String>, refDocIds: List<String>, relationTypes: List<RelationType>): List<DocumentRelation> {
        val query = entityManager.createNativeQuery("""
                     SELECT * FROM document_relation a
                        WHERE ( a.doc_id = ANY(:docIds) OR a.ref_doc_id = ANY(:refDocIds) ) 
                        and a.relation_type = ANY( :relationTypes)
                """, DocumentRelation::class.java
        )
        return query.setParameter("docIds", docIds.toTypedArray())
            .setParameter("refDocIds", refDocIds.toTypedArray())
            .setParameter("relationTypes", relationTypes.map { it.code }.toTypedArray())
            .resultList.map { it as DocumentRelation }
    }
}

@Repository
interface DocumentItemRepository : JpaRepository<DocumentItem, String>, BaseRepository<DocumentItem, String>
{
    fun findByDocId(docId: String): List<DocumentItem>
    fun findAllByDocIdIn(docIds: List<String>): List<DocumentItem>

    @Query("""
        WITH criteria_values AS (
            SELECT
                unnest(?1) as doc_template_code,
                unnest(?2) as account_code,
                unnest(?3) as account_side,
                unnest(?4) as customer_id,
                unnest(?5) as vendor_id,
                unnest(?6) as order_item_id
        )
        SELECT DISTINCT di.* FROM document_item di
        JOIN document_origin origin ON di.doc_id = origin.doc_id 
        JOIN document_item_attribute dia ON di.id = dia.doc_item_id 
        JOIN criteria_values cv ON
            origin.doc_template_code = cv.doc_template_code AND
            di.account_code = cv.account_code AND
            di.account_side = cv.account_side AND
            COALESCE(di.customer_id, '') = COALESCE(cv.customer_id, '') AND
            COALESCE(di.vendor_id, '') = COALESCE(cv.vendor_id, '') AND 
            dia.attribute_type = 'ORDER_ITEM_ID' AND
            COALESCE(dia.value, '') = COALESCE(cv.order_item_id, '')
    """, nativeQuery = true)
    fun findByMatchedParams(
        docTemplateCodes: Array<String>,
        accountCodes: Array<String>,
        accountSides: Array<String>,
        customerIds: Array<String>,
        vendorIds: Array<String>,
        orderItemIds: Array<String>
    ): List<DocumentItem>
}


@Repository
class CustomDocumentItemRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val logger = KotlinLogging.logger {}

    fun findByMatchedParamsByCriteria(
        docTemplateCodes: Array<String>,
        accountCodes: Array<String>,
        accountSides: Array<String>,
        customerIds: Array<String>,
        vendorIds: Array<String>,
        orderItemIds: Array<String>
    ): List<RefDocItemResult> {
        val sql = """
            SELECT DISTINCT
                di.id,
                di.doc_id,
                di.account_code,
                di.account_side,
                di.company_code,
                di.doc_template_code,
                di.customer_id,
                di.vendor_id,
                dia.value as order_item_id
            FROM document_item di
            JOIN document_origin origin ON di.doc_id = origin.doc_id
            JOIN document_item_attribute dia ON di.id = dia.doc_item_id
            WHERE
            (origin.doc_template_code = ANY(?) AND
             di.account_code = ANY(?) AND
             di.account_side = ANY(?) AND
             COALESCE(di.customer_id, '') = ANY(?) AND
             COALESCE(di.vendor_id, '') = ANY(?) AND
             dia.attribute_type = 'ORDER_ITEM_ID' AND
             COALESCE(dia.value, '') = ANY(?))
        """
        return jdbcTemplate.query(sql,
            { rs, _ ->

                val docItemId = rs.getString("id")
                val docId = rs.getString("doc_id")
                val accountCode = rs.getString("account_code")
                val accountSide = AccountSide.of(rs.getString("account_side"))
                val companyCode = CompanyCode.valueOf(rs.getString("company_code"))
                val templateCode = rs.getString("doc_template_code")
                val docTemplateCode = DocumentTemplateCode.findBySymbolOrNull(templateCode)
                val customerId = rs.getString("customer_id")
                val vendorId = rs.getString("vendor_id")
                val orderItemId = rs.getString("order_item_id")

                if ( docTemplateCode == null ) {
                    logger.warn("DocumentTemplateCode not found for symbol: $templateCode, by docId: $docId, accountCode: $accountCode, accountSide: $accountSide, companyCode: $companyCode, docTemplateCode: $docTemplateCode, customerId: $customerId, vendorId: $vendorId, orderItemId: $orderItemId")
                } else {
                    logger.debug("docId: $docId, accountCode: $accountCode, accountSide: $accountSide, companyCode: $companyCode, docTemplateCode: $docTemplateCode, customerId: $customerId, vendorId: $vendorId, orderItemId: $orderItemId")
                }
                RefDocItemResult(
                    docItemId = docItemId,
                    docId = docId,
                    accountCode = accountCode,
                    accountSide = accountSide,
                    companyCode = companyCode,
                    docTemplateCode = docTemplateCode,
                    customerId = customerId,
                    vendorId = vendorId,
                    orderItemId = orderItemId
                )
            },
            docTemplateCodes, accountCodes, accountSides, customerIds, vendorIds, orderItemIds
        )
    }
}

@Repository
interface DocumentItemAttributeRepository : JpaRepository<DocumentItemAttribute, DocumentItemAttributeId>,
    BaseRepository<DocumentItemAttribute, DocumentItemAttributeId> {
    @Query(value = """
        SELECT a FROM DocumentItemAttribute a
        WHERE a.attributeId.docItemId in :docItemIds
    """)
    fun findAllByDocItemIdIn(docItemIds: List<String>): List<DocumentItemAttribute>

    @Modifying
    @Query("DELETE FROM account_balance_record WHERE doc_item_id IN :docItemIds", nativeQuery = true)
    fun deleteAllByDocItemIdIn(docItemIds: List<String>)
}


interface DocumentHistoryRepository : JpaRepository<DocumentHistory, Long>, BaseRepository<DocumentHistory, Long> {
    fun findAllByDocId(docId: String): List<DocumentHistory>
    fun findAllByDocIdIn(docIds: List<String>): List<DocumentHistory>
}


interface DocumentItemHistoryRepository : JpaRepository<DocumentItemHistory, Long>,
    BaseRepository<DocumentItemHistory, Long> {
    fun findAllByDocItemId(docItemId: String): List<DocumentItemHistory>
    fun findAllByDocItemIdIn(docItemIds: List<String>): List<DocumentItemHistory>
}


interface DocumentItemRelationRepository : JpaRepository<DocumentItemRelation, String>,
    BaseRepository<DocumentItemRelation, String> {
}

interface CustomDocumentItemRelationRepository {
    fun findRelations(docItemIds: List<String>, refDocItemIds: List<String>, relationTypes: List<RelationType>): List<DocumentItemRelation>
}

@Repository
class CustomDocumentItemRelationRepositoryImpl(
    private val entityManager: EntityManager
) : CustomDocumentItemRelationRepository {

    private fun query(docItemIds: List<String>, refDocItemIds: List<String>, relationTypes: List<RelationType>): jakarta.persistence.Query {
        when {
            docItemIds.isEmpty() && refDocItemIds.isEmpty() -> {
                val query = entityManager.createNativeQuery("""
                    SELECT * FROM document_item_relation
                    WHERE relation_type = ANY(:relationTypes)
                """, DocumentItemRelation::class.java
                )

                query.setParameter("relationTypes", relationTypes.map { it.code }.toTypedArray())
                return query
            }
            refDocItemIds.isEmpty() -> {
                val query = entityManager.createNativeQuery("""
                    SELECT * FROM document_item_relation
                    WHERE doc_item_id = ANY(:docItemIds) AND relation_type = ANY(:relationTypes)
                """, DocumentItemRelation::class.java
                )
                query.setParameter("docItemIds", docItemIds.toTypedArray())
                    .setParameter("relationTypes", relationTypes.map { it.code }.toTypedArray())
                return query
            }
            docItemIds.isEmpty() -> {
                val query = entityManager.createNativeQuery("""
                    SELECT * FROM document_item_relation
                    WHERE ref_doc_item_id = ANY(:refDocItemIds) AND relation_type = ANY(:relationTypes)
                """, DocumentItemRelation::class.java
                )
                query.setParameter("refDocItemIds", refDocItemIds.toTypedArray())
                    .setParameter("relationTypes", relationTypes.map { it.code }.toTypedArray())
                return query
            }
            else -> {
                val query = entityManager.createNativeQuery("""
                    SELECT * FROM document_item_relation
                    WHERE (doc_item_id = ANY(:docItemIds) OR ref_doc_item_id = ANY(:refDocItemIds))
                    AND relation_type = ANY(:relationTypes)
                """, DocumentItemRelation::class.java
                )
                query.setParameter("docItemIds", docItemIds.toTypedArray())
                    .setParameter("refDocItemIds", refDocItemIds.toTypedArray())
                    .setParameter("relationTypes", relationTypes.map { it.code }.toTypedArray())
                return query
            }
        }
    }

    override fun findRelations(docItemIds: List<String>, refDocItemIds: List<String>, relationTypes: List<RelationType>): List<DocumentItemRelation> {
        val query = query(docItemIds, refDocItemIds, relationTypes)
        return query.resultList.map { it as DocumentItemRelation }
    }
}


interface DocumentNoteRepository : JpaRepository<DocumentNote, Long>, BaseRepository<DocumentNote, Long> {
    @Query("SELECT dn FROM DocumentNote dn WHERE dn.docId IN :docIds AND dn.isDeleted = 'N'")
    fun findAllByDocIdIn(docIds: List<String>): List<DocumentNote>

    @Modifying
    @Query("""
        UPDATE DocumentNote 
           SET contents = :contents
             , updateTime = :updateTime
             , updatedBy = :updatedBy 
         WHERE id = :id
    """)
    fun updateById(id: Long, contents: String, updateTime: OffsetDateTime, updatedBy: String): Int

    @Modifying
    @Query("""
        UPDATE DocumentNote 
           SET isDeleted = 'Y'
             , updateTime = :updateTime
             , updatedBy = :updatedBy 
         WHERE id = :id
    """)
    fun updateIsDeletedById(id: Long, updateTime: OffsetDateTime, updatedBy: String): Int

}


interface DocumentAttachmentRepository : JpaRepository<DocumentAttachment, Long>,
    BaseRepository<DocumentAttachment, Long> {
    fun findAllByDocIdIn(docIds: List<String>): List<DocumentAttachment>
}


