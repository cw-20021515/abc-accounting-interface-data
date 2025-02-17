package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답 지급 계정과목")
class ResAccountCaseData(
    var accountCode: String? = null,
    var accountName: String? = null
)