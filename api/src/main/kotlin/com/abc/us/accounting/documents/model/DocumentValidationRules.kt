package com.abc.us.accounting.documents.model

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.type.*
import com.abc.us.accounting.documents.service.*
import com.abc.us.accounting.commons.domain.type.ResultCode
import com.abc.us.accounting.commons.domain.type.ValidationResult
import com.abc.us.accounting.commons.domain.type.ValidationRule
import org.slf4j.LoggerFactory
import java.math.BigDecimal


/**
 * Document validation rules
 * 전표 생성과 관련된 모든 요청에 대한 검증
 * @property errorCode
 * @property description
 */
enum class CreateDocumentValidationRule(
    val errorCode: ResultCode,
    val description: String
) : ValidationRule<DocumentServiceContext, List<CreateDocumentRequest>> {
    DOC_HASH_UNIQUE_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 갯수와 docHash의 갯수가 일치하는지 체크"
    ) {
        override fun validate(context:DocumentServiceContext, target: List<CreateDocumentRequest>): ValidationResult {
            val actual = target.map { it.docHash }.groupBy { it }

            val duplicateDocHashes = actual.filter { it.value.size > 1 }.keys

            if (actual.size != target.size) {
                return ValidationResult(errorCode, name, "Duplicate DocHash detected:${duplicateDocHashes} by actualSize:${actual.size}, requestSize:${target.size} are not equal")
            }

            return ValidationResult.succeeded()
        }
    },
    DEBIT_CREDIT_BALANCE_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 차대변 합계 금액이 일치 하는지 체크"
    ) {
        override fun validate(context:DocumentServiceContext, target: List<CreateDocumentRequest>): ValidationResult {
            for (request in target) {
                val totalDebit = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
                val totalCredit = calculateItemTxAmount(AccountSide.CREDIT, null, request.docItems)
                val checkValid = totalDebit.compareTo(totalCredit) == 0

                val templateCode = request.docOrigin?.docTemplateCode
                val orderItemId = request.docItems.map {
                    it.attributes.filter { attribute -> attribute.attributeType == DocumentAttributeType.ORDER_ITEM_ID }.map { it.attributeValue }
                }.flatten().firstOrNull()

                if (!checkValid) {
                    return ValidationResult(errorCode, name, "Debit amount:${totalDebit} and credit amount:${totalCredit} are not equal, by templateCode:${templateCode}, orderItemId:${orderItemId}")
                }

            }
            return ValidationResult.succeeded()
        }
    },
    ACCOUNT_CODE_VALIDATION(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 계정이 유효한지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<CreateDocumentRequest>): ValidationResult {

            for (request in target) {
                val accountKeys = request.docItems.map { it.toAccountKey() }
                val accounts = accountService.getValidAccounts(accountKeys)
                val validAccountKeys = accounts.map { it.accountKey }
                val invalidAccountKeys = accountKeys.filter { !validAccountKeys.contains(it) }

                if (invalidAccountKeys.isNotEmpty()) {
                    val errorMessage = "Invalid accountKeys:${invalidAccountKeys}, by accountKeys:${accountKeys}, request:${request}"
                    return ValidationResult(errorCode, name, errorMessage)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    TOTAL_AMOUNT_VALIDATION (
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 총 금액이 0이 아닌지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<CreateDocumentRequest>): ValidationResult {
            for (request in target) {
                val totalDebit = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
                val totalCredit = calculateItemTxAmount(AccountSide.CREDIT, null, request.docItems)
                if (totalDebit <= BigDecimal.ZERO || totalCredit <= BigDecimal.ZERO) {
                    return ValidationResult(errorCode, name, "Debit amount:${totalDebit} and credit amount:${totalCredit} must be greater than 0, by request:${request}")
                }
            }
            return ValidationResult.succeeded()
        }
    },

    DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 계정이 허용된 계정유형인지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<CreateDocumentRequest> ): ValidationResult {
            for (request in target) {
                val documentType = request.docType
                val allowedAccountTypes =documentType.allowAccountTypes
                if (context.debug) {
                    logger.trace("DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK, docType:{}, Allowed account types: {}", documentType, allowedAccountTypes)
                }
                val accountKeys = request.docItems.map { it.toAccountKey() }
                val accounts = accountService.getValidAccounts(accountKeys)
                if (context.debug) {
                    logger.trace("DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK, accounts: {}", accounts)
                }
                val validAccountKeys = accounts.filter { allowedAccountTypes.contains(it.accountType) }.map { it.accountKey }
                if (validAccountKeys.isEmpty()) {
                    val errorMessage = "ValidAccountKeys is Empty by document type:${documentType} and allowed account types:${allowedAccountTypes}, accountKeys:${accountKeys}, by request:${request}"
                    ValidationResult(errorCode, name, errorMessage)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    DOCUMENT_FISCAL_YEAR_MONTH_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전기일과 회계연월이 일치하는지 확인"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<CreateDocumentRequest> ): ValidationResult {


            return ValidationResult.succeeded()
        }
    }
        ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var accountService: AccountServiceable
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, requests: List<CreateDocumentRequest>): List<ValidationResult> {
            // 의존성 주입, 최초1회
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }
            logger.debug("accountService:{}", accountService)

            val results = entries.map { it.validate(context, requests) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, requests: List<CreateDocumentRequest>) {
            val results = validateAll(context, requests)
            if (results.isNotEmpty()) {
                val result = results.first()
                throw result.toDocumentException()
            }
        }
    }
}

