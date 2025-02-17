package com.abc.us.oms.domain.order.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

//@Entity
//@Table(name = "filter_pairing")
@JsonInclude(JsonInclude.Include.NON_NULL)
class FilterPairing(

    @Column(name = "installation_filter_id")
    val installationFilterId: String,

    @Column(name = "usage_rate")
    val usageRate: Double,

    @Column(name = "recent_replacement_time")
    val recentReplacementTime: LocalDateTime? = null,

    @Column(name = "sensor_create_time")
    val sensorCreateTime: LocalDateTime? = null,

    @Column(name = "sensor_update_time")
    val sensorUpdateTime: LocalDateTime? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "installation_filter_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var installationFilter: InstallationFilter?,

) : AuditTimeEntity()
