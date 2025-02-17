package com.abc.us.accounting.payouts.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "payout_item")
class PayoutItem(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("미지급금 아이템 ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "payout_id", length = 255)
    @Comment("미지급금 ID")
    val payoutId: String? = null,

    @Column(name = "name", length = 255)
    @Comment("미지급금 항목의 이름")
    val name: String? = null,

    @Column(name = "description", length = 255)
    @Comment("미지급금 항목의 상세 설명")
    val description: String? = null,

    @Column(name = "quantity", precision = 19, scale = 4)
    @Comment("수량")
    val quantity: BigDecimal? = null,

    @Column(name = "type", length = 255)
    @Comment("거래처 지급 유형: MATERIAL, EMPLOYEE, GENERAL")
    val type: String? = null,

    @Column(name = "unit_measure", precision = 19, scale = 4)
    @Comment("품목의 단위: PIECE, BOX")
    val unitMeasure: BigDecimal? = null,

    @Column(name = "unit_price", precision = 19, scale = 4)
    @Comment("단가")
    val unitPrice: BigDecimal? = null,

    @Column(name = "amount", precision = 19, scale = 4)
    @Comment("공급가액(세금 미포함)")
    val amount: BigDecimal? = null,

    @Column(name = "tax_amount", precision = 19, scale = 4)
    @Comment("세액(순수 세금) 총합")
    val taxAmount: BigDecimal? = null,

    @Column(name = "total_amount", precision = 19, scale = 4)
    @Comment("공급가액(세금 포함)")
    val totalAmount: BigDecimal? = null,

    @Column(name = "line_number")
    @Comment("거래라인 아이템번호")
    val lineNumber: Int? = null,

    @Column(name = "cost_center_id", length = 255)
    @Comment("비용 센터의 ID")
    val costCenterId: String? = null,

    @Column(name = "invoice_item_id", length = 255)
    @Comment("invoice item ID")
    val invoiceItemId: String? = null,

    @Column(name = "purchase_order_item_id", length = 255)
    @Comment("구매 주문 item ID")
    val purchaseOrderItemId: String? = null,

    @Column(name = "material_id", length = 255)
    @Comment("비용항목으로 포함된 상품 또는 제품에 대한 상세 확인용 ID")
    val materialId: String? = null,

    @Column(name = "account_code", length = 255)
    @Comment("계정코드")
    val accountCode: String? = null,

    @Column(name = "budget_usage_time", columnDefinition = "TIMESTAMP")
    @Comment("예산 사용 일자")
    val budgetUsageTime: OffsetDateTime? = null,

    @Column(name = "budget_allocation", length = 255)
    @Comment("예산의 사용처")
    val budgetAllocation: String? = null,

    @Column(name = "remark", length = 255)
    @Comment("추가 코멘트 작성용")
    val remark: String? = null
)