enum class UpdateDraftDocumentValidationRule(
    val errorCode: ResultCode,
    val description: String
) : ValidationRule<DocumentServiceContext, List<UpdateDraftDocumentRequest>> {
    DOCUMENT_ID_AND_TYPE_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "documentId와 documentType이 일치 하는지 확인"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<UpdateDraftDocumentRequest>): ValidationResult {
            for (request in target) {
                val checkValid = request.docId.startsWith(request.docType.code)
                if (!checkValid) {
                    return ValidationResult(errorCode, name, "DocumentId:${request.docId} and DocumentType:${request.docType.code} are not matched, by request:${request}")
                }
            }
            return ValidationResult.succeeded()
        }
    },

    DEBIT_CREDIT_BALANCE_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 차대변 합계 금액이 일치 하는지 체크"
    ) {
        override fun validate(context:DocumentServiceContext, target: List<UpdateDraftDocumentRequest>): ValidationResult {
            for (request in target) {
                val totalDebit = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
                val totalCredit = calculateItemTxAmount(AccountSide.CREDIT, null, request.docItems)
                val checkValid = totalDebit.compareTo(totalCredit) == 0
                if (!checkValid) {
                    return ValidationResult(errorCode, name, "Debit amount:${totalDebit} and credit amount:${totalCredit} are not equal, by request:${request}")
                }

            }
            return ValidationResult.succeeded()
        }
    },

    ACCOUNT_CODE_VALIDATION(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 계정이 유효한지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<UpdateDraftDocumentRequest>): ValidationResult {
            for (request in target) {
                val accountKeys = request.docItems.map { it.toAccountKey() }

                val accounts = accountService.getValidAccounts(accountKeys)
                val validAccountKeys = accounts.map { it.accountKey }
                val invalidAccountKeys = accountKeys.filter { !validAccountKeys.contains(it) }

                if (invalidAccountKeys.isNotEmpty()) {
                    val errorMessage = "Invalid accountKeys:${invalidAccountKeys}, by request:${request}"
                    ValidationResult(errorCode, name, errorMessage)
                }
            }
            return ValidationResult.succeeded()
        }
    },

    DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 계정이 허용된 계정유형인지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<UpdateDraftDocumentRequest> ): ValidationResult {
            for (request in target) {
                val documentType = request.docType
                val allowedAccountTypes =documentType.allowAccountTypes
                if ( context.debug ) {
                   logger.trace("DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK, docType:{}, Allowed account types: {}", documentType, allowedAccountTypes)
                }
                val accountKeys = request.docItems.map { it.toAccountKey() }
                val accounts = accountService.getValidAccounts(accountKeys)

                if ( context.debug ) {
                    logger.trace("DOCUMENT_TPE_ALLOWED_ACCOUNT_TYPES_CHECK, accounts: {}", accounts)
                }
                val validAccountKeys = accounts.filter { allowedAccountTypes.contains(it.accountType) }.map { it.accountKey }
                if (validAccountKeys.isEmpty()) {
                    val errorMessage = "validAccountKeys is Empty, by document type:${documentType} and allowed account types:${allowedAccountTypes}, accountKeys:${accountKeys}, request:${request}"
                    ValidationResult(errorCode, name, errorMessage)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var accountService: AccountServiceable
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, requests: List<UpdateDraftDocumentRequest>): List<ValidationResult> {
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }

            val results = entries.map { it.validate(context, requests) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, requests: List<UpdateDraftDocumentRequest>) {
            val results = validateAll(context, requests)
            if (results.isNotEmpty()) {
                throw results.first().toDocumentException()
            }
        }
    }
}

