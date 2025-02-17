package com.abc.us.oms.domain.promotiontarget.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Id

//@Entity
//@Table(name = "promotion_target_attribute", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionTargetAttribute(
    @Id
    val id: String,
    @Column(name = "name")
    var name: String,
    @Column(name = "type")
    var type: String,
) : AuditTimeOnlyEntity()
