package com.abc.us.accounting.documents.exceptions

import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.commons.domain.type.ResultCode

sealed class BaseException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)


// 비즈니스 예외
sealed class DocumentException(
    override val message: String,
    val errorCode: ResultCode,
    override val cause: Throwable? = null
) : BaseException("[$errorCode] $message", cause) {

    class DocumentValidationException(
        message: String,
        errorCode: ResultCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        validationRule: String,
        cause: Throwable? = null
    ) : DocumentException("validationRule:[$validationRule], $message", errorCode, cause)


    class DocumentSaveException(
        message: String,
        errorCode: ResultCode = ResultCode.DOCUMENT_SAVE_EXCEPTION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class DocumentRequestTypeException(
        message: String,
        errorCode: ResultCode = ResultCode.DOCUMENT_REQUEST_TYPE_ERROR,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)


    class DocumentNotFoundException(
        documentId: String,
        message: String = "Document not found, documentId:$documentId",
        errorCode: ResultCode = ResultCode.DOCUMENT_NOT_FOUND,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class DocumentItemNotFoundException(
        docItemId: String,
        message: String = "DocumentItem not found, docItemId:$docItemId",
        errorCode: ResultCode = ResultCode.DOCUMENT_NOT_FOUND,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class SequenceGenerationException(
        message: String = "Sequence Generation Exception",
        errorCode: ResultCode = ResultCode.SEQUENCE_GENERATION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class DocumentStatusTransitionException(
        from: DocumentStatus,
        to: DocumentStatus,
        message: String = "Document status transition exception, from:$from, to:$to",
        errorCode: ResultCode = ResultCode.DOCUMENT_STATUS_TRANSITION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class DocumentItemStatusTransitionException(
        from: DocumentItemStatus,
        to: DocumentItemStatus,
        message: String = "Document item status transition exception, from:$from, to:$to",
        errorCode: ResultCode = ResultCode.DOCUMENT_ITEM_STATUS_TRANSITION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class WorkflowStatusTransitionException(
        from: WorkflowStatus,
        to: WorkflowStatus,
        message: String = "Approval status transition exception, from:$from, to:$to",
        errorCode: ResultCode = ResultCode.APPROVAL_STATUS_TRANSITION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)


    class AlreadyFiscalClosedException(
        message: String = "Already fiscal closed validation exception",
        errorCode: ResultCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)


    class FiscalClosingValidationException(
        message: String = "Fiscal closing validation exception",
        errorCode: ResultCode = ResultCode.ALREADY_FISCAL_PERIOD_CLOSED,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class ClosingStatusTransitionException(
        from: ClosingStatus,
        to: ClosingStatus,
        message: String = "Closing status transition exception, from:$from, to:$to",
        errorCode: ResultCode = ResultCode.CLOSING_STATUS_TRANSITION,
        cause: Throwable? = null
    ) : DocumentException(message, errorCode, cause)

    class PreviousFiscalClosingStatusException(
        companyCode: CompanyCode,
        fiscalYearMonth: FiscalYearMonth,
        message: String = "Previous period closing status is not closed by companyCode:$companyCode, fiscalYearMonth:$fiscalYearMonth",
        errorCode: ResultCode = ResultCode.PREVIOUS_PERIOD_NOT_CLOSED
    ) : DocumentException(message, errorCode)
}

// 시스템 예외 (예시.. 추후 확장 필요)
sealed class SystemException(
    override val message: String,
    val errorCode: ResultCode,
    override val cause: Throwable? = null
) : BaseException(message, cause) {

    class DatabaseException(
        message: String = "Database error",
        errorCode: ResultCode = ResultCode.DATABASE_ERROR,
        cause: Throwable? = null
    ) : SystemException(message, errorCode, cause)

    class ExternalServiceException(
        message: String = "External service error",
        errorCode: ResultCode = ResultCode.EXTERNAL_SERVICE_ERROR,
        cause: Throwable? = null
    ) : SystemException(message, errorCode, cause)
}