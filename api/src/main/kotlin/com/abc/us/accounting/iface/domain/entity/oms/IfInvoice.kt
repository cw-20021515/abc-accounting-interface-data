package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.model.IfOmsBillingInvoiceCharge
import com.abc.us.accounting.iface.domain.model.IfOmsBillingInvoiceChargeConverter
import com.abc.us.accounting.iface.domain.type.oms.IfInvoiceStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 청구서 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_invoice")
@Comment("청구서 정보(HISTORY)")
class IfInvoice(

    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("인보이스ID")
    @Column(name = "invoice_id")
    val invoiceId: String? = null,

    @Comment("계약ID")
    @Column(name = "contract_id", nullable = false)
    val contractId: String,

    @Comment("인보이스상태")
    @Column(name = "invoice_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val invoiceStatus: IfInvoiceStatus,

    @Comment("청구월")
    @Column(name = "billing_month", nullable = false)
    val billingMonth: String,

    @Comment("납부일자")
    @Column(name = "payment_due_date", nullable = false)
    val paymentDueDate: LocalDate,

    @Comment("전체금액")
    @Column(name = "total_price", nullable = false, precision = 38, scale = 4)
    val totalPrice: BigDecimal,

    @Comment("청구정보")
    @Convert(converter = IfOmsBillingInvoiceChargeConverter::class)
    @Column(name = "charges", nullable = false)
    val charges: List<IfOmsBillingInvoiceCharge>,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
