package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.payouts.domain.entity.AccountsPayable
import com.abc.us.accounting.payouts.model.request.ReqPayoutInqyDto
import com.abc.us.accounting.payouts.model.response.ResItemInqyPayout
import com.abc.us.accounting.payouts.model.response.ResPayoutInfoDto
import com.abc.us.accounting.supports.QueryUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.abc.us.generated.models.PeriodType
import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.jpa.spi.NativeQueryMapTransformer
import org.hibernate.query.NativeQuery
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface IAccountPayableRepository : JpaRepository<AccountsPayable, String> {
//    fun findByIdAndIsDeleted(id:String, isDeleted:Boolean): Optional<AccountsPayable?>?
}

@Component
class AccountPayableRepository {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findByPayoutList(
        pageable: Pageable?,
        reqData: ReqPayoutInqyDto,
    ): Page<ResPayoutInfoDto> {
        //  정렬이 한개 이상인경우 조건 처리
        val pageableSort = pageable?.sort ?: Sort.by(Sort.Direction.DESC, "CREATE_TIME")
        val sortOrder = pageableSort
            .map { "${it.property} ${it.direction.name}" }
            .joinToString(", ")

        var reqQueryList = whereQueryList(reqData)
        var nativeQuery = queryByPayoutList(reqQueryList.get(), reqData, sortOrder)
        reqQueryList.setParameter(nativeQuery)

        if (pageable != null) { // 엑셀 다운로드 조건 처리.
            val pageNumber = pageable.pageNumber
            val pageSize = pageable.pageSize
            nativeQuery.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize)
        }

