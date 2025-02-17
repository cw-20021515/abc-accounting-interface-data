package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.DocumentItemAttributeMaster
import com.abc.us.accounting.documents.domain.type.AccountType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface DocumentItemAttributeMasterRepository : JpaRepository<DocumentItemAttributeMaster, String> {

    @Query("""
        SELECT m
        FROM DocumentItemAttributeMaster m
        WHERE m.accountType in :accountTypes 
        AND m.isActive = :isActive
    """)
    fun findAllByAccountTypeIn (accountTypes: List<AccountType> = AccountType.entries, isActive:Boolean = true): List<DocumentItemAttributeMaster>

    @Query("""
        SELECT A.*
        FROM DOCUMENT_ITEM_ATTRIBUTE_MASTER A
        INNER JOIN ACCOUNT B
        ON A.ACCOUNT_TYPE = B.ACCOUNT_TYPE 
        WHERE TRUE 
        AND A.FIELD_REQUIREMENT IN ('REQUIRED', 'OPTIONAL')
        AND (A.ATTRIBUTE_CATEGORY  = 'ATTRIBUTE' 
        	OR (A.ATTRIBUTE_CATEGORY = 'ASSIGNMENT' and A.FIELD_REQUIREMENT = 'REQUIRED'))
        AND CAST(A.ACCOUNT_TYPE AS VARCHAR) = B.ACCOUNT_TYPE
        AND B.ACCOUNT_CODE  = :accountCode
    """, nativeQuery = true)
    fun getAllByAccountCode(accountCode:String): List<DocumentItemAttributeMaster>

}

