package com.abc.us.accounting.supports.excel

data class ExcelRowDto<T>(
    var rowList: T,
    var cellList: List<ExcelCellDto>? = null,
)