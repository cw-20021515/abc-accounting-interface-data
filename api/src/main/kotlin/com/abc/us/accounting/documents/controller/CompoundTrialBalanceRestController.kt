package com.abc.us.accounting.documents.controller


import com.abc.us.accounting.documents.model.deprecated.ReqCompoundTrialBalanceDto
import com.abc.us.accounting.documents.model.deprecated.ResCompoundTrialBalanceDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
//@Tag(name = "합계잔액시산표 리스트조회 API", description = "합계잔액시산표 리스트조회 REST API")
@RestController
@RequestMapping("/accounting/v1/reports/trial-balance1")
class CompoundTrialBalanceRestController(
    //val compoundTrialBalanceService: CompoundTrialBalanceService
) {

    /**
     * 회계/합계잔액시산표 리스트조회
     * */
    //@Operation(summary = "합계잔액시산표 상세데이터조회(일괄조회)", description = "합계잔액시산표 상세데이터조회(일괄조회)")
    @GetMapping("")
    fun trialBalanceInfoList(
        request: HttpServletRequest, response: HttpServletResponse?,
        @ModelAttribute reqCompoundTrialBalanceDto : ReqCompoundTrialBalanceDto
    ): ResponseEntity<com.abc.us.accounting.model.ApiPageResponse<List<ResCompoundTrialBalanceDto>> > {

        //val data = compoundTrialBalanceService.selectCompoundTrialBalanceListForPaging(reqCompoundTrialBalanceDto)
        return ResponseEntity.ok(
            com.abc.us.accounting.model.ApiPageResponse(
                com.abc.us.accounting.model.ResHeader(),
                Page.empty<ResCompoundTrialBalanceDto>()
            )
        )
    }


    /**
     * /합계잔액시산표/엑셀다운로드
     */

    //@Operation(summary = "합계잔액시산표 엑셀다운로드", description = "합계잔액시산표 엑셀다운로드")
    @GetMapping("/download")
    fun selectTrialBalanceExcelDownload(
        @ModelAttribute reqCompoundTrialBalanceDto: ReqCompoundTrialBalanceDto,
        response: HttpServletResponse
    ): ResponseEntity<com.abc.us.accounting.model.ApiResponse<Any>> {
//        compoundTrialBalanceService.selectJournalExcelDownload(
//            reqCompoundTrialBalanceDto,
//            response
//        )
        return ResponseEntity.ok(com.abc.us.accounting.model.ApiResponse(com.abc.us.accounting.model.ResHeader(), null))
    }
}
