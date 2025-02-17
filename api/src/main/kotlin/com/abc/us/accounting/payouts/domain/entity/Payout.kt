package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.payouts.domain.type.ApprovalStatus
import com.abc.us.accounting.payouts.domain.type.PayoutType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "payout")
class Payout(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("미지급금 ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "title", length = 255)
    @Comment("미지급금 제목")
    val title: String? = null,

    @Column(name = "description", length = 255)
    @Comment("미지급금 상세 설명")
    val description: String? = null,

    @Column(name = "type", length = 255, columnDefinition = "varchar default 'VENDOR'")
    @Comment("미지급금 유형: VENDOR(업체 비용) | EMPLOYEE(개인 비용)")
    @Enumerated(EnumType.STRING)
    val type: PayoutType = PayoutType.VENDOR,

    @Column(name = "currency", length = 255)
    @Comment("통화")
    val currency: String? = null,

    @Column(name = "amount", precision = 19, scale = 4)
    @Comment("공급가액(세금 미포함)")
    val amount: BigDecimal? = null,

    @Column(name = "tax_amount", precision = 19, scale = 4)
    @Comment("세액(순수 세금) 총합")
    val taxAmount: BigDecimal? = null,

    @Column(name = "total_amount", precision = 19, scale = 4)
    @Comment("공급가액(세금 포함)")
    val totalAmount: BigDecimal? = null,

    @Column(name = "document_time", columnDefinition = "TIMESTAMP")
    @Comment("증빙일")
    val documentTime: OffsetDateTime? = null,

    @Column(name = "entry_time", columnDefinition = "TIMESTAMP")
    @Comment("발행일")
    val entryTime: OffsetDateTime? = null,

    @Column(name = "posting_time", columnDefinition = "TIMESTAMP")
    @Comment("전기일")
    val postingTime: OffsetDateTime? = null,

    @Column(name = "due_date", columnDefinition = "DATE")
    @Comment("지급 기일")
    val dueDate: LocalDate? = null,

    @Column(name = "vendor_id", length = 255)
    @Comment("공급 업체 코드")
    val vendorId: String? = null,

    @Column(name = "employee_id", length = 255)
    @Comment("지급 요청 문서 작성자 코드")
    val employeeId: String? = null,

    @Column(name = "department_id", length = 255)
    @Comment("귀속부서 코드")
    val departmentId: String? = null,

    @Column(name = "invoice_id", length = 255)
    @Comment("invoice ID")
    val invoiceId: String? = null,

    @Column(name = "purchase_order_id", length = 255)
    @Comment("구매 주문 ID")
    val purchaseOrderId: String? = null,

    @Column(name = "bill_of_lading_id", length = 255)
    @Comment("선하증권 ID")
    val billOfLadingId: String? = null,

    @Column(name = "approval_id", length = 255)
    @Comment("지급 승인 요청 ID")
    val approvalId: String? = null,

    @Column(name = "approval_status", length = 255)
    @Comment("지급 결재 상태: INIT, SUBMITTED, REJECTED, APPROVED")
    val approvalStatus: ApprovalStatus? = null,

    @Column(name = "create_time", columnDefinition = "TIMESTAMP")
    @Comment("생성 시간")
    val createTime: OffsetDateTime? = null,

    @Column(name = "remark", nullable = false, length = 255)
    @Comment("적요")
    val remark: String,

    @Column(name = "is_active", length = 1, columnDefinition = "varchar(1) default 'Y'")
    @Comment("활성화 여부")
    val isActive: String = "Y"
)
