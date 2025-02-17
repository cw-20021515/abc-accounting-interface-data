package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.repository.DocumentNoteRepository
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.domain.type.ReversalReason
import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.supports.mapper.MapperUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime

interface DocumentSaveServiceable {
    // 전표 등록
    fun createDocument(context: DocumentServiceContext, request: CreateDocumentInputRequest): DocumentResult
    // 전표 수정(Draft, 임시전표)
    fun updateDraftDocument(context: DocumentServiceContext, request: UpdateDraftDocumentInputRequest): DocumentResult
    // 전표 역분개
    fun createReverseDocument(context: DocumentServiceContext, request: ReversingDocumentInputRequest): List<DocumentResult>
}

@Service
class DocumentSaveService (
    private val accountService: AccountServiceable,
    private val supportService: DocumentSupportService,
    private val persistence: DocumentPersistenceService,
    private val documentServiceable: DocumentServiceable,
    private val documentNoteRepository: DocumentNoteRepository
): DocumentSaveServiceable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun createDocument(
        context: DocumentServiceContext,
        request: CreateDocumentInputRequest
    ): DocumentResult {
        logger.info { "createDraftDocument, context:$context, request: ${MapperUtil.logMapCheck(request)}" }

        // 임시 테스트 중
//        val inputItemRequest1 = DocumentItemInputRequest(
//            documentItemId = "001",
//            companyCode = CompanyCode.N200,
//            accountCode = "1101010",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(10.23),
//            creditAmount = BigDecimal(0),
//            remark = "abc",
//            costCenter = "12"
//        )
//        val inputItemRequest2 = DocumentItemInputRequest(
//            documentItemId = "002",
//            companyCode = CompanyCode.N200,
//            accountCode = "1136010",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(0),
//            creditAmount = BigDecimal(5.11),
//            remark = "abc2",
//            costCenter = "122"
//        )
//        val inputItemRequest3 = DocumentItemInputRequest(
//            documentItemId = "003",
//            companyCode = CompanyCode.N200,
//            accountCode = "1136020",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(0),
//            creditAmount = BigDecimal(5.12),
//            remark = "abc2",
//            costCenter = "122"
//        )
//        val testInputRequest = CreateDocumentInputRequest(
//            documentDate = LocalDate.now(),
//            postingDate = LocalDate.now(),
//            companyCode = CompanyCode.N200,
//            txCurrency = CurrencyCode.USD.code,
//            documentReferenceInfo = "aaa",
//            description = "bbb",
//            createTime = OffsetDateTime.now(),
//            createdBy = Constants.APP_NAME,
//            documentsCreateLineItems = mutableListOf(inputItemRequest1, inputItemRequest2, inputItemRequest3)
//        )
//        // text Data
//        logger.info("createDocument testInputRequest : ${MapperUtil.logMapCheck(testInputRequest)}")
//        val documentsRequest = changeToCreateDocumentRequest(testInputRequest)

        val documentsRequest = changeToCreateDocumentRequest(request)
        val result = if (request.saveType == "posting") {
            documentServiceable.posting(DocumentServiceContext.SAVE_DEBUG, listOf(documentsRequest))
        } else {
            documentServiceable.draft(DocumentServiceContext.SAVE_DEBUG, listOf(documentsRequest))
        }

        // document note 등록
        val docId = result.first().docId
        val lineNotes = request.documentsCreateNotesItems.toMutableList()
        println("lineNotes.size: ${lineNotes.size}")
        println("lineNotes content: $lineNotes")
        if (lineNotes.size > 0) {
            val noteList = convertToDocumentNoteRequestList(docId, request.createUser, lineNotes)
            documentNoteRepository.saveAll(noteList.map { it.toCreateEntity(it) })
        }

        return result.first()
    }

    fun convertToDocumentNoteRequestList(
        docId: String,
        createId: String,
        lineNotes: MutableList<DocumentNoteInputRequest>
    ): List<CreateDocumentNoteRequest>{
        val docNoteReq: List<CreateDocumentNoteRequest> = lineNotes.map { it ->
            val result = convertToCreateDocumentNoteRequest(
                docId,
                createId,
                it
            )
            result
        }
        return docNoteReq
    }

    fun convertToCreateDocumentNoteRequest(
        docId: String,
        createId: String,
        lineNote: DocumentNoteInputRequest
    ): CreateDocumentNoteRequest{
        return CreateDocumentNoteRequest(
            docId = docId,
            contents = lineNote.notesContents,
            createdTime = OffsetDateTime.now(),
            createdBy = createId
        )
    }

    //  전표등록 화면에서 받은 파라미터를 CreateDocumentRßequest 로 변경
    fun changeToCreateDocumentRequest(
        param: CreateDocumentInputRequest
    ): CreateDocumentRequest {
        val docType: DocumentType = if (param.documentTypeCode == null || param.documentTypeCode.toString() == "") {
            DocumentType.valueOf(param.documentTypeCode.toString())
        } else {
            DocumentType.JOURNAL_ENTRY // 기본값 설정
        }

        //DocumentItem 관련 변경
        val documentItems: List<DocumentItemRequest> = param.documentsCreateLineItems.map {
            val accountSide: AccountSide = if (it.debitAmount != BigDecimal.ZERO) {
                AccountSide.DEBIT
            } else if (it.creditAmount != BigDecimal.ZERO) {
                AccountSide.CREDIT
            } else {
                AccountSide.DEBIT //기본값
            }
            DocumentItemRequest(
                companyCode = it.companyCode!!,
                accountCode = it.accountCode,
                accountSide = accountSide,
                txCurrency = param.currency,
                txAmount = if (accountSide.code == "D") it.debitAmount else it.creditAmount,
                text = it.remark,
                costCenter = it.costCenter,
            )
        }

        return CreateDocumentRequest(
            documentDate = param.documentDate,
            postingDate = param.postingDate,
            //entryDate = param.entryDate,
            companyCode = param.companyCode,
            docType = docType,
            txCurrency = param.currency,
            reference = param.documentReferenceInfo,
            text = param.description,

            createTime = OffsetDateTime.now(),
            createdBy = param.createUser,
            docItems = documentItems.toMutableList()
            // note
            // attachmentFile
        )
    }


    override fun updateDraftDocument(
        context: DocumentServiceContext,
        request: UpdateDraftDocumentInputRequest
    ): DocumentResult {
        logger.info { "updateDraftDocument, context:$context, request: ${MapperUtil.logMapCheck(request)}" }

        // 임시 테스트 중
//        val inputItemRequest1 = DocumentItemOutputResult(
//            documentItemId = "AB2501400001-001",
//            companyCode = CompanyCode.N200,
//            accountCode = "1101010",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(15.47),
//            creditAmount = BigDecimal(0),
//            remark = "asd",
//            costCenter = "234"
//        )
//        val inputItemRequest2 = DocumentItemOutputResult(
//            documentItemId = "AB2501400001-002",
//            companyCode = CompanyCode.N200,
//            accountCode = "1136010",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(0),
//            creditAmount = BigDecimal(7.23),
//            remark = "asf",
//            costCenter = "235"
//        )
//        val inputItemRequest3 = DocumentItemOutputResult(
//            documentItemId = "AB2501400001-003",
//            companyCode = CompanyCode.N200,
//            accountCode = "1136020",
//            currency = CurrencyCode.USD.code,
//            debitAmount = BigDecimal(0),
//            creditAmount = BigDecimal(8.24),
//            remark = "asg",
//            costCenter = "236"
//        )
//        val testInputRequest = UpdateDraftDocumentInputRequest(
//            documentId = "AB2501400001",
//            documentDate = LocalDate.of(2025,1,14),
//            postingDate = LocalDate.of(2025,1,14),
//            companyCode = CompanyCode.N200,
//            txCurrency = CurrencyCode.USD.code,
//            reference = "qwer",
//            description = "asdf",
//            lineItems = mutableListOf(inputItemRequest1, inputItemRequest2, inputItemRequest3)
//        )
//        // text Data
//        logger.info("updateDraftDocument testInputRequest : ${MapperUtil.logMapCheck(testInputRequest)}")
//        val documentsRequest = changeToUpdateDocumentRequest(testInputRequest)

        val documentsRequest = changeToUpdateDocumentRequest(request)
        val result = documentServiceable.draft(DocumentServiceContext.SAVE_DEBUG, listOf(documentsRequest))
        return result.first()
    }


    //  전표등록 화면에서 받은 파라미터를 Request Filter로 변경
    fun changeToUpdateDocumentRequest(
        param: UpdateDraftDocumentInputRequest
    ): UpdateDraftDocumentRequest {
        val documentType: DocumentType = DocumentType.valueOf(param.documentType.toString())

        //DocumentItem 관련 변경
        val documentItems: List<DocumentItemRequest> = param.lineItems.map {
            val accountSide: AccountSide = if (it.debitAmount != BigDecimal.ZERO) {
                AccountSide.DEBIT
            } else if (it.creditAmount != BigDecimal.ZERO) {
                AccountSide.CREDIT
            } else {
                AccountSide.DEBIT //기본값
            }
            DocumentItemRequest(
                companyCode = it.companyCode!!,
                accountCode = it.accountCode,
                accountSide = accountSide,
                txCurrency = it.currency,
                txAmount = if (accountSide.code == "D") it.debitAmount else it.creditAmount,
                text = it.remark.toString(),
                costCenter = it.costCenter.toString(),
                profitCenter = it.profitCenter,
                segment = it.segment,
                project = it.project,
                customerId = it.customerId,
                vendorId = it.vendorId
                // itemAttribute List 필요
            )
        }

        return UpdateDraftDocumentRequest(
            docId = param.documentId,
            documentDate = param.documentDate,
            postingDate = param.postingDate,
            //entryDate = param.entryDate,
            companyCode = param.companyCode,
            docType = documentType,
            txCurrency = param.txCurrency,
            reference = param.reference,
            text = param.description,

            createTime = OffsetDateTime.now(),
            createdBy = Constants.APP_NAME,
            docItems = documentItems.toMutableList()
            // note
            // attachmentFile
        )
    }

    override fun createReverseDocument(
        context: DocumentServiceContext,
        request: ReversingDocumentInputRequest
    ): List<DocumentResult> {
        logger.info("updateDraftDocument testInputxRequest : ${MapperUtil.logMapCheck(request)}")
        val documentsRequest = changeToReversingDocumentRequest(request)
        // fun reversing(context: DocumentServiceContext, requests: List<ReversingDocumentRequest>): List<DocumentResult>
        val result = documentServiceable.reversing(DocumentServiceContext.SAVE_DEBUG, documentsRequest)
        //val result = documentsRequest?.let { documentServiceable.reversing(DocumentServiceContext.SAVE_DEBUG, it) }
        return result
    }

    //  전표 화면에서 받은 파라미터를 Request Filter로 변경
    fun changeToReversingDocumentRequest(
        param: ReversingDocumentInputRequest
    ): List<ReversingDocumentRequest> {
        val inputReason = param.reason.toString()
        val reason = ReversalReason.getDescriptionEnByCode(inputReason)
        logger.info { "ReversalReason reason = ${reason}, inputReason = ${inputReason}" }
        return param.documentList.map { inputDoc ->
            ReversingDocumentRequest(
                refDocId = inputDoc.documentId,
                reason = reason,
                postingDate = param.postingDate,
                createdBy = param.creatId
            )
        }    }

}