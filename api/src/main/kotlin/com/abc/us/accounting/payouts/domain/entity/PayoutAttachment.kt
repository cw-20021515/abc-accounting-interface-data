package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class PayoutAttachment {
    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("고유 식별자")
    var id: String? = null

    @Comment("트랜잭션 ID")
    var txId: String? = null

    @Comment("원본 파일 이름")
    var originFileName: String? = null

    @Comment("수정된 파일 이름")
    var modifiedFileName: String? = null

    @Comment("파일 리소스 경로")
    var resourcePath: String? = null

    @Comment("파일 리소스 크기 (바이트 단위)")
    var resourceSize: Long? = null

    @Comment("MIME 타입")
    var mimeType: String? = null

    @Comment("파일 생성 일시")
    var createTime: OffsetDateTime? = null

    @Comment("파일 만료 일시")
    var expireTime: OffsetDateTime? = null

    @Convert(converter = YesNoConverter::class)
    @Comment("삭제 여부")
    var isDeleted: Boolean? = null

    @Comment("비고")
    var remark: String? = null
}
