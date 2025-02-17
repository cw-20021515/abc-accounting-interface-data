package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model

import com.abc.us.accounting.payouts.domain.entity.AccountsPayable
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.BeanUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ReqPayoutExcelUploadDto(
    @Schema(description = "지급유형", defaultValue = "", hidden = true)
    var payoutId: String? = null,
    @Schema(description = "승인상태", defaultValue = "DRAFTING")
    var approvalStatus: AccountingPayoutApprovalStatus? = AccountingPayoutApprovalStatus.DRAFTING,

    @Schema(description = "지급 그룹", defaultValue = "", hidden = true)
    var payoutGroup: String? = null,

    @Schema(description = "지급유형", defaultValue = "VENDOR")
    var payoutType: String? = null,
    @Schema(description = "거래처(공급 업체 ID)", defaultValue = "")
    var supplierId: String? = null,
    @Schema(description = "귀속부서", defaultValue = "")
    var costCenter: String? = null,
    @Schema(description = "기안자", defaultValue = "")
    var drafterId: String? = null,
    @Schema(description = "전표번호(accountingId)", defaultValue = "")
    var documentId: String? = null,
    @Schema(description = "증빙일", defaultValue = "")
    var documentDate: String? = null,
    @Schema(description = "발행일", defaultValue = "")
    var entryDate: String? = null,
    @Schema(description = "전기일", defaultValue = "")
    var postingDate: String? = null,
    @Schema(description = "만기일", defaultValue = "")
    var dueDate: String? = null,
    @Schema(description = "통화", defaultValue = "")
    var currency: String? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null,
    @Schema(description = "송장 번호", defaultValue = "")
    var invoiceId: String? = null,
    @Schema(description = "PO", defaultValue = "")
    var purchaseOrderId: String? = null,
    @Schema(description = "BL", defaultValue = "")
    var billOfLadingId: String? = null,
    @Schema(description = "지급총액(세금포함)", defaultValue = "")
    var payoutAmount: String? = null,

    // Start items
    @Schema(description = "금액", defaultValue = "")
    var itemsAmount: String? = null,

    @Schema(description = "단가", defaultValue = "")
    var itemsUnitPrice: String? = null,
    @Schema(description = "수량", defaultValue = "")
    var itemsQuantity: String? = null,

    @Schema(description = "계정코드", defaultValue = "")
    var itemsAccountCode: String? = null,

    @Schema(description = "계정명", defaultValue = "")
    var itemsAccountName: String? = null,

    @Schema(description = "품목", defaultValue = "")
    var itemsGoods: String? = null,

    @Schema(description = "내용", defaultValue = "")
    var itemsNarrative: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "예산 사용 날짜", defaultValue = "")
    var itemsBudgetUsageDate: String? = null,

    @Schema(description = "부서별 예산 금액", defaultValue = "")
    var itemsBudgetDepartmentAmount: String? = null,

    @Schema(description = "부서별 예산 이름", defaultValue = "")
    var itemsBudgetDepartmentName: String? = null,

    @Schema(description = "예산 배정", defaultValue = "")
    var itemsBudgetAllocation: String? = null,

    @Schema(description = "items 적요", defaultValue = "")
    var itemsRemark: String? = null,
    // End items
) {
    fun toPayoutsPayable(): AccountsPayable {
        var source = this
        var accoutsPayable = AccountsPayable().apply {
            this.txId = source.drafterId
            this.transactionType = AccountingPayoutType.values().find { it.name == source.payoutType }
            this.remark = source.remark
            this.documentTime = OffsetDateTime.of(LocalDate.parse(source.documentDate), LocalTime.MIN, ZoneOffset.UTC)
            this.entryTime = OffsetDateTime.of(LocalDate.parse(source.entryDate), LocalTime.MIN, ZoneOffset.UTC)
            this.postingTime = OffsetDateTime.of(LocalDate.parse(source.postingDate), LocalTime.MIN, ZoneOffset.UTC)
            this.dueTime = OffsetDateTime.of(LocalDate.parse(source.dueDate), LocalTime.MIN, ZoneOffset.UTC)
            this.payoutAmount = source.payoutAmount?.toDouble() ?: 0.0
            this.supplierId = source.supplierId
            this.drafterId = source.drafterId
            this.costCenter = source.costCenter
            this.invoiceId = source.invoiceId
            this.purchaseOrderId = source.purchaseOrderId
            this.billOfLadingId = source.billOfLadingId
            this.paymentCurrency = source.currency?.uppercase() ?: "USD"
            this.accountingId = source.documentId
            this.paymentRetry = 0                           // 지급 시도 횟수
        }
        return accoutsPayable
    }

    fun addPayoutsItemPayable(): AccountsPayableItem? {
        var reqItem = this
        if (this.payoutType == AccountingPayoutType.EMPLOYEE.name) {
            var itemsBudgetUsageDate: OffsetDateTime? = null
            if (!reqItem.itemsBudgetUsageDate.isNullOrEmpty()) {
                itemsBudgetUsageDate =
                    OffsetDateTime.of(LocalDate.parse(reqItem.itemsBudgetUsageDate), LocalTime.MIN, ZoneOffset.UTC)
            }
            return AccountsPayableItem().apply {
                txId = payoutId
                accountCode = reqItem.itemsAccountCode?.trim()
                accountName = reqItem.itemsAccountName?.trim()
                description = reqItem.itemsNarrative?.trim()
                amount = reqItem.itemsAmount?.toDouble() ?: 0.0
                remark = reqItem.itemsRemark?.trim()
                budgetUsageTime = itemsBudgetUsageDate
            }
        } else if (this.payoutType == AccountingPayoutType.VENDOR.name) {
            var itemsAmount = (reqItem.itemsUnitPrice?.toDouble() ?: 0.0) * (reqItem.itemsQuantity?.toInt() ?: 0)
            return AccountsPayableItem().apply {
                name = reqItem.itemsGoods?.trim()
                txId = payoutId
                accountCode = reqItem.itemsAccountCode?.trim()
                accountName = reqItem.itemsAccountName?.trim()
                description = reqItem.itemsNarrative?.trim()
                amount = reqItem.payoutAmount?.toDouble() ?: 0.0
                remark = reqItem.remark?.trim()
                unitPrice = reqItem.itemsUnitPrice?.toDouble() ?: 0.0
                quantity = reqItem.itemsQuantity?.toInt() ?: 0
                // 수량, 단가, 총액은 `AccountsPayableItem`의 필드에 맞게 설정 (예: amount는 수량 * 단가)
                amount = itemsAmount
            }
        }
        return null
    }

    @JsonIgnore
    fun getItemsAmountSum(): Double {
        return (this.itemsUnitPrice?.toDouble() ?: 0.0) * (this.itemsQuantity?.toInt() ?: 0)
    }

    /**
     * 중복 처리 방법
     */
    fun toExcelItems(): MutableMap<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        BeanUtils.copyProperties(this, map)
        map["txId"] = payoutId

        // 중복 값 제거 로직
        val uniqueValues = mutableSetOf<Any?>()
        val uniqueMap = mutableMapOf<String, Any?>()

        for ((key, value) in map) {
            if (value !in uniqueValues) {
                uniqueMap[key] = value
                uniqueValues.add(value)
            }
        }

        return uniqueMap
    }
}