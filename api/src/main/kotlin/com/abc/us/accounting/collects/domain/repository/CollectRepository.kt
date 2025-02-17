package com.abc.us.accounting.collects.domain.repository

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.type.ChargeStatusEnum
import com.abc.us.accounting.collects.domain.type.MaterialType
import com.abc.us.accounting.logistics.domain.type.InventoryAssetGradeType
import com.abc.us.accounting.logistics.domain.type.MovementType
import com.abc.us.accounting.rentals.master.domain.type.*
import com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderItemWithExtraInfo
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime


@Repository
interface CollectMaterialRepository: JpaRepository<CollectMaterial, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectMaterial>

    @Query("SELECT m from CollectMaterial m WHERE m.isActive = true AND m.materialId in :materialIds")
    fun findAllByMaterialIdIn(materialIds: List<String>): List<CollectMaterial>

    @Query("SELECT m from CollectMaterial m WHERE m.isActive = true AND m.materialType in :materialTypes")
    fun findAllByMaterialTypeIn(materialTypes: List<MaterialType>): List<CollectMaterial>
}


@Repository
interface CollectChargeItemRepository : JpaRepository<CollectChargeItem, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectChargeItem>
    @Query("SELECT i FROM CollectChargeItem i WHERE i.isActive = true AND i.chargeItemId = :chargeItemId")
    fun findByChargeItemIdAndIsActive(@Param("chargeItemId") chargeItemId: String): CollectChargeItem?

    @Query("""
        SELECT
            cci
        FROM
            CollectChargeItem cci
        WHERE
            cci.chargeId IN :chargeIds
            AND cci.isActive = true
    """)
    fun findByChargeIds(
        chargeIds: List<String>
    ): List<CollectChargeItem>
}

@Repository
interface CollectReceiptRepository : JpaRepository<CollectReceipt, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectReceipt>
    @Query("SELECT i FROM CollectReceipt i WHERE i.isActive = true AND i.receiptId = :receiptId")
    fun findByReceiptIdAndIsActive(@Param("receiptId") receiptId: String): CollectReceipt?

    @Query(
        value = """
            SELECT
                cr
            FROM
                CollectReceipt cr
            WHERE
                cr.receiptTime BETWEEN :startTime AND :endTime
                AND cr.isActive = true
                AND cr.chargeId IN (
                    SELECT
                        cc.chargeId
                    FROM
                        CollectCharge cc
                    WHERE
                        cc.contractId IN (
                            SELECT
                                cc2.contractId
                            FROM
                                CollectContract cc2
                            WHERE
                                cc2.rentalCode IN (
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
        """
    )
    fun findAllByTimeRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): MutableList<CollectReceipt>

    @Query(
        """
            SELECT
                cr
            FROM
                CollectReceipt cr
            WHERE
                cr.transactionId IN :transactionIds
        """
    )
    fun findAllByTransactionIds(
        transactionIds: List<String>
    ): MutableList<CollectReceipt>
}

@Repository
interface CollectTaxLineRepository : JpaRepository<CollectTaxLine, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectTaxLine>
    @Query("SELECT i FROM CollectTaxLine i WHERE i.isActive = true")
    fun findAllActive() : MutableList<CollectTaxLine>?


    @Query(
        value = """
            SELECT t
            FROM CollectTaxLine t
            WHERE t.relation.field = 'order_item_id' and t.relation.value IN :orderItemIds and t.isActive = true
        """
    )
    fun findByOrderItemIdIn(orderItemIds: List<String>): List<CollectTaxLine>
}

@Repository
interface CollectChargeRepository : JpaRepository<CollectCharge, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectCharge>

    @Query(
        """
        SELECT ar
        FROM CollectCharge ar
        WHERE ar.chargeId = :chargeId AND ar.isActive = true
      """
    )
    fun findByChargeIdAndIsActive(@Param("chargeId") chargeId: String): CollectCharge?

    @Query(
        """
            SELECT
                cc
            FROM
                CollectCharge cc
            WHERE
                cc.chargeId IN :chargeIds
        """
    )
    fun findAllByIds(
        chargeIds: List<String>
    ): MutableList<CollectCharge>

    @Query("""
        SELECT
            cc
        FROM
            CollectCharge cc
        JOIN
            CollectContract cc2
        ON
            cc.contractId = cc2.contractId
        WHERE
            cc.targetMonth = :targetMonth
            AND cc.chargeStatus IN :chargeStatuses
            AND cc2.rentalCode IN (
                SELECT
                    rentalCode
                FROM
                    RentalCodeMaster
                WHERE
                    isActive = true
                    AND leaseType IN :leaseTypes
            )
    """)
    fun findByTargetMonth(
        targetMonth: String,
        chargeStatuses: List<ChargeStatusEnum> = ChargeStatusEnum.entries,
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): MutableList<CollectCharge>
}
@Repository
interface CollectVendorRepository : JpaRepository<CollectVendor, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectVendor>
}

