package com.abc.us.accounting.rentals.master.domain.repository

import com.abc.us.accounting.rentals.master.domain.entity.*
import com.abc.us.accounting.rentals.master.domain.type.MaterialCareType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
interface RentalCodeMasterRepository: JpaRepository<RentalCodeMaster, String> {
    override fun findAll():List<RentalCodeMaster>

    fun findByRentalCode(rentalCode: String): RentalCodeMaster?

    @Query("""
        SELECT
            rcm
        FROM
            RentalCodeMaster rcm
        WHERE
            rcm.rentalCode IN :rentalCodes
    """)
    fun findByRentalCodes(rentalCodes: List<String>): List<RentalCodeMaster>
}


@Repository
interface RentalPricingMasterRepository : JpaRepository<RentalPricingMaster, String> {
    override fun findAll():List<RentalPricingMaster>

    @Query(value = """
        select a 
        from RentalPricingMaster a 
        where a.startDate = (
            select max(r.startDate)
            from RentalPricingMaster r
            where r.startDate <= :baseDate
        )
    """
    )
    fun findByBaseDate(baseDate: LocalDate):List<RentalPricingMaster>

    @Query(value = """
            select a
            FROM RentalPricingMaster a
            where a.materialModelNamePrefix in :materialModelNamePrefixes and a.startDate = (
            select max(r.startDate)
            from RentalPricingMaster r
            where r.startDate <= :baseDate
        )
        """
    )
    fun findByMaterialModelNamePrefixContains (materialModelNamePrefixes:List<String>, baseDate: LocalDate = LocalDate.now()):List<RentalPricingMaster>


    @Query(value = """
            select a
            FROM RentalPricingMaster a
            where a.materialModelNamePrefix = :materialModelNamePrefix
                and a.rentalCode = :rentalCode
                and a.materialCareType = :materialCareType
                and :baseDate >= a.startDate
        """
    )
    fun findByMaterialModelNamePrefixAndRentalCodeAndCareType(materialModelNamePrefix:String, rentalCode:String,
                                                              materialCareType: MaterialCareType = MaterialCareType.SELF_CARE, baseDate: LocalDate = LocalDate.now()): List<RentalPricingMaster>

}


@Repository
interface RentalDistributionMasterRepository : JpaRepository<RentalDistributionMaster, String> {
    override fun findAll():List<RentalDistributionMaster>

    @Query(value = """
        select a 
        from RentalDistributionMaster a 
        where a.startDate = (
            select max(r.startDate)
            from RentalDistributionMaster r
            where r.startDate <= :baseDate
        )
    """
    )
    fun findByBaseDate(baseDate: LocalDate):List<RentalDistributionMaster>


    @Query(value = """
            select a
            FROM RentalDistributionMaster a
            where a.materialModelNamePrefix in :materialModelNamePrefixes and :baseDate >= a.startDate 
        """
    )
    fun findByMaterialModelNamePrefixContains(materialModelNamePrefixes: List<String>, baseDate: LocalDate = LocalDate.now()):List<RentalDistributionMaster>
}




@Repository
interface RentalDistributionRuleRepository : JpaRepository<RentalDistributionRule, String> {
    override fun findAll():List<RentalDistributionRule>

    @Query(value = """
        select a 
        from RentalDistributionRule a 
        where a.startDate = (
            select max(r.startDate)
            from RentalDistributionRule r
            where r.startDate <= :baseDate
        )
    """
    )
    fun findByBaseDate(baseDate: LocalDate):List<RentalDistributionRule>

    @Modifying(clearAutomatically = true)  // 쿼리 실행 후 자동으로 영속성 컨텍스트 clear
    @Query(value = """
        DELETE FROM RentalDistributionRule a
        WHERE a.startDate = (
            select max(r.startDate) 
            from RentalDistributionRule r 
            where r.startDate <= :baseDate
)
    """
    )
    fun deleteByBaseDate(baseDate: LocalDate)


    @Modifying(clearAutomatically = true)
    override fun deleteAllInBatch(entities: MutableIterable<RentalDistributionRule>)



    @Query(value = """
        select new com.abc.us.accounting.rentals.master.domain.entity.RentalDistributionMappingInfo(rdm, rcm, rpm, m)
        from RentalDistributionMaster rdm, RentalCodeMaster rcm, RentalPricingMaster rpm, IfMaterial m
        where rdm.materialModelNamePrefix = rpm.materialModelNamePrefix
            and rcm.rentalCode = rpm.rentalCode
            and rdm.materialModelNamePrefix = m.materialModelNamePrefix
            and rdm.startDate = (
            select max(r.startDate)
            from RentalDistributionMaster r
            where r.startDate <= :baseDate
        ) and rpm.startDate = (
            select max(r.startDate)
            from RentalPricingMaster r
            where r.startDate <= :baseDate
        ) 
    """
    )
    fun findRentalPriceRuleInfo(baseDate: LocalDate = LocalDate.now()): List<RentalDistributionMappingInfo>
}