enum class DocumentItemValidationRule(
    val errorCode: ResultCode,
    val description: String
) : ValidationRule<DocumentServiceContext, List<DocumentItemRequest>> {
    CUSTOMER_VENDOR_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "계정코드를 보고 customer, vendor 필수여부 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<DocumentItemRequest>): ValidationResult {
            val accountKeys = target.map { it.toAccountKey() }

            val accounts = accountService.getValidAccounts(accountKeys)
            val validAccountKeys = accounts.map { it.accountKey }

            target.map { request ->
                val accountKey = request.toAccountKey()
                val account = accounts.find { it.accountKey == accountKey }
                if (account == null) {
                    return ValidationResult(errorCode, name, "Account not found by accountKey:${accountKey}, request:${request}")
                }
                try {

                    val accountType = account.accountType

                    val customerIdRequired = accountType.customerIdRequired()
                    val vendorIdRequired = accountType.vendorIdRequired()
                    if (customerIdRequired && request.customerId == null) {
                        return ValidationResult(errorCode, name, "Customer id is required for accountKey:${accountKey}, account type: $accountType, request:${request}")
                    }

                    if (vendorIdRequired && request.vendorId == null) {
                        return ValidationResult(errorCode, name, "Vendor id is required for accountKey:${accountKey}, account type: $accountType, request:${request}")
                    }
                }catch (e: Exception) {
                    logger.error("validation error cached by validationRule:${name}, accountKey:${accountKey}, account:${account}, request:${request}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, accountKey:${accountKey}, account:${account}, request:${request}", e)
                }
            }

            val invalidAccountKeys = accountKeys.filter { !validAccountKeys.contains(it) }

            return if (invalidAccountKeys.isEmpty()) {
                ValidationResult.succeeded()
            } else {
                val errorMessage = "Invalid account keys:${invalidAccountKeys}"
                ValidationResult(errorCode, name, errorMessage)
            }
        }
    },
    DOCUMENT_ITEM_ATTRIBUTE_FIELD_REQUIREMENT_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "계정코드를 보고 customer, vendor 필수여부 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<DocumentItemRequest>): ValidationResult {
            val accountKeys = target.map { it.toAccountKey() }

            val accounts = accountService.getValidAccounts(accountKeys)

            val accountTypes = accounts.map { it.accountType }
            val attributeMasters = documentMasterService.getAllByAccountTypeIn(accountTypes)

            target.map { request ->
                val accountKey = request.toAccountKey()
                val account = accounts.find { it.accountKey == accountKey }
                if (account == null) {
                    return ValidationResult(errorCode, name, "account not found by accountKey:${accountKey}, request:${request}")
                }
                try {
                    val filteredAttributeMasters = attributeMasters.filter { it.accountType == account.accountType }

                    val requiredChecks = filteredAttributeMasters.filter { attributeMaster ->
                        attributeMaster.fieldRequirement == FieldRequirement.REQUIRED
                    }

                    val notAllowedChecks = filteredAttributeMasters.filter { attributeMaster ->
                        attributeMaster.fieldRequirement == FieldRequirement.NOT_ALLOWED
                    }

                    requiredChecks.map { requiredCheck ->
                        val checkValid = request.attributes.any { it.attributeType == requiredCheck.attributeType }
                        if (!checkValid) {
                            return ValidationResult(errorCode, name, "Required attributeType: ${requiredCheck.attributeType} is not found in request with accountType:${account.accountType}, request:${request}")
                        }
                    }

                    notAllowedChecks.map { notAllowedCheck ->
                        val exist = request.attributes.any { it.attributeType == notAllowedCheck.attributeType }
                        if (exist) {
                            return ValidationResult(errorCode, name, "NotAllowed attributeType:${notAllowedCheck.attributeType} must not be found in request with accountType:${account.accountType}, request:${request}")
                        }
                    }
                }catch (e: Exception) {
                    logger.error("Exception cached by validationRule:${name}, accountKey:${accountKey}, account:${account}, request:${request}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule${name}, accountKey:${accountKey}, account:${account}, request:${request}", e)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    DOCUMENT_ITEM_AMOUNT_VALIDATION (
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표 항목의 금액이 0 이상인지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target: List<DocumentItemRequest>): ValidationResult {
            for (item in target) {
                if (item.txAmount <= BigDecimal.ZERO) {
                    return ValidationResult(errorCode, name, "Tx Amount:${item.txAmount} must be greater than 0, by docItemRequest:${item}")
                }
            }
            return ValidationResult.succeeded()
        }
    },


    ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var accountService: AccountServiceable
        private lateinit var documentMasterService: DocumentMasterServiceable
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, target: List<DocumentItemRequest>): List<ValidationResult> {
            // AccountRepository 빈을 직접 주입
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }
            SpringContext.getBean(DocumentMasterServiceable::class.java).let {
                documentMasterService = it
            }

            val results = entries.map { it.validate(context, target) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, target: List<DocumentItemRequest>) {
            val results = validateAll(context, target)
            if (results.isNotEmpty()) {
                throw results.first().toDocumentException()
            }
        }
    }
}



