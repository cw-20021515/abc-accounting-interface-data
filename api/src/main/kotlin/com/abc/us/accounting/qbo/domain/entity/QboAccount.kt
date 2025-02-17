package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.qbo.domain.entity.key.QboAccountKey
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboAccount (

    @Id
    @EmbeddedId
    val key: QboAccountKey,

    @Comment("외부 시스템에서 발행된 workspace 의 ID (회사 또는 환경별 추적 ID)")
    var realmId: String,

    @Comment("외부 시스템에서 발행된 workspace 의 ID (회사 또는 환경별 추적 ID)")
    var companyCode: String,

    @Comment("item 생성 unique name")
    val displayName : String,

    @Comment("QBO 에 등록될 경우 생성된 동기화 번호")
    var syncToken: String? = null,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    val submitResult: String,


    @Comment("생성 일시")
    var createTime: OffsetDateTime? = null,

    @Comment("생성 일시")
    var updateTime: OffsetDateTime? = null,

    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,
) {

}