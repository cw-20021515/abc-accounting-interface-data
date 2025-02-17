package com.abc.us.accounting.rentals.lease.domain.repository

import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.lease.model.ResRentalFinancialLeaseSchedule
import com.abc.us.accounting.supports.QueryUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface RentalFinancialLeaseMasterRepository : JpaRepository<RentalFinancialDepreciationHistoryEntity, String> {
    @Query(value = """
    SELECT
        A.ID AS F_LEASE_ID,                          /** 금융 리스 고유 식별자 **/
        A.ORDER_ID,                                  /** 주문 ID **/
        A.ORDER_ITEM_ID,                             /** 주문 항목 ID **/
        A.CUSTOMER_ID,                               /** 고객 ID **/
        A.SERIAL_NUMBER,                             /** 일련 번호 **/
        A.CONTRACT_ID,                               /** 계약 ID **/
        A.MATERIAL_ID,                               /** 자재 ID **/
        A.MATERIAL_SERIES_CODE,                      /** 자재 시리즈 코드 **/
        TO_CHAR(CAST(A.BASE_DATE AS DATE), 'yyyy-MM-dd') AS BASE_DATE,           /** 기준일 **/
        TO_CHAR(CAST(A.CONTRACT_DATE AS DATE), 'yyyy-MM-dd') AS CONTRACT_DATE,   /** 계약 날짜 **/
        TO_CHAR(CAST(A.CONTRACT_END_DATE AS DATE), 'yyyy-MM-dd') AS CONTRACT_END_DATE,   /** 계약 종료일 **/
        A.CONTRACT_PERIOD,                           /** 계약 기간 **/
        A.INTEREST_RATE,                             /** 이자율 **/
        ROUND(A.INITIAL_BOOK_VALUE, 2) AS INITIAL_BOOK_VALUE,                     /** 최초 장부 가치 **/
        ROUND(A.RENTAL_AMOUNT, 2) AS RENTAL_AMOUNT,                              /** 대여금액 **/
        ROUND(A.RENTAL_AMOUNT_FOR_GOODS, 2) AS RENTAL_AMOUNT_FOR_GOODS,          /** 렌탈료(재화) **/
        ROUND(A.INITIAL_BOOK_VALUE, 2) AS INITIAL_BOOK_VALUE,                     /** 최초 장부 금액 **/
        ROUND(A.INITIAL_PRESENT_VALUE, 2) AS INITIAL_PRESENT_VALUE,               /** 최초 현재 가치(PV) **/
        ROUND(A.INITIAL_CURRENT_DIFFERENCE, 2) AS INITIAL_CURRENT_DIFFERENCE,     /** 최초 현 할차 **/
        A.CREATE_TIME,                               /** 등록일 **/
        B.MATERIAL_SERIES_CODE AS CATEGORY,          /* 카테고리(SERIES_CODE) 정보 */
        B.MATERIAL_TYPE,                             /* 제품 분류 코드 */
        B.MATERIAL_MODEL_NAME AS MODEL_NAME          /* 모델명 */
    FROM
        RENTAL_FINANCIAL_DEPRECIATION_HISTORY A
    LEFT JOIN
        COLLECT_MATERIAL B ON A.MATERIAL_ID = B.MATERIAL_ID
    WHERE
        A.RENTAL_EVENT_TYPE = 'FLEASE_REGISTRATION'
    AND (:contractId IS NULL OR A.CONTRACT_ID = :contractId)
    ORDER BY
        A.CONTRACT_DATE DESC
    LIMIT 1
""", nativeQuery = true)
    fun selectBySearchRentalsInfo(
        @Param("contractId") contractId: String?,
    ): ResRentalFinancialLeaseSchedule?
}

