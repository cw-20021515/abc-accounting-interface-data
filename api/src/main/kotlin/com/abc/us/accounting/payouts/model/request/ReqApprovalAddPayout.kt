package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model

import com.abc.us.accounting.payouts.domain.entity.ApprovalRequest
import com.abc.us.accounting.payouts.model.response.ResApprovalRequestPayout
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "요청_지급_승인_등록")
class ReqApprovalAddPayout(
    @Schema(description = "지급 ID", defaultValue = "")
    var payoutId: String? = null,
    @Schema(description = "승인자 ID", defaultValue = "")
    var approverId: String? = null,
    @Schema(description = "기안자", defaultValue = "")
    var drafterName: String? = null,
    @Schema(description = "기안자 이메일", defaultValue = "")
    var email: String? = null,
    @Schema(description = "기안자 부서", defaultValue = "")
    var department: String? = null,
    @Schema(description = "기안 제목", defaultValue = "")
    var title: String? = null,
    @Schema(description = "기안의 상세 설명", defaultValue = "")
    var description: String? = null,
    @Schema(description = "기안자 전화", defaultValue = "")
    var phone: String? = null,
    @Schema(description = "결재 취소 사유", defaultValue = "")
    var cancellationReason: String? = null,
    @Schema(description = "승인이 필요한 문서의 유형", defaultValue = "")
    var approvalTargetType: String? = null,
    @Schema(description = "결재 진행 상태", defaultValue = "DRAFTING")
    var approvalStatus: AccountingPayoutApprovalStatus? = null,
) {

    // db save convert
    /*
    request의 컬럼값이 null이면 this
    request와 this 와 다르면 this가 먼저 들어가고, 같으면 request 데이터가 들어간다.
    */
    fun approvalRequest(payoutId: String, request: ApprovalRequest): ApprovalRequest {
        request.approvalStatus      = request.approvalStatus        ?: this.approvalStatus?.value
        request.approverId          = request.approverId            ?: this.approverId
        request.drafterName         = request.drafterName           ?: this.drafterName
        request.email               = request.email                 ?: this.email
        request.title               = request.title                 ?: this.title
        request.description         = request.description           ?: this.description
        request.phone               = request.phone                 ?: this.phone
        request.cancellationReason  = request.cancellationReason    ?: this.cancellationReason
        request.txId                = request.txId                  ?: payoutId
        request.approvalTargetType  = request.approvalTargetType    ?: this.approvalTargetType
        // 상태가 CANCELLED일 경우 추가 정보를 설정
        if (request.approvalStatus == AccountingPayoutApprovalStatus.CANCELLED.value) {
            request.cancellationTime = OffsetDateTime.now()
            request.cancellationReason = this.cancellationReason
        }
        return request
    }


    fun approvalListConvert(approval: ApprovalRequest): ResApprovalRequestPayout {
        val request = ResApprovalRequestPayout()
        request.approvalId          = approval.id
        request.approvalStatus      = approval.approvalStatus?.let{AccountingPayoutApprovalStatus.valueOf(it)}
        request.approverId          = approval.approverId
        request.drafterName         = approval.drafterName
        request.email               = approval.email
        request.costCenter        = approval.costCenter
        request.title               = approval.title
        request.description         = approval.description
        request.phone               = approval.phone
        request.cancellationReason  = approval.cancellationReason
        request.txId                = approval.txId
        request.approvalTargetType  = approval.approvalTargetType
        request.cancellationTime    = approval.cancellationTime
        request.cancellationReason  = approval.cancellationReason
        return request
    }
}