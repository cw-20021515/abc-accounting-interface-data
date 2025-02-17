package  com.abc.us.accounting.payouts.controller

import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.request.ReqPayoutExcelUploadDto
import com.abc.us.accounting.payouts.model.request.ReqPayoutInqyDto
import com.abc.us.accounting.payouts.service.PayoutService
import com.abc.us.accounting.supports.MockUtil
import com.abc.us.accounting.supports.excel.ExcelRowDto
import com.abc.us.accounting.supports.excel.ExcelUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
@Tag(name = "지급 현황 엑셀 API", description = "지급 현황 엑셀 REST API")
@RestController
@RequestMapping("/accounting/v1/payouts")
class PayoutExcelRestController(
    var payoutService: PayoutService,
) {
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    /**
     * 지급 현황 결과 다운로드
     */
    @Operation(summary = "지급 현황 결과 다운로드", description = "지급 현황 결과 다운로드")
    @GetMapping("/rawdata/download")
    fun selectPayoutExcelDownload(
        response: HttpServletResponse,
        @ModelAttribute reqPayoutInqyDto: ReqPayoutInqyDto,
    ) {
        payoutService.selectPayoutExcelDown(response, reqPayoutInqyDto)
    }

    /**
     * 지급 현황 결과 업로드
     */
    @Operation(summary = "지급 현황 결과 업로드", description = "지급 현황 결과 업로드")
    @PostMapping("/rawdata/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun selectPayoutExcelUpload(
        @RequestPart(value = "fileUpload", required = true) fileUpload: MultipartFile?,
    ): ResponseEntity<ApiResponse<ExcelRowDto<List<ReqPayoutExcelUploadDto>>>> {
        var resHeader = ResHeader()
        if (!ExcelUtil.isExcel(fileUpload)) {
            throw RuntimeException("File Upload FORMAT(only xlsx) Error")
        }

        if (fileUpload != null) {
//            if (profilesActive?.contains("local") == false) {
//                return ResponseEntity.ok(MockUtil.getDataFromJson(ClassOperInfo.PAYOUT_2.mockUrl))
//            }
            return ResponseEntity.ok(payoutService.selectPayoutExcelUpload(fileUpload))
        }

        return ResponseEntity.ok(ApiResponse(resHeader.failed()))
    }

    @Operation(summary = "지급 현황 엑셀 업로드 양식 파일 다운로드", description = "지급 현황 엑셀 업로드 양식 파일 다운로드")
    @GetMapping("/rawdata/download-form")
    @Throws(IOException::class)
    fun selectPayoutFileFormDownload(response: HttpServletResponse) {
        MockUtil.excelDownload(
            "/excel/payouts/payoutFormSample-v1.xlsx",
            response
        )
    }
}