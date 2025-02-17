package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.collects.domain.entity.collect.CollectChannel
import com.abc.us.accounting.payouts.domain.entity.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepository : JpaRepository<Employee, String> {

    fun findAllByIsActiveTrue(): List<Employee>
}