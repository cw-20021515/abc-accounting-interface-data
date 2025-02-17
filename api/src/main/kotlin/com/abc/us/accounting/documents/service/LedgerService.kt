package com.abc.us.accounting.documents.service

import com.abc.us.accounting.commons.domain.type.Sort
import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.entity.DocumentItemAttribute
import com.abc.us.accounting.documents.domain.repository.*
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.DocumentItemStatus
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.generated.models.AccountingLedgerState
import com.opencsv.CSVWriter
import io.lettuce.core.output.ListOfMapsOutput
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal

@Service
class LedgerService   (
    private val persistenceService: DocumentPersistenceService,
    private val documentSearchServiceable: DocumentSearchServiceable,
    private val ledgerSearchRepository: LedgerSearchRepository,
    private val accountService: AccountServiceable
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // 원장 조회
    @Transactional(readOnly = true)
    fun searchLedgers(context: DocumentServiceContext, filters: SearchLedgerFilters): Page<LedgerOutputResult> {
        logger.info { "searchLedgers, context:$context, filters: ${filters.toStringByReflection()}" }
        val ledgers = ledgerSearchRepository.searchLedger(filters)
        val docIds = ledgers.map { it.docId }.toList()
        val docItemIds = ledgers.map { it.docItemId }.toList()
        val ledgerResults = searchAllLedgerByDocIds(context, docIds as List<String>, docItemIds as List<String>)
        //logger.info {"ledgerResults data : ${MapperUtil.logMapCheck(ledgerResults)} "}
        val outputResults = createLedgerOutputResult(ledgerResults)
        logger.info {"searchLedgers - outputResults.size : ${outputResults.size}} "}
        return PageImpl(outputResults, ledgers.pageable, ledgers.totalElements)
    }

    // 원장 조회 엑셀다운로드
    @Transactional(readOnly = true)
    fun searchLedgersExcelDownload(context: DocumentServiceContext,
                                   filters: SearchLedgerFilters,
                                   response: HttpServletResponse
    ) {
        logger.info { "searchLedgersExcelDownload, context:$context, filters: ${filters.toStringByReflection()}" }
        val ledgers = ledgerSearchRepository.searchLedger(filters)
        val docIds = ledgers.map { it.docId }.toList()
        val docItemIds = ledgers.map { it.docItemId }.toList()
        val ledgerResults = searchAllLedgerByDocIds(context, docIds as List<String>, docItemIds as List<String>)
        logger.info {"ledgerResults data : ${MapperUtil.logMapCheck(ledgerResults)} "}
        val outputResults = createLedgerOutputResult(ledgerResults)
        logger.info {"searchLedgersExcelDownload - outputResults.size : ${outputResults.size}} "}

        val headers = listOf(
            "전표상태",
            "전표ID",
            "전표유형코드",
            "전표유형",
            "증빙일",      // 5
            "전기일",
            "발행일",
            "전표항목ID",
            "회사코드",
            "계정코드",    // 10
            "계정명",
            "적요",
            "통화",
            "차변",
            "대변",       // 15
            "잔액",
            "코스트센터",
            "고객",
            "주문ID",
            "주문아이템ID", // 20
            "시리얼번호",
            "판매유형",
            "판매항목",
            "렌탈코드",
            "채널ID",     // 25
            "레퍼럴코드",
            "거래처ID",
            "지급ID",
            "자재ID"      // 29
        )

        val datas = outputResults.map {
            listOf(
                it.documentStatus,
                it.documentId,
                it.documentTypeCode,
                it.documentType,
                it.documentDate,
                it.postingDate,
                it.entryDate,
                it.documentItemId,
                it.companyCode,
                it.accountCode,         // 계정코드
                it.accountName,
                it.remark,
                it.currency,
                it.debitAmount,
                it.creditAmount,
                it.balance,
                it.costCenter,
                it.customerId,
                it.orderId,
                it.orderItemId,         // 주문아이템ID
                it.serialNumber,
                it.salesType,
                it.salesItem,
                it.rentalCode,
                it.channelId,
                it.referralCode,
                it.vendorId,
                it.payoutId,
                it.materialId
            )
        } as List<List<Any>>

//        // 다운로드 폴더 경로 설정
//        val downloadFolderPath = documentSearchServiceable.getDownloadFolderPath()
//        val outputPath = "$downloadFolderPath/Ledgers.csv"
//        // CSV 파일 생성
//        documentSearchServiceable.generateCsv(headers, datas, outputPath)

        // Excel Download 임시 원복
        val fileName = "원장조회"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    // 원장 조회 CSV 다운로드
    @Transactional(readOnly = true)
    fun searchLedgersExportToCsv(
        context: DocumentServiceContext,
        filters: SearchLedgerFilters,
        response: HttpServletResponse
    ) {
        logger.info { "searchLedgersExportToCsv, context:$context, filters: ${filters.toStringByReflection()}" }
        val ledgers = ledgerSearchRepository.searchLedger(filters)
        logger.info { "searchLedgersExportToCsv, ledgers.size : ${ledgers.size}" }
        logger.info { "searchLedgersExportToCsv, ledgers.totalPages: ${ledgers.totalPages}" }
        val totalPageCount = ledgers.totalPages   //전체 페이지 수
        var pageNumber = 0

        val headers = listOf(
            "전표상태",
            "전표ID",
            "전표유형코드",
            "전표유형",
            "증빙일",      // 5
            "전기일",
            "발행일",
            "전표항목ID",
            "회사코드",
            "계정코드",    // 10
            "계정명",
            "적요",
            "통화",
            "차변",
            "대변",       // 15
            "잔액",
            "코스트센터",
            "고객",
            "주문ID",
            "주문아이템ID", // 20
            "시리얼번호",
            "판매유형",
            "판매항목",
            "렌탈코드",
            "채널ID",     // 25
            "레퍼럴코드",
            "거래처ID",
            "지급ID",
            "자재ID"      // 29
        )

        // 응답 헤더 설정
        response.contentType = "text/csv"
        response.characterEncoding = "UTF-8"
        response.setHeader("Content-Disposition", "attachment; filename=\"ledger.csv\"")

        response.writer.use { writer ->
            CSVWriter(writer, '|', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END).use { csvWriter ->
                // CSV 헤더 작성
                csvWriter.writeNext(headers.toTypedArray())

                do {
                    // pageNumber만 변경해서 조회 반복
                    val pageFilters = filters.copy(pageable = filters.pageable.copy(page = pageNumber))
                    val repeatResults = searchLedgers(context, pageFilters)

                    repeatResults.forEach { it ->
                        csvWriter.writeNext(
                            listOf(
                                it.documentStatus.toString(),
                                it.documentId,
                                it.documentTypeCode,
                                it.documentType.name,
                                it.documentDate.toString(),
                                it.postingDate.toString(),
                                it.entryDate.toString(),
                                it.documentItemId,
                                it.companyCode.code,
                                it.accountCode,         // 계정코드
                                it.accountName,
                                it.remark,
                                it.currency,
                                it.debitAmount.toString(),
                                it.creditAmount.toString(),
                                it.balance.toString(),
                                it.costCenter,
                                it.customerId,
                                it.orderId,
                                it.orderItemId,         // 주문아이템ID
                                it.serialNumber,
                                it.salesType,
                                it.salesItem,
                                it.rentalCode,
                                it.channelId,
                                it.referralCode,
                                it.vendorId,
                                it.payoutId,
                                it.materialId
                            ).toTypedArray()
                        )
                    }

                    logger.info { "&& searchLedgersExportToCsv, pageNumber: ${pageNumber}" }
                    pageNumber++
                } while (pageNumber < totalPageCount)
            }
        }
    }


    // docItemId에 해당하는 List<DocumentItemAttributeResult> 중에서 category 값이 특정 DocumentAttributeType과 같은 value의 값을 찾는다.
    fun findValueByCategory(itemAttributes: List<DocumentItemAttributeResult>, targetCategory: DocumentAttributeType): String {
        val itemAttribute = itemAttributes.find { it.type == targetCategory }
        val value = itemAttribute?.value
        return value ?: ""
    }

    fun setAccountingLedgerStateByDocItemStatus(docItemStatus: DocumentItemStatus): AccountingLedgerState {
        var ledgerState = AccountingLedgerState.OPEN
        if(docItemStatus == DocumentItemStatus.NORMAL || docItemStatus == DocumentItemStatus.PARTIAL){
            ledgerState = AccountingLedgerState.OPEN
        } else if (docItemStatus == DocumentItemStatus.CLEARED || docItemStatus == DocumentItemStatus.CLEARING
            || docItemStatus == DocumentItemStatus.REVERSED || docItemStatus == DocumentItemStatus.REVERSAL ){
            ledgerState = AccountingLedgerState.CLEARED
        }
        return ledgerState
    }

    fun searchAllLedgerByDocIds(context: DocumentServiceContext, docIds: List<String>, docItemIds: List<String>): List<LedgerResult> {
        logger.info { "searchAllByDocIds, context:$context, documentIds: $docIds, documentItemIds: $docItemIds" }
        val docItems = persistenceService.findDocumentItems(docItemIds)
        val docItemAttributes = persistenceService.findDocumentItemAttributesByDocItemIdIn(docItems.map { it.id })
        val documents = persistenceService.findDocuments(docIds)

        return createLedgerResults(context, docItems, docItemAttributes, documents)
    }

    fun createLedgerResults(context: DocumentServiceContext,
                            docItems: List<DocumentItem>,
                            docItemAttributes: List<DocumentItemAttribute>,
                            documents: List<Document>): List<LedgerResult> {
        return docItems.map { docItem ->
            val document = documents.firstOrNull{ it.id == docItem.docId }
            val docItemAttribute = docItemAttributes.filter { it.attributeId.docItemId in docItem.id }
            val result = createLedgerResult(context, docItem, docItemAttribute, document)
            result
        }
    }

    fun createLedgerResult(context: DocumentServiceContext,
                            docItem: DocumentItem,
                            docItemAttribute: List<DocumentItemAttribute>,
                            document: Document? = null): LedgerResult {
        val ledgerResult = LedgerResult(
            docItemId = docItem.id,
            docId = docItem.docId,
            lineNumber = docItem.lineNumber,
            docItemStatus = docItem.docItemStatus,
            accountCode = docItem.accountCode,
            accountSide = docItem.accountSide,
            exchangeRateId = docItem.exchangeRateId,
            text = docItem.text,
            docTemplateCode = docItem.docTemplateCode,

            currency = docItem.money.currency.toString(),
            amount = docItem.money.amount,
            costCenter = docItem.costCenter,
            profitCenter = docItem.profitCenter,
            segment = docItem.segment,
            project = docItem.project,

            customerId = docItem.customerId,
            vendorId = docItem.vendorId,

            createTime = docItem.createTime,
            createdBy = docItem.createdBy,
            updateTime = docItem.updateTime,
            updatedBy = docItem.updatedBy,
            document = document?.toResult(),
            itemAttributes = docItemAttribute.map { it.toResult() }.toMutableList()
        )

        return ledgerResult
    }

    // OUTPUT을 위한 값으로 설정 변경
    fun createLedgerOutputResult(ledgerResults : List<LedgerResult>): List<LedgerOutputResult> {
        return ledgerResults.map {
            if (it.document == null) {
                null
            } else {
                LedgerOutputResult(
                    documentStatus = setAccountingLedgerStateByDocItemStatus(it.docItemStatus),
                    documentId = it.document.docId,
                    documentTypeCode = it.document.docType.code,
                    documentType = it.document.docType,

                    documentDate = it.document.documentDate,
                    postingDate = it.document.postingDate,
                    entryDate = it.document.entryDate,

                    documentItemId = it.docItemId,
                    companyCode = it.document.companyCode,
                    accountCode = it.accountCode,
                    accountName = accountService.getAccount(it.toAccountKey()).name,
                    remark = it.text,

                    currency = it.currency.toString(),
                    debitAmount = if(it.accountSide== AccountSide.DEBIT){it.amount} else { BigDecimal(0) },
                    creditAmount = if(it.accountSide== AccountSide.CREDIT){it.amount} else { BigDecimal(0) },
                    balance = it.amount,

                    documentTemplateCode = it.docTemplateCode.toString(),
                    costCenter = it.costCenter,
                    profitCenter = it.profitCenter,
                    segment = it.segment,
                    project = it.project,

                    customerId = it.customerId,
//                    customerId = if(it.itemAttributes.isNotEmpty()) {
//                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CUSTOMER_ID)
//                    } else {
//                        ""
//                    }.toString(),
                    orderId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.ORDER_ID)
                    } else {
                        ""
                    }.toString(),
                    orderItemId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.ORDER_ITEM_ID)
                    } else {
                        ""
                    }.toString(),
                    contractId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CONTRACT_ID)
                    } else {
                        ""
                    }.toString(),
                    serialNumber = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.SERIAL_NUMBER)
                    } else {
                        ""
                    }.toString(),

                    salesType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.SALES_TYPE)
                    } else {
                        ""
                    }.toString(),
                    salesItem = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.SALES_ITEM)
                    } else {
                        ""
                    }.toString(),
                    rentalCode = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.RENTAL_CODE)
                    } else {
                        ""
                    }.toString(),
                    //channelId = "Not find channelId",
                    channelId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CHANNEL_ID)
                    } else {
                        ""
                    }.toString(),
                    referralCode = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.REFERRAL_CODE)
                    } else {
                        ""
                    }.toString(),

                    vendorId = it.vendorId,
