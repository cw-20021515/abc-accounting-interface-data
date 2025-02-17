package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.model.response.ResAccountCaseData
import com.abc.us.accounting.supports.QueryUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component

@Component
class AccountingAccountRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager


    // 기타 부대비용
    fun selectOrderInventoryAssetsAccount() : List<ResAccountCaseData> {
        val query = """
            SELECT
                aai.account_code,
                aai.account_name
            FROM
                accounts_account_info aai
            WHERE
                aai.group_name IN ('GR/IR','Other Inventory Assets')
        """.trimIndent()
        val response =  QueryUtil.getQuery(entityManager, query, ResAccountCaseData::class.java )
        return response.resultList as List<ResAccountCaseData>
    }

    // 개인비용 지급
    fun selectExpenseAccount() : List<ResAccountCaseData> {
        val query = """
            SELECT
                aai.account_code,
                aai.account_name
            FROM
                accounts_account_info aai
            WHERE
                is_group_account='N' AND account_type ='O' or account_type ='V'
        """.trimIndent()
        val response =  QueryUtil.getQuery( entityManager, query, ResAccountCaseData::class.java )
        return response.resultList as List<ResAccountCaseData>
    }

    // 물품대금 지급
    fun selectAccountsPayableAccount(): List<ResAccountCaseData> {
        val query = """
            SELECT
                aai.account_code,
                aai.account_name
            FROM
                accounts_account_info aai
            WHERE
                aai.group_name IN('Goods in Consignment','GR/IR','Cost of Other Sales') AND
                aai.account_type IN('M','O')
        """.trimIndent()
        val response =  QueryUtil.getQuery( entityManager, query, ResAccountCaseData::class.java )
        return response.resultList as List<ResAccountCaseData>
    }
}