        var resultCamel: String = MapperUtil.convertJsonKeysToCamelCase(MapperUtil.gson.toJson(nativeQuery.resultList))
        val collectionType = object : TypeReference<List<ResPayoutInfoDto>>() {}
        var resultData: List<ResPayoutInfoDto> = MapperUtil.mapper.readValue(resultCamel, collectionType)
        log.info("resultList : ${MapperUtil.logMapCheck(resultData)}")
        var totalCnt: Long = resultData.getOrNull(0)?.totalCnt ?: 0L
        if (pageable != null) {
            resultData.map { t -> t.totalCnt = null }
            return PageImpl(resultData, pageable, totalCnt)
        }
        return PageImpl(resultData, Pageable.unpaged(), 0L)
    }

    fun queryByPayoutList(reqQueryList: String, reqData: ReqPayoutInqyDto, sortOrder: String): NativeQuery<*> {

        val q = """
        /* queryByPayoutList */
        SELECT A.*
        FROM
        (
            ${queryByPayoutDetail("")}
            $reqQueryList
        ) A
        ORDER BY $sortOrder
                """.trimIndent()
// ORDER BY ${reqData.sortBy.typeId} $orderDirection
        var query = entityManager.createNativeQuery(q)
        return query.unwrap(NativeQuery::class.java).setTupleTransformer(NativeQueryMapTransformer.INSTANCE)
    }

    fun queryByPayoutDetail(queryString: String): String {
        var q = """
        /* queryByPayoutDetail */
        SELECT COUNT(1) OVER () 		AS TOTAL_CNT
            , AP.ID						AS PAYOUT_ID
            , AP.TX_ID
            , AP.ACCOUNTING_ID			AS DOCUMENT_ID				/* 전표전호 */
            , AP.TRANSACTION_TYPE       AS PAYOUT_TYPE				/* 지급유형 */
            , AP.COST_CENTER 			                	        /* 코스트센터(부서) */
            , AP.COMPANY_ID			            				    /* 지급의 주체가 되는 회사ID */
            , AP.SUPPLIER_ID			            				/* 거래처(공급 업체 코드)개인-직원 */
            , AP.DRAFTER_ID  			AS DRAFTER_ID				/* 기안자코드 */
            , AP.PAYMENT_CURRENCY		AS CURRENCY				    /* 현지 통화 */
            , AP.PAYOUT_AMOUNT			        			        /* 지급금액 */
            , AP.REMARK
            , AP.PURCHASE_ORDER_ID
            , AP.INVOICE_ID
            , AP.BILL_OF_LADING_ID
            , TO_CHAR(CAST(AP.DOCUMENT_TIME AS DATE), 'yyyy-MM-dd') AS DOCUMENT_DATE
            , TO_CHAR(CAST(AP.ENTRY_TIME AS DATE)   , 'yyyy-MM-dd') AS ENTRY_DATE
            , TO_CHAR(CAST(AP.POSTING_TIME AS DATE) , 'yyyy-MM-dd') AS POSTING_DATE
            , TO_CHAR(CAST(AP.DUE_TIME AS DATE)     , 'yyyy-MM-dd') AS DUE_DATE
            , AP.CREATE_TIME                                        /* 등록일 */
            , AP.APPROVAL_STATUS
        FROM
        (
            SELECT AP.*
                , 'DRAFTING' AS APPROVAL_STATUS
            FROM ACCOUNTS_PAYABLE AP
        ) AP
        $queryString
        """.trimIndent()
        return q
    }

    fun findByPayoutDetail(
        payoutId: String,
    ): ResItemInqyPayout? {
        // where 조건문 추가
        var reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("payoutId", payoutId, "AND AP.ID = :payoutId"))
        // 쿼리 생성
        val q = queryByPayoutDetail(reqQueryList.get())
        // 네이티브 쿼리 생성

        val nativeQuery = QueryUtil.getJsonQuery(entityManager, q, ResItemInqyPayout::class.java)
        // 쿼리 파라미터 설정
        reqQueryList.setParameter(nativeQuery)
        var resultList = nativeQuery.resultList
        if(resultList.isNotEmpty()){
            // 결과를 JSON 문자열로 변환
            val resultCamel: String = MapperUtil.convertJsonKeysToCamelCase(MapperUtil.gson.toJson(resultList.first()))
//        // JSON 문자열을 객체로 역직렬화
            val collectionType = object : TypeReference<ResItemInqyPayout>() {}
            return MapperUtil.mapper.readValue(resultCamel, collectionType)
        }else{
            return null
        }
    }

    // 지급 현황 > 리스트 조회 검색 조건 처리.
    fun whereQueryList(reqData: ReqPayoutInqyDto): QueryUtil.Where {
        // where 조건문 추가
        var periodType = reqData.periodType?.value
        var reqQueryList = QueryUtil.makeWhere(
            reqData,
            setOf("page", "size", "sortBy", "direction", "periodType"),
        ) { key, value ->
            val valueData = when (value) {
                is AccountingPayoutType -> if (value == AccountingPayoutType.ALL) "" else value.name
                is PeriodType -> value.name /* 파라미터 key 정보와 DB Column 정보가 일치 하지 않아 개별 사용중. 지우지말것. 241010 */
                is AccountingPayoutApprovalStatus -> if (value == AccountingPayoutApprovalStatus.ALL) "" else value.value
                else -> value
            }
            // 쿼리 문자열 동적 생성
            val queryCondition = when (key) {
                "costCenter"        -> "AND UPPER(COST_CENTER)            = UPPER(:$key)"
                "drafterId"         -> "AND UPPER(DRAFTER_ID)             = UPPER(:$key)"
                "periodFromDate"    -> "AND DATE($periodType)     >= ${QueryUtil.queryTimestamp(key, valueData)}"
                "periodToDate"      -> "AND DATE($periodType)     <= ${QueryUtil.queryTimestamp(key, valueData)}"
                "payoutType"        -> "AND UPPER(TRANSACTION_TYPE)       = UPPER(:$key)"
                "supplierId"        -> "AND UPPER(SUPPLIER_ID)            = UPPER(:$key)"
//                "approvalStatus"    -> "AND UPPER(APPROVAL_STATUS)        = UPPER(:$key)"
                "invoiceId"         -> "AND UPPER(INVOICE_ID)             = UPPER(:$key)"
                "purchaseOrderId"   -> "AND UPPER(PURCHASE_ORDER_ID)      = UPPER(:$key)"
                "billOfLadingId"    -> "AND UPPER(BILL_OF_LADING_ID)      = UPPER(:$key)"
                "companyId"         -> "AND UPPER(COMPANY_ID)             = UPPER(:$key)"

                else -> "" // 기본값
            }

//            println("whereQueryList >>> $key: $valueData")
            ReqQueryNative(
                key = key,
                value = valueData?.toString()?.uppercase(),
                column = queryCondition
            )
        }
        return reqQueryList
    }

}
