package com.abc.us.accounting.iface.domain.repository.oms

import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.iface.domain.type.oms.IfContractStatus
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemStatus
import com.abc.us.accounting.iface.domain.type.oms.IfOrderItemType
import com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface IfOrderItemRepository: JpaRepository<IfOrderItem, String> {
    @Query("""
        SELECT
            if_oi
        FROM
            IfOrderItem if_oi
        WHERE
            if_oi.updateTime BETWEEN :startTime AND :endTime
            AND if_oi.orderItemType IN :orderItemTypes
            AND if_oi.orderItemStatus IN :orderItemStatuses
    """)
    fun findAllByTimeRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        orderItemTypes: List<IfOrderItemType> = IfOrderItemType.entries,
        orderItemStatuses: List<IfOrderItemStatus> = IfOrderItemStatus.entries,
        pageable: Pageable = Pageable.unpaged()
    ): Slice<IfOrderItem>

    @Query("""
        SELECT
            if_oi
        FROM
            IfOrderItem if_oi
        WHERE
            if_oi.updateTime BETWEEN :startTime AND :endTime
            AND if_oi.orderItemId IN :orderItemIds
            AND if_oi.orderItemType IN :orderItemTypes
            AND if_oi.orderItemStatus IN :orderItemStatuses
    """)
    fun findAllByTimeRangeAndOrderItemIds(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        orderItemIds: List<String>,
        orderItemTypes: List<IfOrderItemType> = IfOrderItemType.entries,
        orderItemStatuses: List<IfOrderItemStatus> = IfOrderItemStatus.entries
    ): List<IfOrderItem>

    @Query("""
        SELECT
            if_oi
        FROM
            IfOrderItem if_oi
        WHERE
            if_oi.orderItemId IN :orderItemIds
    """)
    fun findByOrderItemIdsIn(
        orderItemIds: List<String>
    ): List<IfOrderItem>

    @Query("""
        SELECT
            if_oi
        FROM
            IfOrderItem if_oi
        WHERE
            if_oi.orderId IN :orderIds
    """)
    fun findByOrderIdsIn(
        orderIds: List<String>
    ): List<IfOrderItem>

    @Query("""
        SELECT if_oi
        FROM IfOrderItem if_oi
        WHERE if_oi.orderId IN :orderIds
        AND NOT EXISTS (
            SELECT 1 
            FROM IfOrderItem newer 
            WHERE newer.orderId = if_oi.orderId
            AND newer.id > if_oi.id
        )
""")
    fun findDistinctByOrderIdsIn(orderIds: List<String>): List<IfOrderItem>
}


@Repository
interface IfOnetimePaymentRepository : JpaRepository<IfOnetimePayment, String>{

    @Query("""
    SELECT
        op
    FROM
        IfOnetimePayment op
    WHERE
        op.updateTime BETWEEN :startTime AND :endTime
        AND (
            (:isRefund = true AND op.refunds is not null)
            OR
            (:isRefund = false AND op.refunds is null)
        )
        AND NOT EXISTS(
            SELECT 1 FROM IfOnetimePayment op2
            WHERE op2.paymentId = op.paymentId 
            AND (
                (:isRefund = true AND op2.refunds is not null)
                OR
                (:isRefund = false AND op2.refunds is null)
            )
            AND op2.id > op.id
        )
""")
    fun findOnetimePayments(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        isRefund: Boolean = false,
        pageable: Pageable = Pageable.unpaged()
    ): Slice<IfOnetimePayment>
}


@Repository
interface IfChannelRepository: JpaRepository<IfChannel, String> {
    @Query("""
        SELECT
            if_c
        FROM
            IfChannel if_c
        WHERE
            if_c.channelId IN :channelIds
    """)
    fun findByChannelIdIn(
        channelIds: List<String>
    ): List<IfChannel>
}

@Repository
interface IfContractRepository: JpaRepository<IfContract, String> {
    @Query("""
        SELECT
            if_c
        FROM
            IfContract if_c
        WHERE
            if_c.orderItemId IN :orderItemIds
            AND if_c.contractStatus IN :contractStatuses
            AND if_c.rentalCode IN (
                SELECT
                    rentalCode
                FROM
                    RentalCodeMaster rcm
                WHERE
                    rcm.isActive = true
                    AND rcm.leaseType IN :leaseTypes
                    AND rcm.currentTerm = 1
            )
    """)
    fun findAllByOrderItemIds(
        orderItemIds: List<String>,
        contractStatuses: List<IfContractStatus> = IfContractStatus.entries,
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): List<IfContract>

    @Query("""
        SELECT
            if_c
        FROM
            IfContract if_c
        WHERE
            if_c.contractId IN (
                SELECT
                    contractId
                FROM
                    IfCharge
                WHERE
                    chargeId IN :chargeIds
            )
            AND if_c.contractStatus IN :contractStatuses
    """)
    fun findAllByChargeIds(
        chargeIds: List<String>,
        contractStatuses: List<IfContractStatus> = IfContractStatus.entries,
    ): List<IfContract>
}

