package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.payouts.domain.type.CostCenterCategory
import com.abc.us.accounting.qbo.domain.entity.key.QboClassKey
import com.abc.us.accounting.qbo.domain.type.ClassType
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboClass(
    @Id
    @EmbeddedId
    val key: QboClassKey,

    @Comment("외부 시스템에서 발행된 workspace 의 ID (회사 또는 환경별 추적 ID)")
    var companyCode: String,

    @Comment("class code")
    val code: String,

    @Comment("class type")
    @Enumerated(EnumType.STRING)
    val type: ClassType,

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