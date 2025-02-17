package com.abc.us.accounting.iface.domain.entity.oms


import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 청구서 관계정보 테이블 (M:N)
 */
@Entity
@Table(name = "if_charge_invoice")
@Comment("청구서 관계정보(M:N)")
class IfChargeInvoice(

    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("청구ID")
    @Column(name = "charge_id", nullable = false)
    val chargeId: String,

    @Comment("청구서ID")
    @Column(name = "invoice_id", nullable = false)
    val invoiceId: String,

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)