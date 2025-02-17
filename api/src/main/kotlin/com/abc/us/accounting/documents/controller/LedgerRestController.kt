package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.LedgerService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream

//@Tag(name = "회계/원장 API", description = "회계/원장 API")
@RestController
@RequestMapping("/accounting/v1/ledgers")
class LedgerRestController(
    private val ledgerService: LedgerService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 회계/원장 리스트조회
     * */
    //목업데이터관련 서버체크
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    //@Operation(summary = "원장 조회", description = "원장 조회")
    @GetMapping("")
    fun accountLedgerInfoList(
        @ModelAttribute reqLedger : SearchLedgerFilters
    ) : ResponseEntity<ApiPageResponse<List<LedgerResult>>> {
        logger.info("accountLedgerInfoList : ${MapperUtil.logMapCheck(reqLedger)}")
        val current = if (reqLedger.current == 1) 0 else reqLedger.current

        // AttributeType 체크
        val attributeType: DocumentAttributeType?
        if(reqLedger.accountingAttributeType == null || reqLedger.accountingAttributeType.toString() == "ALL") {
            attributeType = null
        } else {
            attributeType = DocumentAttributeType.valueOf(reqLedger.accountingAttributeType.toString())
        }
        val data = ledgerService.searchLedgers(
                        DocumentServiceContext.ONLY_DEBUG,
                        SearchLedgerFilters(
                            pageable = SearchPageRequest(current, reqLedger.size, sortDirection = reqLedger.direction, sortBy = Sort.By.POSTING_DATE),
                            dateType = reqLedger.dateType!!,
                            fromDate = reqLedger.fromDate!!,
                            toDate = reqLedger.toDate!!,
                            companyCode = reqLedger.companyCode!!,
                            accountCodeFrom = reqLedger.accountCodeFrom.takeIf { !it.isNullOrEmpty() },
                            accountCodeTo = reqLedger.accountCodeTo.takeIf { !it.isNullOrEmpty() },
                            costCenter = reqLedger.costCenter.takeIf { !it.isNullOrEmpty() },
                            orderId = reqLedger.orderId.takeIf { !it.isNullOrEmpty() },
                            customerId = reqLedger.customerId.takeIf { !it.isNullOrEmpty() },
                            materialId = reqLedger.materialId.takeIf { !it.isNullOrEmpty() },
                            serialNumber = reqLedger.serialNumber.takeIf { !it.isNullOrEmpty() },
                            vendorId = reqLedger.vendorId.takeIf { !it.isNullOrEmpty() },
                            payoutId = reqLedger.payoutId.takeIf { !it.isNullOrEmpty() },
                            purchaseOrderId = reqLedger.purchaseOrderId.takeIf { !it.isNullOrEmpty() },
                            attributeType = attributeType,
                            attributeTypeValue = reqLedger.attributeTypeValue.takeIf { !it.isNullOrEmpty() },
                            accountingLedgerState = reqLedger.accountingLedgerState
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
     * 원장/엑셀다운로드
     */
    //@Operation(summary = "원장 엑셀 다운로드", description = "원장 엑셀 다운로드")
    @GetMapping("/rawdata/downloadExcel")
    fun selectAccountLedgerExcelDownload(
        @ModelAttribute reqLedger : SearchLedgerFilters,
        response: HttpServletResponse
    ) { //  : ResponseEntity<ApiResponse<Any>>
        logger.info("selectAccountLedgerExcelDownload : ${MapperUtil.logMapCheck(reqLedger)}")
        //val current = if (reqLedger.current == 1) 0 else reqLedger.current

        // AttributeType 체크
        val attributeType: DocumentAttributeType?
        if(reqLedger.accountingAttributeType == null || reqLedger.accountingAttributeType.toString() == "ALL") {
            attributeType = null
        } else {
            attributeType = DocumentAttributeType.valueOf(reqLedger.accountingAttributeType.toString())
        }
        val data = ledgerService.searchLedgersExcelDownload(
            DocumentServiceContext.ONLY_DEBUG,
            SearchLedgerFilters(
                pageable = SearchPageRequest(0, 100000, sortDirection = reqLedger.direction, sortBy = Sort.By.POSTING_DATE),
                dateType = reqLedger.dateType!!,
                fromDate = reqLedger.fromDate!!,
                toDate = reqLedger.toDate!!,
                companyCode = reqLedger.companyCode!!,
                accountCodeFrom = reqLedger.accountCodeFrom.takeIf { !it.isNullOrEmpty() },
                accountCodeTo = reqLedger.accountCodeTo.takeIf { !it.isNullOrEmpty() },
                costCenter = reqLedger.costCenter.takeIf { !it.isNullOrEmpty() },
                orderId = reqLedger.orderId.takeIf { !it.isNullOrEmpty() },
                customerId = reqLedger.customerId.takeIf { !it.isNullOrEmpty() },
                materialId = reqLedger.materialId.takeIf { !it.isNullOrEmpty() },
                serialNumber = reqLedger.serialNumber.takeIf { !it.isNullOrEmpty() },
                vendorId = reqLedger.vendorId.takeIf { !it.isNullOrEmpty() },
                payoutId = reqLedger.payoutId.takeIf { !it.isNullOrEmpty() },
                purchaseOrderId = reqLedger.purchaseOrderId.takeIf { !it.isNullOrEmpty() },
                attributeType = attributeType,
                attributeTypeValue = reqLedger.attributeTypeValue.takeIf { !it.isNullOrEmpty() },
                accountingLedgerState = reqLedger.accountingLedgerState
            ),
            response
        )

//        return ResponseEntity.ok(
//            ApiResponse(
//                ResHeader(),
//                null
//            )
//        )
    }

    /**
     * 원장/엑셀다운로드 (기존 엑셀다운로드 api로 적용)
     */
    //@Operation(summary = "원장 CSV 다운로드", description = "원장 CSV 다운로드")
    @GetMapping("/rawdata/download")
    fun selectAccountLedgerCsvDownload(
        @ModelAttribute reqLedger : SearchLedgerFilters,
        response: HttpServletResponse
    ) {
        logger.info("selectAccountLedgerCsvDownload : ${MapperUtil.logMapCheck(reqLedger)}")

        // AttributeType 체크
        val attributeType: DocumentAttributeType?
        if(reqLedger.accountingAttributeType == null || reqLedger.accountingAttributeType.toString() == "ALL") {
            attributeType = null
        } else {
            attributeType = DocumentAttributeType.valueOf(reqLedger.accountingAttributeType.toString())
        }

        val pageSize = 300
        ledgerService.searchLedgersExportToCsv(
            DocumentServiceContext.ONLY_DEBUG,
            SearchLedgerFilters(
                pageable = SearchPageRequest(0, pageSize, sortDirection = reqLedger.direction, sortBy = Sort.By.POSTING_DATE),
                dateType = reqLedger.dateType!!,
                fromDate = reqLedger.fromDate!!,
                toDate = reqLedger.toDate!!,
                companyCode = reqLedger.companyCode!!,
                accountCodeFrom = reqLedger.accountCodeFrom.takeIf { !it.isNullOrEmpty() },
                accountCodeTo = reqLedger.accountCodeTo.takeIf { !it.isNullOrEmpty() },
                costCenter = reqLedger.costCenter.takeIf { !it.isNullOrEmpty() },
                orderId = reqLedger.orderId.takeIf { !it.isNullOrEmpty() },
                customerId = reqLedger.customerId.takeIf { !it.isNullOrEmpty() },
                materialId = reqLedger.materialId.takeIf { !it.isNullOrEmpty() },
                serialNumber = reqLedger.serialNumber.takeIf { !it.isNullOrEmpty() },
                vendorId = reqLedger.vendorId.takeIf { !it.isNullOrEmpty() },
                payoutId = reqLedger.payoutId.takeIf { !it.isNullOrEmpty() },
                purchaseOrderId = reqLedger.purchaseOrderId.takeIf { !it.isNullOrEmpty() },
                attributeType = attributeType,
                attributeTypeValue = reqLedger.attributeTypeValue.takeIf { !it.isNullOrEmpty() },
                accountingLedgerState = reqLedger.accountingLedgerState
            ),
            response
        )

//        val file = File(filePath)
//        val resource = InputStreamResource(FileInputStream(file))

//        return ResponseEntity.ok()
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ledger.csv\"")
//            .contentType(MediaType.parseMediaType("text/csv"))
//            .body(resource)
    }


    //@Operation(summary = "원장 공통코드 조회", description = "원장 공통코드 조회")
    @GetMapping("/codes")
    fun getCodes(): ResponseEntity<com.abc.us.accounting.model.ApiResponse<Map<String, List<*>?>>> {
        //val data = codesService.getCodes()
        return ResponseEntity.ok(com.abc.us.accounting.model.ApiResponse(com.abc.us.accounting.model.ResHeader(), null))
    }

}