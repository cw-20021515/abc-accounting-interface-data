package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "응답 지급 팝업 자제 조회")
class ResAccountMaterialDto(
    @Schema(description = "자재 식별자", defaultValue = "")
    var materialId: String? = null,

    @Schema(description = "자재 유형", defaultValue = "")
    var materialType: String? = null,

    @Schema(description = "자재 명", defaultValue = "")
    var materialName: String? = null,

    @Schema(description = "실물 재화 시리얼 번호", defaultValue = "")
    var seriesCode: String? = null,

    @Schema(description = "카테고리  코드", defaultValue = "")
    var categoryCode: String? = null,

    @Schema(description = "소비자가(판매가)", defaultValue = "")
    var retailPrice: Double? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "생성 시간", defaultValue = "")
    var createTime: OffsetDateTime? = null,

    @Schema(description = "설명", defaultValue = "")
    var description: String? = null
)