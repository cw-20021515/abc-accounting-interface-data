package com.abc.us.accounting.iface.domain.entity.logistics

import java.math.BigDecimal
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 발주 Invoice Item 정보 테이블
 */
@Entity
@Table(name = "if_purchase_order_invoice_item")
@Comment("발주 Invoice Item 정보")
class IfPurchaseOrderInvoiceItem(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("Invoice 번호(SAP CINO)")
    @Column(name = "invoice_no", nullable = false, length = 25)
    val invoiceNo: String,

    @Comment("Invoice Item ID(SAP POSNR)")
    @Column(name = "invoice_item_id", nullable = false, length = 6)
    val invoiceItemId: String,

    @Comment("자재 설명(SAP BEZEI)")
    @Column(name = "description", nullable = false, length = 40)
    val description: String,

    @Comment("제조사 생산코드(SAP MATNR)")
    @Column(name = "manufacturer_code", nullable = false, length = 18)
    val manufacturerCode: String,

    @Comment("자재 모델명(SAP MAKTX)")
    @Column(name = "model_name", nullable = false, length = 100)
    val modelName: String,

    @Comment("판매 단위(SAP VRKME)")
    @Column(name = "sales_unit", nullable = false, length = 3)
    val salesUnit: String,

    @Comment("발주 수량(SAP LFIMG)")
    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Comment("발주 단가(SAP NETPR)")
    @Column(name = "unit_price", nullable = false, precision = 38, scale = 4)
    val unitPrice: BigDecimal,

    @Comment("총 금액(SAP NETWR)")
    @Column(name = "total_amount", nullable = false, precision = 38, scale = 4)
    val totalAmount: BigDecimal,

    @Comment("통화 코드(SAP WAERK)")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("SAP 조회 일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
