package com.abc.us.accounting.iface.domain.model

import com.abc.us.accounting.iface.domain.type.oms.IfOfferType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class IfOffer(
    var offerType: IfOfferType? = null,
    var materialId: String? = null
)
