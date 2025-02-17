package com.abc.us.accounting.payouts.model.request//package com.abc.us.accounting.ap.model
//
import com.abc.us.accounting.payouts.domain.type.PeriodType
import com.abc.us.accounting.payouts.enums.OrderbyPayouts
import com.abc.us.accounting.payouts.model.response.ResPayoutInfoDto
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.StringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.time.OffsetDateTime

@Schema(name = "요청_지급_검색_조회")
class ReqPayoutInqyDto {

    @Schema(description = "페이징", defaultValue = "1")
    var current: Int = 1

    @Schema(description = "페이지 크기", defaultValue = "50")
    var size: Int = 50

    @Schema(description = "정렬기준", defaultValue = "CREATE_TIME")
    var sortBy: OrderbyPayouts = OrderbyPayouts.CREATE_TIME

    @Schema(description = "정렬방법", defaultValue = "DESC")
    var direction: Sort.Direction = Sort.Direction.DESC

    @Schema(description = "지급 유형", defaultValue = "ALL")
    var payoutType: AccountingPayoutType? = null

    @Schema(description = "지급의 주체가 되는 회사 ID", defaultValue = "")
    var companyId: String? = null

    @Schema(description = "거래처", defaultValue = "${StringUtils.EMPTY}")
    var supplierId: String? = null

    @Schema(description = "귀속부서코드", defaultValue = "${StringUtils.EMPTY}")
    var costCenter: String? = null

    @Schema(description = "기안자 코드", defaultValue = "${StringUtils.EMPTY}")
    var drafterId: String? = null

    @Schema(description = "기간유형", defaultValue = "ENTRY_DATE")
    var periodType: PeriodType? = null

    @Schema(description = "시작일", defaultValue = "2024-08-31")
    var periodFromDate: LocalDate? = null

    @Schema(description = "종료일", defaultValue = "2024-08-31")
    var periodToDate: LocalDate? = null

    @Schema(description = "승인상태", defaultValue = "ALL")
    var approvalStatus: AccountingPayoutApprovalStatus? = null

    @Schema(description = "인보이스 ID", defaultValue = "")
    var invoiceId: String? = null

    @Schema(description = "구매 ID", defaultValue = "")
    var purchaseOrderId: String? = null

    @Schema(description = "Bill Of Lading - 운송관련 정보 ID", defaultValue = "")
    var billOfLadingId: String? = null

    @JsonIgnore
    @Schema(example = "tx-12345", description = "지급 수단 소유자")
    val txId: String? = null

    // 변환 함수
    fun transformRes(res: Page<ResPayoutInfoDto>): List<List<Any>> {
        return res.content.map {
            listOf(
                it.payoutId ?: "",
                it.txId ?: "",
                it.documentId ?: "",
                it.payoutType ?: "",
                it.companyId ?: "",
                it.supplierId ?: "",
                it.costCenter ?: "",
                it.drafterId ?: "",
                it.currency ?: "",
                it.payoutAmount ?: "",
                it.remark ?: "",
                it.approvalStatus?.name ?: "",
                it.invoiceId ?: "",
                it.purchaseOrderId ?: "",
                it.billOfLadingId ?: "",
                it.documentDate ?: OffsetDateTime.MIN,
                it.entryDate ?: OffsetDateTime.MIN,
                it.postingDate ?: OffsetDateTime.MIN,
                it.dueDate ?: LocalDate.MIN,
                it.createTime ?: OffsetDateTime.MIN,
                it.isExpired ?: false,
                it.isCompleted ?: false,
                it.billOfLadingId ?: ""
            )
        }
    }

}