@Repository
interface CollectOrderRepository : JpaRepository<CollectOrder,String> {

    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectOrder>

    @Query(
        value = """
            SELECT * 
            FROM collect_order 
            WHERE order_id = :orderId AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByOrderId(orderId : String) : MutableList<CollectOrder>?

    @Query(
        value = """
            SELECT * 
            FROM collect_order 
            WHERE order_id IN :orderIds AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByOrderIdsIn(orderIds: List<String>) : MutableList<CollectOrder>?

    @Query(
        value = """
            SELECT * 
            FROM collect_order 
            WHERE order_create_time BETWEEN :startTime AND :endTime AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveWithinCreateTimeRange(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): MutableList<CollectOrder>?

    @Query(
        value = """
            SELECT COUNT(DISTINCT "customer_id") AS unique_customer_count FROM "collect_order"
        """,
        nativeQuery = true
    )
    fun countDistinctCustomerIds() : Long

}

@Repository
interface CollectOrderItemRepository : JpaRepository<CollectOrderItem, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectOrderItem>

    @Query(
        value = """
            SELECT * 
            FROM collect_order_item
            WHERE order_id = :orderId AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findOrderItemsByOrderId(orderId : String) : List<CollectOrderItem>?

    @Query(
        value = """
            SELECT * 
            FROM collect_order_item
            WHERE update_time BETWEEN :startTime AND :endTime 
              AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveOrderItemsWithinUpdateTimeRange(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): MutableList<CollectOrderItem>

    @Query(
        value = """
            SELECT coi
            FROM CollectOrderItem coi
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses
                AND coi.isActive = true
        """
    )
    fun findAllByCriteria(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderItem>

    @Query(
        value = """
            SELECT new com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderItemWithExtraInfo(coi, co.customerId, co.channelId, co.referrerCode) 
            FROM CollectOrderItem coi
            LEFT JOIN (
                SELECT DISTINCT o.orderId as orderId, o.customerId as customerId, 
                        o.channelId as channelId, o.referrerCode as referrerCode 
                FROM CollectOrder o
            ) co on coi.orderId = co.orderId
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses
                AND coi.isActive = true
        """
    )
    fun findAllWithCustomerIdByCriteria(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderItemWithExtraInfo>

    @Query("""
            SELECT
                coi
            FROM
                CollectOrderItem coi
            WHERE
                coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemId IN :orderItemIds
                AND coi.orderItemStatus IN :orderItemStatuses
                AND coi.isActive = true
    """)
    fun findAllByTimeRangeAndOrderItemIds(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemIds: List<String>,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries
    ): List<CollectOrderItem>

    @Query("""
            SELECT
                coi
            FROM
                CollectOrderItem coi
            WHERE
                coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemStatus IN :orderItemStatuses
                AND coi.isActive = true
    """)
    fun findAllByTimeRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries
    ): List<CollectOrderItem>
}
@Repository
interface CollectContractRepository : JpaRepository<CollectContract, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectContract>

    @Query(
        value = """
            SELECT DISTINCT ON(contract_id) * 
            FROM collect_contract
            WHERE start_date BETWEEN :startDate AND :endDate AND contract_status='ACTIVE'
              AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveContractsWithinCreateTimeRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): MutableList<CollectContract>?

    @Query(
        value = """
            SELECT
                cc
            FROM
                CollectContract cc
            WHERE
                cc.contractId IN (
                    SELECT
                        contractId
                    FROM
                        CollectCharge
                    WHERE
                        chargeId IN :chargeIds
                )
                AND cc.contractStatus IN :contractStatuses
                AND cc.isActive = true
        """
    )
    fun findAllByChargeIds(
        chargeIds: List<String>,
        contractStatuses: List<String> = ContractStatus.entries.map { it.name }
    ): MutableList<CollectContract>

    @Query(
        value = """
            SELECT
                cc
            FROM
                CollectContract cc
            WHERE
                cc.orderItemId IN :orderItemIds
                AND cc.contractStatus IN :contractStatuses
                AND cc.isActive = true
                AND cc.rentalCode IN (
                    SELECT
                        rentalCode
                    FROM
                        RentalCodeMaster rcm
                    WHERE
                        rcm.isActive = true
                        AND rcm.leaseType IN :leaseTypes
                        AND rcm.currentTerm = 1
                )
        """
    )
    fun findAllByOrderItemIds(
        orderItemIds: List<String>,
        contractStatuses: List<String> = ContractStatus.entries.map { it.name },
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): MutableList<CollectContract>

    @Query("""
        SELECT
            cc
        FROM
            CollectContract cc
        WHERE
            cc.orderItemId = :orderItemId
            AND cc.contractStatus IN :contractStatuses
        ORDER BY
            cc.updateTime DESC
        LIMIT 1
    """)
    fun findTopOrderByCreateTime(
        orderItemId: String,
        contractStatuses: List<String> = ContractStatus.entries.map { it.name }
    ): CollectContract?
}

@Repository
interface CollectCustomerRepository : JpaRepository<CollectCustomer, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectCustomer>
    @Query(
        value = """
            SELECT DISTINCT ON(customer_id) * 
            FROM collect_customer
            WHERE customer_id = :customerId AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByCustomerId(customerId : String) : CollectCustomer?

    @Query(
        value = """
            SELECT * 
            FROM collect_customer
            WHERE customer_id IN :customerIds AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByCustomerIds(customerIds: List<String>): List<CollectCustomer>


    @Query(
        value = """
            SELECT DISTINCT ON(customer_id) * 
            FROM collect_customer
            WHERE create_time BETWEEN :startTime AND :endTime 
              AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveCustomerWithinCreateTimeRange(
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): MutableList<CollectCustomer>?


}

@Repository
interface CollectInstallationRepository : JpaRepository<CollectInstallation, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectInstallation>

    @Query("""
        SELECT
            ci
        FROM
            CollectInstallation ci
        WHERE
            ci.isActive = true
            AND ci.serialNumber != ''
            AND ci.serialNumber IS NOT NULL
            AND ci.installationTime IS NOT NULL
            AND ci.orderItemId IN :orderItemIds
    """)
    fun findValidByOrderItemIdIn(
        orderItemIds: List<String>
    ): List<CollectInstallation>

    @Query("""
        SELECT
            ci
        FROM
            CollectInstallation ci
        WHERE
            ci.isActive = true
            AND ci.installationTime IS NOT NULL
            AND ci.orderItemId IN :orderItems
            AND FUNCTION('TO_CHAR', ci.installationTime, 'YYYY') != :year
            AND FUNCTION('TO_CHAR', ci.installationTime, 'MM') = :month
    """)
    fun findSameMonth(
        orderItems: List<String>,
        year: String,
        month: String
    ): List<CollectInstallation>

    @Query("""
        SELECT
            ci
        FROM
            CollectInstallation ci
        WHERE
            ci.isActive = true
            AND ci.serialNumber = :serialNumber
        ORDER BY
            ci.installationTime DESC
        LIMIT 1
    """)
    fun findTopOrderByInstallationTime(
        serialNumber: String
    ): CollectInstallation?

    @Query("""
        SELECT
            ci
        FROM
            CollectInstallation ci
        WHERE
            ci.isActive = true
            AND ci.serialNumber IN :serialNumbers
    """)
    fun findAllBySerialNumber(
        serialNumbers: List<String>
    ): List<CollectInstallation>
}

@Repository
interface CollectServiceFlowRepository : JpaRepository<CollectServiceFlow, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectServiceFlow>

    @Query(
        value = """
            SELECT csf 
            FROM CollectServiceFlow csf
            WHERE 
                csf.orderItemId in :orderItemIds AND csf.serviceType in :serviceTypes AND csf.serviceStatus in :serviceStatues
                AND csf.isActive = true AND csf.updateTime BETWEEN :startTime AND :endTime
        """,
        nativeQuery = false
    )
    fun findAllBy(orderItemIds:List<String>,
                  serviceTypes: List<ServiceFlowType> = listOf(ServiceFlowType.INSTALL),
                  serviceStatues: List<ServiceFlowStatus> = ServiceFlowStatus.entries,
                  startTime: OffsetDateTime  = OffsetDateTime.now().minusMonths(1),
                  endTime: OffsetDateTime = OffsetDateTime.now()
    ): List<CollectServiceFlow>

    fun findByOrderItemIdIn(orderItemIds: List<String>): List<CollectServiceFlow>

    @Query("""
        SELECT
            csf
        FROM
            CollectServiceFlow csf
        WHERE
            csf.serviceType in :serviceTypes
            AND csf.serviceStatus in :serviceStatues
            AND csf.isActive = true
    """)
    fun findBy(
        serviceTypes: List<ServiceFlowType> = ServiceFlowType.entries,
        serviceStatues: List<ServiceFlowStatus> = ServiceFlowStatus.entries
    ): List<CollectServiceFlow>
}

@Repository
interface CollectChannelRepository : JpaRepository<CollectChannel, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectChannel>

    @Query(
        value = """
            SELECT * 
            FROM collect_channel
            WHERE channel_id = :channelId AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByChannelId(channelId : String) : List<CollectChannel>?

    @Query(
        value = """
            SELECT * 
            FROM collect_channel
            WHERE channel_id IN :channelIds AND is_active = 'Y'
        """,
        nativeQuery = true
    )
    fun findActiveByChannelIdIs(channelIds: List<String>) : List<CollectChannel>?

    @Query("""
        SELECT
            cc
        FROM
            CollectChannel cc
        WHERE
            cc.isActive = true AND cc.channelId in :channelIds
    """)
    fun findAllByActiveChannelIdIn(channelIds: List<String>): List<CollectChannel>

    @Query("""
        SELECT
            cc
        FROM
            CollectChannel cc
        WHERE
            cc.isActive = true AND cc.relation.value IN :orderIds
    """)
    fun findAllByOrderIds(
        orderIds: List<String>
    ): List<CollectChannel>


}

@Repository
interface CollectLocationRepository : JpaRepository<CollectLocation, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectLocation>
}

@Repository
interface CollectDepositRepository: JpaRepository<CollectDeposit, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectLocation>

    @Query(
        value = """
            SELECT
                cd
            FROM
                CollectDeposit cd
            WHERE
                cd.updateTime BETWEEN :startTime AND :endTime
                AND cd.transactionId IN (
                    SELECT
                        cr.transactionId
                    FROM
                        CollectReceipt cr
                    WHERE
                        cr.receiptTime BETWEEN :startTime AND :endTime
                        AND cr.isActive = true
                        AND cr.chargeId IN (
                            SELECT
                                cc.chargeId
                            FROM
                                CollectCharge cc
                            WHERE
                                cc.contractId IN (
                                    SELECT
                                        cc2.contractId
                                    FROM
                                        CollectContract cc2
                                    WHERE
                                        cc2.rentalCode IN (
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
                )
        """
    )
    fun findAllByTimeRange(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        leaseTypes: List<LeaseType> = LeaseType.entries
    ): MutableList<CollectDeposit>
}

@Repository
interface CollectInventoryValuationRepository: JpaRepository<CollectInventoryValuation, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectInventoryValuation>

    @Query("""
        SELECT A
        FROM CollectInventoryValuation A
        WHERE A.materialId in :materialIds
          AND A.gradeType = :gradeType
          AND A.movementType = :movementType
          AND A.issuedTime = (
            SELECT MAX(R.issuedTime)
            FROM CollectInventoryValuation R
            WHERE R.issuedTime <= :issueTime
                AND A.materialId in :materialIds
                AND A.gradeType = :gradeType
                AND A.movementType = :movementType
          )
    """)
    fun findAllBy(
        materialIds: List<String>,
        gradeType: InventoryAssetGradeType = InventoryAssetGradeType.GRADE_A,
        movementType: MovementType = MovementType.ENDING_INVENTORY,
        issueTime: OffsetDateTime = OffsetDateTime.now()
    ): List<CollectInventoryValuation>
}

@Repository
interface CollectShippingRepository: JpaRepository<CollectShipping, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectShipping>
}

@Repository
interface CollectPromotionRepository: JpaRepository<CollectPromotion, String> {
    // BulkDistinctInserter 에서 호출됨
    fun findByHashCodeIn(hashCodes: List<String>) : List<CollectPromotion>
}