/**
 * 역분개 전표에 대한 검증 룰
 */
enum class ReversingDocumentValidationRule(
    val errorCode: ResultCode,
    val description: String,
) : ValidationRule<DocumentServiceContext, List<ReversingDocumentRequest>> {
    REVERSING_REF_ID_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "역분개 대상 전표의 ID가 유효한지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ReversingDocumentRequest>): ValidationResult {

            val refDocIds = target.map { it.refDocId }
            try {
                val documents = persistenceService.findDocuments(refDocIds)

                val invalidDocIds = target.filter { request -> documents.none { it.id == request.refDocId } }.map { it.refDocId }
                return if (documents.size == target.size) {
                    ValidationResult.succeeded()
                } else {
                    val errorMessage = "Invalid document ids:${invalidDocIds} by refDocIds:${refDocIds}"
                    ValidationResult(errorCode, name, errorMessage )
                }
            }catch (e: Exception) {
                logger.error("Exception cached by validationRule:${name}, refDocId:${target.map { it.refDocId }}", e)
                return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, refDocIds:${refDocIds}", e)
            }
        }
    },
    ALREADY_REVERSING_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "역분개 가능한지 확인 (역분개 대상 전표가 이미 역분개 되었는지 체크)"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ReversingDocumentRequest>): ValidationResult {

            val refDocIds = target.map { it.refDocId }

            try {
                // 역분개 대상 전표가 이미 역분개 되었는지 확인, (역분개 한것과 역분개 된 것 모두 포함해야 함)
                val relations = persistenceService.findDocumentRelations(refDocIds, refDocIds, listOf( RelationType.REVERSING) )

                return if (relations.isEmpty()) {
                    ValidationResult.succeeded()
                } else {
                    val errorMessage = "Already reversed document ids:${relations.map { it.docId }}, by refDocIds:${refDocIds}"
                    ValidationResult(errorCode, name, errorMessage )
                }
            }catch (e: Exception) {
                logger.error("Exception cached by validationRule:${name}, refDocId:${target.map { it.refDocId }}", e)
                return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, refDocIds:${refDocIds}", e)
            }
        }
    },
    ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var persistenceService: DocumentPersistenceService
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, requests: List<ReversingDocumentRequest>): List<ValidationResult> {
            SpringContext.getBean(DocumentPersistenceService::class.java).let {
                persistenceService = it
            }

            val results = entries.map { it.validate(context, requests) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, requests: List<ReversingDocumentRequest>) {
            val results = validateAll(context, requests)
            if (results.isNotEmpty()) {
                throw results.first().toDocumentException()
            }
        }
    }
}



