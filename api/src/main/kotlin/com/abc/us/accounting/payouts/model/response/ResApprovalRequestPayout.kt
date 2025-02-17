package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model
//
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.accounting.payouts.domain.entity.ApprovalRequest
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(name = "응답_지급_승인_요청_결과")
data class ResApprovalRequestPayout(
    @Schema(description = "승인 요청 식별 ID", defaultValue = "")
    var approvalId: String? = null,
    @Schema(description = "승인자 ID", defaultValue = "")
    var approverId: String? = null,
    @Schema(description = "기안자", defaultValue = "")
    var drafterName: String? = null,
    @Schema(description = "기안자 이메일", defaultValue = "")
    var email: String? = null,
    @Schema(description = "기안자 부서", defaultValue = "")
    var costCenter: String? = null,
    @Schema(description = "기안 제목", defaultValue = "")
    var title: String? = null,
    @Schema(description = "기안의 상세 설명", defaultValue = "")
    var description: String? = null,
    @Schema(description = "기안자 전화", defaultValue = "")
    var phone: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "승인 요청 발생 일", defaultValue = "")
    var issueTime: OffsetDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "결재 취소 시간", defaultValue = "")
    var cancellationTime: OffsetDateTime? = null,
    @Schema(description = "결재 취소 사유", defaultValue = "")
    var cancellationReason: String? = null,
    @Schema(description = "지급 수단 소유자", defaultValue = "")
    var txId: String? = null,
    @Schema(description = "승인이 필요한 문서의 유형", defaultValue = "")
    var approvalTargetType: String? = null,
    @Schema(description = "결재 진행 상태", defaultValue = "")
    var approvalStatus: AccountingPayoutApprovalStatus? = null,
) {

    companion object {
        fun approvalListConvert(list: List<ApprovalRequest>?): List<ResApprovalRequestPayout> {
            val listData: MutableList<ResApprovalRequestPayout> = mutableListOf()

            list?.forEach { request ->
                val res = ResApprovalRequestPayout(
                    approvalId = request.id,
                    approverId = request.approverId,
                    drafterName = request.drafterName,
                    email = request.email,
                    costCenter = request.costCenter,
                    title = request.title,
                    description = request.description,
                    phone = request.phone,
                    issueTime = request.issueTime,
                    cancellationTime = request.cancellationTime,
                    cancellationReason = request.cancellationReason,
                    txId = request.txId,
                    approvalTargetType = request.approvalTargetType,
                    approvalStatus = request.approvalStatus?.let { AccountingPayoutApprovalStatus.valueOf(it) } // Enum 변환
                )
                listData.add(res)
            }

            println("Final listData: ${MapperUtil.logMapCheck(listData)}")
            return listData
        }
    }
}