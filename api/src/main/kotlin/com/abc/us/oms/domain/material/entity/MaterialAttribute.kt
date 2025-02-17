@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.material.entity

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
//@Table(name = "material_attribute", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MaterialAttribute(
    @Column(name = "material_id")
    val materialId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "material_id",
        referencedColumnName = "id",
        updatable = false,
        insertable = false,
    )
    var material: Material? = null,
    @Column(name = "type")
    var type: String,
    @Column(name = "code")
    var code: String,
    @Column(name = "code_prefix")
    var codePrefix: String? = null,
    @Column(name = "color_hex_code")
    var colorHexCode: String? = null,
    @Column(name = "name")
    var name: String? = null,
) : AuditTimeEntity()
