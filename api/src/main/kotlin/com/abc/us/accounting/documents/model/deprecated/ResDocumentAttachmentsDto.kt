package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "원장_전표상세_첨부파일(attachment)")
class ResDocumentAttachmentsDto(
     var attachmentFileName     : String? =null  /*파일명*/
   , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
     var attachmentRegisterTime: OffsetDateTime? = null /*등록일시*/
   , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
     var attachmentUpdateTime: OffsetDateTime? = null /*수정일시*/
   , var attachmentRegister     : String? =null         /*노트등록자*/
)