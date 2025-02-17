package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.model.response.ResCompanyInfoData
import com.abc.us.accounting.qbo.domain.repository.QboCompanyRepository
import com.abc.us.accounting.supports.QueryUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component

@Component
class AccountingCompanyRepository(var qboCompanyRepository: QboCompanyRepository) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    // 회사 조회
   fun selectBySearchCompany() : List<ResCompanyInfoData> {
        val query = """
            SELECT CODE AS COMPANY_ID, NAME AS COMPANY_NAME, COUNTRY
            FROM COMPANY
        """.trimIndent()
        val response =  QueryUtil.getQuery(entityManager, query, ResCompanyInfoData::class.java )
        return response.resultList as List<ResCompanyInfoData>
    }

}