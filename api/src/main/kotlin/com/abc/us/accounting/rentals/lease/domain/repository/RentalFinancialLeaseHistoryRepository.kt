package com.abc.us.accounting.rentals.lease.domain.repository

import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.rentals.lease.domain.entity.RentalFinancialDepreciationHistoryEntity
import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseInqySchedule
import com.abc.us.accounting.rentals.lease.model.ResRentalFinancialLeaseInqyScheduleTemp
import com.abc.us.accounting.supports.QueryUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RentalFinancialLeaseHistoryRepository : JpaRepository<RentalFinancialDepreciationHistoryEntity, String> {
    fun findByContractIdInAndRentalEventType(
        contractIds: List<String>,
        rentalEventType: String
    ): List<RentalFinancialDepreciationHistoryEntity>

    @Query(
        """
            select
                a
            from
                RentalFinancialDepreciationHistoryEntity a
            where
                a.docHashCode in :docHashCodes
        """
    )
    fun findByDocHashCodes(docHashCodes: List<String>): List<RentalFinancialDepreciationHistoryEntity>

    @Query(
        """
        select
            a
        from
            RentalFinancialDepreciationHistoryEntity a
        where
            a.contractId in :contractIds
            and a.baseDate <= :baseDate
    """
    )
    fun findByContractIdsAndBaseDate(
        contractIds: List<String>,
        baseDate: LocalDate
    ): List<RentalFinancialDepreciationHistoryEntity>
}

