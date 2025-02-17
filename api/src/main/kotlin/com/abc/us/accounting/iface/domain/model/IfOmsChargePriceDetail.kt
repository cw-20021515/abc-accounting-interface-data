package com.abc.us.accounting.iface.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOmsChargePriceDetail {
    var discountPrice: BigDecimal = BigDecimal.ZERO
    var itemPrice: BigDecimal = BigDecimal.ZERO
    var currency: String = "USD"
    var prepaidAmount: BigDecimal = BigDecimal.ZERO
    var promotions: List<IfOrderItemPromotion>? = null
}
