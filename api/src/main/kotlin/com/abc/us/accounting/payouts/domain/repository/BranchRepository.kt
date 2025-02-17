package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.Branch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BranchRepository : JpaRepository<Branch, String> {

    fun findByCompanyCodeAndName(companyCode: String, name: String): Branch?

}