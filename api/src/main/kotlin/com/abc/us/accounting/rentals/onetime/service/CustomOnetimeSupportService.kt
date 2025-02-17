package com.abc.us.accounting.rentals.onetime.service

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.type.MaterialType
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowStatus
import com.abc.us.accounting.rentals.master.domain.type.ServiceFlowType
import com.abc.us.accounting.rentals.onetime.domain.entity.CollectOrderMappingInfo
import com.abc.us.accounting.rentals.onetime.domain.repository.CustomOnetimeSupportRepository
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Component
class CustomOnetimeSpecifications {
    fun createBaseSpecification(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        orderItemTypes: List<OrderItemType>,
        orderItemStatuses: List<OrderItemStatus>,
        materialTypes: List<MaterialType>
    ): Specification<CollectOrderItem> {
        return Specification { root, query, builder ->
            val predicates = mutableListOf<Predicate>()

            if (query.resultType == CollectOrderMappingInfo::class.java) {
                val materialJoin = root.join<CollectOrderItem, CollectMaterial>("material", JoinType.LEFT)
                query.multiselect(root, materialJoin)
                predicates.add(materialJoin.get<MaterialType>("materialType").`in`(materialTypes))
            }

            // Base conditions
            predicates.add(builder.between(root.get("createTime"), startTime, endTime))
            predicates.add(root.get<OrderItemType>("orderItemType").`in`(orderItemTypes))
            predicates.add(root.get<OrderItemStatus>("orderItemStatus").`in`(orderItemStatuses))
            predicates.add(builder.equal(root.get<Boolean>("isActive"), true))

            builder.and(*predicates.toTypedArray())
        }
    }

    fun withServiceFlow(
        serviceFlowTypes: List<ServiceFlowType>,
        serviceFlowStatuses: List<ServiceFlowStatus>
    ): Specification<CollectOrderItem> {
        return Specification { root, query, builder ->
            if (query.resultType != CollectOrderMappingInfo::class.java) {
                return@Specification builder.conjunction()
            }

            val serviceFlowJoin = root.join<CollectOrderItem, CollectServiceFlow>("serviceFlow", JoinType.LEFT)
            val materialJoin = root.join<CollectOrderItem, CollectMaterial>("material", JoinType.LEFT)

            query.multiselect(root, materialJoin, serviceFlowJoin)

            val predicates = mutableListOf<Predicate>()
            predicates.add(serviceFlowJoin.get<ServiceFlowType>("serviceType").`in`(serviceFlowTypes))
            predicates.add(serviceFlowJoin.get<ServiceFlowStatus>("serviceStatus").`in`(serviceFlowStatuses))
            predicates.add(builder.isNotNull(serviceFlowJoin.get<Long>("serviceFlowId")))

            builder.and(*predicates.toTypedArray())
        }
    }

    fun withInstallation(): Specification<CollectOrderItem> {
        return Specification { root, query, builder ->
            if (query.resultType != CollectOrderMappingInfo::class.java) {
                return@Specification builder.conjunction()
            }

            val installationJoin = root.join<CollectOrderItem, CollectInstallation>("installation", JoinType.LEFT)
            val materialJoin = root.join<CollectOrderItem, CollectMaterial>("material", JoinType.LEFT)

            query.multiselect(root, materialJoin, installationJoin)

            val predicates = mutableListOf<Predicate>()
            predicates.add(builder.isNotNull(installationJoin.get<Long>("installId")))
            predicates.add(builder.isNotNull(installationJoin.get<OffsetDateTime>("installationTime")))

            builder.and(*predicates.toTypedArray())
        }
    }

    fun withInventoryValuation(baseTime: OffsetDateTime): Specification<CollectOrderItem> {
        return Specification { root, query, builder ->
            if (query.resultType != CollectOrderMappingInfo::class.java) {
                return@Specification builder.conjunction()
            }

            val materialJoin = root.join<CollectOrderItem, CollectMaterial>("material", JoinType.LEFT)

            val valuationSubquery = query.subquery(OffsetDateTime::class.java)
            val valuationRoot = valuationSubquery.from(CollectInventoryValuation::class.java)

            valuationSubquery.select(builder.greatest<OffsetDateTime>(valuationRoot.get("baseTime")))
                .where(
                    builder.equal(valuationRoot.get<String>("materialId"), materialJoin.get<String>("materialId")),
                    builder.equal(valuationRoot.get<String>("movementType"), "ENDING_INVENTORY"),
                    builder.equal(valuationRoot.get<String>("gradeType"), "A"),
                    builder.lessThanOrEqualTo(valuationRoot.get("baseTime"), baseTime)
                )

            val valuationJoin = root.join<CollectOrderItem, CollectInventoryValuation>("inventoryValuation", JoinType.LEFT)
            query.multiselect(root, materialJoin, valuationJoin)

            val predicates = mutableListOf<Predicate>()
            predicates.add(builder.equal(valuationJoin.get<String>("movementType"), "ENDING_INVENTORY"))
            predicates.add(builder.equal(valuationJoin.get<String>("gradeType"), "A"))
            predicates.add(builder.equal(valuationJoin.get<OffsetDateTime>("baseTime"), valuationSubquery))

            builder.and(*predicates.toTypedArray())
        }
    }
}

@Service
class CustomOnetimeSupportService(
    private val collectOrderItemRepository: CustomOnetimeSupportRepository,
    private val specifications: CustomOnetimeSpecifications
) {

    fun findOrderItemMappings(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        orderItemTypes: List<OrderItemType>,
        orderItemStatuses: List<OrderItemStatus>,
        materialTypes: List<MaterialType>,
        serviceFlowTypes: List<ServiceFlowType>? = null,
        serviceFlowStatuses: List<ServiceFlowStatus>? = null,
        withInstallation: Boolean = false,
        withInventoryValuation: Boolean = false,
        pageable: Pageable
    ): Slice<CollectOrderMappingInfo> {
        var spec = specifications.createBaseSpecification(
            startTime, endTime, orderItemTypes, orderItemStatuses, materialTypes
        )

        if (serviceFlowTypes != null && serviceFlowStatuses != null) {
            spec = spec.and(specifications.withServiceFlow(serviceFlowTypes, serviceFlowStatuses))
        }

        if (withInstallation) {
            spec = spec.and(specifications.withInstallation())
        }

        if (withInventoryValuation) {
            spec = spec.and(specifications.withInventoryValuation(startTime))
        }

        // The result is already in CollectOrderMappingInfo format due to the multiselect in specifications
        return collectOrderItemRepository.findAll(spec, pageable) as Slice<CollectOrderMappingInfo>
    }

    fun countOrderItems(
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        orderItemTypes: List<OrderItemType>,
        orderItemStatuses: List<OrderItemStatus>,
        materialTypes: List<MaterialType>,
        serviceFlowTypes: List<ServiceFlowType>,
        serviceFlowStatuses: List<ServiceFlowStatus>,
        withInstallation: Boolean = false,
    ): Long {
        var spec = specifications.createBaseSpecification(
            startTime, endTime, orderItemTypes, orderItemStatuses, materialTypes
        )

        if (serviceFlowTypes.isNotEmpty() && serviceFlowStatuses.isNotEmpty()) {
            spec = spec.and(specifications.withServiceFlow(serviceFlowTypes, serviceFlowStatuses))
        }

        if (withInstallation) {
            spec = spec.and(specifications.withInstallation())
        }

        return collectOrderItemRepository.count(spec)
    }
}