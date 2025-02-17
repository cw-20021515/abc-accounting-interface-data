package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DepositsRepository: JpaRepository<RentalAssetHistory, Int> {
    @Query("""
        select
            cr.receipt_time as receipt_date,
            cr.receipt_id,
            cr.billing_type as receipt_type,
            cc.charge_status as receipt_status,
            null as receipt_provider,
            cr.receipt_method,
            cd.currency,
            cr.total_price as receipt_amount,
            cd.fees as fee,
            cd.amount as deposit_amount,
            (cr.total_price - cast(cd.amount as decimal)) as difference_amount,
            cd.deposit_date,
            cd.deposit_id,
            cr.charge_id as bill_id,
            cc2.order_id,
            cc2.customer_id,
            null as reference_id,
            null as reference_type,
            null as remark
        from
            collect_deposit cd
        join
            collect_receipt cr
        on
            cd.deposit_id = cr.deposit_id
        join
            collect_charge cc
        on
            cr.charge_id = cc.charge_id
        join
            collect_contract cc2
        on
            cc.contract_id = cc2.contract_id
        where
            (
                (
                    :periodType = 'RECEIPT_DATE' -- 수납일자
                    and date(cr.receipt_time) between :periodFromDate and :periodToDate
                )
                or (
                    :periodType = 'DEPOSIT_DATE' -- 입금일자
                    and cd.deposit_date between :periodFromDate and :periodToDate
                )
            )
            and (
                :receiptType is null
                or cr.billing_type = :receiptType
            )
            and (
                :receiptStatus is null
                or cc.charge_status = :receiptStatus
            )
            and (
                :receiptMethod is null
                or cr.receipt_method = :receiptMethod
            )
            and (
                :orderId is null
                or cc2.order_id = :orderId
            )
            and (
                :customerId is null
                or cc2.customer_id = :customerId
            )
            and (
                :depositId is null
                or cd.deposit_id = :depositId
            )
    """, nativeQuery = true)
    fun findByReq(
        @Param("periodType") periodType: String? = null,
        @Param("periodFromDate") periodFromDate: LocalDate? = null,
        @Param("periodToDate") periodToDate: LocalDate? = null,
        @Param("receiptType") receiptType: String? = null,
        @Param("receiptStatus") receiptStatus: String? = null,
        @Param("receiptMethod") receiptMethod: String? = null,
        @Param("orderId") orderId: String? = null,
        @Param("customerId") customerId: String? = null,
        @Param("depositId") depositId: String? = null,
        pageable: Pageable? = null
    ): Page<Map<String, Any>>
}