package com.abc.us.accounting.payouts.enums

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(name = "지급_유형_ORDER_BY_NAME")
enum class OrderbyPayouts(
    @Schema(description = "지급_유형_ORDER_BY_NAME 타입코드")
    var typeId:String,
    @Schema(description = "지급_유형_ORDER_BY_NAME 타입코드명")
    var typeNm:String) {
    ALL("CREATE_TIME"                               , "전체"),
    PAYOUT_ID("PAYOUT_ID"                           , "지급ID"),
    DRAFTER_ID("DRAFTER_ID"                         , "기안자코드"),
    SUPPLIER_ID("SUPPLIER_ID"                       , "거래처(업체, 직원)"),
    AFFILIATED_DEPARTMENT("AFFILIATED_DEPARTMENT"   , "귀속부서"),
    APPROVAL_STATUS("APPROVAL_STATUS"               , "승인상태"),
    ENTRY_DATE("ENTRY_TIME"                         , "발행일"),
    POSTING_DATE("POSTING_TIME"                     , "전기일"),
    DUE_DATE("DUE_TIME"                             , "만기일"),
    CREATE_TIME("CREATE_TIME"                       , "등록일"),

    ;

    companion object {
        fun findByName(name: String): OrderbyPayouts? {
            return Arrays.stream(values())
                .filter { c -> c.name == name }
                .findFirst()
                .orElse(null)
        }
    }
}