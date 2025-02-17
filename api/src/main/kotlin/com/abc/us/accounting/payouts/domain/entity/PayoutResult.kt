package com.abc.us.accounting.payouts.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "payout_result")
class PayoutResult(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("객체 식별 ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "payout_id", length = 255)
    @Comment("미지급금 ID")
    val payoutId: String? = null,

    @Column(name = "type", length = 255)
    @Comment("지급 유형: MATERIAL, EMPLOYEE, GENERAL")
    val type: String? = null,

    @Column(name = "status", length = 255)
    @Comment("지급 상태")
    val status: String? = null,

    @Column(name = "block_reason", length = 255)
    @Comment("지불 차단 사유")
    val blockReason: String? = null,

    @Column(name = "amount", precision = 19, scale = 4)
    @Comment("지급총액(세금 미포함)")
    val amount: BigDecimal? = null,

    @Column(name = "tax_amount", precision = 19, scale = 4)
    @Comment("지급 세액(순수 세금) 총합")
    val taxAmount: BigDecimal? = null,

    @Column(name = "total_amount", precision = 19, scale = 4)
    @Comment("지급 총액(세금 포함)")
    val totalAmount: BigDecimal? = null,

    @Column(name = "balance", precision = 19, scale = 4)
    @Comment("지급 잔액")
    val balance: BigDecimal? = null,

    @Column(name = "transaction_id", length = 255)
    @Comment("실제 지급 ID")
    val transactionId: String? = null,

    @Column(name = "transaction_retry")
    @Comment("지급 시도 횟수")
    val transactionRetry: Int? = null,

    @Column(name = "currency", length = 255)
    @Comment("지급 통화")
    val currency: String? = null,

    @Column(name = "due_date", columnDefinition = "DATE")
    @Comment("지급 기일")
    val dueDate: LocalDate? = null,

    @Column(name = "complete_time", columnDefinition = "TIMESTAMP")
    @Comment("지급 완료일")
    val completeTime: OffsetDateTime? = null,

    @Column(name = "process_time", columnDefinition = "TIMESTAMP")
    @Comment("지급 처리 일시")
    val processTime: OffsetDateTime? = null,

    @Column(name = "method", length = 255)
    @Comment("지불 수단")
    val method: String? = null,

    @Column(name = "description", length = 255)
    @Comment("미지급금 항목의 상세 설명")
    val description: String? = null,

    @Column(name = "remark", length = 255)
    @Comment("추가 코멘트 작성용")
    val remark: String? = null,

    @Column(name = "is_expired", length = 1, columnDefinition = "varchar(1) default 'Y'")
    @Comment("만료 여부")
    val isExpired: String = "Y",

    @Column(name = "is_completed", length = 1, columnDefinition = "varchar(1) default 'Y'")
    @Comment("완료 여부")
    val isCompleted: String = "Y"
)
