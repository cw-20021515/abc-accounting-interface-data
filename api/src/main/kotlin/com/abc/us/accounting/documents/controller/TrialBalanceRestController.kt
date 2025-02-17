package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.TrialBalanceService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

//AWS 인증
//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
//@Tag(name = "회계/합계잔액시산표 API", description = "회계/합계잔액시산표 API")
@RestController
@RequestMapping("/accounting/v1/reports")
class TrialBalanceRestController(
    //목업데이터관련 서버체크
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null,
    private val trailBalanceService: TrialBalanceService,
//    val codesService: CodesService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 회계/합계잔액시산표 리스트조회
     * */
    //@Operation(summary = "합계잔액시산표 조회", description = "합계잔액시산표 조회")
    @GetMapping("/trial-balance")
    fun accountReportInfoList(
        @ModelAttribute reqReport : SearchTrialBalanceFilters
    ) : ResponseEntity<ApiPageResponse<List<TrialBalanceResult>>> {
        logger.info("accountReportInfoList - reqReport : ${MapperUtil.logMapCheck(reqReport)}")
        // 조회 시작월의 1일자 세팅
        val fromDate = trailBalanceService.convertToFirstDayOfMonth(reqReport.fromMonth)
        // 조회 종료월의 마지막일자 세팅
        val toDate = trailBalanceService.convertToLastDayOfMonth(reqReport.toMonth)
        // 기초잔액 계산을 위한 일자 세팅
        val beginningToDate = trailBalanceService.calculateBeginingToDate(fromDate)
        val comparisonDate = LocalDate.of(2024, 7, 1)  // 2024-07-01
        val beginningFromDate = if (beginningToDate.isAfter(comparisonDate)) {
            comparisonDate
        } else {
            beginningToDate
        }

        val current = if (reqReport.current == 1) 0 else reqReport.current
//        logger.info {"/trial-balance - fromMonth = " + reqReport.fromMonth}
//        logger.info {"/trial-balance - toMonth   = " + reqReport.toMonth}
//        logger.info {"/trial-balance - fromDate = " + fromDate}
//        logger.info {"/trial-balance - toDate   = " + toDate}
//        logger.info {"/trial-balance - beginningFromDate = " + beginningFromDate}
//        logger.info {"/trial-balance - beginningToDate   = " + beginningToDate}
//        val levels = reqReport.displayLevel.toString().split(",").mapNotNull {
//                        AccountingDisplayLevel.valueOf(it.trim())
//                    }

        val data = trailBalanceService.searchTrialBalance(
                        DocumentServiceContext.ONLY_DEBUG,
                        SearchTrialBalanceFilters(
                            pageable = SearchPageRequest(current, reqReport.size, sortDirection = reqReport.sortDirection, sortBy = Sort.By.ACCOUNT_CODE),
                            fromMonth = reqReport.fromMonth,
                            toMonth = reqReport.toMonth,
                            fromDate = fromDate,
                            toDate = toDate,
                            beginningFromDate = beginningFromDate,
                            beginningToDate = beginningToDate,
                            companyCode = reqReport.companyCode!!,
                            accountGroupFrom = reqReport.accountGroupFrom,
                            accountGroupTo = reqReport.accountGroupTo,
                            accountCodeFrom = reqReport.accountCodeFrom,
                            accountCodeTo = reqReport.accountCodeTo
                        )
        )

        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    /**
     * 합계잔액시산표/엑셀다운로드
     */

    //@Operation(summary = "합계잔액시산표 엑셀 다운로드", description = "합계잔액시산표 엑셀 다운로드")
    @GetMapping("/trial-balance/download")
    fun selectAccountTrialBalanceExcelDownload(
        @ModelAttribute reqReport : SearchTrialBalanceFilters,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        logger.info("selectAccountTrialBalanceExcelDownload - reqReport : ${MapperUtil.logMapCheck(reqReport)}")
        // 조회 시작월의 1일자 세팅
        val fromDate = trailBalanceService.convertToFirstDayOfMonth(reqReport.fromMonth)
        // 조회 종료월의 마지막일자 세팅
        val toDate = trailBalanceService.convertToLastDayOfMonth(reqReport.toMonth)
        // 기초잔액 계산을 위한 일자 세팅
        val beginningToDate = trailBalanceService.calculateBeginingToDate(fromDate)
        val comparisonDate = LocalDate.of(2024, 7, 1)  // 2024-07-01
        val beginningFromDate = if (beginningToDate.isAfter(comparisonDate)) {
            comparisonDate
        } else {
            beginningToDate
        }

//        val current = if (reqReport.current == 1) 0 else reqReport.current
//        logger.info {"/trial-balance - fromMonth = " + reqReport.fromMonth}
//        logger.info {"/trial-balance - toMonth   = " + reqReport.toMonth}
//        logger.info {"/trial-balance - fromDate = " + fromDate}
//        logger.info {"/trial-balance - toDate   = " + toDate}
//        logger.info {"/trial-balance - beginningFromDate = " + beginningFromDate}
//        logger.info {"/trial-balance - beginningToDate   = " + beginningToDate}
//        val levels = reqReport.displayLevel.toString().split(",").mapNotNull {
//                        AccountingDisplayLevel.valueOf(it.trim())
//                    }

        trailBalanceService.searchTrialBalanceExcelDownload(
            DocumentServiceContext.ONLY_DEBUG,
            SearchTrialBalanceFilters(
                pageable = SearchPageRequest(0, 10000, sortDirection = reqReport.sortDirection, sortBy = Sort.By.ACCOUNT_CODE),
                fromMonth = reqReport.fromMonth,
                toMonth = reqReport.toMonth,
                fromDate = fromDate,
                toDate = toDate,
                beginningFromDate = beginningFromDate,
                beginningToDate = beginningToDate,
                companyCode = reqReport.companyCode!!,
                accountGroupFrom = reqReport.accountGroupFrom,
                accountGroupTo = reqReport.accountGroupTo,
                accountCodeFrom = reqReport.accountCodeFrom,
                accountCodeTo = reqReport.accountCodeTo
            ),
            response
        )

        return ResponseEntity.ok(
            ApiResponse(
                ResHeader(),
                null
            )
        )
    }

}