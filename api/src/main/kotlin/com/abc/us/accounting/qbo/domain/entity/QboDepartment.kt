package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.qbo.domain.entity.key.QboCustomerKey
import com.abc.us.accounting.qbo.domain.entity.key.QboDepartmentKey
import com.abc.us.accounting.qbo.domain.type.DepartmentType
import com.abc.us.accounting.qbo.domain.type.VendorType
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboDepartment (

    @Id
    @EmbeddedId
    var key: QboDepartmentKey,

    @Type(JsonType::class)
    @Column(name = "name")
    val name: String,

    @Type(JsonType::class)
    @Column(name = "branch_id")
    val branchId: String,

    @Comment("warehouse_id ID")
    @Column(name = "warehouse_id")
    val warehouseId: String,

    @Comment("Department type")
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val type: DepartmentType = DepartmentType.BRANCH,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    val submitResult: String,

    @Comment("생성 시간")
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("갱신 시간")
    var updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
)