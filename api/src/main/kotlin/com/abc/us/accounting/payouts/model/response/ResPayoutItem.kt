package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답_지급_ITEM_결과")
class ResPayoutItem <T>(
    @Schema(description = "지급(미지급) ID", defaultValue = "true")
    var payoutId: String? = null,
    @Schema(description = "Item ID", defaultValue = "")
    var payoutItemId: T? = null,
)