package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApprovalRequest {
    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("승인 요청 식별 ID")
    var id: String? = null

    @Comment("승인자 ID")
    var approverId: String? = null

    @Comment("기안자")
    var drafterName: String? = null

    @Comment("기안자 이메일")
    var email: String? = null

    @Comment("코스트센터")
    var costCenter: String? = null

    @Comment("기안 제목")
    var title: String? = null

    @Comment("기안의 상세 설명")
    var description: String? = null

    @Comment("기안자 전화")
    var phone: String? = null

    @Comment("승인 요청 발생 일")
    var issueTime: OffsetDateTime? = null

    @Comment("결재 취소 시간")
    var cancellationTime: OffsetDateTime? = null

    @Comment("결재 취소 사유")
    var cancellationReason: String? = null

    @Comment("지급 수단 소유자")
    var txId: String? = null

    @Comment("승인이 필요한 문서의 유형 (예: invoice)")
    var approvalTargetType: String? = null

    @Comment("결재 진행 상태 조정 (예: PENDING - 결재 진행 잠시 멈춤, CANCELLATION - 결재 회수)")
    var approvalStatus: String? = null
}
