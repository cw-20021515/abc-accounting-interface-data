package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

class ResAttachmentInfo(
    @Schema(description = "증빙자료 식별자 ID", defaultValue = "")
    var attachmentId: String? = null,
    @Schema(description = "ID", defaultValue = "")
    var originFileName: String? = null,
    @Schema(description = "ID", defaultValue = "")
    var modifiedFileName: String? = null,
    @Schema(description = "저장 경로", defaultValue = "")
    var resourcePath: String? = null,
    @Schema(description = "size", defaultValue = "")
    var resourceSize: Long? = null,
    @Schema(description = "mime 유형", defaultValue = "")
    var mimeType: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "생성일", defaultValue = "")
    var createDatetime: OffsetDateTime? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "만료일(?)", defaultValue = "")
    var expireDatetime: OffsetDateTime? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null,
)