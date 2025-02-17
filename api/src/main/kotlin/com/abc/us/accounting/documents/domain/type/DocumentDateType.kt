package com.abc.us.accounting.documents.domain.type

enum class DocumentDateType(val code:String, val value:String, val engName:String, val korName:String) {
    POSTING_DATE("P", "posting_date", "Posting Date", "전기일"),
    DOCUMENT_DATE("D", "document_date", "Document Date", "증빙일"),
    ENTRY_DATE("E", "entry_date", "Entry Date", "입력일"),
}

