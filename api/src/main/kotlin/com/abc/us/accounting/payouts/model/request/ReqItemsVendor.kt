package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model

import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

class ReqItemsVendor(

    @Schema(description = "미지급금 아이템 식별자", defaultValue = "")
    var payoutItemId:String? = null,
    @Schema(description = "계정코드", defaultValue = "")
    var accountCode:String? = null,
    @Schema(description = "계정명", defaultValue = "")
    var accountName:String? = null,
    @Schema(description = "품목ID", defaultValue = "")
    var materialId:String? = null,
    @Schema(description = "내용(지급 개별 항목 이름)", defaultValue = "")
    var narrative:String? = null,
    @Schema(description = "단가", defaultValue = "")
    var unitPrice:Double? = null,
    @Schema(description = "수량", defaultValue = "")
    var quantity:Int? = null,
    @Schema(description = "지급액", defaultValue = "")
    var payoutAmount:Double? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark:String? = null,
    @JsonIgnore
    @field:Schema(description = "총 개수", hidden = true)
    var totalCnt: Long? = 0,
){
    fun toItems(payoutId:String, accountsPayableItem : AccountsPayableItem) : AccountsPayableItem {
        var reqItem = this
        return accountsPayableItem.apply {
            id              = reqItem.payoutItemId?.trim()
            name            = reqItem.narrative?.trim()
            txId            = payoutId
            accountCode     = reqItem.accountCode?.trim()
            accountName     = reqItem.accountName?.trim()
            materialId      = reqItem.materialId
            remark          = reqItem.remark?.trim()
            unitPrice       = reqItem.unitPrice
            quantity        = reqItem.quantity
            // 수량, 단가, 총액은 `AccountsPayableItem`의 필드에 맞게 설정 (예: amount는 수량 * 단가)
            amount          = (reqItem.unitPrice ?: 0.0) * (reqItem.quantity ?: 0)
        }
    }

    fun fromItems(payoutId:String, reqItem : AccountsPayableItem) : ReqItemsVendor {
        return ReqItemsVendor().apply {
            payoutItemId    = reqItem.id
            narrative       = reqItem.name
            accountCode     = reqItem.accountCode
            accountName     = reqItem.accountName
            materialId      = reqItem.materialId
            payoutAmount    = reqItem.amount
            remark          = reqItem.remark
            unitPrice       = reqItem.unitPrice
            quantity        = reqItem.quantity
        }
    }
}