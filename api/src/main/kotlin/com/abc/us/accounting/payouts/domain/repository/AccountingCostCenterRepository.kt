package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.model.response.ResAccountCostCenterDto
import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.supports.QueryUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component

@Component
class AccountingCostCenterRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager


    // 코스트센터
    fun selectBillingCostCenterList(costCenter:String?) : List<ResAccountCostCenterDto> {
        var reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("isActive", "Y", "AND IS_ACTIVE = 'Y'"))
            .add(ReqQueryNative("costCenter", costCenter, """
                AND (CENTER_ID = :costCenter
                    OR UPPER(ACCOUNT_TYPE) LIKE UPPER(CONCAT('%',:costCenter, '%'))
                    OR UPPER(CENTER_NAME) LIKE UPPER(CONCAT('%',:costCenter, '%'))
                    OR UPPER(DESCRIPTION) LIKE UPPER(CONCAT('%',:costCenter, '%'))
                )
            """))
        val query = querySelect(reqQueryList.get())
        val response =  QueryUtil.getQuery( entityManager, query, ResAccountCostCenterDto::class.java )
        reqQueryList.setParameter(response)
        return response.resultList as List<ResAccountCostCenterDto>
    }

    fun querySelect(where:String?):String{
        // 확인이후 적용(N100\t\t\t) ,TRIM(BOTH '		' FROM COMPANY_ID) AS COMPANY_ID
        val query = """
        SELECT ACCOUNT_TYPE                          /* 유형 */
            ,CENTER_ID                               /* 센터 아이디 */
            ,PARENT_CENTER_ID                        /* 부모 센터 아이디 */
            ,CENTER_NAME                             /* 센터 명 */
            ,CENTER_TYPE                             /* 센터 타입 PROFIT COST */
            ,CENTER_SUB_TYPE AS CATEGORY             /* 카테고리 */
            ,DESCRIPTION                             /* 설명 */
            ,COMPANY_ID                              /* 회사 식별자 */
        FROM COST_CENTER_INFO A
        $where
        """.trimIndent()
        return query
    }
}