package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model
//
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.abc.us.accounting.payouts.domain.entity.PayoutAttachment
import com.abc.us.accounting.payouts.domain.type.PayoutCaseType
import com.abc.us.accounting.payouts.model.request.ReqItemsEmployee
import com.abc.us.accounting.payouts.model.request.ReqItemsVendor
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.query.sqm.tree.SqmNode.log
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "응답_지급_상세_결과")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResItemInqyPayout(

    @Schema(description = "payout ID", defaultValue = "")
    var payoutId: String? = null,
    @Schema(description = "tx ID", defaultValue = "")
    var txId: String? = null,
    @Schema(description = "지급 유형", defaultValue = "")
    var payoutType: AccountingPayoutType? = null,
    @Schema(description = "거래처(공급 업체 코드)", defaultValue = "")
    var supplierId: String? = null,
    @Schema(description = "전표번호(accountingId)", defaultValue = "")
    var documentId: String? = null,

    @Schema(description = "지급의 주체가 되는 회사 ID", defaultValue = "")
    var companyId: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "코스트센터", defaultValue = "")
    var costCenter: String? = null,
    @Schema(description = "기안자 code", defaultValue = "")
    var drafterId: String? = null,

    @Schema(description = "승인상태", defaultValue = "DRAFTING")
    var approvalStatus: String? = AccountingPayoutApprovalStatus.DRAFTING.name,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "증빙일", defaultValue = "")
    var documentDate: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "발행일", defaultValue = "")
    var entryDate: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "전기일", defaultValue = "")
    var postingDate: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "만기일", defaultValue = "")
    var dueDate: LocalDate? = null,

    @Schema(description = "통화", defaultValue = "")
    var currency: String? = null,
    @Schema(description = "지급금액", defaultValue = "")
    var payoutAmount: Double? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null,
    @Schema(description = "인보이스 id", defaultValue = "")
    var invoiceId: String? = null,
    @Schema(description = "PO", defaultValue = "")
    var purchaseOrderId: String? = null,
    @Schema(description = "BL", defaultValue = "")
    var billOfLadingId: String? = null,

    @Schema(description = "개인지급 비용항목", defaultValue = "")
    var employeeItems: MutableList<ReqItemsEmployee>? = null,
    @Schema(description = "자제지급 비용항목", defaultValue = "")
    var materialItems: MutableList<ReqItemsVendor>? = null,
    @Schema(description = "자제지급 비용항목", defaultValue = "")
    var generalItems: MutableList<ReqItemsVendor>? = null,
    @Schema(description = "업로드 파일 리스트", defaultValue = "")
    var attachmentItems: MutableList<ResAttachmentInfo>? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "등록일", defaultValue = "")
    var createTime: OffsetDateTime? = null,
    @field:Schema(description = "items 개수", hidden = true)
    var totalCnt: Int? = null,

    ) {
    fun itemsConvert(payoutType: String?, list: List<AccountsPayableItem>) {
        // 초기화
        employeeItems = mutableListOf()
        materialItems = mutableListOf()
        generalItems = mutableListOf()
        if ((list != null) && list.isNotEmpty()) {
            this.totalCnt = list.size
            // 타입에 따라 리스트에 아이템 추가
            for (item in list) {
                when (payoutType) {
                    AccountingPayoutType.EMPLOYEE.name -> {
                        // ReqItemsEmployee 변환 로직 추가
                        val reqItem: ReqItemsEmployee? = this.payoutId?.let { ReqItemsEmployee().fromItems(it, item) }
                        if (reqItem != null) {
                            employeeItems?.add(reqItem)
                        }
                    }

                    AccountingPayoutType.VENDOR.name -> {
                        // materialItems 직접 추가
                        val reqItem = this.payoutId?.let { ReqItemsVendor().fromItems(it, item) }
                        if (reqItem != null) {
                            if(item.payoutCaseType == PayoutCaseType.MATERIAL.name){
                                materialItems?.add(reqItem)
                            }else if(item.payoutCaseType == PayoutCaseType.GENERAL.name){
                                generalItems?.add(reqItem)
                            }
                        }
                    }

                    AccountingPayoutType.ALL.name -> {
                        log.info("itemsConvert payoutType is ALL")
                    }

                    null -> {
                        log.info("itemsConvert payoutType is Null")
                    }
                }
            }
        }
    }

    fun itemsConvertAttachments(list: MutableList<PayoutAttachment?>?) {
        attachmentItems = mutableListOf()
        if (!list.isNullOrEmpty()) {
            list.forEach { attachment ->
                attachment?.let {
                    val resAttachmentInfo = ResAttachmentInfo().apply {
                        attachmentId       = it.id                 // 고유 식별자
                        originFileName      = it.originFileName     // 원본 파일 이름
                        modifiedFileName    = it.modifiedFileName   // 수정된 파일 이름
                        resourcePath        = it.resourcePath       // 파일 경로
                        resourceSize        = it.resourceSize       // 파일 크기 (바이트)
                        mimeType            = it.mimeType           // MIME 타입
                        createDatetime      = it.createTime     // 생성 일시
                        expireDatetime      = it.expireTime     // 만료 일시
                        remark              = it.remark             // 비고
                    }
                    attachmentItems?.add(resAttachmentInfo)
                }
            }
        }
    }
}