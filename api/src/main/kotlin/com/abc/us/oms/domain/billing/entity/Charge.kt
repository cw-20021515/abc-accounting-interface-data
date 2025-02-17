package com.abc.us.oms.domain.billing.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.contract.entity.Contract
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDate

//@Entity
//@Table(name = "charge", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Charge(
    @Column(name = "billing_cycle")
    val billingCycle: Int,

    @Column(name = "target_month")
    var targetMonth: String? = null,

    @Column(name = "charge_status")
    var chargeStatus: String,

    @Column(name = "contract_id")
    val contractId: String,

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @OneToMany(mappedBy = "charge", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val chargeItems: MutableList<ChargeItem> = mutableListOf(),

    @OneToOne(mappedBy = "charge", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var chargePayment: ChargePayment? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "id", updatable = false, insertable = false)
    val contract: Contract? = null,

    @OneToMany(mappedBy = "charge", fetch = FetchType.LAZY)
    val chargeInvoices: MutableList<ChargeInvoice>? = mutableListOf(),

    @Id
    @Column(name = "id")
    val id: String,
) : AuditTimeOnlyEntity()
