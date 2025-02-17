package com.abc.us.oms.domain.customerservice.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

//@Entity
//@Table(name = "customer_service_ticket_attach_file", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerServiceTicketAttachFile(
    @Column(name = "file_name")
    var fileName: String? = null,
    @Column(name = "resource_id")
    var resourceId: String,
    @Column(name = "customer_service_ticket_id")
    var customerServiceTicketId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_service_ticket_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customerServiceTicket: CustomerServiceTicket? = null,
) : AuditTimeEntity()
