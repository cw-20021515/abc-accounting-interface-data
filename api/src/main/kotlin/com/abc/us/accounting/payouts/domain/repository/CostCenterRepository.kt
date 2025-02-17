package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.CostCenter
import com.abc.us.accounting.payouts.domain.entity.PayoutAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CostCenterRepository : JpaRepository<CostCenter, String> {
    fun findByParentIdIsNull(): List<CostCenter>
    fun findByParentId(parentId: String): List<CostCenter>
}