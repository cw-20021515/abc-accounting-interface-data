package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.DocumentApprovalRule
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentApprovalRuleRepository: JpaRepository<DocumentApprovalRule, Long> {
    fun findAllByIsActive(isActive: Boolean=true): MutableList<DocumentApprovalRule>

    fun findAllByIsActiveOrderByPriorityDesc(isActive: Boolean=true): List<DocumentApprovalRule>
}