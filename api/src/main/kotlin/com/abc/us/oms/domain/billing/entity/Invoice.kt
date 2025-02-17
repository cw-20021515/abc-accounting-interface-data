package com.abc.us.oms.domain.billing.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.contract.entity.Contract
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDate

//@Entity
//@Table(name = "invoice", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Invoice(

    @Column(name = "contract_id")
    val contractId: String,

    @Column(name = "invoice_status")
    val invoiceStatus: String,

    @Column(name = "billing_month")
    val billingMonth: String,

    @Column(name = "payment_due_date")
    val paymentDueDate: LocalDate,

    @Column(name = "total_price")
    val totalPrice: Double,

    @Column(name = "charges")
    @Type(JsonType::class)
    val charges: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "id", updatable = false, insertable = false)
    var contract: Contract? = null,

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    val chargeInvoices: MutableList<ChargeInvoice> = mutableListOf(),

    @Id
    @Column(name = "id")
    val id: String,

) : AuditTimeOnlyEntity()
