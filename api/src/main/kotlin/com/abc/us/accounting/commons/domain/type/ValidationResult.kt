package com.abc.us.accounting.commons.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException

data class ValidationResult(
    val code: ResultCode,
    val rule: String = "",
    val message: String ="",
    val exception: Exception? = null
) {
    fun isSucceeded() = code == ResultCode.SUCCEEDED
    fun isFailed() = code != ResultCode.SUCCEEDED

    fun toDocumentException(): DocumentException {
        return DocumentException.DocumentValidationException(message, code, rule, exception)
    }

    companion object {
        fun succeeded() = ValidationResult(ResultCode.SUCCEEDED, "", "SUCCEEDED")
        fun failed(rule:String, message: String) = ValidationResult(ResultCode.FAILED, rule, message)
    }

}

interface ValidationRule<A, B> {
    fun validate(context: A, target: B): ValidationResult
}