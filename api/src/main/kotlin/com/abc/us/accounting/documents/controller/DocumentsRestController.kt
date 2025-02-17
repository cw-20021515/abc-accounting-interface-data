package com.abc.us.accounting.documents.controller

import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.documents.domain.type.DocumentStatus
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.DocumentSaveServiceable
import com.abc.us.accounting.documents.service.DocumentSearchServiceable
import com.abc.us.accounting.documents.service.SettlementService
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.supports.MockUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException

//AWS 인증
//@PreAuthorize("@abcSdkAuthorizer.isValidRequestApiKey()")
//@Tag(name = "회계/전표 API", description = "회계/전표 API")
@RestController
@RequestMapping("/accounting/v1/documents")
class DocumentsRestController(
    //목업데이터관련 서버체크
    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null,
    val documentSearchServiceable: DocumentSearchServiceable,
    val documentSaveServiceable: DocumentSaveServiceable,
    val settlementService: SettlementService
) {
    /**
     * 회계/원장/전표
     **/
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    //@Operation(summary = "전표 조회", description = "전표 조회")
    @GetMapping("")
    fun findDocumentsList(
        @ModelAttribute reqDocument : SearchDocumentFilters
    ) : ResponseEntity<ApiPageResponse<List<DocumentResult>>> {
        logger.info("searchDocument reqDocument : ${MapperUtil.logMapCheck(reqDocument)}")

        val docType: DocumentType? =
            if (reqDocument.documentType == null || reqDocument.documentType.toString() == "ALL") null
            else DocumentType.valueOf(reqDocument.documentType.toString())

        val docStatus: DocumentStatus? =
            if (reqDocument.documentStatus == null || reqDocument.documentStatus.toString() == "ALL") null
            else DocumentStatus.valueOf(reqDocument.documentStatus.toString())

//        logger.info("reqDocument docType : " + docType)
//        logger.info("reqDocument docStatus : " + docStatus)
        val current = if (reqDocument.current == 1) 0 else reqDocument.current
        val data = documentSearchServiceable.searchDocuments(
                        DocumentServiceContext.ONLY_DEBUG,
                        SearchDocumentFilters(
                            pageable = SearchPageRequest(current, reqDocument.size, sortDirection = reqDocument.direction, sortBy = Sort.By.POSTING_DATE),
                            dateType = reqDocument.dateType,
                            fromDate = reqDocument.fromDate,
                            toDate = reqDocument.toDate,
                            companyCode = reqDocument.companyCode!!,
                            fiscalYear = reqDocument.fiscalYear?.toInt(),
                            fiscalMonth = reqDocument.fiscalMonth?.toInt(),
                            docType = docType,
                            createdBy = reqDocument.createUser.takeIf { !it.isNullOrEmpty() },
                            docStatus = docStatus
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
     * 회계/원장/전표 엑셀 다운로드
     */
    //@Operation(summary = "전표 엑셀 다운로드", description = "전표 엑셀 다운로드")
    @GetMapping("/rawdata/downloadExcel")
    fun selectDocumentsExcelDownload(
        @ModelAttribute reqDocument : SearchDocumentFilters,
        response: HttpServletResponse
    ) {     // : ResponseEntity<ApiResponse<Any>>
        logger.info("searchDocument reqDocument : ${MapperUtil.logMapCheck(reqDocument)}")

        val docType: DocumentType? =
            if (reqDocument.documentType == null || reqDocument.documentType.toString() == "ALL") null
            else DocumentType.valueOf(reqDocument.documentType.toString())

        val docStatus: DocumentStatus? =
            if (reqDocument.documentStatus == null || reqDocument.documentStatus.toString() == "ALL") null
            else DocumentStatus.valueOf(reqDocument.documentStatus.toString())

//        logger.info("reqDocument docType : " + docType)
//        logger.info("reqDocument docStatus : " + docStatus)
        //val current = if (reqDocument.current == 1) 0 else reqDocument.current
        documentSearchServiceable.searchDocumentsExcelDownload(
            DocumentServiceContext.ONLY_DEBUG,
            SearchDocumentFilters(
                pageable = SearchPageRequest(0, 100000, sortDirection = reqDocument.direction),
                dateType = reqDocument.dateType,
                fromDate = reqDocument.fromDate,
                toDate = reqDocument.toDate,
                companyCode = reqDocument.companyCode!!,
                fiscalYear = reqDocument.fiscalYear?.toInt(),
                fiscalMonth = reqDocument.fiscalMonth?.toInt(),
                docType = docType,
                createdBy = reqDocument.createUser.takeIf { !it.isNullOrEmpty() },
                docStatus = docStatus
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
     * 회계/원장/전표 CSV 다운로드 (기존 엑셀다운로드 api로 적용)
     */
    //@Operation(summary = "전표 CSV 다운로드", description = "전표 CSV 다운로드")
    @GetMapping("/rawdata/download")
    fun selectDocumentsCsvDownload(
        @ModelAttribute reqDocument : SearchDocumentFilters,
        response: HttpServletResponse
    ) {
        logger.info("selectDocumentsCsvDownload reqDocument : ${MapperUtil.logMapCheck(reqDocument)}")
        val filePath = "documents.csv"

        val docType: DocumentType? =
            if (reqDocument.documentType == null || reqDocument.documentType.toString() == "ALL") null
            else DocumentType.valueOf(reqDocument.documentType.toString())
        val docStatus: DocumentStatus? =
            if (reqDocument.documentStatus == null || reqDocument.documentStatus.toString() == "ALL") null
            else DocumentStatus.valueOf(reqDocument.documentStatus.toString())

        val pageSize = 500
        documentSearchServiceable.searchDocumentsExportToCsv(
            DocumentServiceContext.ONLY_DEBUG,
            SearchDocumentFilters(
                pageable = SearchPageRequest(0, pageSize, sortDirection = reqDocument.direction),
                dateType = reqDocument.dateType,
                fromDate = reqDocument.fromDate,
                toDate = reqDocument.toDate,
                companyCode = reqDocument.companyCode!!,
                fiscalYear = reqDocument.fiscalYear?.toInt(),
                fiscalMonth = reqDocument.fiscalMonth?.toInt(),
                docType = docType,
                createdBy = reqDocument.createUser.takeIf { !it.isNullOrEmpty() },
                docStatus = docStatus
            ),
            response
        )

//        val file = File(filePath)
//        val resource = InputStreamResource(FileInputStream(file))

//        return ResponseEntity.ok()
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.csv\"")
//            .contentType(MediaType.parseMediaType("text/csv"))
//            .body(resource)
    }



    /**
     * 전표 상세조회
     */
    //@Operation(summary = "전표상세 조회", description = "전표상세 조회")
    @GetMapping("/{documentId}")
    fun findDocumentsDetail(
        @PathVariable("documentId") documentId: String
    ) : ResponseEntity<ApiResponse<DocumentDetailOutputResult>> {
        val data = documentSearchServiceable.searchByDocId(DocumentServiceContext.ONLY_DEBUG, documentId)
        logger.info("findDocumentDetail data : ${MapperUtil.logMapCheck(data)}")

        return ResponseEntity.ok().body(
            ApiResponse(
                ResHeader(),
                data
            )
        )
    }

    //@Operation(summary = "전표 엑셀 업로드 양식 다운로드", description = "전표 엑셀 업로드 양식 다운로드")
    @GetMapping("/rawdata/download-form")
    @Throws(IOException::class)
    fun selectDocumentFileFormDownload(response: HttpServletResponse){
        MockUtil.excelDownload(
            "/excel/accounts/ledgerDocumentTemplet.xlsx",
            response
        )
    }

    /**
     * 전표 등록
     */
    //@Operation(summary = "전표 등록", description = "전표 등록")
    @PostMapping("")
    fun createDocument(
        @RequestBody request : CreateDocumentInputRequest
    ) : ResponseEntity<ApiResponse<DocumentResult>> {
        logger.info("createDocument request : ${MapperUtil.logMapCheck(request)}")
        val result = documentSaveServiceable.createDocument(DocumentServiceContext.SAVE_DEBUG, request)

        return ResponseEntity.ok(
            ApiResponse(
                ResHeader(),
                result
            )
        )
    }

    /**
     * 전표 수정
     */
    //@Operation(summary = "전표 수정", description = "전표 수정")
    @PutMapping("/{documentId}")
    fun updateDocument(
        @PathVariable("documentId") documentId: String,
        @RequestBody request : UpdateDraftDocumentInputRequest
    ) : ResponseEntity<ApiResponse<DocumentResult>> {
        logger.info("updateDocument documentId : ${documentId}, request : ${MapperUtil.logMapCheck(request)}")
        val result = documentSaveServiceable.updateDraftDocument(DocumentServiceContext.SAVE_DEBUG, request)

        return ResponseEntity.ok().body(
            ApiResponse(
                ResHeader(),
                result
            )
        )
    }

    /**
     * 역분개 전표 생성
     */
    //@Operation(summary = "역분개 전표 생성", description = "역분개 전표 생성")
    @PostMapping("reverse")
    fun createReverseDocument(
        @RequestBody request : ReversingDocumentInputRequest
    ) : ResponseEntity<ApiResponse<List<DocumentResult>>> {
        logger.info("createReverseDocument request : ${MapperUtil.logMapCheck(request)}")
        val result = documentSaveServiceable.createReverseDocument(DocumentServiceContext.SAVE_DEBUG, request)

        return ResponseEntity.ok().body(
            ApiResponse(
                ResHeader(),
                result
            )
        )
    }

    //@Operation(summary = "계정코드 조회", description = "계정코드 조회")
    @GetMapping("/accountCode")
    fun findAccountCode(
        @ModelAttribute request : SearchAccountFilters,
    ): ResponseEntity<ApiPageResponse<List<AccountCodeOutputResult>>> {
        logger.info("findAccountCode request : ${MapperUtil.logMapCheck(request)}")
        val data = settlementService.searchAccountCode(request)

        return ResponseEntity.ok(
            ApiPageResponse(
                ResHeader(),
                data
            )
        )
    }


}