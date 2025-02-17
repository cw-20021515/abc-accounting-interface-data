package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.iface.domain.type.oms.IfDiscountType
import com.abc.us.accounting.iface.domain.type.oms.IfPromotionDiscountTargetType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class IfDiscount {
    var discountTargetType: IfPromotionDiscountTargetType? = null
    var discountType: IfDiscountType? = null
    var amount: BigDecimal? = null
    var rate: BigDecimal? = null
}
