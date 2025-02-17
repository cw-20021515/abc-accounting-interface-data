package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.iface.domain.type.oms.IfPromotionType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOrderItemPromotionPromotion {
    var promotionCycles: List<String>? = null
    var promotionType: IfPromotionType? = null
    var offer: IfOffer? = null
    var discount: IfDiscount? = null
}
