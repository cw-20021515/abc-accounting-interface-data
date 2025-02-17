package com.abc.us.accounting.supports.excel

import org.apache.poi.ss.util.CellRangeAddress

data class ExcelHeader(
    var column: String? = null,
    var mergeRowIndex:Int? = 0,
    var mergeLength: Int? = null,
    var merge: CellRangeAddress?=null
)
