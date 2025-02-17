package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.CostCenter
import com.abc.us.accounting.payouts.domain.entity.Employee
import com.abc.us.accounting.payouts.domain.entity.Vendor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VendorRepository : JpaRepository<Vendor, String> {
    fun findAllByIsActiveTrue(): List<Vendor>
}