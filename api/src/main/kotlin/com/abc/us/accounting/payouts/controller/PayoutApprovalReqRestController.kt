package  com.abc.us.accounting.payouts.controller

import com.abc.us.accounting.payouts.service.ApprovalRequestService
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.request.ReqApprovalAddPayout
import com.abc.us.accounting.payouts.model.response.ResApprovalRequestPayout
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
@Tag(name = "지급 현황 승인 API", description = "지급 현황 승인 REST API")
@RestController
@RequestMapping("/accounting/v1/payouts")
class PayoutApprovalReqRestController(
    var approvalRequestService: ApprovalRequestService
) {

    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    /**
     * 지급 현황 결제 상세 등록
     */
    @Operation(summary = "지급 현황 결제 정보 등록", description = "지급 현황 결제 정보 등록")
    @PostMapping("/{payoutId}/approval")
    fun addPayoutsApproval(
        @PathVariable(value = "payoutId") payoutId: String,
        @RequestBody reqApproval: ReqApprovalAddPayout,
    ): ResponseEntity<ApiResponse<ResApprovalRequestPayout?>> {
//        if (profilesActive?.contains("local") == false) { // 목업
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_APPROVAL_1.mockUrl))
//        }

        var resApprovalRequestPayout = approvalRequestService.addApprovalReqByTxId(payoutId, reqApproval)
        var resHeader = ResHeader()
        if (resApprovalRequestPayout == null) {
            resHeader.failed()
        }
        return ResponseEntity.ok().body(ApiResponse(resHeader, resApprovalRequestPayout))
    }

    /**
     * 지급 현황 결제자 승인 요청 및 취소, 반려
     */
    @Operation(summary = "지급 현황 결제자 승인 요청 및 취소, 반려", description = "지급 현황 결제자 승인 요청 및 취소, 반려")
    @PutMapping("/{payoutId}/approval")
    fun updatePayoutsApproval(
        @PathVariable(value = "payoutId") payoutId: String,
        @RequestParam(value = "approvalId") approvalId: String,
        @RequestParam(value = "approvalStatus") approvalStatus: AccountingPayoutApprovalStatus,
    ): ResponseEntity<ApiResponse<ResApprovalRequestPayout?>> {
//        if (profilesActive?.contains("local") == false) { // 목업
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_APPROVAL_2.mockUrl))
//        }

        var resApprovalRequestPayout = approvalRequestService.updateApprovalReqByTxId(payoutId, approvalId, approvalStatus)
        var resHeader = ResHeader()
        if (resApprovalRequestPayout == null) {
            resHeader.failed()
        }
        return ResponseEntity.ok().body(ApiResponse(resHeader, resApprovalRequestPayout))
    }

    /**
     * 지급 현황 결제 승인 상세 조회
     */
    @Operation(summary = "지급 현황 결제 승인 상세 조회", description = "지급 현황 결제 승인 조회")
    @GetMapping("/{payoutId}/approval")
    fun getPayoutsApproval(
        @PathVariable(value = "payoutId") payoutId: String,
    ): ResponseEntity<ApiResponse<ResApprovalRequestPayout>> {
//        if (profilesActive?.contains("local") == false) { // 목업
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_APPROVAL_2.mockUrl))
//        }

        var approvalRequest = approvalRequestService.selectApprovalReqByTxId(payoutId)
        return ResponseEntity.ok().body(ApiResponse(ResHeader(), approvalRequest))
    }

}