@Repository
interface RentalFinancialLeaseHistoryDepRepository : JpaRepository<RentalFinancialDepreciationHistoryEntity, Int> {
    @Query(value = """
    SELECT * 
             , COUNT(1) OVER () 		AS TOTAL_CNT 
        FROM (
            SELECT A.*
            FROM (
                SELECT 
                    A.ORDER_ID,                                  /** 주문 ID **/
                    A.ORDER_ITEM_ID,                             /** 주문 항목 ID **/
                    A.CUSTOMER_ID,                               /** 고객 ID **/
                    A.SERIAL_NUMBER,                             /** 일련 번호 **/
                    A.CONTRACT_ID,                               /** 계약 ID **/
                    A.MATERIAL_ID,                               /** 자재 ID **/
                    A.MATERIAL_SERIES_CODE,                      /** 자재 시리즈 코드 **/
                    TO_CHAR(CAST(A.BASE_DATE AS DATE)           , 'yyyy-MM-dd') AS BASE_DATE,           /** 기준일 **/
                    TO_CHAR(CAST(A.CONTRACT_DATE AS DATE)       , 'yyyy-MM-dd') AS CONTRACT_DATE,       /** 계약 날짜 **/
                    TO_CHAR(CAST(A.CONTRACT_END_DATE AS DATE)   , 'yyyy-MM-dd') AS CONTRACT_END_DATE,   /** 계약 종료일 **/
                    A.CONTRACT_PERIOD,                           /** 계약 기간 **/
                    A.INITIAL_BOOK_VALUE,                           /** 최초 장부 가치 **/
                    A.INTEREST_RATE,                                     /** 이자율 **/
                    A.RENTAL_AMOUNT,                                     /** 대여금액 **/
                    A.INITIAL_PRESENT_VALUE,                     /** 최초 현재 가치(PV) **/
                    A.INITIAL_CURRENT_DIFFERENCE,           /** 최초 현 할차 **/
                    A.DEPRECIATION_COUNT,                                                           /** 금융삼각 회차 **/
                    A.DEPRECIATION_BOOK_VALUE,                 /** 장부 금액 **/
                    A.DEPRECIATION_PRESENT_VALUE,           /** 현재가치(PV) **/
                    A.DEPRECIATION_CURRENT_DIFFERENCE, /** 현 할차 **/
                    COALESCE(A.DEPRECIATION_INTEREST_INCOME, 0) as DEPRECIATION_INTEREST_INCOME,       /** 이자 수익 **/
                    A.CUMULATIVE_INTEREST_INCOME,           /** 이자 수익(누계) **/
                    A.CREATE_TIME,                               /** 등록일 **/
                    A.DOC_HASH_CODE,                             /** 해시 코드 **/
                    A.RENTAL_EVENT_TYPE,                         /** 이벤트 타입 **/
                    A.CURRENCY,                                  /** 통화 **/
                    A.DEPRECIATION_YEAR_MONTH,                   /** 기준년월 */
                    (case when A.DEPRECIATION_COUNT > 0 then A.DEPRECIATION_BILL_YEAR_MONTH else null end) as DEPRECIATION_BILL_YEAR_MONTH, /** 청구년월 */         
                    A.RENTAL_AMOUNT_FOR_GOODS,                   /** 렌탈료(재화) **/
                    B.MATERIAL_CATEGORY_CODE AS MATERIAL_CATEGORY, /** 카테고리(CATEGORY_CODE) 정보 **/
                    B.MATERIAL_TYPE,                            /* 제품 분류 코드 */
                    B.MATERIAL_MODEL_NAME AS MODEL_NAME,		/* 모델명 */
                    C.DEPRECIATION_COUNT 				AS DEPRECIATION_BILL_COUNT,
                    C.DEPRECIATION_BOOK_VALUE 			AS DEPRECIATION_BILL_BOOK_VALUE,                   /** 청구 장부 금액 **/
                    C.DEPRECIATION_PRESENT_VALUE 		AS DEPRECIATION_BILL_PRESENT_VALUE,                /** 청구 현재가치(PV) **/
                    C.DEPRECIATION_CURRENT_DIFFERENCE 	AS DEPRECIATION_BILL_CURRENT_DIFFERENCE,           /** 청구 현 할차 **/
                    C.DEPRECIATION_INTEREST_INCOME 		AS DEPRECIATION_BILL_INTEREST_INCOME,              /** 청구 이자 수익 **/
                    A.RENTAL_EVENT_TYPE,
                    ROW_NUMBER() OVER (PARTITION BY A.ORDER_ITEM_ID ORDER BY A.CREATE_TIME DESC) AS RN
                    /** PARTITION BY로 ORDER_ITEM_ID별로 데이터를 나누고, CREATE_TIME 기준으로 순위를 매김 **/
                FROM RENTAL_FINANCIAL_DEPRECIATION_HISTORY A
                LEFT JOIN COLLECT_MATERIAL B
                ON A.MATERIAL_ID = B.MATERIAL_ID
                LEFT JOIN RENTAL_FINANCIAL_DEPRECIATION_SCHEDULE C
                ON A.ORDER_ITEM_ID = C.ORDER_ITEM_ID
                AND A.DEPRECIATION_BILL_YEAR_MONTH = C.DEPRECIATION_BILL_YEAR_MONTH
                WHERE TRUE
                AND A.RENTAL_EVENT_TYPE not in ('BOOKING_SCHEDULED', 'BOOKING_CONFIRMED') /* 화면에 제외 대상 추가 */
                AND (:baseDate IS NULL OR A.BASE_DATE <= :baseDate)
        }
            ) A
            WHERE RN = 1
        ) A
        AND (:contractFromDate IS NULL OR A.CONTRACT_DATE >= :contractFromDate)
        AND (:contractToDate IS NULL OR A.CONTRACT_DATE <= :contractToDate)
        AND (:customerId IS NULL OR A.CUSTOMER_ID LIKE :customerId)
        AND (:orderItemId IS NULL OR A.ORDER_ITEM_ID LIKE :orderItemId)
        AND (:serialNumber IS NULL OR A.SERIAL_NUMBER LIKE :serialNumber)
        AND (:contractId IS NULL OR A.CONTRACT_ID LIKE :contractId)
        AND (:materialId IS NULL OR A.MATERIAL_ID LIKE :materialId)
        AND (:materialCategory IS NULL OR B.MATERIAL_CATEGORY_CODE = :materialCategory)
        ORDER BY A.CONTRACT_DATE DESC
""", nativeQuery = true)
    fun findByReq(
        @Param("baseDate") baseDate: LocalDate? = null,
        @Param("contractFromDate") contractFromDate: LocalDate? = null,
        @Param("contractToDate") contractToDate: LocalDate? = null,
        @Param("customerId") customerId: String? = null,
        @Param("orderItemId") orderItemId: String? = null,
        @Param("serialNumber") serialNumber: String? = null,
        @Param("contractId") contractId: String? = null,
        @Param("materialId") materialId: String? = null,
        @Param("materialCategory") materialCategory: String? = null,
        pageable: Pageable? = null
    ): Page<Map<String, Any>>
}

