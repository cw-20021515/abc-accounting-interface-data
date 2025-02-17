package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.ApprovalRuleCondition
import com.abc.us.accounting.documents.domain.entity.DocumentApprovalRule
import com.abc.us.accounting.documents.domain.repository.DocumentApprovalRuleRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.model.HashableDocumentRequest
import com.abc.us.accounting.supports.utils.Range
import mu.KotlinLogging
import org.springframework.stereotype.Service

interface DocumentApprovalRuleServiceable {
    fun evaluate(requests: List<HashableDocumentRequest>): List<EvaluateResult>
    fun evaluate(request: HashableDocumentRequest): EvaluateResult
}

@Service
class DocumentApprovalRuleService (
    private val ruleRepository : DocumentApprovalRuleRepository,
    private val accountService: AccountService,

    private val cachedRules:MutableList<DocumentApprovalRule> = mutableListOf(),
) : DocumentApprovalRuleServiceable{
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        initialize()
    }

    private final fun initialize() {
        logger.info("initialize loading document approval rules, approval rules data for cache")
        if (cachedRules.isEmpty()) {
            cachedRules.clear()
            cachedRules.addAll(ruleRepository.findAllByIsActive())
            cachedRules.sortByDescending { it.priority }
        }
        logger.info("initialize document approval rules:${cachedRules.size} data done")
    }

    fun createRules(rules:List<DocumentApprovalRule>):List<DocumentApprovalRule> {
        logger.info("creating rules for ${rules.size} approvals")

        val saved = ruleRepository.saveAll(rules)

        if ( saved.isNotEmpty() ) {
            saved.filter { it.isActive }.forEach { rule ->
                cachedRules.add(rule)
            }
            cachedRules.sortByDescending{it.priority}
        }
        return saved
    }


    fun createRule(rule:DocumentApprovalRule):DocumentApprovalRule {
        logger.info("creating rule for ${rule} approvals")
        val saved = ruleRepository.save(rule)
        if ( saved.isActive ) {
            cachedRules.add(saved)
            cachedRules.sortByDescending{it.priority}
        }
        return saved
    }

    /**
     * for test
     */
    fun cleanup() {
        logger.info("cleanup document appproval rules")
        cachedRules.clear()
        ruleRepository.deleteAll()
    }

    fun listRules(): List<DocumentApprovalRule> {
        if (cachedRules.isNotEmpty()) {
            return cachedRules
        }

        return ruleRepository.findAllByIsActiveOrderByPriorityDesc()
    }

    /**
     * 전표 승인이 필요한지 판단
     * 개별 전표중에 1개라도 승인이 필요하면 true
     */
    override fun evaluate (requests: List<HashableDocumentRequest>): List<EvaluateResult> {
        val evaluates = requests.map { request ->
            evaluate(request)
        }
        return evaluates
    }

    /**
     * 전표 승인이 필요한지 판단
     * Approval Rule이 적합한지 판단해서 승인피 필요하면 true, 그렇지 않으면 false를 리턴
     * 1) 조건을 만족하는 규칙이 없으면 승인이 필요없음
     * 2) 조건을 만족하는 첫번째 규칙의 requireApprovals 조건을 따라감
     */
    override fun evaluate(request: HashableDocumentRequest): EvaluateResult {
        val approvalRules = evaluateApprovalRules(request)
        val approvalRule = approvalRules.firstOrNull() ?: return EvaluateResult(request, false)
        return EvaluateResult(request, approvalRule.requiresApproval, approvalRule)
    }

    private fun evaluateApprovalRules(request: HashableDocumentRequest): List<DocumentApprovalRule> {
        val activeRules = listRules()
        return activeRules.filter { rule -> matchesRule(rule, request) }
    }

    /**
     * 승인조건 확인
     */
    private fun matchesRule(rule: DocumentApprovalRule, request: HashableDocumentRequest): Boolean {
        return rule.conditions.all { condition ->
            matchRule(rule.companyCode, condition, request)
        }
    }

    private fun matchRule (companyCode: CompanyCode?, condition: ApprovalRuleCondition, request: HashableDocumentRequest): Boolean {
        val companyCodes = if (companyCode == null) CompanyCode.entries else listOf(companyCode)

        val bizSystemType = request.docOrigin?.bizSystem
        val docTemplateCode = request.docOrigin?.docTemplateCode
        val accountCodes = request.docItems.map { it.accountCode }.distinct()

        val accountTypes = accountService.getValidAccounts(companyCodes, accountCodes).map { it.accountType }.distinct()
        val amounts = request.docItems.map { it.txAmount }.distinct()
        val costCenters = request.docItems.map { it.costCenter }.distinct().toSet()
        val createdBy = request.createdBy

        // 모든 조건이 만족 되어야 함
        if ( !companyCodes.contains(request.companyCode) ) return false

        if ( !condition.bizSystemTypes.isNullOrEmpty() && (bizSystemType != null && condition.bizSystemTypes!!.contains(bizSystemType.name))) return false

        if ( !condition.docTemplateCodes.isNullOrEmpty() && (docTemplateCode!= null && condition.docTemplateCodes!!.contains(docTemplateCode.name)) ) return false

        if ( condition.accountCodeRange != null && !isInRange(condition.accountCodeRange, accountCodes) ) return false

        if ( condition.amountRange != null && !isInRange(condition.amountRange, amounts)) return false

        // TODO: 추후 부서코드와 코스트센터 간의 관계 정립 필요
        if ( !condition.departments.isNullOrEmpty() && condition.departments!!.intersect(costCenters).isEmpty() ) return false

        if ( !condition.costCenters.isNullOrEmpty() && condition.costCenters!!.intersect(costCenters).isEmpty() ) return false

        if ( !condition.users.isNullOrEmpty() && condition.users!!.contains(createdBy) ) return false

        if ( !condition.documentTypes.isNullOrEmpty() && condition.documentTypes!!.contains(request.docType.name) ) return false

        if ( !condition.accountTypes.isNullOrEmpty() && condition.accountTypes!!.intersect(accountTypes).isEmpty() ) return false

        return true
    }

    private fun <T : Comparable<T>> isInRange(range: Range<T>, value: T): Boolean =
        range.isInRange(value)

    private fun <T : Comparable<T>> isInRange(range: Range<T>, values: List<T>): Boolean {
        return values.any { isInRange(range, it) }
    }
}

data class EvaluateResult (
    val request: HashableDocumentRequest,
    val result: Boolean,
    val approvalRule: DocumentApprovalRule? = null,
)