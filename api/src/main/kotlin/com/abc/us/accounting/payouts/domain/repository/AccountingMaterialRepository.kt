package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.model.response.ResAccountMaterialDto
import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.supports.QueryUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component

@Component
class AccountingMaterialRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    // 자제
    fun selectBySearchMatterial(material:String?) : List<ResAccountMaterialDto> {
        var reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("isActive", "Y", "AND IS_ACTIVE = 'Y'"))
            .add(ReqQueryNative("material", material, """
                AND (UPPER(MATERIAL_ID)         LIKE UPPER(CONCAT('%',:material, '%'))
                    OR UPPER(MATERIAL_NAME)     LIKE UPPER(CONCAT('%',:material, '%'))
                )
            """)
            )
        val query = querySelect(reqQueryList.get())
        val response =  QueryUtil.getQuery( entityManager, query, ResAccountMaterialDto::class.java )
        reqQueryList.setParameter(response)
        return response.resultList as List<ResAccountMaterialDto>
    }

    fun querySelect(where:String?):String{
        val query = """
        SELECT
            MATERIAL_ID                       		/* 자재 식별자 */
            ,MATERIAL_TYPE                          /* 자재 유형 */
            ,MATERIAL_NAME							/* 자재 명 */
            ,CREATE_TIME                            /* 생성 시간 */
            ,DESCRIPTION                            /* 설명 */
        FROM COLLECT_MATERIAL A
        $where
        ORDER BY MATERIAL_ID
        """.trimIndent()
        return query
    }
}