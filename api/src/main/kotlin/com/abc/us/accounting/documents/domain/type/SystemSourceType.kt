package com.abc.us.accounting.documents.domain.type

enum class SystemSourceType(val symbol: String, val description:String) {
    QBO("QBO", "QuickBooks Online"),
    ABC("ABC", "ABC System"),
}
