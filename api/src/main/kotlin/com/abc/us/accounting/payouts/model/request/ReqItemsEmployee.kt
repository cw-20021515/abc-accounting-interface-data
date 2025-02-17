package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model
//
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ReqItemsEmployee(
    @Schema(description = "벤더 식별ID", defaultValue = "")
    var payoutItemId: String? = null,
    @Schema(description = "계정코드", defaultValue = "")
    var accountCode: String? = null,
    @Schema(description = "계정명", defaultValue = "")
    var accountName: String? = null,
    @Schema(description = "내용", defaultValue = "")
    var narrative: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "예산 사용일자", defaultValue = "")
    var budgetUsageDate: LocalDate? = null,

    @Schema(description = "예산 부서 금액", defaultValue = "")
    var budgetAmount: Double? = null,
    @Schema(description = "코스트 센터", defaultValue = "")
    var costCenter: String? = null,
    @Schema(description = "예산 사용처", defaultValue = "")
    var budgetAllocation: String? = null,

    @Schema(description = "지급액", defaultValue = "")
    var payoutAmount: Double? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null,

    @JsonIgnore
    @field:Schema(description = "총 개수", hidden = true)
    var totalCnt: Long? = 0,
) {
    fun toItems(payoutId: String, accountsPayableItem: AccountsPayableItem): AccountsPayableItem {
        var reqItem = this
        return accountsPayableItem.apply {
            id                  = reqItem.payoutItemId?.trim()
            txId                = payoutId
            accountCode         = reqItem.accountCode?.trim()
            accountName         = reqItem.accountName?.trim()
            name                = reqItem.narrative?.trim()
            amount              = reqItem.payoutAmount
            remark              = reqItem.remark?.trim()
            budgetUsageTime     = OffsetDateTime.of(reqItem.budgetUsageDate, LocalTime.MIN, ZoneOffset.UTC)
            budgetAllocation    = reqItem.budgetAllocation
            costCenter          = reqItem.costCenter
            budgetAmount        = reqItem.budgetAmount
//            budgetUsageTime = reqItem.budgetUsageDate?.atStartOfDay()?.atOffset(ZoneOffset.UTC)
        }
    }

    fun fromItems(payoutId: String, reqItem: AccountsPayableItem): ReqItemsEmployee {
        return ReqItemsEmployee().apply {
            payoutItemId        = reqItem.id?.trim()
            accountCode         = reqItem.accountCode?.trim()
            accountName         = reqItem.accountName?.trim()
            narrative           = reqItem.name?.trim()
            payoutAmount        = reqItem.amount
            remark              = reqItem.remark
            budgetUsageDate     = reqItem.budgetUsageTime?.toLocalDate()
            budgetAllocation    = reqItem.budgetAllocation
            costCenter          = reqItem.costCenter
            budgetAmount        = reqItem.budgetAmount
        }
    }
}