package com.abc.us.accounting.qbo.model

import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem

data class AssembledDocument (
    val header : Document,
    val lines : List<DocumentItem> = emptyList()
) {
}