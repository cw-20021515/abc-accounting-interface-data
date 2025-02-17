package com.abc.us.oms.domain.serviceflow.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDate

//@Entity
//@Table(name = "service_slot", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceSlot(
    @Column(name = "service_flow_id")
    var serviceFlowId: String? = null,
    @Column(name = "booking_date")
    var bookingDate: LocalDate? = null,
    @Column(name = "slot_type")
    var slotType: String?,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_flow_id", referencedColumnName = "id", updatable = false, insertable = false)
    var serviceFlow: ServiceFlow?,
    @Id
    @Column(name = "id")
    var id: String? = null,
)