@Component
class RentalFinancialDepreciationMasterRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    // 상세조회
    fun selectBySearchRentalsInfo(contractId:String) : ResRentalFinancialLeaseSchedule? {
        var reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("contractId", contractId, "AND A.CONTRACT_ID = :contractId"))
        val query = querySelect(reqQueryList.get())
        val nativeQuery =  QueryUtil.getJsonQuery( entityManager, query, ResRentalFinancialLeaseSchedule::class.java )
        reqQueryList.setParameter(nativeQuery)
        var resultList = nativeQuery.resultList
        val collectionType = object : TypeReference<ResRentalFinancialLeaseSchedule>() {}
        if(!resultList.isNullOrEmpty()){
            val resultCamel: String = MapperUtil.convertJsonKeysToCamelCase(MapperUtil.gson.toJson(resultList.first()))
            return MapperUtil.mapper.readValue(resultCamel, collectionType)
        }else{
            return null
        }
    }

    fun querySelect(where:String?):String{
        val query = """

        SELECT * FROM 
        (
            SELECT
                A.ID AS F_LEASE_ID                          /** 금융 리스 고유 식별자 **/
                , A.ORDER_ID                                /** 주문 ID **/
                , A.ORDER_ITEM_ID                           /** 주문 항목 ID **/
                , A.CUSTOMER_ID                             /** 고객 ID **/
                , A.SERIAL_NUMBER                           /** 일련 번호 **/
                , A.CONTRACT_ID                             /** 계약 ID **/
                , A.MATERIAL_ID                             /** 자재 ID **/
                , A.MATERIAL_SERIES_CODE                    /** 자재 시리즈 코드 **/
                , TO_CHAR(CAST(A.BASE_DATE AS DATE)         , 'yyyy-MM-dd') AS BASE_DATE            /** 기준일 **/
                , TO_CHAR(CAST(A.CONTRACT_DATE AS DATE)     , 'yyyy-MM-dd') AS CONTRACT_DATE        /** 계약 날짜 **/
                , TO_CHAR(CAST(A.CONTRACT_END_DATE AS DATE) , 'yyyy-MM-dd') AS CONTRACT_END_DATE    /** 계약 종료일 **/
                , A.CONTRACT_PERIOD                         /** 계약 기간 **/
                , A.INTEREST_RATE                            /** 이자율 **/
                , ROUND(A.INITIAL_BOOK_VALUE, 2) as INITIAL_BOOK_VALUE                       /** 최초 장부 가치 **/
                , ROUND(A.RENTAL_AMOUNT, 2) as RENTAL_AMOUNT                            /** 대여금액 **/
                , ROUND(A.RENTAL_AMOUNT_FOR_GOODS, 2) as RENTAL_AMOUNT_FOR_GOODS                  /** 렌탈료(재화) **/
                , ROUND(A.INITIAL_BOOK_VALUE, 2) as INITIAL_BOOK_VALUE                       /** 최초 장부 금액 **/
                , ROUND(A.INITIAL_PRESENT_VALUE, 2) as INITIAL_PRESENT_VALUE                    /** 최초 현재 가치(PV) **/
                , ROUND(A.INITIAL_CURRENT_DIFFERENCE, 2) as INITIAL_CURRENT_DIFFERENCE               /** 최초 현 할차 **/
                , A.CREATE_TIME                             /** 등록일 **/
                , B.MATERIAL_SERIES_CODE AS CATEGORY		/* 카테고리(SERIES_CODE) 정보 */
                , B.MATERIAL_TYPE                            /* 제품 분류 코드 */
                , B.MATERIAL_MODEL_NAME AS MODEL_NAME		/* 모델명 */
            FROM (
                SELECT
                    *
                FROM
                    RENTAL_FINANCIAL_DEPRECIATION_HISTORY
                WHERE
                    RENTAL_EVENT_TYPE = 'FLEASE_REGISTRATION'
            ) A
            LEFT JOIN COLLECT_MATERIAL B
            ON A.MATERIAL_ID = B.MATERIAL_ID 
        ) A
        $where
        ORDER BY A.CONTRACT_DATE DESC
        """.trimIndent()
        return query
    }
}