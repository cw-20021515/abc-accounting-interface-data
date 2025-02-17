package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

// TODO 적용대상 식별 정보
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class AccountsPayable {
    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("고유 식별자")
    var id: String? = null

    @Comment("트랜잭션 ID")
    var txId: String? = null

    @Comment("회계 ID(전표번호)")
    var accountingId: String? = null

    @Comment("지급 항목에 대한 주석")
    var remark: String? = null

    @Comment("지급 항목의 제목")
    var title: String? = null

    @Comment("지급 항목에 대한 설명")
    var description: String? = null

    @Comment("지불 ID")
    var paymentId: String? = null

    @Comment("지불 유형")
    var paymentType: String? = null

    @Comment("지급 유형을 나타냄")
    @Enumerated(EnumType.STRING)
    var transactionType: AccountingPayoutType? = null

    @Comment("지불 날짜 및 시간")
    var paymentDateTime: OffsetDateTime? = null

    @Comment("지불 소계 금액")
    var paymentSubTotalAmount: Double? = null

    @Comment("지불 총액")
    var paymentTotalAmount: Double? = null

    @Comment("지불 상태")
    var paymentStatus: String? = null

    @Comment("지불 차단 사유")
    var paymentBlockingReason: String? = null

    @Comment("지급 시도 횟수")
    var paymentRetry: Int? = null

    @Comment("지불 통화")
    var paymentCurrency: String? = null

    @Comment("지불 잔액")
    var paymentBalance: Int? = null

    @Comment("지급 금액")
    var payoutAmount: Double? = null

    @Comment("세금 금액")
    var taxAmount: Double? = null

    @Comment("로컬 통화")
    var localCurrency: String? = null

    @Comment("로컬 금액")
    var localAmount: Double? = null

    @Comment("생성 시간")
    @CreationTimestamp
    var createTime: OffsetDateTime? = null

    @Comment("증빙일")
    var documentTime: OffsetDateTime? = null

    @Comment("발행일")
    var entryTime: OffsetDateTime? = null

    @Comment("전기일")
    var postingTime: OffsetDateTime? = null

    @Comment("지급 처리일시")
    var processTime: OffsetDateTime? = null

    @Comment("만기일")
    var dueTime: OffsetDateTime? = null

    @Convert(converter = YesNoConverter::class)
    @Comment("만료 여부")
    var isExpired: Boolean? = null

    @Convert(converter = YesNoConverter::class)
    @Comment("완료 여부")
    var isCompleted: Boolean? = null

    @Comment("공급 업체 ID")
    var supplierId: String? = null

    @Comment("고객 ID")
    var customerId: String? = null

    @Comment("기안자 ID")
    var drafterId: String? = null

    @Comment("코스트 센터")
    var costCenter: String? = null

    @Comment("송장 ID")
    var invoiceId: String? = null

    @Comment("구매 주문 ID")
    var purchaseOrderId: String? = null

    @Comment("선하증권 ID")
    var billOfLadingId: String? = null

    @Comment("승인 트랜잭션 ID")
    var approvalTxId: String? = null

    @Comment("증빙 트랜잭션 ID")
    var attachmentsTxId: String? = null

    @Comment("지급의 주체가 되는 회사 ID")
    var companyId: String? = null
}