//                    vendorId = if(it.itemAttributes.isNotEmpty()) {
//                        findValueByCategory(it.itemAttributes, DocumentAttributeType.VENDOR_ID)
//                    } else {
//                        ""
//                    }.toString(),
                    payoutId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.PAYOUT_ID)
                    } else {
                        ""
                    }.toString(),
                    //invoiceId = "Not find invoiceId",
                    invoiceId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.VENDOR_INVOICE_ID)
                    } else {
                        ""
                    }.toString(),
                    purchaseOrderId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.PURCHASE_ORDER)
                    } else {
                        ""
                    }.toString(),
                    materialId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.MATERIAL_ID)
                    } else {
                        ""
                    }.toString(),

                    materialType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.MATERIAL_TYPE)
                    } else {
                        ""
                    }.toString(),
                    materialCategory = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.MATERIAL_CATEGORY_CODE)
                    } else {
                        ""
                    }.toString(),
                    installType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.INSTALLATION_TYPE)
                    } else {
                        ""
                    }.toString(),
                    filterType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.FILTER_TYPE)
                    } else {
                        ""
                    }.toString(),
                    featureType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.FEATURE_TYPE)
                    } else {
                        ""
                    }.toString(),

                    commitmentDuration = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.COMMITMENT_DURATION)
                    } else {
                        ""
                    }.toString(),
                    channelName = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CHANNEL_NAME)
                    } else {
                        ""
                    }.toString(),
                    channelType = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CHANNEL_TYPE)
                    } else {
                        ""
                    }.toString(),
                    channelDetail = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.CHANNEL_DETAIL)
                    } else {
                        ""
                    }.toString(),
                    branchId = if(it.itemAttributes.isNotEmpty()) {
                        findValueByCategory(it.itemAttributes, DocumentAttributeType.BRANCH_ID)
                    } else {
                        ""
                    }.toString(),

                    searchTime = null,
                    syncTime = null
                )
            }
        }.filterNotNull()
    }

}