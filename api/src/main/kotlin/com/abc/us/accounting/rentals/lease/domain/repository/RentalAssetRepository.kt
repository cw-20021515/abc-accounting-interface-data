package com.abc.us.accounting.rentals.lease.domain.repository

import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationMaster
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetDepreciationSchedule
import com.abc.us.accounting.rentals.lease.domain.entity.RentalAssetHistory
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RentalAssetDepreciationMasterRepository: JpaRepository<RentalAssetDepreciationMaster, Int> {
    fun findByMaterialId(
        materialId: String
    ): RentalAssetDepreciationMaster?
}

@Repository
interface RentalAssetHistoryRepository: JpaRepository<RentalAssetHistory, Int> {
    @Query(
        """
            select
            a
        from
            RentalAssetHistory a
        where
            a.hash in :hashs
        """
    )
    fun findByHashs(hashs: List<String>): List<RentalAssetHistory>

    @Query(value = """
        select
            a.*,
            im.material_model_name as model_name,
            isf.update_time as installation_date
        from
            rental_asset_history a
        left outer join
            if_material im
        on
            a.material_id = im.material_id
        left outer join
            if_service_flow isf
        on
            a.serial_number = isf.serial_number
            and a.order_item_id = isf.order_item_id
            and isf.service_type = 'INSTALL'
            and isf.service_status = 'SERVICE_COMPLETED'
        where
            a.id in (
                select
                    max(a.id) as id
                from
                    rental_asset_history a
                where
                    a.id in (
                        select
                            b.id
                        from
                            rental_asset_history b
                        where
                            date(:baseDate) is null
                            or (
                                (
                                    b.depreciation_date is null
                                    and b.contract_date <= date(:baseDate)
                                )
                                or
                                (
                                    b.depreciation_date is not null
                                    and b.depreciation_date <= date(:baseDate)
                                )
                            )
                    )
                    and (
                        date(:contractFromDate) is null
                        or date(a.contract_date) >= date(:contractFromDate)
                    )
                    and (
                        date(:contractToDate) is null
                        or date(a.contract_date) <= date(:contractToDate)
                    )
                    and (
                        :customerId is null
                        or a.customer_id like (trim(:customerId) || '%')
                    )
                    and (
                        :orderIdFrom is null
                        or a.order_id >= trim(:orderIdFrom)
                    )
                    and (
                        :orderIdTo is null
                        or a.order_id <= trim(:orderIdTo)
                    )
                    and (
                        :serialNumber is null
                        or a.serial_number like (trim(:serialNumber) || '%')
                    )
                    and (
                        :materialId is null
                        or a.material_id like (trim(:materialId) || '%')
                    )
                group by
                    a.serial_number
            )
            and (
                :materialCategory is null
                or im.material_category_code = :materialCategory
            )
        order by
            a.id desc
    """, nativeQuery = true)
    fun findByReq(
        @Param("baseDate") baseDate: LocalDate? = null,
        @Param("contractFromDate") contractFromDate: LocalDate? = null,
        @Param("contractToDate") contractToDate: LocalDate? = null,
        @Param("customerId") customerId: String? = null,
        @Param("orderIdFrom") orderIdFrom: String? = null,
        @Param("orderIdTo") orderIdTo: String? = null,
        @Param("serialNumber") serialNumber: String? = null,
        @Param("materialId") materialId: String? = null,
        @Param("materialCategory") materialCategory: String? = null,
        pageable: Pageable? = null
    ): Page<Map<String, Any>>
}

@Repository
interface RentalAssetDepreciationScheduleRepository: JpaRepository<RentalAssetDepreciationSchedule, Int> {
    @Query(value = """
        select
            a
        from
            RentalAssetDepreciationSchedule a
        where
            a.serialNumber = :serialNumber
        order by
            a.depreciationCount
    """)
    fun findBySerialNumber(serialNumber: String): List<RentalAssetDepreciationSchedule>

    @Modifying
    @Transactional
    @Query(value = """
        delete from
            RentalAssetDepreciationSchedule a
        where
            a.serialNumber IN :serialNumbers
    """)
    fun deleteBySerialNumbers(serialNumbers: List<String>): Int

    @Query(value = """
        select
            a
        from
            RentalAssetDepreciationSchedule a
        where
            a.serialNumber in :serialNumbers
            and to_char(a.depreciationDate, 'YYYY-MM') = to_char(date(:baseDate), 'YYYY-MM')
    """)
    fun findBySerialNumbersAndDate(
        serialNumbers: List<String>,
        baseDate: LocalDate
    ): List<RentalAssetDepreciationSchedule>
}
