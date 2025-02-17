package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.DocumentServiceable
import com.abc.us.accounting.documents.service.SettlementService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.supports.mapper.MapperUtil
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounting/v1/settlements")
class SettlementController(
    //목업데이터관련 서버체크
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null,
    private val settlementService: SettlementService,
    private val documentServiceable: DocumentServiceable,
//    val codesService: CodesService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 반제 대상 리스트 조회
     * */
    //@Operation(summary = "반제 조회", description = "반제 조회")
    @GetMapping("")
    fun findSettlement(
        @ModelAttribute request : SearchSettlementFilters
    ) : ResponseEntity<ApiPageResponse<List<SettlementOutputResult>>>? {
        logger.info("findSettlement - request : ${MapperUtil.logMapCheck(request)}")
        val data = settlementService.searchSettlement(DocumentServiceContext.ONLY_DEBUG, request)

        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    /**
     * 전기 (반제 수동 등록)
     * */
    //@Operation(summary = "전기", description = "전기")
    @PostMapping("")
    fun createSettlement(
        @RequestBody request : ClearingDocumentInputRequest
    ) : ResponseEntity<ApiPageResponse<List<Any>>> {
        logger.info("createSettlement - request : ${MapperUtil.logMapCheck(request)}")
        val data = settlementService.createSettlementDocuments(DocumentServiceContext.ONLY_DEBUG, request)

        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

    /**
     *  미결항목 계정코드 리스트 조회
     * */
    //@Operation(summary = "미결항목 계정코드 리스트 조회", description = "미결항목 계정코드 리스트 조회")
    @GetMapping("/accountCode")
    fun findOpenItemMgmt(
        @ModelAttribute request : SearchAccountFilters
    ) : ResponseEntity<ApiPageResponse<List<AccountCodeOutputResult>>> {
        logger.info("findSettlement - request : ${MapperUtil.logMapCheck(request)}")
        val data = settlementService.searchOpenItemAccountCode(request)

        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }

}