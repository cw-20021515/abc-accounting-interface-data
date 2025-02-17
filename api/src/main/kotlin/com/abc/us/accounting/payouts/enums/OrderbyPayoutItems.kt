package com.abc.us.accounting.payouts.enums

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(name = "지급_유형_ITEM_ORDER_BY_NAME")
enum class OrderbyPayoutItems(typeId:String, typeNm:String) {
    NAME("name"                     , "계정명"),
    BUDGET_USAGE_DATE("budgetUsageDate"                   , "사용일자"),
    APPROVAL_STATUS("APPROVAL_STATUS"               , "승인상태"),
    ENTRY_DATE("ENTRY_TIME"                         , "발행일"),
    POSTING_DATE("POSTING_TIME"                     , "전기일"),
    DUE_DATE("DUE_TIME"                             , "만기일"),
    CREATE_TIME("CREATE_TIME"                       , "등록일"),
    ;

    @Schema(description = "지급_유형_ORDER_BY_NAME 타입코드")
    val typeId: String? = null

    companion object {
        fun findByName(name: String): OrderbyPayoutItems? {
            return Arrays.stream(values())
                .filter { c -> c.name == name }
                .findFirst()
                .orElse(null)
        }
    }
}