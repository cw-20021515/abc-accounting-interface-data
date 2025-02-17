package com.abc.us.accounting.collects.domain.entity.audit

import com.abc.us.accounting.collects.domain.type.AuditActionTypeEnum
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class AuditTargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var entityName: String? = null

    @Comment("회사 코드")
    var companyId: String? = null

    @Enumerated(EnumType.STRING)
    var auditActionType: AuditActionTypeEnum? = null

    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
}