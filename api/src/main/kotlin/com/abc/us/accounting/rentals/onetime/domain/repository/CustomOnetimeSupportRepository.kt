package com.abc.us.accounting.rentals.onetime.domain.repository

import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.type.MaterialType
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface CustomOnetimeSupportRepository :
    JpaRepository<CollectOrderItem, String>,
    JpaSpecificationExecutor<CollectOrderItem> {

    @Query(
        value = """
            SELECT COUNT(DISTINCT coi.order_item_id)
            FROM collect_order_item coi
            JOIN collect_material cm on coi.material_id = cm.material_id
                AND cm.material_type IN :materialTypes
            WHERE coi.update_time BETWEEN :startTime AND :endTime
                AND coi.order_item_type IN :orderItemTypes
                AND coi.order_item_status IN :orderItemStatuses 
                AND coi.is_active = 'Y'
        """, nativeQuery = true
    )
    fun countByCriteria(startTime: OffsetDateTime,
                        endTime: OffsetDateTime = OffsetDateTime.now(),
                        orderItemTypes: List<String>,
                        orderItemStatuses: List<String>,
                        materialTypes: List<String>): Int


    @Query(
        value = """
            SELECT COUNT(DISTINCT t.order_item_id) FROM (
                SELECT coi.order_item_id, csf.service_flow_id
                FROM collect_order_item coi
                JOIN collect_material cm on coi.material_id = cm.material_id
                    AND cm.material_type IN :materialTypes
                LEFT JOIN collect_service_flow csf on coi.order_item_id = csf.order_item_id
                    AND csf.service_type IN :serviceFlowTypes 
                    AND csf.service_status IN :serviceFlowStatuses            
                WHERE coi.update_time BETWEEN :startTime AND :endTime
                    AND coi.order_item_type IN :orderItemTypes
                    AND coi.order_item_status IN :orderItemStatuses 
                    AND coi.is_active = 'Y'
            ) AS t
            WHERE t.service_flow_id IS NOT NULL
        """, nativeQuery = true
    )
    fun countByCriteriaWithServiceFlow(startTime: OffsetDateTime,
                        endTime: OffsetDateTime = OffsetDateTime.now(),
                        orderItemTypes: List<String>,
                        orderItemStatuses: List<String>,
                        materialTypes: List<String>,
                        serviceFlowTypes: List<String>,
                        serviceFlowStatuses: List<String>): Int

    @Query(
        value = """
            SELECT COUNT(DISTINCT t.order_item_id) FROM (
                SELECT coi.order_item_id, csf.service_flow_id, ci.install_id, ci.installation_time
                FROM collect_order_item coi
                JOIN collect_material cm on coi.material_id = cm.material_id
                    AND cm.material_type IN :materialTypes
                LEFT JOIN collect_service_flow csf on coi.order_item_id = csf.order_item_id
                    AND csf.service_type IN :serviceFlowTypes 
                    AND csf.service_status IN :serviceFlowStatuses
                LEFT JOIN collect_installation ci on coi.order_item_id = ci.order_item_id
                    AND ci.installation_time IS NOT NULL
                WHERE coi.update_time BETWEEN :startTime AND :endTime
                    AND coi.order_item_type IN :orderItemTypes
                    AND coi.order_item_status IN :orderItemStatuses 
                    AND coi.is_active = 'Y'
            ) AS t
            WHERE t.service_flow_id IS NOT NULL 
                    AND t.install_id IS NOT NULL
                    AND t.installation_time IS NOT NULL
        """, nativeQuery = true
    )
    fun countByCriteriaWithServiceFlowAndInstall(startTime: OffsetDateTime,
                                       endTime: OffsetDateTime = OffsetDateTime.now(),
                                       orderItemTypes: List<String>,
                                       orderItemStatuses: List<String>,
                                       materialTypes: List<String>,
                                       serviceFlowTypes: List<String>,
                                       serviceFlowStatuses: List<String>): Int

    @Query(
        value = """
            SELECT new com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo(coi, cm)
            FROM CollectOrderItem coi
            LEFT JOIN CollectMaterial cm on coi.materialId = cm.materialId
                AND cm.materialType IN :materialTypes
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses 
                AND coi.isActive = true
        """
    )
    fun findByCriteria(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        materialTypes: List<MaterialType> = listOf(MaterialType.PRODUCT),
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderMappingInfo>


    @Query(
        value = """
            SELECT new com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo(coi, cm, csf)
            FROM CollectOrderItem coi
            LEFT JOIN CollectMaterial cm on coi.materialId = cm.materialId
                AND cm.materialType IN :materialTypes
            LEFT JOIN CollectServiceFlow csf on coi.orderItemId = csf.orderItemId
                AND csf.serviceType IN :serviceFlowTypes 
                AND csf.serviceStatus IN :serviceFlowStatuses
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses 
                AND coi.isActive = true
        """
    )
    fun findByCriteriaWithServiceFlow(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        materialTypes: List<MaterialType> = listOf(MaterialType.PRODUCT),
        serviceFlowTypes: List<ServiceFlowType> = listOf(ServiceFlowType.INSTALL),
        serviceFlowStatuses: List<ServiceFlowStatus> = listOf(
            ServiceFlowStatus.SERVICE_SCHEDULED,
            ServiceFlowStatus.SERVICE_COMPLETED
        ),
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderMappingInfo>

    @Query(
        value = """
            SELECT new com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo(coi, cm, null, ci, null)
            FROM CollectOrderItem coi
            LEFT JOIN CollectMaterial cm on coi.materialId = cm.materialId
                AND cm.materialType IN :materialTypes
            LEFT JOIN CollectInstallation ci on coi.orderItemId = ci.orderItemId
                AND ci.installationTime IS NOT NULL
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses 
                AND coi.isActive = true
                AND ci.installId IS NOT NULL
                AND ci.installationTime IS NOT NULL
        """
    )
    fun findByCriteria3(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        materialTypes: List<MaterialType> = listOf(MaterialType.PRODUCT),
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderMappingInfo>

    @Query(
        value = """
            SELECT new com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo(coi, cm, csf, ci, null)
            FROM CollectOrderItem coi
            LEFT JOIN CollectMaterial cm on coi.materialId = cm.materialId
                AND cm.materialType IN :materialTypes
            LEFT JOIN CollectServiceFlow csf on coi.orderItemId = csf.orderItemId
                AND csf.serviceType IN :serviceFlowTypes 
                AND csf.serviceStatus IN :serviceFlowStatuses
            LEFT JOIN CollectInstallation ci on coi.orderItemId = ci.orderItemId
                AND ci.installationTime IS NOT NULL
            WHERE coi.updateTime BETWEEN :startTime AND :endTime
                AND coi.orderItemType IN :orderItemTypes
                AND coi.orderItemStatus IN :orderItemStatuses 
                AND coi.isActive = true
                AND csf.serviceFlowId IS NOT NULL
                AND ci.installId IS NOT NULL
                AND ci.installationTime IS NOT NULL
        """
    )
    fun findByCriteria4(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime = OffsetDateTime.now(),
        orderItemTypes: List<OrderItemType> = OrderItemType.entries,
        orderItemStatuses: List<OrderItemStatus> = OrderItemStatus.entries,
        materialTypes: List<MaterialType> = listOf(MaterialType.PRODUCT),
        serviceFlowTypes: List<ServiceFlowType> = listOf(ServiceFlowType.INSTALL),
        serviceFlowStatuses: List<ServiceFlowStatus> = listOf(ServiceFlowStatus.SERVICE_COMPLETED),
        pageable: Pageable = Pageable.unpaged()
    ): Slice<CollectOrderMappingInfo>

}