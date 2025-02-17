package com.abc.us.accounting.supports.excel

data class ExcelCellDto(
    var cellValue: Any? = null,
    var columnIndex: Int = 0,
    var rowIndex: Int? = null,
    var rowName: String? = null,
    var isFailed: Boolean? = true,
    var fieldName: String? = null,
    var message: String? = "OK",
)