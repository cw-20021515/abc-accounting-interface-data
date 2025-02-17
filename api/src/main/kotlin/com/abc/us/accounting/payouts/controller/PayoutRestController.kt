package  com.abc.us.accounting.payouts.controller

import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.request.ReqItemPayoutSave
import com.abc.us.accounting.payouts.model.request.ReqPayoutInqyDto
import com.abc.us.accounting.payouts.model.response.ResItemInqyPayout
import com.abc.us.accounting.payouts.model.response.ResPayout
import com.abc.us.accounting.payouts.model.response.ResPayoutInfoDto
import com.abc.us.accounting.payouts.model.response.ResPayoutItem
import com.abc.us.accounting.payouts.service.PayoutDetailService
import com.abc.us.accounting.payouts.service.PayoutService
import com.abc.us.accounting.supports.mapper.MapperUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
@Tag(name = "지급 현황 및 상세 계정과목 API", description = "지급 현황 및 상세 계정과목 REST API")
@RestController
@RequestMapping("/accounting/v1/payouts")
class PayoutRestController(
    var payoutService: PayoutService,
    var payoutDetailService: PayoutDetailService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    /**
     * 지급 현황 리스트 조회
     */
    @Operation(summary = "지급 현황 리스트 조회", description = "지급 현황 Item 조회")
    @GetMapping("")
    fun selectPayoutList(
        request: HttpServletRequest, response: HttpServletResponse?,
        @ModelAttribute reqPayoutInqyDto: ReqPayoutInqyDto,
    ): ResponseEntity<ApiPageResponse<List<ResPayoutInfoDto>>> {

        logger.info("reqPayoutInqyDto : ${MapperUtil.logMapCheck(reqPayoutInqyDto)}")
//        if (profilesActive?.contains("local") == false) {
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_1.mockUrl))
//        }

        val resList: Page<ResPayoutInfoDto>? = payoutService.selectPayoutList(reqPayoutInqyDto)
        return ResponseEntity.ok().body(ApiPageResponse(ResHeader(), resList))
    }

    /**
     * 지급 현황 결제 상세 조회
     */
    @Operation(summary = "지급 현황 결제 상세 조회", description = "지급 현황 결제 상세 조회")
    @GetMapping("/{payoutsId}")
    fun selectPayoutItems(
        @PathVariable(value = "payoutsId") payoutId: String,
//                           @ModelAttribute reqItemInqyPayout: ReqItemInqyPayout
    ): ResponseEntity<ApiResponse<ResItemInqyPayout>> {
//        if (profilesActive?.contains("local") == false) {
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_DETAIL_1.mockUrl))
//        }

        var payoutItems = payoutDetailService.selectPayoutDetail(payoutId)
        logger.info("payoutItems : $payoutItems")
        var resHeader = ResHeader()
//        if(payoutItems == null) {
//            resHeader.rsltMsge = "search data is empty."
//            resHeader.rsltCode = "${BizCode.SERVER_ERROR.name}"
//        }
        return ResponseEntity.ok().body(ApiResponse(resHeader, payoutItems))
//        throw Exception("search data is empty.")
    }

    /**
     * 지급현황 > 지급 신규 생성
     */
    @Operation(summary = "지급현황 > 지급 신규 생성", description = "지급현황 > 지급 신규 생성")
    @PostMapping("")
    fun addPayoutItems(
        @RequestBody reqItemPayout: ReqItemPayoutSave,
    ): ResponseEntity<ApiResponse<ResPayoutItem<MutableList<String>>?>> {
//        if (profilesActive?.contains("local") == false) {
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_3.mockUrl))
//        }
        reqItemPayout.payoutId = null
        var resItemPayout = payoutDetailService.savePayoutsDetail(reqItemPayout)
        return ResponseEntity.ok().body(ApiResponse(ResHeader(), resItemPayout))
    }

    /**
     * 지급현황 > 지급 수정
     */
    @Operation(summary = "지급현황 > 지급 수정", description = "지급현황 > 지급 수정")
    @PutMapping("{payoutsId}")
    fun updatePayoutItems(
        @PathVariable(value = "payoutsId") payoutsId: String,
        @RequestBody reqItemPayout: ReqItemPayoutSave,
    ): ResponseEntity<ApiResponse<ResPayoutItem<MutableList<String>>?>> {
        reqItemPayout.payoutId = payoutsId
//        if (profilesActive?.contains("local") == false) {
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_3.mockUrl))
//        }
//        reqItemPayout.payoutId = payoutsId
        var resItemPayout = payoutDetailService.savePayoutsDetail(reqItemPayout)
        return ResponseEntity.ok().body(ApiResponse(ResHeader(), resItemPayout))
    }

    /**
     * 지급 현황 미지급금 내역 임시저장 삭제
     */
    @Operation(summary = "지급 현황 미지급금 내역 임시저장 삭제", description = "지급 현황 미지급금 내역 임시저장 삭제")
    @DeleteMapping("{payoutsId}")
    fun deletePayoutItems(
        @PathVariable(value = "payoutsId") payoutsId: String,
    ): ResponseEntity<ApiResponse<ResPayout?>> {
//        if (profilesActive?.contains("local") == false) {
//            return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_5.mockUrl))
//        }

        var resPayout = payoutDetailService.deletePayoutsDetail(payoutsId)

        return ResponseEntity.ok().body(ApiResponse(ResHeader(), resPayout))
    }

}