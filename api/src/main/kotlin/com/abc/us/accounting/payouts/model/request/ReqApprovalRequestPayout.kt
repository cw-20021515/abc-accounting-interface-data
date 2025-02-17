package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model
//

import com.abc.us.accounting.payouts.domain.entity.ApprovalRequest
import com.abc.us.accounting.payouts.model.response.ResApprovalRequestPayout
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "요청_지급_승인_수정")
class ReqApprovalRequestPayout(
    @Schema(description = "승인 요청 식별 ID", defaultValue = "")
    var approvalId: String? = null,
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
    var status: AccountingPayoutApprovalStatus? = null,
) {

    // db save convert
    fun approvalRequest(payoutId: String, request: ApprovalRequest): ApprovalRequest {
        request.approvalStatus = this.status?.value
        request.id = this.approvalId
        request.approverId = this.approverId
        request.drafterName = this.drafterName
        request.email = this.email
        request.costCenter = this.department
        request.title = this.title
        request.description = this.description
        request.phone = this.phone
        request.cancellationReason = this.cancellationReason
        request.txId = payoutId
        request.approvalTargetType = this.approvalTargetType
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