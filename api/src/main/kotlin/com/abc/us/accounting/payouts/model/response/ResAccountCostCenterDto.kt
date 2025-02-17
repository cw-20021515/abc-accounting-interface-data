package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "응답_코스트센터 검색조회")
class ResAccountCostCenterDto(

    @Schema(description = "유형", defaultValue = "Assets")
    var accountType: String? = null,

    @Schema(description = "고유 센터 식별자", defaultValue = "1000")
    var centerId: String? = null,

    @Schema(description = "부모 고유 센터 식별자", defaultValue = "null")
    var parentCenterId: String? = null, // 부모 코드가 없을 수 있으므로 nullable로 설정

    @Schema(description = "이름", defaultValue = "Corporate Headquarters")
    var centerName: String? = null,

    @Schema(description = "비용 유형", defaultValue = "COST")
    var centerType: String? = null,

    @Schema(description = "카테고리(center_sub_type)", defaultValue = "ADMINISTRATIVE")
    var category: String? = null,

    @Schema(description = "설명", defaultValue = "본사")
    var description: String? = null,

    @Schema(description = "회사식별자", defaultValue = "N100")
    var companyId: String? = null
)
