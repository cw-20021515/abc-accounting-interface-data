package com.abc.us.accounting.documents.domain.type

enum class StatementCategory (val symbol:String, val korName:String, val engName:String) {
    BALANCE_SHEET("B", "재무상태표", "BALANCE SHEET"),
    INCOME_STATEMENT("I", "손익계산서", "INCOME STATEMENT")
}