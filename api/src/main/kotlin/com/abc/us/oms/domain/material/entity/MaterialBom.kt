@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.material.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDate

//@Entity
//@Table(name = "material_bom", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MaterialBom(
    @Column(name = "imatnr")
    var imatnr: String,
    @Column(name = "idnrk")
    var idnrk: String,
    @Column(name = "posnr")
    var posnr: String,
    @Column(name = "ojtxp")
    var ojtxp: String,
    @Column(name = "menge")
    var menge: Double,
    @Column(name = "meins")
    var meins: String,
    @Column(name = "datuv")
    var datuv: LocalDate,
    @Column(name = "datub")
    var datub: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: Status? = null,
) : AuditTimeEntity() {
    enum class Status {
        ACTIVE, // 현재 활성 상태
        UPDATED, // 값이 변경된 상태
        DELETED, // 삭제된 상태 (SAP, 논리적 삭제)
    }
}
