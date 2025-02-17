package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model

import com.abc.us.accounting.payouts.domain.entity.AccountsPayable
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.abc.us.accounting.payouts.domain.type.PayoutCaseType
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Schema(name = "요청_지급_계정항목_저장(임시포함)")
class ReqItemPayoutSave {

    @Schema(description = "미지급금 ID", defaultValue = "")
    var payoutId: String? = null

    @Schema(description = "트랜잭션 ID", defaultValue = "")
    var txId: String? = null

    @Schema(description = "지급유형", defaultValue = "VENDOR")
    var payoutType: AccountingPayoutType? = null

    @Schema(description = "승인상태", defaultValue = "DRAFTING")
    var approvalStatus: AccountingPayoutApprovalStatus? = null

    @Schema(description = "지급의 주체가 되는 회사 ID", defaultValue = "")
    var companyId: String? = null

    @Schema(description = "거래처(공급 업체 ID)", defaultValue = "")
    var supplierId: String? = null

    @Schema(description = "귀속부서", defaultValue = "")
    var costCenter: String? = null

    @Schema(description = "기안자", defaultValue = "")
    var drafterId: String? = null

    @Schema(description = "전표번호(accountingId)", defaultValue = "")
    var documentId: String? = null

    @Schema(description = "증빙일", defaultValue = "")
    var documentDate: LocalDate? = null

    @Schema(description = "발행일", defaultValue = "")
    var entryDate: LocalDate? = null

    @Schema(description = "전기일", defaultValue = "")
    var postingDate: LocalDate? = null

    @Schema(description = "만기일", defaultValue = "")
    var dueDate: LocalDate? = null

    @Schema(description = "통화", defaultValue = "")
    var currency: String? = null

    @Schema(description = "지급총액(세금포함)", defaultValue = "0.0")
    var payoutAmount: Double? = null

    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null

    @Schema(description = "송장 번호", defaultValue = "")
    var invoiceId: String? = null

    @Schema(description = "PO", defaultValue = "")
    var purchaseOrderId: String? = null

    @Schema(description = "BL", defaultValue = "")
    var billOfLadingId: String? = null

    @Schema(description = "업체지급 비용항목", defaultValue = "")
    var materialItems: List<ReqItemsVendor>? = null

    @Schema(description = "업체지급 비용항목", defaultValue = "")
    var generalItems: List<ReqItemsVendor>? = null

    @Schema(description = "개인지급 비용항목", defaultValue = "")
    var employeeItems: List<ReqItemsEmployee>? = null

    @JsonIgnore
    @Schema(description = "지급총액(세금포함)", defaultValue = "")
    var tempAmount: Double? = null

    fun toPayoutsPayable(reqAccountsPayable: AccountsPayable): AccountsPayable {
        var source = this
        employeeItems?.map { reqItem ->
            reqItem.payoutAmount?.let { tempAmount?.plus(it) }
        }
        materialItems?.map { reqItem ->
            reqItem.payoutAmount?.let { tempAmount?.plus(it) }
        }
        generalItems?.map { reqItem ->
            reqItem.payoutAmount?.let { tempAmount?.plus(it) }
        }
//        if(tempAmount != payoutAmount){ // TODO 총금액이 다르면 오류. 241008
//            throw BadRequestException("Items amount and payoutAmount do not match.")
//        }
        var accoutsPayable = reqAccountsPayable.apply {
            this.id = this.id ?: source.payoutId
            this.txId = this.txId ?: source.txId ?: source.drafterId
            this.transactionType = source.payoutType
            this.companyId = source.companyId
            this.remark = source.remark
            this.documentTime = OffsetDateTime.of(source.documentDate, LocalTime.MIN, ZoneOffset.UTC)
            this.entryTime = OffsetDateTime.of(source.entryDate, LocalTime.MIN, ZoneOffset.UTC)
            this.postingTime = OffsetDateTime.of(source.postingDate, LocalTime.MIN, ZoneOffset.UTC)
            this.dueTime = OffsetDateTime.of(source.dueDate, LocalTime.MIN, ZoneOffset.UTC)
            this.payoutAmount = source.payoutAmount
            this.supplierId = source.supplierId
            this.drafterId = source.drafterId
            this.costCenter = source.costCenter
            this.invoiceId = source.invoiceId
            this.purchaseOrderId = source.purchaseOrderId
            this.billOfLadingId = source.billOfLadingId
            this.paymentCurrency = source.currency?.uppercase() ?: "USD"
            this.accountingId = source.documentId
            this.paymentRetry = 0                           // 지급 시도 횟수
            this.localCurrency = Currency.getInstance(Locale.US).toString() // 기본값
            this.attachmentsTxId = this.id
            this.approvalTxId = this.id

        }
        return accoutsPayable
    }

    fun addPayoutsItemPayable(payoutsId: String): List<AccountsPayableItem>? {
//        var infoList: List<AccountsPayableItem>? = mutableListOf<AccountsPayableItem>()
        val infoList = mutableListOf<AccountsPayableItem>()
        if (this.payoutType == AccountingPayoutType.EMPLOYEE) {
            employeeItems?.forEach { reqItem ->
                tempAmount = reqItem.payoutAmount?.let { tempAmount?.plus(it) }
                infoList.add(reqItem.toItems(payoutsId,
                                             AccountsPayableItem()
                ))
            }
        } else if (this.payoutType == AccountingPayoutType.VENDOR) {
            materialItems?.forEach { reqItem ->
                tempAmount = reqItem.payoutAmount?.let { tempAmount?.plus(it) }
                var items = reqItem.toItems(payoutsId,
                                            AccountsPayableItem()
                )
                items.payoutCaseType = PayoutCaseType.MATERIAL.name
                infoList.add(items)
            }
            generalItems?.forEach { reqItem ->
                tempAmount = reqItem.payoutAmount?.let { tempAmount?.plus(it) }
                var items = reqItem.toItems(payoutsId,
                                            AccountsPayableItem()
                )
                items.payoutCaseType = PayoutCaseType.GENERAL.name
                items.amount = reqItem.payoutAmount
                infoList.add(items)
            }

        }
        return infoList
    }

}


