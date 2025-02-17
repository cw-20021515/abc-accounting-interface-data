package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.LedgerService.Companion
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.opencsv.CSVWriter
import io.lettuce.core.output.ListOfMapsOutput
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileWriter
import java.io.IOException

interface DocumentSearchServiceable {
    // 전표 상세 조회(api 호출용)
    fun searchByDocId(context: DocumentServiceContext, documentId:String): DocumentDetailOutputResult
    // 전표 여러건 조회
    fun searchDocuments(context: DocumentServiceContext, filters: SearchDocumentFilters): Page<DocumentOutputResult>
    // 전표 여러건 조회(엑셀 다운로드)
    fun searchDocumentsExcelDownload(context: DocumentServiceContext, filters: SearchDocumentFilters, response: HttpServletResponse)

    fun getDownloadFolderPath(): String
    fun generateCsv(headers: List<String>, data: List<List<Any>>, outputPath: String)
    fun searchDocumentsExportToCsv(context: DocumentServiceContext, filters: SearchDocumentFilters, response: HttpServletResponse)
}

@Service
class DocumentSearchService (
    private val accountService: AccountServiceable,
    private val supportService: DocumentSupportService,
    private val persistence: DocumentPersistenceService,
    private val documentServiceable: DocumentServiceable
): DocumentSearchServiceable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun searchByDocId(context: DocumentServiceContext, documentId: String): DocumentDetailOutputResult {
        logger.info { "searchByDocId, context:$context, documentId: $documentId" }
        val results = documentServiceable.findAllByDocIds(context, listOf(documentId))
        require(results.isNotEmpty()) { "documentId:$documentId is not found" }
        require(results.size == 1) { "documentId:$documentId is not unique" }

        // api spec에 맞도록 적용
        val result = results.first()
        val itemAttributeResults =
            persistence.findDocumentItemAttributesByDocItemIdIn(result.docItems.map { it.docItemId })
        val resultItems = result.docItems.map {
            val getCompanyCode = result.companyCode
            val getAccountName = accountService.getAccount(it.toAccountKey()).name
            val getDocumentStatus = result.docStatus
            DocumentItemOutputResult.toResult(
                it, getCompanyCode, getAccountName, getDocumentStatus,
                itemAttributeResults.filter { attrResult ->
                    it.docItemId == attrResult.attributeId.docItemId
                }
            )
        }
        val totalCreditAmount = resultItems.sumOf { it.creditAmount }
        val totalDebitAmount = resultItems.sumOf { it.debitAmount }
        logger.info { "itemAttributeResults :  ${MapperUtil.logMapCheck(itemAttributeResults)}" }
        logger.info { "resultItems :  ${MapperUtil.logMapCheck(resultItems)}" }
        val outputResult = DocumentDetailOutputResult(
            documentId = result.docId,
            documentType = result.docType,
            documentStatus = result.docStatus,
            approvalStatus = result.workflowStatus,
            workflowId = result.workflowId,
            companyCode = result.companyCode,
            fiscalYear = result.fiscalYear,
            fiscalPeriod = result.fiscalMonth,
            documentDate = result.documentDate,
            postingDate = result.postingDate,
            entryDate = result.entryDate,
//            currency = result.currency,
//            amount = result.amount,
            reference = result.reference,
            description = result.text,

            createId = result.createdBy,

            //referenceDocumentType = it.docRelations?.relationType,
            //referenceDocumentId = it.docRelations?.refDocItemId,
            //bizTransactionType = it.docOrigin?.bizSystem,
            referenceDocumentType = null,
            referenceDocumentId = null,
            bizTransactionTypeId = result.docOrigin?.bizSystem.toString(),
            bizTransactionId = result.docOrigin?.bizTxId,
            reversalDocumentId = null,
            reversalReasonCode = null,
            reversalReason = null,
            searchTime = null,
            syncTime = null,
            lineItems = resultItems,
            totalCreditAmount = totalCreditAmount,
            totalDebitAmount = totalDebitAmount,

            createTime = result.createTime
        )

        return outputResult
    }


    @Transactional(readOnly = true)
    override fun searchDocuments(
        context: DocumentServiceContext,
        filters: SearchDocumentFilters
    ): Page<DocumentOutputResult> {
        logger.info { "searchDocuments, context:$context, filters: ${filters.toStringByReflection()}" }
        val documents = persistence.searchDocuments(filters)
        val docOrigins = persistence.findDocumentOriginsByDocIdIn(documents.content.map { it.id })

        val content = supportService.createDocumentResults(context, documents.toList(), docOrigins, listOf(), listOf())
        val filledDocumentResults = supportService.fillDetails(context, content)

        // api spec에 맞도록 적용
        val outputResults = createDocumentOutputResult(filledDocumentResults)
        //logger.info { "searchDocuments, pageable: ${documents.pageable}" }
        //logger.info { "searchDocuments, totalElements: ${documents.totalElements}" }
        //logger.info { "searchDocuments, totalPages: ${documents.totalPages}" }

        return PageImpl(outputResults, documents.pageable, documents.totalElements)
    }


    @Transactional(readOnly = true)
    override fun searchDocumentsExcelDownload(
        context: DocumentServiceContext,
        filters: SearchDocumentFilters,
        response: HttpServletResponse
    ) {
        // 전표 조회를 그대로 활용. 추후 보완 필요
        logger.info { "searchDocumentsExcelDownload, context:$context, filters: ${filters.toStringByReflection()}" }
        val documents = persistence.searchDocuments(filters)
        val docOrigins = persistence.findDocumentOriginsByDocIdIn(documents.content.map { it.id })

        val content = supportService.createDocumentResults(context, documents.toList(), docOrigins, listOf(), listOf())
        val filledDocumentResults = supportService.fillDetails(context, content)

        // api spec에 맞도록 적용
        val outputResults = createDocumentOutputResult(filledDocumentResults)

        val headers = listOf(
            "전표ID",
            "전표유형코드",
            "전표유형",
            "전표상태",
            "승인상태",         // 5
            "회사코드",
            "증빙일",
            "전기일",
            "발행일",
            "통화",           // 10
            "금액",
            "적요",
            "참조",
            "생성자",
            "참조전표유형",     // 15
            "참조전표ID",
            "비즈거래유형",
            "비즈거래ID",
            "역분개전표ID",
            "역분개사유코드",    // 20
            "역분개사유",
        )
        val datas = outputResults.map {
            listOf(
                it.documentId,
                it.documentTypeCode,
                it.documentType,
                it.documentStatus,
                it.approvalStatus,
                it.companyCode,
                it.documentDate,
                it.postingDate,
                it.entryDate,
                it.currency,
                it.amount,
                it.remark,
                it.reference,
                it.createId,
                it.referenceDocumentType,
                it.referenceDocumentId,
                it.bizTransactionType,
                it.bizTransactionId,
                it.reversalDocumentId,
                it.reversalReasonCode,
                it.reversalReason
            )
        } as List<List<Any>>

//        // 다운로드 폴더 경로 설정
//        val downloadFolderPath = getDownloadFolderPath()
//        val outputPath = "$downloadFolderPath/Documents.csv"
//        // CSV 파일 생성
//        generateCsv(headers, datas, outputPath)

        // Excel Download 임시 원복
        val fileName = "전표조회"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    // OUTPUT을 위한 값으로 설정 변경
    fun createDocumentOutputResult(documentResults: List<DocumentResult>): List<DocumentOutputResult> {
        return documentResults.map {
            DocumentOutputResult(
                documentId = it.docId,
                documentTypeCode = it.docType.code,
                documentType = it.docType,
                documentStatus = it.docStatus,
                approvalStatus = it.workflowStatus,
                companyCode = it.companyCode,
                documentDate = it.documentDate,
                postingDate = it.postingDate,
                entryDate = it.entryDate,
                currency = it.currency,
                amount = it.amount,
                remark = it.text ?: "",
                reference = it.reference ?: "",
                createId = it.createdBy,

                //referenceDocumentType = it.docRelations?.relationType,
                //referenceDocumentId = it.docRelations?.refDocItemId,
                referenceDocumentType = "",
                referenceDocumentId = "",
                bizTransactionType = it.docOrigin?.bizSystem.toString(),
                bizTransactionId = it.docOrigin?.bizTxId,
                reversalDocumentId = "",
                reversalReasonCode = "",
                reversalReason = "",
                searchTime = null,
                syncTime = null,
                createTime = it.createTime
            )
        }
    }

    override fun getDownloadFolderPath(): String {
        val downloadPath = System.getProperty("user.home") + "/Downloads"
        val downloadDir = File(downloadPath)

        if (!downloadDir.exists()) {
            downloadDir.mkdirs() // 다운로드 폴더가 없으면 생성
        }
        return downloadPath
    }

    override fun generateCsv(headers: List<String>, datas: List<List<Any>>, filePath: String) {
        val stringBuilder = StringBuilder()
        // 헤더 추가
        stringBuilder.append(headers.joinToString("|")).append("\n")
        // 데이터 추가
        datas.forEach { row ->
            // 각 항목에 대해 null을 대체
            val processedRow = row.map { it?.toString() ?: "" }  // null 처리
            stringBuilder.append(processedRow.joinToString("|")).append("\n")
        }

        try {
            val file = File(filePath)

            // 파일이 이미 존재하는지 확인하고 이름 변경
            var newFilePath = filePath
            var counter = 1
            while (File(newFilePath).exists()) {
                // 확장자를 기준으로 파일명과 확장자 분리
                val fileExtension = file.extension
                val baseFileName = file.nameWithoutExtension

                // "(숫자)" 형태로 파일명 변경
                newFilePath = "${file.parent}/${baseFileName}($counter).$fileExtension"
                counter++
            }

            // 로컬 파일 작성
            FileWriter(newFilePath).use { writer ->
                writer.write(stringBuilder.toString())  // 파일 내용 작성
            }
            logger.info { "CSV 파일이 성공적으로 생성되었습니다: $newFilePath" }

//            // 생성된 파일을 HTTP 응답으로 반환
//            val fileBytes = File(newFilePath).readBytes()
//
//            return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
//                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
//                .body(fileBytes)

        } catch (e: IOException) {
            // 예외 발생 시 오류 메시지 반환
            logger.info { "파일 생성 중 오류 발생: ${e.message}" }
//            return ResponseEntity.status(500).body("파일 생성 중 오류가 발생했습니다.".toByteArray())
        } catch (e: Exception) {
            // 예기치 않은 예외 처리
            logger.info { "예기치 못한 오류 발생: ${e.message}" }
//            return ResponseEntity.status(500).body("예기치 못한 오류가 발생했습니다.".toByteArray())
        }
    }


    @Transactional(readOnly = true)
    override fun searchDocumentsExportToCsv(
        context: DocumentServiceContext,
        filters: SearchDocumentFilters,
        response: HttpServletResponse
    ) {
        // csv 다운로드
        logger.info { "searchDocumentsExportToCsv, context:$context, filters: ${filters.toStringByReflection()}" }
        val documents = persistence.searchDocuments(filters)
        logger.info { "searchDocumentsExportToCsv, documents.size: ${documents.size}" }
        logger.info { "searchDocumentsExportToCsv, documents.totalPages: ${documents.totalPages}" }
        val totalPageCount = documents.totalPages

        var pageNumber = 0
        //var resultSet: MutableList<DocumentOutputResult> = mutableListOf()

        val headers = listOf(
            "전표ID",
            "전표유형코드",
            "전표유형",
            "전표상태",
            "승인상태",         // 5
            "회사코드",
            "증빙일",
            "전기일",
            "발행일",
            "통화",           // 10
            "금액",
            "적요",
            "참조",
            "생성자",
            "참조전표유형",     // 15
            "참조전표ID",
            "비즈거래유형",
            "비즈거래ID",
            "역분개전표ID",
            "역분개사유코드",    // 20
            "역분개사유",
        )

        // 응답 헤더 설정
        response.contentType = "text/csv"
        response.characterEncoding = "UTF-8"
        response.setHeader("Content-Disposition", "attachment; filename=\"documents.csv\"")

        response.writer.use { writer ->
            CSVWriter(writer, '|', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END).use { csvWriter ->
                // CSV 헤더 작성
                csvWriter.writeNext(headers.toTypedArray())

                do {
                    // pageNumber만 변경해서 조회 반복
                    val pageFilters = filters.copy(pageable = filters.pageable.copy(page = pageNumber))
                    val repeatResults = searchDocuments(context, pageFilters)

                    repeatResults.forEach { it ->
                        csvWriter.writeNext(
                            listOf(
                                it.documentId,
                                it.documentTypeCode,
                                it.documentType.name,
                                it.documentStatus.toString(),
                                it.approvalStatus.toString(),
                                it.companyCode.code,
                                it.documentDate.toString(),
                                it.postingDate.toString(),
                                it.entryDate.toString(),
                                it.currency,
                                it.amount.toString(),
                                it.remark,
                                it.reference,
                                it.createId,
                                it.referenceDocumentType,
                                it.referenceDocumentId,
                                it.bizTransactionType,
                                it.bizTransactionId,
                                it.reversalDocumentId,
                                it.reversalReasonCode,
                                it.reversalReason
                            ).toTypedArray()
                        )
                    }

                    logger.info { "&& searchDocumentsExportToCsv, pageNumber: ${pageNumber}" }
                    //logger.info { " searchDocumentsExportToCsv, resultSet.size: ${resultSet.size}" }
                    pageNumber++
                } while (pageNumber < totalPageCount)
            }
        }
        //logger.info { "&&&&& searchDocumentsExportToCsv, resultSet.totalSize: ${resultSet.size}" }
    }



}