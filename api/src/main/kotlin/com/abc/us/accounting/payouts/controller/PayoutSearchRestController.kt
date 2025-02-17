package  com.abc.us.accounting.payouts.controller

import com.abc.us.accounting.payouts.service.PayoutSearchService
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.response.ResAccountCaseData
import com.abc.us.accounting.payouts.model.response.ResAccountCostCenterDto
import com.abc.us.accounting.payouts.model.response.ResAccountMaterialDto
import com.abc.us.accounting.payouts.model.response.ResCompanyInfoData
import com.abc.us.generated.models.PayoutAccountCaseType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
@Tag(name = "지급 현황 검색 API", description = "지급 현황 검색 REST API")
@RestController
@RequestMapping("/accounting/v1/payouts")
class PayoutSearchRestController(var payoutSearchService : PayoutSearchService) {
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    /**
     * 코스트센터 조회
     */
    @Operation(summary = "지급 현황 코스트센터 조회", description = "지급 현황 코스트센터 조회")
    @GetMapping("/cost-centers")
    fun selectPayoutSearchCostCenter(request: HttpServletRequest, response: HttpServletResponse?,
        @RequestParam(name = "costCenter", required = false, defaultValue = "") costCenter: String
    ): ResponseEntity<ApiResponse<List<ResAccountCostCenterDto>>> {
        var resData:List<ResAccountCostCenterDto>? = payoutSearchService.selectAccountByCostCenterList(costCenter);
        return ResponseEntity.ok(ApiResponse(ResHeader(), resData))
    }

    /**
     * 지급 현황 계정과목 조회
     */
    @Operation(summary = "지급 현황 계정과목 조회", description = "지급 현황 계정과목 조회")
    @GetMapping("/accounts")
    fun selectPayoutSearchAccount(request: HttpServletRequest, response: HttpServletResponse?,
        @RequestParam(name = "payoutAccountCaseType", required = true, defaultValue = "GENERAL") payoutAccountCaseType: PayoutAccountCaseType
    ): ResponseEntity<ApiResponse<List<ResAccountCaseData>>> {
        var resData:List<ResAccountCaseData>? = payoutSearchService.selectBySearchAccount(payoutAccountCaseType)
        return ResponseEntity.ok(ApiResponse(ResHeader(), resData))
    }
    /**
     * 지급 현황 자제 조회
     */
    @Operation(summary = "지급 현황 자제 조회", description = "지급 현황 자제 조회")
    @GetMapping("/materials")
    fun selectPayoutSearchMaterials(request: HttpServletRequest, response: HttpServletResponse?,
        @RequestParam(name = "material", required = true, defaultValue = "WP") material: String?
    ): ResponseEntity<ApiResponse<List<ResAccountMaterialDto>>> {
        var resData:List<ResAccountMaterialDto>? = payoutSearchService.selectBySearchMatterial(material)
        return ResponseEntity.ok(ApiResponse(ResHeader(), resData))
    }
    /**
     * 지급 현황 지급주체 회사 조회
     */
    @Operation(summary = "지급 현황 지급주체 회사 조회", description = "지급 현황 지급주체 회사 조회")
    @GetMapping("/companies")
    fun selectPayoutSearchCompany(request: HttpServletRequest, response: HttpServletResponse?,
    ): ResponseEntity<ApiResponse<List<ResCompanyInfoData>>> {
        var resData:List<ResCompanyInfoData>? = payoutSearchService.selectBySearchCompany()
        return ResponseEntity.ok(ApiResponse(ResHeader(), resData))
    }
}