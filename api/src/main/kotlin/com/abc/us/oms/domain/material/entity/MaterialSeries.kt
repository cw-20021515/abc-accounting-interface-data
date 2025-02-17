@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.material.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

//@Entity
//@Table(name = "material_series", catalog = "abc_oms", schema = "public")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MaterialSeries(
    @Column(name = "material_id")
    val materialId: String,
    @Column(name = "material_model_name")
    var materialModelName: String? = null,
    @OneToMany(mappedBy = "series", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var materials: MutableList<Material> = mutableListOf(),
    @Column(name = "category_code")
    val categoryCode: String,
    @Column(name = "code")
    var code: String? = null,
    @Column(name = "code_prefix")
    var codePrefix: String? = null,
    @Column(name = "name")
    var name: String? = null,
) : AuditTimeEntity()
