package com.abc.us.accounting.iface.domain.repository.oms

import com.abc.us.accounting.iface.domain.entity.oms.IfCustomer
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerType
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface IfCustomerRepository: JpaRepository<IfCustomer, String> {


    @Query(
        value = """
            SELECT * 
            FROM if_customer
            WHERE customer_id IN :customerIds
        """,
        nativeQuery = true
    )
    fun findActiveByCustomerIds(customerIds: List<String>): List<IfCustomer>

    fun findByCustomerId(customerId : String) : IfCustomer?

    @Query(
        value = """
            SELECT DISTINCT ON(customer_id) * 
            FROM if_customer
            WHERE create_time BETWEEN :startTime AND :endTime
        """,
        nativeQuery = true
    )
    fun findActiveCustomerWithinCreateTimeRange(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): MutableList<IfCustomer>?

    fun findAllByAccountType(accountType: IfCustomerType): List<IfCustomer>
}
