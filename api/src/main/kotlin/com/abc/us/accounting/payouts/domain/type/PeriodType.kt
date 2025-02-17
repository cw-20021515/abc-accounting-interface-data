package com.abc.us.accounting.payouts.domain.type

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "기간_유형_타입_DATE")
enum class PeriodType(var value: String) {

    @Schema(description = "증빙일", defaultValue = "")
    DOCUMENT_DATE("DOCUMENT_TIME"),
    @Schema(description = "전기일", defaultValue = "")
    POSTING_DATE("POSTING_TIME"),
    @Schema(description = "발행일", defaultValue = "")
    ENTRY_DATE("ENTRY_TIME"),
    @Schema(description = "만기일", defaultValue = "")
    DUE_DATE("DUE_TIME");

}
