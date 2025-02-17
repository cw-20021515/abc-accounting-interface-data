package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "원장_전표상세_노트(Notes)")
class ResDocumentNotesDto(
     var notesContents        : String? =null      /*노트내용*/
   , @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
     var notesRegisterTime: OffsetDateTime? = null /*노트등록일시*/
   , var notesRegister     : String? =null         /*노트등록자*/
)