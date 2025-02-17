package com.abc.us.accounting.iface.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOrderItemPromotion {
    var promotionId: String = ""
    var promotionName: String = ""
    var promotionDescription: String? = null
    var startDate: OffsetDateTime? = null
    var endDate: OffsetDateTime? = null
    var discountPrice: String? = null
    var promotion: IfOrderItemPromotionPromotion = IfOrderItemPromotionPromotion()
}
