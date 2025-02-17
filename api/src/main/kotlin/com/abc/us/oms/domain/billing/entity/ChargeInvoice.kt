package com.abc.us.oms.domain.billing.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

//@Entity
//@Table(name = "charge_invoice", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ChargeInvoice(

    @Column(name = "charge_id")
    val chargeId: String,

    @Column(name = "invoice_id")
    val invoiceId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", updatable = false, insertable = false)
    val charge: Charge? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", updatable = false, insertable = false)
    val invoice: Invoice? = null,
) : AuditTimeEntity()
