package com.abc.us.accounting.payouts.model.response//package com.abc.us.accounting.ap.model
//
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Convert
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "응답_지급_조회")
class ResPayoutInfoDto(
    @Schema(description = "미지급금 ID", defaultValue = "")
    var payoutId: String? = null,
    @Schema(description = "거래처(공급 업체 코드)", defaultValue = "")
    var supplierId: String? = null,
    @Schema(description = "TX ID", defaultValue = "")
    var txId: String? = null,
    @Schema(description = "전표번호", defaultValue = "")
    var documentId: String? = null,
    @Schema(description = "지급유형", defaultValue = "")
    var payoutType: AccountingPayoutType? = null,
//    @Schema(description = "거래처(공급 업체 코드)", defaultValue = "")
//    var supplierId:String? = null,
    @Schema(description = "지급의 주체가 되는 회사 ID", defaultValue = "")
    var companyId: String? = null,
    @Schema(description = "귀속부서", defaultValue = "")
    var costCenter: String? = null,
    @Schema(description = "기안자코드", defaultValue = "")
    var drafterId: String? = null,
    @Schema(description = "통화", defaultValue = "")
    var currency: String? = null,
    @Schema(description = "지급금액", defaultValue = "")
    var payoutAmount: Double? = null,
    @Schema(description = "적요", defaultValue = "")
    var remark: String? = null,
    @Schema(description = "승인상태", defaultValue = "")
    var approvalStatus: AccountingPayoutApprovalStatus? = null,
    @Schema(description = "인보이스ID", defaultValue = "")
    var invoiceId: String? = null,
    @Schema(description = "PO", defaultValue = "")
    var purchaseOrderId: String? = null,
    @Schema(description = "BL", defaultValue = "")
    var billOfLadingId: String? = null,
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "등록일", defaultValue = "")
    var createTime: OffsetDateTime? = null,

    @Schema(description = "지급 만료 여부", defaultValue = "")
    @Convert(converter = YesNoConverter::class)
    var isExpired: Boolean? = null,
    @Schema(description = "지급 완료 여부", defaultValue = "")
    @Convert(converter = YesNoConverter::class)
    var isCompleted: Boolean? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "총 개수", hidden = true)
    var totalCnt: Long? = 0,
)

