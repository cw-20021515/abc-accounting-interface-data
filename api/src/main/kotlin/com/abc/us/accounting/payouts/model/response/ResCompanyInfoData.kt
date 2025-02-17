package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model
//
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답 지급 지급의주체가되는 회사정보")
class ResCompanyInfoData(
    var companyId: String? = null,
    var companyName: String? = null,
    var country: String? = null
)