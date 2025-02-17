package  com.abc.us.accounting.payouts.controller

import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.response.ResAttachmentInfo
import com.abc.us.accounting.payouts.service.AttachmentsService
import com.abc.us.accounting.payouts.service.PayoutService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
@Tag(name = "지급 현황 증빙자료 파일 API", description = "지급 현황 증빙자료파일 REST API")
@RestController
@RequestMapping("/accounting/v1/payouts")
class PayoutFileRestController(
    var payoutService: PayoutService,
    var attachmentsService: AttachmentsService,
) {

    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    @Operation(summary = "지급 현황 증빙자료 파일 조회", description = "지급 현황 증빙자료 파일 조회")
    @GetMapping("/{payoutId}/attachments")
    @Throws(IOException::class)
    fun reloadDownload(
        @PathVariable(value = "payoutId") payoutId: String?,
        @RequestParam(value = "attachmentId", required = false) attachmentId: String?,
        response: HttpServletResponse,
    ) {
//        if (profilesActive?.contains("local") == false) {
//            MockUtil.excelDownload("/excel/payouts/payoutFormSample-v1.xlsx", response)
//        } else {
//            attachmentsService.selectAttachmentsById(attachmentsId, response)
//        }
        attachmentsService.selectAttachmentsById(attachmentId, response)
    }

    @Operation(summary = "지급 현황 증빙자료 삭제", description = "지급 현황 증빙자료 삭제")
    @DeleteMapping("/{payoutId}/attachments")
    @Throws(IOException::class)
    fun deletePayoutFile(
        @PathVariable(value = "payoutId") payoutsId: String,
        @RequestParam(value = "attachmentId", required = true) attachmentId: String,
    ): ResponseEntity<ApiResponse<Any>> {
//        DOCUMENT_DATE, POSTING_DATE, ENTRY_DATE, DUE_DATE
        // 증빙자료 삭제
        var response: ApiResponse<Any> = ApiResponse(ResHeader().failed())

//        if (profilesActive?.contains("local") == false) {
//            response = ApiResponse(ResHeader())
//            return ResponseEntity.ok().body(response)
//        }

        val isDelete = attachmentsService.deleteAttachments(attachmentId)
        if (isDelete) {
            response = ApiResponse(ResHeader(), attachmentId)
        }

        log.info(response.toString())
        return ResponseEntity.ok().body(response)
    }

    /**
     * 지급 현황 엑셀 증빙자료 업로드
     */
    @Operation(summary = "지급 현황 증빙자료 업로드", description = "지급 현황 증빙자료 업로드")
    @PostMapping("{payoutId}/attachments/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun selectPayoutFileUpload(
        @PathVariable(value = "payoutId") payoutId: String,
        @RequestPart(value = "fileUpload", required = true) fileUpload: Array<MultipartFile?>?,
    ): ResponseEntity<ApiResponse<Any>> {
        var resHeader: ResHeader = ResHeader()
//        var fileValidExt: List<String> = listOf("zip", "jpg", "png","xlsx", "xls")
//        if (!FileUtil.fileValidCheck(fileUpload, fileValidExt)) {
//            resHeader = resHeader.failed("404", "File Upload Error")
//        }

        // mutableListOf를 사용하여 초기화
        var resData: MutableList<ResAttachmentInfo> = mutableListOf()
        var response: ApiResponse<Any> = ApiResponse(resHeader, resData)

        if ("SUCCESS" == resHeader.rsltCode) {
//            if (profilesActive?.contains("local") == false) {
//                response = ApiResponse(ResHeader())
//                return ResponseEntity.ok(response)
//            }

            for (fileInfo in fileUpload!!) {
                var attachmentsInfo = attachmentsService.fileUpload(fileInfo, payoutId)
                if (attachmentsInfo != null) {
                    resData.add(attachmentsInfo)
                } else {
                    response = ApiResponse(ResHeader().failed())
                    break
                }
            }
        }

        log.debug(response.toString())
        return ResponseEntity.ok(response)
    }

//    @Operation(summary = "지급 현황 엑셀 증빙자료 파일 다운로드", description = "지급 현황 엑셀 증빙자료 파일 다운로드")
//    @GetMapping("/{payoutsId}/zip")
//    @Throws(IOException::class)
//    fun reloadZipDownload(
//        @PathVariable(value = "payoutsId") payoutsId: String?,
//        @PathVariable(value = "txId") txId: String?,
//        response: HttpServletResponse,
//    ) {
//        var fileList = attachmentsService.selectAttachmentsByTxId(txId, response)
//        FileUtil.createDownloadZipFiles(response, fileList, "payouts-${LocalTime.now()}")
//    }
}