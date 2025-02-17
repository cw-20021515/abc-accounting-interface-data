package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.ApprovalRequest
import com.abc.us.accounting.payouts.model.response.ResApprovalRequestPayout
import com.abc.us.accounting.model.ReqQueryNative
import com.abc.us.accounting.supports.QueryUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.jpa.spi.NativeQueryMapTransformer
import org.hibernate.query.NativeQuery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ApprovalRequestRepository : JpaRepository<ApprovalRequest, String> {
    @Query(
        """
        /* findByTxId*/
        FROM ApprovalRequest a
        WHERE a.txId = :txId
          AND a.issueTime IN (
              SELECT MAX(b.issueTime)
              FROM ApprovalRequest b
              WHERE b.txId = :txId
              GROUP BY approverId
          )
        ORDER BY a.issueTime DESC
    """
    )
    fun findAllByTxId(
        @Param("txId") txId: String,
    ): MutableList<ApprovalRequest>

    @Query(
        """
        /* findByTxId*/
        FROM ApprovalRequest a
        WHERE a.txId = :txId
          AND a.approvalStatus NOT IN ('ALL', 'CANCELLED', 'REJECTED')
          AND a.issueTime IN (
              SELECT MAX(b.issueTime)
              FROM ApprovalRequest b
              WHERE b.txId = :txId
              GROUP BY approverId
          )
        ORDER BY a.issueTime DESC
    """
    )
    fun findByTxId(
        @Param("txId") txId: String,
    ): Optional<MutableList<ApprovalRequest>>

    @Query(
        "SELECT a FROM ApprovalRequest a " +
                "WHERE a.txId = :txId " +
                "AND a.id = :approvalId " +
                "AND a.approvalStatus NOT IN ('ALL', 'DRAFTING')"
    )
    fun findByTxIdAndApproverId(
        @Param("txId") txId: String,
        @Param("approvalId") approvalId: String,
    ): Optional<ApprovalRequest?>
}


@Component
class ApprovalRequestRepositoryImpl {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun findByPayoutApproval(
        payoutId: String,
    ): List<ResApprovalRequestPayout>? {
        // where 조건문 추가
        var reqQueryList = QueryUtil.where()
            .add(ReqQueryNative("txId", payoutId, "AND TX_ID = :txId"))
        // 쿼리 생성
        val q = queryByPayoutApproval(reqQueryList.get())
        println("query : $q")
        // 네이티브 쿼리 생성
        var query = entityManager.createNativeQuery(q)
        val nativeQuery = query.unwrap(NativeQuery::class.java).setTupleTransformer(NativeQueryMapTransformer.INSTANCE)
        reqQueryList.setParameter(nativeQuery)

        var resultCamel: String = MapperUtil.convertJsonKeysToCamelCase(MapperUtil.gson.toJson(nativeQuery.resultList))
        val collectionType = object : TypeReference<List<ResApprovalRequestPayout>>() {}
        var resultData: List<ResApprovalRequestPayout>? = MapperUtil.mapper.readValue(resultCamel, collectionType)
        return resultData
    }

    fun queryByPayoutApproval(queryString: String): String {
        var q = """
/* queryByPayoutApproval */
SELECT
    ID                      as APPROVAL_ID  /* 승인 요청 식별자 (기본 키) */
    , APPROVER_ID                           /* 승인자의 ID */
    , NAME                                  /* 기안자의 이름 */
    , EMAIL                                 /* 기안자의 이메일 */
    , DEPARTMENT                            /* 기안자의 부서 */
    , TITLE                                 /* 기안 제목 */
    , DESCRIPTION                           /* 기안의 상세 설명 */
    , PHONE                                 /* 기안자의 전화번호 */
    , ISSUE_TIME                            /* 승인 요청 발생 일시 */
    , CANCELLATION_TIME                     /* 결재 취소 일시 */
    , CANCELLATION_REASON                   /* 결재 취소 사유 */
    , TX_ID                                 /* 지급 수단 소유자 ID (ACCOUNTS_PAYABLE 테이블 참조) */
    , APPROVAL_TARGET_TYPE                  /* 승인이 필요한 문서의 유형 (예: 'INVOICE') */
    , APPROVAL_STATUS                       /* 결재 진행 상태 (예: 'PENDING', 'CANCELLATION') */
FROM
    APPROVAL_REQUEST
$queryString
        """.trimIndent()
        return q
    }
}