/**
 * 역분개 전표에 대한 검증 룰
 */
enum class ClearingDocumentValidationRule(
    val errorCode: ResultCode,
    val description: String,
) : ValidationRule<DocumentServiceContext, List<ClearingDocumentRequest>> {
    CLEARING_REF_DOC_ID_ACCOUNT_CODE_SAME(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "요청별 참조 항목 ID의 계정과목이 동일한지 체크 (반제는 동일 계정코드에 대해서 전표를 만들어야 함)"
    ) {

        override fun validate(context: DocumentServiceContext, target:List<ClearingDocumentRequest>): ValidationResult {
            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistenceService.findDocumentItems(refDocItemIds)

            target.map { request ->
                try {
                    val distinctRefDocItemIds = request.refDocItemIds.distinct()
                    val curRefDocItems = refDocItems.filter { distinctRefDocItemIds.contains(it.id) }

                    val accountCodes = curRefDocItems.distinctBy { it.accountCode }
                    for ( accountCode in accountCodes ) {

                        val curRefDocItemsWithAccountCode = curRefDocItems.filter { it.accountCode == accountCode.accountCode }

                        val curItemRequests = request.docItems.filter { it.accountCode == accountCode.accountCode }
                        curItemRequests.distinct().let {
                            if (it.size > 1) {
                                return ValidationResult(errorCode, name, "Invalid account codes:${request.docItems}, refDocItems:${refDocItems}, by request:${request}")
                            }
                        }

                        val accountSides = curRefDocItemsWithAccountCode.map { it.accountSide }.distinct()
                        if (accountSides.size > 1) {
                            val requestInfo = curItemRequests.map { "[accountCode:${it.accountCode}, accountSide:${it.accountSide}]"}
                            val referenceInfo = curRefDocItemsWithAccountCode.map { "[docItemId:${it.id}, accountCode:${it.accountCode}, accountSide:${it.accountSide}]"}

                            return ValidationResult(errorCode, name, "Invalid posting types:${requestInfo}, refDocItems:${referenceInfo}, by request:${request}")
                        }
                    }
                }catch (e: Exception) {
                    logger.error("Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}, by request:${request}", e)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    ACCOUNT_CODE_IS_OPEN_ITEM_MGMT_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "계정과목이 Open Item 관리 대상인지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ClearingDocumentRequest>): ValidationResult {

            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistenceService.findDocumentItems(refDocItemIds)

            target.map { request ->
                try {
                    val curRefDocItems = refDocItems.filter { request.refDocItemIds.contains(it.id) }

                    val accountKeys = curRefDocItems.map { it.toAccountKey() }.distinct()
                    val accounts = accountService.getValidAccounts(accountKeys)

                    for (account in accounts) {
                        if (!account.isOpenItemMgmt) {
                            return ValidationResult(errorCode, name, "Account code:${account.accountKey} is open item management flag is disabled, by request:${request}")
                        }
                    }
                }catch (e: Exception) {
                    logger.error("Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}, by request:${request}", e)
                }
            }

            return ValidationResult.succeeded()
        }
    },
    CUSTOMER_ID_OR_VENDOR_IS_SAME_CHECK (
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "CustomerId, VendorId가 동일한지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ClearingDocumentRequest>): ValidationResult {

            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistenceService.findDocumentItems(refDocItemIds)

            target.map { request ->
                val curRefDocItems = refDocItems.filter { request.refDocItemIds.contains(it.id) }
                val accountKeys = curRefDocItems.map { it.toAccountKey() }.distinct()
                try {
                    val accounts = accountService.getValidAccounts(accountKeys)
                    for ( account in accounts ) {
                        val accountKey = account.accountKey
                        val isCustomerIdRequired = account.accountType.customerIdRequired()
                        val isVendorIdRequired = account.accountType.vendorIdRequired()

                        if ( isCustomerIdRequired ) {
                            val customerIds = request.docItems.filter { it.toAccountKey() == accountKey }.map { it.customerId }.distinct()
                            if ( customerIds.size > 1 ) {
                                return ValidationResult(errorCode, name, "CustomerIds:${customerIds} are not same, by request:${request}")
                            }
                        }

                        if ( isVendorIdRequired ) {
                            val vendorIds = request.docItems.filter { it.toAccountKey() == accountKey }.map { it.vendorId }.distinct()
                            if ( vendorIds.size > 1 ) {
                                return ValidationResult(errorCode, name, "VendorIds:${vendorIds} are not same, by request:${request}")
                            }
                        }
                    }
                }catch (e: Exception) {
                    logger.error("Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, refDocItemIds:${request.refDocItemIds}, by request:${request}", e)
                }
            }

            return ValidationResult.succeeded()
        }
    },
    DEBIT_CREDIT_BALANCE_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "전표의 차대변 합계 금액이 일치 하는지 체크"
    ) {
        override fun validate(context:DocumentServiceContext, target: List<ClearingDocumentRequest>): ValidationResult {
            target.map { request ->
                val totalDebit = calculateItemTxAmount(AccountSide.DEBIT, null, request.docItems)
                val totalCredit = calculateItemTxAmount(AccountSide.CREDIT, null, request.docItems)
                val checkValid = totalDebit.compareTo(totalCredit) == 0
                if (!checkValid) {
                    return ValidationResult(errorCode, name, "Debit amount:${totalDebit} and credit amount:${totalCredit} are not equal, by request:${request}")
                }
            }

            return ValidationResult.succeeded()
        }
    },
    ALREADY_REVERSED_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "이미 역분개된 전표인지 체크"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ClearingDocumentRequest>): ValidationResult {

            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistenceService.findDocumentItems(refDocItemIds)
            val refDocIds = persistenceService.findDocuments(refDocItems.map { it.docId }).map { it.id }

            // 역분개 된 상황 확인 (역분개한, 역분개된 모두 점검)
            persistenceService.findDocumentRelations(refDocIds, refDocIds, listOf( RelationType.REVERSING) ).let {
                if (it.isNotEmpty()) {
                    return ValidationResult(errorCode, name, "Already reversed document ids:${it.map { it.docId }}, by refDocItemIds:${refDocItemIds}")
                }
            }
            return ValidationResult.succeeded()
        }
    },
    ALREADY_CLEARED_CHECK(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "이미 반제된 전표인지 체크 (역분개는 안된 상황, 위에서 점검)"
    ) {
        override fun validate(context: DocumentServiceContext, target:List<ClearingDocumentRequest>): ValidationResult {

            // 역분개는 아닌 조건에서

            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()

            try {
                val candidates = documentRelationServiceable.getEffectiveClearingDocumentItemRelations(context, refDocItemIds).distinct()
                logger.debug("Effective clearing document item ids: {}", candidates.map { it.docItemId })
            }catch (e: Exception) {
                return ValidationResult(errorCode, name, e.message ?: "Already cleared document item ids:${refDocItemIds}")
            }

            return ValidationResult.succeeded()
        }
    },

    CLEARING_AMOUNT_CHECK (
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "계정 과목별 반제 대상금액과 반제금액이 초과하지 않는지 체크"
    ) {
        override fun validate(context:DocumentServiceContext, target: List<ClearingDocumentRequest>): ValidationResult {
            val refDocItemIds = target.map { it.refDocItemIds }.flatten().distinct()
            val refDocItems = persistenceService.findDocumentItems(refDocItemIds)

            // 이미 부분반제한 관계를 조회
            val alreadyPartialClearingRelations = documentRelationServiceable.getEffectiveClearingDocumentItemRelations(context, refDocItemIds)
            val alreadyPartialClearingDocItems = persistenceService.findDocumentItems(alreadyPartialClearingRelations.map { it.docItemId })


            target.map { request ->
                val curRefDocItems = refDocItems.filter { request.refDocItemIds.contains(it.id) }

                // 이미 반제된 금액을 계산
                // 1) 참조전표항목ID에 반제한 전표항목(partial clearing, active)한 항목을 추출
                val curAlreadyPartialClearingRelations = alreadyPartialClearingRelations.filter { request.refDocItemIds.contains(it.refDocItemId) }
                val curAlreadyPartialClearingDocItemIds = curAlreadyPartialClearingRelations.map { it.docItemId }
                val curAlreadyPartialClearingDocItems = alreadyPartialClearingDocItems.filter { curAlreadyPartialClearingDocItemIds.contains(it.id) }


                // 위의 Validation Rule에서 유일함을 보장해 줌
                val candidateAccountCode = curRefDocItems.map { it.accountCode }.first()
                val candidateaccountSide = curRefDocItems.map { it.accountSide }.first()

                val candidateAmount = curRefDocItems.filter { request.refDocItemIds.contains(it.id) }.sumOf { it.txMoney.amount }
                val alreadyPartialClearingAmount = curAlreadyPartialClearingDocItems.filter { it.accountCode == candidateAccountCode && it.accountSide == candidateaccountSide }.sumOf { it.txMoney.amount }

                val clearingAmount = calculateItemTxAmount(candidateaccountSide.reverse(), candidateAccountCode, request.docItems)

                if (candidateAmount < (alreadyPartialClearingAmount + clearingAmount) ) {
                    return ValidationResult(errorCode, name,
                        "Reference $candidateaccountSide amount:${candidateAmount}, already partial clearing amount:${alreadyPartialClearingAmount}" +
                                ", clearing amount:${clearingAmount}, reference amount must be greater than clearing amount, by request:${request}")
                }
            }

            return ValidationResult.succeeded()
        }
    },

    ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var accountService: AccountServiceable
        private lateinit var documentRelationServiceable: DocumentRelationServiceable
        private lateinit var persistenceService: DocumentPersistenceService
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, requests: List<ClearingDocumentRequest>): List<ValidationResult> {
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }
            SpringContext.getBean(DocumentPersistenceService::class.java).let {
                persistenceService = it
            }
            SpringContext.getBean(DocumentRelationServiceable::class.java).let {
                documentRelationServiceable = it
            }

            val results = entries.map { it.validate(context, requests) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, requests: List<ClearingDocumentRequest>) {
            val results = validateAll(context, requests)
            if (results.isNotEmpty()) {
                throw results.first().toDocumentException()
            }
        }
    }

}


