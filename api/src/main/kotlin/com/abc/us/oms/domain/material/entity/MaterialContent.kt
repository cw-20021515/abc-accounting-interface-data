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
//@Table(name = "material_content", catalog = "abc_oms", schema = "public")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MaterialContent(
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
    @Column(name = "name")
    var name: String? = null,
    @Column(name = "description")
    var description: String? = null,
    @Column(name = "type")
    var type: String,
    @Column(name = "image_category")
    var imageCategory: String? = null,
    @Column(name = "is_representative_image")
    var isRepresentativeImage: Boolean? = false,
    @Column(name = "content_file_name")
    var contentFileName: String? = null,
    @Column(name = "resolved_input_url")
    var resolvedInputUrl: String? = null,
    @Column(name = "content_url")
    var contentUrl: String? = null,
) : AuditTimeEntity()
