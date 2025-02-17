package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateKey
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.CompanyCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DocumentTemplateRepository: JpaRepository<DocumentTemplate, DocumentTemplateKey> {
}


@Repository
interface DocumentTemplateItemRepository: JpaRepository<DocumentTemplateItem, Int> {
    @Query("""
        select i from DocumentTemplateItem i
        where i.docTemplateKey.companyCode in :companyCode
    """)
    fun findAllByCompanyCode(companyCode:CompanyCode): List<DocumentTemplateItem>

    fun findByDocTemplateKey(docTemplateKey: DocumentTemplateKey): List<DocumentTemplateItem>
    fun findAllByDocTemplateKeyIn(docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplateItem>

    fun findByDocTemplateKeyAndAccountSide(docTemplateKey: DocumentTemplateKey, accountSide: AccountSide): List<DocumentTemplateItem>


    @Query("""
        select i from DocumentTemplateItem i
        where i.docTemplateKey in :docTemplateKeys and i.refDocTemplateCode is not null
    """)
    fun findCandidateForClearingItems(docTemplateKeys: List<DocumentTemplateKey>): List<DocumentTemplateItem>

    @Query("""
        SELECT new kotlin.Pair(clearing, cleared)
        FROM DocumentTemplateItem clearing
        LEFT JOIN DocumentTemplateItem cleared ON
                clearing.docTemplateKey.companyCode = cleared.docTemplateKey.companyCode
                and clearing.refDocTemplateCode = cleared.docTemplateKey.docTemplateCode
                and clearing.accountCode = cleared.accountCode
                and clearing.accountSide != cleared.accountSide
        WHERE clearing.docTemplateKey in :docTemplateKeys and clearing.refDocTemplateCode is not null
    """)

//    @Query("""
//        SELECT new kotlin.Pair(clearing, cleared)
//        FROM DocumentTemplateItem clearing
//        JOIN UNNEST(clearing.refDocTemplateCodes) refCode
//        LEFT JOIN DocumentTemplateItem cleared ON
//                clearing.docTemplateKey.companyCode = cleared.docTemplateKey.companyCode
//                and cleared.docTemplateKey.docTemplateCode = refCode
//                and clearing.accountCode = cleared.accountCode
//                and clearing.accountSide != cleared.accountSide
//        WHERE clearing.docTemplateKey in :docTemplateKeys
//              and clearing.refDocTemplateCodes is not empty
//    """)
    fun findDocTemplateItemPairsForClearing(docTemplateKeys: List<DocumentTemplateKey>): List<Pair<DocumentTemplateItem, DocumentTemplateItem>>
}