@Repository
interface IfServiceFlowRepository: JpaRepository<IfServiceFlow, String> {
    @Query("""
        SELECT
            if_sf
        FROM
            IfServiceFlow if_sf
        WHERE
            if_sf.serviceType IN :serviceTypes
            AND if_sf.serviceStatus IN :serviceStatuses
            AND (:serialNumbers IS NULL OR if_sf.serialNumber IN :serialNumbers)
    """)
    fun findBy(
        serviceTypes: List<com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType> = com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.entries,
        serviceStatuses: List<IfServiceFlowStatus> = IfServiceFlowStatus.entries,
        serialNumbers: List<String>? = null
    ): List<IfServiceFlow>

    @Query("""
        SELECT
            if_sf
        FROM
            IfServiceFlow if_sf
        WHERE
            if_sf.serviceType IN :serviceTypes
            AND if_sf.serviceStatus IN :serviceStatuses
            AND if_sf.orderItemId IN :orderItemIds
    """)
    fun findByOrderItemIdIn(
        orderItemIds: List<String>,
        serviceTypes: List<com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType> = com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.entries,
        serviceStatuses: List<IfServiceFlowStatus> = IfServiceFlowStatus.entries
    ): List<IfServiceFlow>

    @Query("""
        SELECT
            if_sf
        FROm
            IfServiceFlow if_sf
        WHERE
            if_sf.orderItemId IN :orderItemIds
            AND FUNCTION('TO_CHAR', if_sf.createTime, 'YYYY') != :year
            AND FUNCTION('TO_CHAR', if_sf.createTime, 'MM') = :month
    """)
    fun findSameMonth(
        orderItemIds: List<String>,
        year: String,
        month: String
    ): List<IfServiceFlow>
}

@Repository
interface IfChargeRepository: JpaRepository<IfCharge, String> {
    @Query("""
        SELECT
            if_c
        FROM
            IfCharge if_c
        WHERE
            if_c.chargeId IN :chargeIds
    """)
    fun findAllByIds(
        chargeIds: List<String>
    ): List<IfCharge>

    @Query("""
        SELECT
            if_c
        FROM
            IfCharge if_c
        JOIN
            IfContract if_c2
        ON
            if_c.contractId = if_c2.contractId
        WHERE
            if_c.targetMonth = :targetMonth
            AND if_c2.rentalCode IN (
                SELECT
                    rentalCode
                FROM
                    RentalCodeMaster
                WHERE
                    isActive = true
                    AND leaseType IN :leaseTypes
            )
            AND (
                :chargeIds IS NULL OR
                if_c.chargeId IN :chargeIds
            )
    """)
    fun findByTargetMonth(
        targetMonth: String,
        leaseTypes: List<LeaseType> = LeaseType.entries,
        chargeIds: List<String>? = null
    ): List<IfCharge>
}

@Repository
interface IfChargeItemRepository: JpaRepository<IfChargeItem, String> {
    @Query("""
        SELECT
            if_ci
        FROM
            IfChargeItem if_ci
        WHERE
            if_ci.chargeId IN :chargeIds
    """)
    fun findByChargeIds(
        chargeIds: List<String>
    ): List<IfChargeItem>
}

@Repository
interface IfChargeInvoiceRepository: JpaRepository<IfChargeInvoice, String> {
    @Query("""
        SELECT
            if_ci
        FROM
            IfChargeInvoice if_ci
        WHERE
            if_ci.chargeId IN :chargeIds
    """)
    fun findByChargeIds(
        chargeIds: List<String>
    ): List<IfChargeInvoice>
}

@Repository
interface IfInvoiceRepository: JpaRepository<IfInvoice, String> {
    @Query("""
        SELECT
            if_i
        FROM
            IfInvoice if_i
        WHERE
            if_i.invoiceId IN :invoiceIds
    """)
    fun findByIds(
        invoiceIds: List<String>
    ): List<IfInvoice>
}

@Repository
interface IfChargePaymentRepository: JpaRepository<IfChargePayment, String> {
    @Query("""
        SELECT
            if_cp
        FROM
            IfChargePayment if_cp
        WHERE
            if_cp.paymentTime BETWEEN :startTime AND :endTime
            AND if_cp.chargeId IN (
                SELECT
                    if_c.chargeId
                FROM
                    IfCharge if_c
                WHERE
                    if_c.contractId IN (
                        SELECT
                            if_c2.contractId
                        FROM
                            IfContract if_c2
                        WHERE
                            if_c2.rentalCode IN (
                                SELECT
                                    rentalCode
                                FROM
                                    RentalCodeMaster
                                WHERE
                                    isActive = true
                                    AND leaseType IN :leaseTypes
                            )
                    )
            )
    """)
    fun findAllByTimeRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): List<IfChargePayment>
}