@Component
class RentalFinancialDepreciationHistoryRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    // 리스트 조회
    fun selectBySearchRentalsList(
        request: ReqRentalFinancialLeaseInqySchedule,
        pageable: Pageable?,
    ) : PageImpl<ResRentalFinancialLeaseInqyScheduleTemp>? {
        val reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("materialCategory"  , request.materialCategory?.name, "AND A.MATERIAL_CATEGORY = :materialCategory"))
            .add(ReqQueryNative("contractFromDate"  , request.contractFromDate  , "AND DATE(A.CONTRACT_DATE) >= DATE(:contractFromDate)"))
            .add(ReqQueryNative("contractToDate"    , request.contractToDate    , "AND DATE(A.CONTRACT_DATE) <= DATE(:contractToDate)"))
            .add(ReqQueryNative("orderItemId"       , request.orderItemId       , "AND A.ORDER_ITEM_ID  LIKE '%${request.orderItemId}%'"))
            .add(ReqQueryNative("customerId"        , request.customerId        , "AND A.CUSTOMER_ID    LIKE '%${request.customerId}%'"))
            .add(ReqQueryNative("contractId"        , request.contractId        , "AND A.CONTRACT_ID    LIKE '%${request.contractId}%'"))
            .add(ReqQueryNative("serialNumber"      , request.serialNumber      , "AND A.SERIAL_NUMBER  LIKE '%${request.serialNumber}%'"))
            .add(ReqQueryNative("materialId"        , request.materialId        , "AND A.MATERIAL_ID    LIKE '%${request.materialId}%'"))

        val baseCurrentDate = request.baseDate?.toString()
        val query = querySelect(reqQueryList.get(), baseCurrentDate)
        val nativeQuery =  QueryUtil.getJsonQuery( entityManager, query, ResRentalFinancialLeaseInqyScheduleTemp::class.java )
        reqQueryList.setParameter(nativeQuery)
        if (pageable != null) { // 엑셀 다운로드 조건 처리.
            val pageNumber = pageable.pageNumber
            val pageSize = pageable.pageSize
            nativeQuery.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize)
        }

        var resultList = nativeQuery.resultList
        if(!resultList.isNullOrEmpty()){
            val resultCamel: String = MapperUtil.convertJsonKeysToCamelCase(MapperUtil.gson.toJson(resultList))
            val collectionType = object : TypeReference<List<ResRentalFinancialLeaseInqyScheduleTemp>>() {}
            var resultData = MapperUtil.mapper.readValue(resultCamel, collectionType)
            var totalCnt: Long = resultData.getOrNull(0)?.totalCnt ?: 0L
            resultData.map { t ->
                t.totalCnt = null
            }
            if (pageable != null) {
                return PageImpl(resultData, pageable, totalCnt)
            }
            return PageImpl(resultData, Pageable.unpaged(), 0L)
        }else{
            if (pageable != null) { 
                return PageImpl(emptyList(), pageable, 0L)
            }

            return null
        }
    }

    fun querySelect(where:String?, baseCurrentDate:String?):String{
        var baseDate = ""
        if(!baseCurrentDate.isNullOrEmpty()){
            baseDate = "AND A.BASE_DATE <= '$baseCurrentDate'"
        }
        val query = """
        /* selectBySearchRentalsList */
        SELECT * 
             , COUNT(1) OVER () 		AS TOTAL_CNT 
        FROM (
            SELECT A.*
            FROM (
                SELECT 
                    A.ORDER_ID,                                  /** 주문 ID **/
                    A.ORDER_ITEM_ID,                             /** 주문 항목 ID **/
                    A.CUSTOMER_ID,                               /** 고객 ID **/
                    A.SERIAL_NUMBER,                             /** 일련 번호 **/
                    A.CONTRACT_ID,                               /** 계약 ID **/
                    A.MATERIAL_ID,                               /** 자재 ID **/
                    A.MATERIAL_SERIES_CODE,                      /** 자재 시리즈 코드 **/
                    TO_CHAR(CAST(A.BASE_DATE AS DATE)           , 'yyyy-MM-dd') AS BASE_DATE,           /** 기준일 **/
                    TO_CHAR(CAST(A.CONTRACT_DATE AS DATE)       , 'yyyy-MM-dd') AS CONTRACT_DATE,       /** 계약 날짜 **/
                    TO_CHAR(CAST(A.CONTRACT_END_DATE AS DATE)   , 'yyyy-MM-dd') AS CONTRACT_END_DATE,   /** 계약 종료일 **/
                    A.CONTRACT_PERIOD,                           /** 계약 기간 **/
                    A.INITIAL_BOOK_VALUE,                           /** 최초 장부 가치 **/
                    A.INTEREST_RATE,                                     /** 이자율 **/
                    A.RENTAL_AMOUNT,                                     /** 대여금액 **/
                    A.INITIAL_PRESENT_VALUE,                     /** 최초 현재 가치(PV) **/
                    A.INITIAL_CURRENT_DIFFERENCE,           /** 최초 현 할차 **/
                    A.DEPRECIATION_COUNT,                                                           /** 금융삼각 회차 **/
                    A.DEPRECIATION_BOOK_VALUE,                 /** 장부 금액 **/
                    A.DEPRECIATION_PRESENT_VALUE,           /** 현재가치(PV) **/
                    A.DEPRECIATION_CURRENT_DIFFERENCE, /** 현 할차 **/
                    COALESCE(A.DEPRECIATION_INTEREST_INCOME, 0) as DEPRECIATION_INTEREST_INCOME,       /** 이자 수익 **/
                    A.CUMULATIVE_INTEREST_INCOME,           /** 이자 수익(누계) **/
                    A.CREATE_TIME,                               /** 등록일 **/
                    A.DOC_HASH_CODE,                             /** 해시 코드 **/
                    A.RENTAL_EVENT_TYPE,                         /** 이벤트 타입 **/
                    A.CURRENCY,                                  /** 통화 **/
                    A.DEPRECIATION_YEAR_MONTH,                   /** 기준년월 */
                    (case when A.DEPRECIATION_COUNT > 0 then A.DEPRECIATION_BILL_YEAR_MONTH else null end) as DEPRECIATION_BILL_YEAR_MONTH, /** 청구년월 */         
                    A.RENTAL_AMOUNT_FOR_GOODS,                   /** 렌탈료(재화) **/
                    B.MATERIAL_CATEGORY_CODE AS MATERIAL_CATEGORY, /** 카테고리(CATEGORY_CODE) 정보 **/
                    B.MATERIAL_TYPE,                            /* 제품 분류 코드 */
                    B.MATERIAL_MODEL_NAME AS MODEL_NAME,		/* 모델명 */
                    C.DEPRECIATION_COUNT 				AS DEPRECIATION_BILL_COUNT,
                    C.DEPRECIATION_BOOK_VALUE 			AS DEPRECIATION_BILL_BOOK_VALUE,                   /** 청구 장부 금액 **/
                    C.DEPRECIATION_PRESENT_VALUE 		AS DEPRECIATION_BILL_PRESENT_VALUE,                /** 청구 현재가치(PV) **/
                    C.DEPRECIATION_CURRENT_DIFFERENCE 	AS DEPRECIATION_BILL_CURRENT_DIFFERENCE,           /** 청구 현 할차 **/
                    C.DEPRECIATION_INTEREST_INCOME 		AS DEPRECIATION_BILL_INTEREST_INCOME,              /** 청구 이자 수익 **/
                    A.RENTAL_EVENT_TYPE,
                    ROW_NUMBER() OVER (PARTITION BY A.ORDER_ITEM_ID ORDER BY A.CREATE_TIME DESC) AS RN
                    /** PARTITION BY로 ORDER_ITEM_ID별로 데이터를 나누고, CREATE_TIME 기준으로 순위를 매김 **/
                FROM RENTAL_FINANCIAL_DEPRECIATION_HISTORY A
                LEFT JOIN COLLECT_MATERIAL B
                ON A.MATERIAL_ID = B.MATERIAL_ID
                LEFT JOIN RENTAL_FINANCIAL_DEPRECIATION_SCHEDULE C
                ON A.ORDER_ITEM_ID = C.ORDER_ITEM_ID
                AND A.DEPRECIATION_BILL_YEAR_MONTH = C.DEPRECIATION_BILL_YEAR_MONTH
                WHERE TRUE
                and A.RENTAL_EVENT_TYPE not in ('BOOKING_SCHEDULED', 'BOOKING_CONFIRMED') /* 화면에 제외 대상 추가 */
                $baseDate
            ) A
            WHERE RN = 1
        ) A
        $where
        ORDER BY A.CONTRACT_DATE DESC
        """.trimIndent()
        return query
    }
}