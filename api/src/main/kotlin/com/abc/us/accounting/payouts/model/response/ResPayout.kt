package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답_지급_ITEM_결과")
class ResPayout(
    @Schema(description = "미지급금 ID", defaultValue = "")
    var payoutId: String? = null,
)