enum class LookupRefDocItemValidationRule(
    val errorCode: ResultCode,
    val description: String,
) : ValidationRule<DocumentServiceContext, List<LookupRefDocItemRequest>> {
    REQUEST_IS_VALID(
        errorCode = ResultCode.DOCUMENT_VALIDATION_ERROR,
        description = "요청이 유효한지 확인"
    ) {

        override fun validate(context: DocumentServiceContext, target:List<LookupRefDocItemRequest>): ValidationResult {

            val docTemplates = documentTemplateServiceable.findDocTemplates(target.map { it.toDocTemplateKey() })
            val docTemplateItems = documentTemplateServiceable.findDocTemplateItems( target.map{it.toDocTemplateKey()} )
            val accounts = accountService.getValidAccounts(target.map { it.toAccountKey() })

            target.map { request ->
                val docTemplateKey = request.toDocTemplateKey()
                val docTemplate = docTemplates.find { it.docTemplateKey == docTemplateKey }
                if (docTemplate == null) {
                    return ValidationResult(errorCode, name, "Invalid docTemplateKey:${docTemplateKey}, by request:${request}")
                }
                if ( docTemplateKey != docTemplate.docTemplateKey ) {
                    return ValidationResult(errorCode, name, "DocTemplate not found by docTemplateKey:${docTemplateKey}, by request:${request}")
                }
                val docTemplateCode = request.docTemplateCode
                try {

                    if ( docTemplate.documentType != request.docType ) {
                        return ValidationResult(errorCode, name, "Invalid docType:${request.docType} by docTemplateKey:${docTemplateKey}, request:${request}")
                    }

                    val curDocTemplateItems = docTemplateItems
                        .filter { it.docTemplateKey == docTemplateKey }
                        .filter { it.accountSide == request.accountSide }
                        .filter { it.accountCode == request.accountCode }

                    val curDocTemplate = docTemplates.firstOrNull{ it.docTemplateKey == docTemplateKey }
                    require(curDocTemplate != null) { "curTemplate must not be null by docTemplateCode:${request.docTemplateCode}" }

                    val accountKey = AccountKey.of(curDocTemplate.docTemplateKey.companyCode, request.accountCode)
                    require (accountKey == request.toAccountKey()){ "accountKey must be same, but accountKey:${accountKey} by templateCode: ${docTemplateCode}, accountKey:${request.toAccountKey()} by request, request:${request}" }

                    val curAccounts = accounts.filter { it.accountKey == request.toAccountKey() }
                    if ( curAccounts.isEmpty() ){
                        return ValidationResult(errorCode, name, "Invalid account code:${request.accountCode}")
                    }
                    val curAccountKeys = curAccounts.map { it.accountKey }

                    // Template Code에서 CompanyCode를 추가해야 함
                    val filteredTemplatedItems = curDocTemplateItems.filter { curAccountKeys.contains(accountKey) }
                    if (filteredTemplatedItems.isEmpty()) {
                        return ValidationResult(errorCode, name, "templateItems not found by  accountKey: $accountKey by docTemplateCode:${request.docTemplateCode}")
                    }

                    val matchedAccount = curAccounts.firstOrNull { account -> filteredTemplatedItems.map { it.accountCode }.contains(account.accountKey.accountCode) }
                    if (matchedAccount == null) {
                        return ValidationResult(errorCode, name, "matched account not found, by account code:${request.accountCode}, docTemplateCode:${docTemplateCode}, request:${request}")
                    }

                    val isCustomerIdRequired = matchedAccount.accountType.customerIdRequired()
                    val isVendorIdRequired = matchedAccount.accountType.vendorIdRequired()
                    if ( isCustomerIdRequired && request.customerId == null ) {
                        return ValidationResult(errorCode, name, "CustomerId is required for account code:${request.accountCode}")
                    }
                    if ( isVendorIdRequired && request.vendorId == null ) {
                        return ValidationResult(errorCode, name, "VendorId is required for account code:${request.accountCode}")
                    }

                }catch (e: Exception) {
                    logger.error("Exception cached by validationRule:${name}, docTemplateKey:${docTemplate.docTemplateKey}, request:${request}", e)
                    return ValidationResult(errorCode, name, "Exception cached by validationRule:${name}, templateCode:${docTemplateCode} request:${request}", e)
                }
            }
            return ValidationResult.succeeded()
        }
    },
    ;

    companion object    {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private lateinit var accountService: AccountServiceable
        private lateinit var documentTemplateServiceable: DocumentTemplateServiceable
        /**
         * 전체 룰 검증, 오류만 리턴
         */
        fun validateAll(context: DocumentServiceContext, target: List<LookupRefDocItemRequest>): List<ValidationResult> {

            SpringContext.getBean(DocumentTemplateServiceable::class.java).let {
                documentTemplateServiceable = it
            }
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }

            val results = entries.map { it.validate(context, target) }
            return results.filter { !it.isSucceeded() }
        }

        /**
         * 전체 룰 검증, 첫번째 오류만 리턴
         */
        fun validate(context: DocumentServiceContext, target: List<LookupRefDocItemRequest>) {
            val results = validateAll(context, target)
            if (results.isNotEmpty()) {
                throw results.first().toDocumentException()
            }
        }
    }

}

