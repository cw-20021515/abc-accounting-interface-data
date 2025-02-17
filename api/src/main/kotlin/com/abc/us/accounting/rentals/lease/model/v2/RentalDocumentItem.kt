package com.abc.us.accounting.rentals.lease.model.v2

import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import java.math.BigDecimal

data class RentalDocumentItem(
    val amount: BigDecimal,
    val docTemplateItems: List<DocumentTemplateItem>
)
